package com.rajeswaran.common.saga.event;

import java.time.Instant;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Base abstract class for all events with common fields.
 * Provides shared functionality and ensures Spring Cloud Stream compatibility.
 */
@Getter
@ToString
@SuperBuilder
public abstract class BaseEvent implements Event {
    
    @NotNull
    private String eventId;
    
    @NotNull
    private Long sagaId;
    
    @NotNull
    private String correlationId;
    
    @NotNull
    private Instant timestamp;
    
    private boolean success;
    
    private String errorMessage;
    
    @Override
    public String getEventType() {
        return this.getClass().getSimpleName();
    }

    public BaseEvent() {
    }

    public BaseEvent(String eventId, Long sagaId, String correlationId, Instant timestamp, boolean success, String errorMessage) {
        this.eventId = eventId;
        this.sagaId = sagaId;
        this.correlationId = correlationId;
        this.timestamp = timestamp;
        this.success = success;
        this.errorMessage = errorMessage;
    }
}
