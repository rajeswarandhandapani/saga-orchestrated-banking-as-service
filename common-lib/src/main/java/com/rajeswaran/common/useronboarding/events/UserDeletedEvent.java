package com.rajeswaran.common.useronboarding.events;

import com.rajeswaran.common.event.BaseEvent;
import com.rajeswaran.common.saga.SagaId;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.ToString;

import java.time.Instant;

/**
 * Event indicating a user was successfully deleted (compensation action).
 */
@Getter
@ToString
public class UserDeletedEvent extends BaseEvent {
    @NotBlank
    private final String username;

    public UserDeletedEvent(String eventId, SagaId sagaId, String correlationId, Instant timestamp,
                           boolean success, String errorMessage, String username) {
        super(eventId, sagaId, correlationId, timestamp, success, errorMessage);
        this.username = username;
    }

    public static UserDeletedEvent create(SagaId sagaId, String correlationId, String username) {
        return new UserDeletedEvent(
            java.util.UUID.randomUUID().toString(),
            sagaId,
            correlationId,
            Instant.now(),
            true,
            null,
            username
        );
    }
}
