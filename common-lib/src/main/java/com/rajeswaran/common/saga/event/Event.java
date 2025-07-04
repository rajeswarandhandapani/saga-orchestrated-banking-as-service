package com.rajeswaran.common.saga.event;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;

/**
 * Simple event interface for saga operations.
 */
public interface Event {
    
    @NotNull
    String getEventId();
    
    @NotNull
    Long getSagaId();
    
    @NotNull
    String getCorrelationId();
    
    @NotNull
    Instant getTimestamp();
    
    @NotNull
    String getEventType();
    
    boolean isSuccess();
    
    String getErrorMessage();
}
