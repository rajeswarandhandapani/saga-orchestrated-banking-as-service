package com.rajeswaran.sagaorchestrator.service;

import com.rajeswaran.sagaorchestrator.entity.SagaInstance;

public interface SagaOrchestrator {

    // Saga lifecycle
    SagaInstance startSaga(String sagaName);
    void completeSaga(Long sagaId);
    void failSaga(Long sagaId);
    
    // Step lifecycle (with mandatory payload for audit trail)
    void startStep(Long sagaId, String stepName, String payload);
    void completeStep(Long sagaId, String stepName, String payload);
    void failStep(Long sagaId, String stepName, String errorMessage);

}
