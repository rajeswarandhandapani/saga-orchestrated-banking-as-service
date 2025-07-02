package com.rajeswaran.sagaorchestrator.saga;

import com.rajeswaran.sagaorchestrator.entity.SagaInstance;
import com.rajeswaran.sagaorchestrator.service.SagaStateManager;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.stream.function.StreamBridge;

/**
 * Abstract base class defining the core saga lifecycle operations.
 * 
 * This abstract class provides common fields and default implementations for saga operations
 * and delegates to the SagaStateManager for state management.
 * 
 * @author Rajeswaran
 * @since 1.0.0
 */
@RequiredArgsConstructor
public abstract class Saga {
    
    protected final SagaStateManager sagaStateManager;
    protected final StreamBridge streamBridge;
    
    /**
     * Get the name of this saga type.
     * 
     * @return the saga name
     */
    public abstract String getSagaName();
    
    /**
     * Start the saga business logic after the saga instance is created.
     * This method should trigger the first command in the saga flow.
     * 
     * @param sagaId the saga ID
     * @param payload the initial payload to start the saga
     */
    public abstract void startSagaFlow(Long sagaId, Object payload);
    
    /**
     * Complete the saga business logic before marking the saga as completed.
     * This method can be used for any final cleanup or notifications.
     * 
     * @param sagaId the saga ID
     */
    public abstract void completeSagaFlow(Long sagaId);
    
    /**
     * Start a new saga instance.
     * 
     * @return the created SagaInstance with generated saga ID
     */
    public SagaInstance startSaga() {
        return sagaStateManager.startSaga(getSagaName());
    }
    
    /**
     * Start a new saga instance and immediately begin the saga flow.
     * 
     * @param payload the initial payload to start the saga
     * @return the created SagaInstance with generated saga ID
     */
    public SagaInstance startSaga(Object payload) {
        SagaInstance sagaInstance = startSaga();
        startSagaFlow(sagaInstance.getId(), payload);
        return sagaInstance;
    }
    
    /**
     * Complete a saga instance successfully.
     * 
     * @param sagaId the saga ID to complete
     */
    public void completeSaga(Long sagaId) {
        completeSagaFlow(sagaId);
        sagaStateManager.completeSaga(sagaId);
    }
    
    /**
     * Fail a saga instance.
     * 
     * @param sagaId the saga ID to fail
     */
    public void failSaga(Long sagaId) {
        sagaStateManager.failSaga(sagaId);
    }
    
    /**
     * Record the start of a saga step.
     * 
     * @param sagaId the saga ID
     * @param stepName the step name
     * @param payload the step payload
     */
    public void startStep(Long sagaId, String stepName, Object payload) {
        sagaStateManager.startStep(sagaId, stepName, payload);
    }
    
    /**
     * Complete a saga step successfully.
     * 
     * @param sagaId the saga ID
     * @param stepName the step name
     * @param payload the step payload
     */
    public void completeStep(Long sagaId, String stepName, Object payload) {
        sagaStateManager.completeStep(sagaId, stepName, payload);
    }
    
    /**
     * Fail a saga step.
     * 
     * @param sagaId the saga ID
     * @param stepName the step name
     * @param errorMessage the error message or payload
     */
    public void failStep(Long sagaId, String stepName, Object errorMessage) {
        sagaStateManager.failStep(sagaId, stepName, errorMessage);
    }
}
