package com.rajeswaran.common.event;

import com.rajeswaran.common.saga.SagaId;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

/**
 * Base abstract class for all events with common fields.
 * Provides shared functionality and ensures Spring Cloud Stream compatibility.
 */
@Getter
@AllArgsConstructor
public abstract class BaseEvent implements Event {
    
    @NotNull
    private final String eventId;
    
    @NotNull
    private final SagaId sagaId;
    
    @NotNull
    private final String correlationId;
    
    @NotNull
    private final Instant timestamp;
    
    private final boolean success;
    
    private final String errorMessage;
    
    @Override
    public String getEventType() {
        return this.getClass().getSimpleName();
    }
}
