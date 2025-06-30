package com.rajeswaran.saga.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rajeswaran.saga.definition.SagaDefinition;
import com.rajeswaran.saga.definition.SagaDefinitionRegistry;
import com.rajeswaran.saga.definition.SagaStepDefinition;
import com.rajeswaran.saga.entity.SagaInstance;
import com.rajeswaran.saga.model.SagaStatus;
import com.rajeswaran.saga.repository.SagaInstanceRepository;
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

        SagaInstance sagaInstance = SagaInstance.builder()
                .sagaName(sagaName)
                .status(SagaStatus.IN_PROGRESS)
                .currentStep(0)
                .payload(payload)
                .build();
        SagaInstance savedInstance = sagaInstanceRepository.save(sagaInstance);

        // Execute the first step
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

            if (isFailure) {
                handleFailure(sagaInstance, sagaDefinition, payload);
            } else {
                handleSuccess(sagaInstance, sagaDefinition, payload);
            }
        } catch (JsonProcessingException e) {
            log.error("Error parsing payload for saga reply", e);
        }
    }

    private void handleSuccess(SagaInstance sagaInstance, SagaDefinition sagaDefinition, String payload) {
        int nextStepIndex = sagaInstance.getCurrentStep() + 1;

        if (nextStepIndex < sagaDefinition.getSteps().size()) {
            // More steps to go
            sagaInstance.setCurrentStep(nextStepIndex);
            sagaInstance.setPayload(payload); // Update payload with the result from the previous step
            sagaInstanceRepository.save(sagaInstance);
            executeStep(sagaInstance, sagaDefinition, nextStepIndex, payload);
        } else {
            // Saga is complete
            sagaInstance.setStatus(SagaStatus.COMPLETED);
            sagaInstanceRepository.save(sagaInstance);
            log.info("Saga {} completed successfully.", sagaInstance.getId());
        }
    }

    private void handleFailure(SagaInstance sagaInstance, SagaDefinition sagaDefinition, String payload) {
        log.error("Saga {} failed at step {}. Initiating compensation.", sagaInstance.getId(), sagaInstance.getCurrentStep());
        sagaInstance.setStatus(SagaStatus.FAILED);
        sagaInstanceRepository.save(sagaInstance);

        // For now, we just log the failure. A full implementation would trigger compensation.
        // In a real-world scenario, you would iterate backwards from the current step
        // and send compensation commands.
        SagaStepDefinition failedStep = sagaDefinition.getSteps().get(sagaInstance.getCurrentStep());
        log.info("Executing compensation for step: {}", failedStep.getCompensationDestination());
        streamBridge.send(failedStep.getCompensationDestination(), payload);
    }


    private void executeStep(SagaInstance sagaInstance, SagaDefinition sagaDefinition, int stepIndex, String payload) {
        SagaStepDefinition currentStep = sagaDefinition.getSteps().get(stepIndex);
        log.info("Executing step {} for saga instance {}", stepIndex, sagaInstance.getId());
        streamBridge.send(currentStep.getCommandDestination(), payload);
    }
}
