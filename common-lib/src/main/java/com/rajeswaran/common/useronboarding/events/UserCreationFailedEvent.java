package com.rajeswaran.common.useronboarding.events;

import com.rajeswaran.common.event.BaseEvent;
import com.rajeswaran.common.saga.SagaId;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

/**
 * Event indicating user creation failed.
 */
@Getter
@ToString
@SuperBuilder
public class UserCreationFailedEvent extends BaseEvent {

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
