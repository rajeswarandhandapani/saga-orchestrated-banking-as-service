package com.rajeswaran.common.saga;

/**
 * Enumeration of possible saga states throughout its lifecycle.
 * Tracks the overall progress and outcome of a saga workflow.
 */
public enum SagaStatus {
    /**
     * Saga has been created but not yet started.
     */
    CREATED,
    
    /**
     * Saga is currently executing its steps.
     */
    RUNNING,
    
    /**
     * Saga completed successfully - all steps executed without errors.
     */
    COMPLETED,
    
    /**
     * Saga failed and is executing compensation logic (rollback).
     */
    COMPENSATING,
    
    /**
     * Saga compensation completed successfully - all rollbacks executed.
     */
    COMPENSATED,
    
    /**
     * Saga failed and could not be compensated - manual intervention required.
     */
    COMPENSATION_FAILED,
    
    /**
     * Saga was cancelled before completion.
     */
    CANCELLED,
    
    /**
     * Saga execution timed out.
     */
    TIMEOUT;
    
    /**
     * Checks if the saga is in a final state (completed, compensated, or failed).
     * @return true if saga is in a terminal state
     */
    public boolean isFinal() {
        return this == COMPLETED || 
               this == COMPENSATED || 
               this == COMPENSATION_FAILED || 
               this == CANCELLED || 
               this == TIMEOUT;
    }
    
    /**
     * Checks if the saga can be cancelled in its current state.
     * @return true if cancellation is allowed
     */
    public boolean isCancellable() {
        return this == CREATED || this == RUNNING;
    }
}
