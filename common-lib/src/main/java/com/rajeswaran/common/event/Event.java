package com.rajeswaran.common.event;

import com.rajeswaran.common.saga.SagaId;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

/**
 * Simple event interface for saga operations.
 */
public interface Event {
    
    @NotNull
    String eventId();
    
    @NotNull
    SagaId sagaId();
    
    @NotNull
    String correlationId();
    
    @NotNull
    Instant timestamp();
    
    @NotNull
    String eventType();
    
    boolean isSuccess();
    
    String errorMessage();
}
