package com.rajeswaran.common.event;

import com.rajeswaran.common.saga.SagaId;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

/**
 * Simple event interface for saga operations.
 */
public interface Event {
    
    @NotNull
    String getEventId();
    
    @NotNull
    SagaId getSagaId();
    
    @NotNull
    String getCorrelationId();
    
    @NotNull
    Instant getTimestamp();
    
    @NotNull
    String getEventType();
    
    boolean isSuccess();
    
    String getErrorMessage();
}
