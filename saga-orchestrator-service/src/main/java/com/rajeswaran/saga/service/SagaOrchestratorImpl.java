package com.rajeswaran.saga.service;

import com.rajeswaran.saga.definition.SagaDefinition;
import com.rajeswaran.saga.definition.SagaDefinitionRegistry;
import com.rajeswaran.saga.definition.SagaStepDefinition;
import com.rajeswaran.saga.entity.SagaInstance;
import com.rajeswaran.saga.model.SagaStatus;
import com.rajeswaran.saga.repository.SagaInstanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SagaOrchestratorImpl implements SagaOrchestrator {

    private final SagaInstanceRepository sagaInstanceRepository;
    private final SagaDefinitionRegistry sagaDefinitionRegistry;
    private final StreamBridge streamBridge;

    @Override
    @Transactional
    public SagaInstance startSaga(String sagaName, String payload) {
        SagaDefinition sagaDefinition = sagaDefinitionRegistry.getSagaDefinition(sagaName);
        if (sagaDefinition == null) {
            throw new IllegalArgumentException("Saga not found: " + sagaName);
        }

        SagaInstance sagaInstance = SagaInstance.builder()
                .sagaName(sagaName)
                .status(SagaStatus.STARTED)
                .build();
        SagaInstance savedInstance = sagaInstanceRepository.save(sagaInstance);

        // Execute the first step
        executeStep(savedInstance, sagaDefinition, payload);

        return savedInstance;
    }

    private void executeStep(SagaInstance sagaInstance, SagaDefinition sagaDefinition, String payload) {
        // For simplicity, we are not handling the step index here.
        // In a real implementation, you would get the current step from the sagaInstance.
        SagaStepDefinition currentStep = sagaDefinition.getSteps().get(0); // Assuming we start at step 0

        // Send command to the participant service
        streamBridge.send(currentStep.getCommandDestination(), payload);
    }
}
