package com.rajeswaran.common.saga.useronboarding.events;

import java.time.Instant;

import com.rajeswaran.common.saga.event.BaseEvent;
import com.rajeswaran.common.saga.SagaId;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Event indicating user creation failed.
 */
@Getter
@ToString
@SuperBuilder
public class UserCreationFailedEvent extends BaseEvent {

    public UserCreationFailedEvent() {
    }

    public UserCreationFailedEvent(String eventId, SagaId sagaId, String correlationId, Instant timestamp, boolean success, String errorMessage) {
        super(eventId, sagaId, correlationId, timestamp, success, errorMessage);
    }

    public static UserCreationFailedEvent create(SagaId sagaId, String correlationId, String errorMessage) {
        return UserCreationFailedEvent.builder()
            .eventId(java.util.UUID.randomUUID().toString())
            .sagaId(sagaId)
            .correlationId(correlationId)
            .timestamp(Instant.now())
            .success(false)
            .errorMessage(errorMessage)
            .build();
    }
}
