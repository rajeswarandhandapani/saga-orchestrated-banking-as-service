package com.rajeswaran.common.saga.useronboarding.events;

import com.rajeswaran.common.saga.event.BaseEvent;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

/**
 * Event indicating a user was successfully deleted (compensation action).
 */
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class UserDeletedEvent extends BaseEvent {
    @NotBlank
    private String username;

    public static UserDeletedEvent create(Long sagaId, String username) {
        return UserDeletedEvent.builder()
            .eventId(java.util.UUID.randomUUID().toString())
            .sagaId(sagaId)
            .timestamp(Instant.now())
            .success(true)
            .errorMessage(null)
            .username(username)
            .build();
    }
}
