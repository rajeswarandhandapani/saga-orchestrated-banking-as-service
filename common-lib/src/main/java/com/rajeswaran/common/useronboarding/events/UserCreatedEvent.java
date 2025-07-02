package com.rajeswaran.common.useronboarding.events;

import com.rajeswaran.common.event.BaseEvent;
import com.rajeswaran.common.model.dto.UserDTO;
import com.rajeswaran.common.saga.SagaId;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.Instant;

/**
 * Event indicating a user was successfully created.
 */
@Getter
public class UserCreatedEvent extends BaseEvent {
    @NotBlank
    private final String userId;
    @NotNull
    private final UserDTO user;

    public UserCreatedEvent(String eventId, SagaId sagaId, String correlationId, Instant timestamp,
                            boolean success, String errorMessage, String userId, UserDTO user) {
        super(eventId, sagaId, correlationId, timestamp, success, errorMessage);
        this.userId = userId;
        this.user = user;
    }

    public static UserCreatedEvent create(SagaId sagaId, String correlationId, String userId, UserDTO user) {
        return new UserCreatedEvent(
            java.util.UUID.randomUUID().toString(),
            sagaId,
            correlationId,
            Instant.now(),
            true,
            null,
            userId,
            user
        );
    }
}
