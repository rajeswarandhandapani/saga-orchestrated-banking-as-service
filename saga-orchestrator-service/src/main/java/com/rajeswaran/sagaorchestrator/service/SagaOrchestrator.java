package com.rajeswaran.sagaorchestrator.service;

import com.rajeswaran.sagaorchestrator.entity.SagaInstance;
import com.rajeswaran.sagaorchestrator.model.SagaStatus;
import com.rajeswaran.sagaorchestrator.model.SagaStepStatus;

import java.util.Map;

public interface SagaOrchestrator {

    SagaInstance startSaga(String sagaName, Map<String, Object> payload);
    
    // Methods for UserOnboardingSaga support
    void recordStep(Long sagaId, String stepName, SagaStepStatus status);
    
    void recordStep(Long sagaId, String stepName, SagaStepStatus status, String payload);
    
    void updateSagaState(Long sagaId, SagaStatus status);
    
    void compensate(Long sagaId);

}
