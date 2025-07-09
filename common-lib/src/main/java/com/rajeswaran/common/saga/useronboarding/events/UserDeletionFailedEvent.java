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
 * Event indicating user deletion failed (compensation action failed).
 */
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class UserDeletionFailedEvent extends BaseEvent {
    @NotBlank
    private String username;

    public static UserDeletionFailedEvent create(Long sagaId, String username, String errorMessage) {
        return UserDeletionFailedEvent.builder()
            .eventId(java.util.UUID.randomUUID().toString())
            .sagaId(sagaId)
            .timestamp(Instant.now())
            .success(false)
            .errorMessage(errorMessage)
            .username(username)
            .build();
    }
}
