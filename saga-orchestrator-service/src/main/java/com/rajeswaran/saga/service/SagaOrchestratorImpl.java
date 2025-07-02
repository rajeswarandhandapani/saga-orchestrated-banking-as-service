package com.rajeswaran.saga.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rajeswaran.saga.entity.SagaInstance;
import com.rajeswaran.saga.entity.SagaStepInstance;
import com.rajeswaran.saga.model.SagaStatus;
import com.rajeswaran.saga.model.SagaStepStatus;
import com.rajeswaran.saga.repository.SagaInstanceRepository;
import com.rajeswaran.saga.repository.SagaStepInstanceRepository;
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
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public SagaInstance startSaga(String sagaName, Map<String, Object> payload) {
        log.info("Starting saga: {} with payload keys: {}", sagaName, payload.keySet());

        String payloadAsString;
        try {
            payloadAsString = objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize saga payload for saga: {}", sagaName, e);
            throw new RuntimeException("Failed to serialize saga payload", e);
        }

        // Create the main saga instance tracker
        SagaInstance sagaInstance = SagaInstance.builder()
                .sagaName(sagaName)
                .status(SagaStatus.IN_PROGRESS)
                .currentStep(0)
                .build();
        SagaInstance savedInstance = sagaInstanceRepository.save(sagaInstance);

        // Create a step instance for the initial payload
        SagaStepInstance initialStep = SagaStepInstance.builder()
                .sagaInstance(savedInstance)
                .stepName("SagaInitiated")
                .status(SagaStepStatus.COMPLETED)
                .payload(payloadAsString)
                .build();
        sagaStepInstanceRepository.save(initialStep);

        log.info("Saga {} created successfully with ID: {}", sagaName, savedInstance.getId());
        return savedInstance;
    }

    @Override
    @Transactional
    public void recordStep(Long sagaId, String stepName, SagaStepStatus status) {
        log.info("Recording step '{}' with status '{}' for saga {}", stepName, status, sagaId);
        
        SagaInstance sagaInstance = sagaInstanceRepository.findById(sagaId)
                .orElseThrow(() -> new RuntimeException("Saga instance not found: " + sagaId));
        
        SagaStepInstance stepInstance = SagaStepInstance.builder()
                .sagaInstance(sagaInstance)
                .stepName(stepName)
                .status(status)
                .payload("") // Empty payload for now
                .build();
        
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
