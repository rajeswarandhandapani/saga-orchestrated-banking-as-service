package com.rajeswaran.common.useronboarding.events;

import com.rajeswaran.common.event.BaseEvent;
import com.rajeswaran.common.saga.SagaId;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

import java.time.Instant;

/**
 * Event indicating a user was successfully created.
 */
@Getter
public class UserCreatedEvent extends BaseEvent {
    @NotBlank
    private final String userId;
    @NotBlank
    private final String username;
    @NotBlank
    private final String email;
    @NotBlank
    private final String fullName;

    public UserCreatedEvent(String eventId, SagaId sagaId, String correlationId, Instant timestamp,
                            boolean success, String errorMessage, String userId, String username, String email, String fullName) {
        super(eventId, sagaId, correlationId, timestamp, success, errorMessage);
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
    }

    public static UserCreatedEvent create(SagaId sagaId, String correlationId,
                                          String userId, String username, String email, String fullName) {
        return new UserCreatedEvent(
            java.util.UUID.randomUUID().toString(),
            sagaId,
            correlationId,
            Instant.now(),
            true,
            null,
            userId,
            username,
            email,
            fullName
        );
    }
}
