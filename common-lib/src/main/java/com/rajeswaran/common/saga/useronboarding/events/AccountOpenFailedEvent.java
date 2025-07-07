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
 * Event indicating an account opening failed.
 */
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class AccountOpenFailedEvent extends BaseEvent {
    @NotBlank
    private String userId;
    
    @NotBlank
    private String username;

    public static AccountOpenFailedEvent create(Long sagaId, String userId, String username, String errorMessage) {
        return AccountOpenFailedEvent.builder()
            .eventId(java.util.UUID.randomUUID().toString())
            .sagaId(sagaId)
            .timestamp(Instant.now())
            .success(false)
            .errorMessage(errorMessage)
            .userId(userId)
            .username(username)
            .build();
    }
}
