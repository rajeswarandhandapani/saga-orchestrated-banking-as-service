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

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SagaOrchestratorImpl implements SagaOrchestrator {

    private final SagaInstanceRepository sagaInstanceRepository;
    private final SagaStepInstanceRepository sagaStepInstanceRepository;

    @Override
    @Transactional
    public SagaInstance startSaga(String sagaName, Map<String, Object> payload) {
        log.info("Starting saga: {} with payload keys: {}", sagaName, payload.keySet());

        // Create the main saga instance tracker
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
    public void recordStep(Long sagaId, String stepName, SagaStepStatus status, String payload) {
        log.info("Recording step '{}' with status '{}' for saga {}", stepName, status, sagaId);
        
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
    
    @Override
    @Transactional
    public void updateStepStatus(Long sagaId, String stepName, SagaStepStatus status) {
        log.info("Updating step '{}' to status '{}' for saga {}", stepName, status, sagaId);
        
        SagaInstance sagaInstance = sagaInstanceRepository.findById(sagaId)
                .orElseThrow(() -> new RuntimeException("Saga instance not found: " + sagaId));
        
        // Find the most recent step with the given name
        SagaStepInstance stepInstance = sagaStepInstanceRepository.findFirstBySagaInstanceAndStepNameOrderByCreatedAtDesc(sagaInstance, stepName)
                .orElseThrow(() -> new RuntimeException("Step instance not found for saga " + sagaId + " and step: " + stepName));
        
        stepInstance.setStatus(status);
        sagaStepInstanceRepository.save(stepInstance);
    }
    
    @Override
    @Transactional
    public void updateSagaState(Long sagaId, SagaStatus status) {
        log.info("Updating saga {} status to: {}", sagaId, status);
        
        SagaInstance sagaInstance = sagaInstanceRepository.findById(sagaId)
                .orElseThrow(() -> new RuntimeException("Saga instance not found: " + sagaId));
        
        sagaInstance.setStatus(status);
        sagaInstanceRepository.save(sagaInstance);
    }
    
    @Override
    @Transactional
    public void compensate(Long sagaId) {
        log.info("Starting compensation for saga {}", sagaId);
        
        SagaInstance sagaInstance = sagaInstanceRepository.findById(sagaId)
                .orElseThrow(() -> new RuntimeException("Saga instance not found: " + sagaId));
        
        // Mark the saga as rolled back
        sagaInstance.setStatus(SagaStatus.ROLLED_BACK);
        sagaInstanceRepository.save(sagaInstance);
        log.info("Saga {} has been successfully rolled back.", sagaInstance.getId());
    }
}
