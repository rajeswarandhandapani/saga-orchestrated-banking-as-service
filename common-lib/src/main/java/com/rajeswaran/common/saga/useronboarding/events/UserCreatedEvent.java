package com.rajeswaran.common.saga.useronboarding.events;

import java.time.Instant;

import com.rajeswaran.common.entity.User;
import com.rajeswaran.common.saga.event.BaseEvent;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Event indicating a user was successfully created.
 */
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class UserCreatedEvent extends BaseEvent {
    @NotNull
    private User user;

    public static UserCreatedEvent create(Long sagaId, String correlationId, User user) {
        return UserCreatedEvent.builder()
            .eventId(java.util.UUID.randomUUID().toString())
            .sagaId(sagaId)
            .correlationId(correlationId)
            .timestamp(Instant.now())
            .success(true)
            .errorMessage(null)
            .user(user)
            .build();
    }
}
