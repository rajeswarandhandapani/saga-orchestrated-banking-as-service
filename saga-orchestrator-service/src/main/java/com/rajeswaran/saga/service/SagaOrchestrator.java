package com.rajeswaran.saga.service;

import com.rajeswaran.saga.entity.SagaInstance;
import com.rajeswaran.saga.model.SagaStatus;
import com.rajeswaran.saga.model.SagaStepStatus;

import java.util.Map;

public interface SagaOrchestrator {

    SagaInstance startSaga(String sagaName, Map<String, Object> payload);
    
    // Methods for UserOnboardingSaga support
    void recordStep(Long sagaId, String stepName, SagaStepStatus status);
    
    void updateSagaState(Long sagaId, SagaStatus status);
    
    void compensate(Long sagaId);

}
