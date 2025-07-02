package com.rajeswaran.sagaorchestrator.service;

import com.rajeswaran.sagaorchestrator.entity.SagaInstance;
import com.rajeswaran.sagaorchestrator.entity.SagaStepInstance;
import com.rajeswaran.sagaorchestrator.model.SagaStatus;
import com.rajeswaran.sagaorchestrator.model.SagaStepStatus;
import com.rajeswaran.sagaorchestrator.repository.SagaInstanceRepository;
import com.rajeswaran.sagaorchestrator.repository.SagaStepInstanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SagaStateManagerImpl implements SagaStateManager {

    private final SagaInstanceRepository sagaInstanceRepository;
    private final SagaStepInstanceRepository sagaStepInstanceRepository;

    @Override
    @Transactional
    public SagaInstance startSaga(String sagaName) {
        log.info("Starting saga: {}", sagaName);

        SagaInstance sagaInstance = SagaInstance.builder()
                .sagaName(sagaName)
                .status(SagaStatus.IN_PROGRESS)
                .currentStep(0)
                .build();
        SagaInstance savedInstance = sagaInstanceRepository.save(sagaInstance);

        log.info("Saga {} created successfully with ID: {}", sagaName, savedInstance.getId());
        return savedInstance;
    }

    @Override
    @Transactional
    public void completeSaga(Long sagaId) {
        log.info("Completing saga {}", sagaId);
        updateSagaStatus(sagaId, SagaStatus.COMPLETED);
    }

    @Override
    @Transactional
    public void failSaga(Long sagaId) {
        log.info("Failing saga {}", sagaId);
        updateSagaStatus(sagaId, SagaStatus.ROLLED_BACK);
    }

    @Override
    @Transactional
    public void startStep(Long sagaId, String stepName, Object payload) {
        log.info("Starting step '{}' for saga {}", stepName, sagaId);
        recordStep(sagaId, stepName, SagaStepStatus.STARTED, payload != null ? payload.toString() : "");
    }

    @Override
    @Transactional
    public void completeStep(Long sagaId, String stepName, Object payload) {
        log.info("Completing step '{}' for saga {}", stepName, sagaId);
        updateStepStatus(sagaId, stepName, SagaStepStatus.COMPLETED, payload != null ? payload.toString() : "");
    }

    @Override
    @Transactional
    public void failStep(Long sagaId, String stepName, Object errorMessage) {
        log.info("Failing step '{}' for saga {} with error: {}", stepName, sagaId, errorMessage);
        updateStepStatus(sagaId, stepName, SagaStepStatus.FAILED, errorMessage != null ? errorMessage.toString() : "");
    }

    // Private helper methods
    
    private void updateSagaStatus(Long sagaId, SagaStatus status) {
        SagaInstance sagaInstance = sagaInstanceRepository.findById(sagaId)
                .orElseThrow(() -> new RuntimeException("Saga instance not found: " + sagaId));
        
        sagaInstance.setStatus(status);
        sagaInstanceRepository.save(sagaInstance);
    }

    private void recordStep(Long sagaId, String stepName, SagaStepStatus status, String payload) {
        SagaInstance sagaInstance = sagaInstanceRepository.findById(sagaId)
                .orElseThrow(() -> new RuntimeException("Saga instance not found: " + sagaId));
        
        SagaStepInstance stepInstance = SagaStepInstance.builder()
                .sagaInstance(sagaInstance)
                .stepName(stepName)
                .status(status)
                .payload(payload != null ? payload : "")
                .build();
        
        sagaStepInstanceRepository.save(stepInstance);
    }
    
    private void updateStepStatus(Long sagaId, String stepName, SagaStepStatus status, String payload) {
        SagaInstance sagaInstance = sagaInstanceRepository.findById(sagaId)
                .orElseThrow(() -> new RuntimeException("Saga instance not found: " + sagaId));
        
        // Find the most recent step with the given name
        SagaStepInstance stepInstance = sagaStepInstanceRepository.findFirstBySagaInstanceAndStepNameOrderByCreatedAtDesc(sagaInstance, stepName)
                .orElseThrow(() -> new RuntimeException("Step instance not found for saga " + sagaId + " and step: " + stepName));
        
        stepInstance.setStatus(status);
        stepInstance.setPayload(payload != null ? payload : "");
        sagaStepInstanceRepository.save(stepInstance);
    }
}
