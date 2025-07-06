package com.rajeswaran.sagaorchestrator.service;

import com.rajeswaran.sagaorchestrator.entity.SagaInstance;

import java.util.List;

public interface SagaStateManager {

    // Saga lifecycle
    SagaInstance startSaga(String sagaName);
    void completeSaga(Long sagaId);
    void failSaga(Long sagaId);
    
    // Step lifecycle (with mandatory payload for audit trail)
    void startStep(Long sagaId, String stepName, Object payload);
    void completeStep(Long sagaId, String stepName, Object payload);
    void failStep(Long sagaId, String stepName, Object errorMessage);

    // Saga query operations
    List<SagaInstance> getAllSagaInstances();

}
