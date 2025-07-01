package com.rajeswaran.common.useronboarding.events;

import com.rajeswaran.common.event.BaseEvent;
import com.rajeswaran.common.saga.SagaId;
import lombok.Getter;

import java.time.Instant;

/**
 * Event indicating user creation failed.
 */
public class UserCreationFailedEvent extends BaseEvent {
    
    public UserCreationFailedEvent(String eventId, SagaId sagaId, String correlationId, Instant timestamp,
                                 String errorMessage) {
        super(eventId, sagaId, correlationId, timestamp, false, errorMessage);
    }
    
    public static UserCreationFailedEvent create(SagaId sagaId, String correlationId, String errorMessage) {
        return new UserCreationFailedEvent(
            java.util.UUID.randomUUID().toString(),
            sagaId,
            correlationId,
            Instant.now(),
            errorMessage
        );
    }
}
