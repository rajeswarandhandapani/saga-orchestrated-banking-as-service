package com.rajeswaran.saga.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rajeswaran.saga.definition.SagaDefinition;
import com.rajeswaran.saga.definition.SagaDefinitionRegistry;
import com.rajeswaran.saga.definition.SagaStepDefinition;
import com.rajeswaran.saga.entity.SagaInstance;
import com.rajeswaran.saga.entity.SagaStepInstance;
import com.rajeswaran.saga.model.SagaStatus;
import com.rajeswaran.saga.model.SagaStepStatus;
import com.rajeswaran.saga.repository.SagaInstanceRepository;
import com.rajeswaran.saga.repository.SagaStepInstanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SagaOrchestratorImpl implements SagaOrchestrator {

    private final SagaInstanceRepository sagaInstanceRepository;
    private final SagaStepInstanceRepository sagaStepInstanceRepository;
    private final SagaDefinitionRegistry sagaDefinitionRegistry;
    private final StreamBridge streamBridge;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public SagaInstance startSaga(String sagaName, String payload) {
        SagaDefinition sagaDefinition = sagaDefinitionRegistry.getSagaDefinition(sagaName);
        if (sagaDefinition == null) {
            throw new IllegalArgumentException("Saga not found: " + sagaName);
        }

        // 1. Create the main saga instance tracker
        SagaInstance sagaInstance = SagaInstance.builder()
                .sagaName(sagaName)
                .status(SagaStatus.IN_PROGRESS)
                .currentStep(0)
                .build();
        SagaInstance savedInstance = sagaInstanceRepository.save(sagaInstance);

        // 2. Create a step instance for the initial payload
        SagaStepInstance initialStep = SagaStepInstance.builder()
                .sagaInstance(savedInstance)
                .stepName("SagaInitiated")
                .status(SagaStepStatus.COMPLETED)
                .payload(payload)
                .build();
        sagaStepInstanceRepository.save(initialStep);


        // 3. Execute the first real step
        executeStep(savedInstance, sagaDefinition, 0, payload);

        return savedInstance;
    }

    @Override
    @Transactional
    public void handleReply(String replyDestination, String payload, boolean isFailure) {
        log.info("Handling reply from destination: '{}', isFailure: {}", replyDestination, isFailure);

        Optional<SagaDefinition> sagaDefinitionOpt = sagaDefinitionRegistry.findSagaDefinitionByReplyDestination(replyDestination);
        if (sagaDefinitionOpt.isEmpty()) {
            log.error("No saga definition found for reply destination: {}", replyDestination);
            return;
        }
        SagaDefinition sagaDefinition = sagaDefinitionOpt.get();

        try {
            JsonNode payloadNode = objectMapper.readTree(payload);
            Long sagaId = payloadNode.get("sagaId").asLong();
            SagaInstance sagaInstance = sagaInstanceRepository.findById(sagaId)
                    .orElseThrow(() -> new RuntimeException("Saga instance not found: " + sagaId));

            SagaStepDefinition currentStepDefinition = sagaDefinition.getSteps().get(sagaInstance.getCurrentStep());

            if (isFailure) {
                handleFailure(sagaInstance, currentStepDefinition, payload);
            } else {
                handleSuccess(sagaInstance, sagaDefinition, currentStepDefinition, payload);
            }
        } catch (JsonProcessingException e) {
            log.error("Error parsing payload for saga reply", e);
        }
    }

    private void handleSuccess(SagaInstance sagaInstance, SagaDefinition sagaDefinition, SagaStepDefinition currentStepDefinition, String payload) {
        // Record the successful step
        SagaStepInstance stepInstance = SagaStepInstance.builder()
                .sagaInstance(sagaInstance)
                .stepName(currentStepDefinition.getReplyDestination())
                .status(SagaStepStatus.COMPLETED)
                .payload(payload)
                .build();
        sagaStepInstanceRepository.save(stepInstance);

        int nextStepIndex = sagaInstance.getCurrentStep() + 1;

        if (nextStepIndex < sagaDefinition.getSteps().size()) {
            // More steps to go
            sagaInstance.setCurrentStep(nextStepIndex);
            sagaInstanceRepository.save(sagaInstance);
            executeStep(sagaInstance, sagaDefinition, nextStepIndex, payload);
        } else {
            // Saga is complete
            sagaInstance.setStatus(SagaStatus.COMPLETED);
            sagaInstanceRepository.save(sagaInstance);
            log.info("Saga {} completed successfully.", sagaInstance.getId());
        }
    }

    private void handleFailure(SagaInstance sagaInstance, SagaStepDefinition currentStepDefinition, String payload) {
        log.error("Saga {} failed at step {}. Initiating compensation.", sagaInstance.getId(), currentStepDefinition.getReplyDestination());

        // 1. Record the failed step
        SagaStepInstance failedStepInstance = SagaStepInstance.builder()
                .sagaInstance(sagaInstance)
                .stepName(currentStepDefinition.getReplyDestination()) // Use reply destination to identify the step
                .status(SagaStepStatus.FAILED)
                .payload(payload) // This is the error payload
                .build();
        sagaStepInstanceRepository.save(failedStepInstance);

        // 2. Start compensation process
        compensate(sagaInstance);
    }

    @Transactional
    public void compensate(SagaInstance sagaInstance) {
        SagaDefinition sagaDefinition = sagaDefinitionRegistry.getSagaDefinition(sagaInstance.getSagaName());
        int currentStepIndex = sagaInstance.getCurrentStep();

        // Iterate backwards from the current (failed) step to compensate completed steps
        for (int i = currentStepIndex; i >= 0; i--) {
            SagaStepDefinition stepToCompensate = sagaDefinition.getSteps().get(i);

            // Find the corresponding completed step instance to get the original payload
            Optional<SagaStepInstance> stepInstanceOpt = sagaStepInstanceRepository
                    .findFirstBySagaInstanceAndStepNameAndStatusOrderByCreatedAtDesc(
                            sagaInstance, stepToCompensate.getReplyDestination(), SagaStepStatus.COMPLETED);

            if (stepInstanceOpt.isPresent()) {
                SagaStepInstance completedStep = stepInstanceOpt.get();
                log.info("Compensating step: '{}' for saga: {}", stepToCompensate.getReplyDestination(), sagaInstance.getId());

                // Send compensation command with the *original* payload of that step
                streamBridge.send(stepToCompensate.getCompensationDestination(), completedStep.getPayload());

                // Mark the original step as compensated
                completedStep.setStatus(SagaStepStatus.COMPENSATED);
                sagaStepInstanceRepository.save(completedStep);
            }
        }

        // 3. Mark the saga as rolled back
        sagaInstance.setStatus(SagaStatus.ROLLED_BACK);
        sagaInstanceRepository.save(sagaInstance);
        log.info("Saga {} has been successfully rolled back.", sagaInstance.getId());
    }


    private void executeStep(SagaInstance sagaInstance, SagaDefinition sagaDefinition, int stepIndex, String payload) {
        SagaStepDefinition currentStep = sagaDefinition.getSteps().get(stepIndex);
        log.info("Executing step {} for saga instance {}", stepIndex, sagaInstance.getId());
        streamBridge.send(currentStep.getCommandDestination(), payload);
    }
}
