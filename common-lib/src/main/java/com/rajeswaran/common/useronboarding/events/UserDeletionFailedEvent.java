package com.rajeswaran.common.useronboarding.events;

import com.rajeswaran.common.event.BaseEvent;
import com.rajeswaran.common.saga.SagaId;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.ToString;

import java.time.Instant;

/**
 * Event indicating user deletion failed (compensation action failed).
 */
@Getter
@ToString
public class UserDeletionFailedEvent extends BaseEvent {
    @NotBlank
    private final String username;

    public UserDeletionFailedEvent(String eventId, SagaId sagaId, String correlationId, Instant timestamp,
                                  boolean success, String errorMessage, String username) {
        super(eventId, sagaId, correlationId, timestamp, success, errorMessage);
        this.username = username;
    }

    public static UserDeletionFailedEvent create(SagaId sagaId, String correlationId, 
                                               String username, String errorMessage) {
        return new UserDeletionFailedEvent(
            java.util.UUID.randomUUID().toString(),
            sagaId,
            correlationId,
            Instant.now(),
            false,
            errorMessage,
            username
        );
    }
}
