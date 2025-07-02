package com.rajeswaran.common.useronboarding.events;

import com.rajeswaran.common.event.BaseEvent;
import com.rajeswaran.common.saga.SagaId;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

import java.time.Instant;

/**
 * Event indicating an account opening failed.
 */
@Getter
public class AccountOpenFailedEvent extends BaseEvent {
    @NotBlank
    private final String userId;
    
    @NotBlank
    private final String username;

    public AccountOpenFailedEvent(String eventId, SagaId sagaId, String correlationId, Instant timestamp,
                                  boolean success, String errorMessage, String userId, String username) {
        super(eventId, sagaId, correlationId, timestamp, success, errorMessage);
        this.userId = userId;
        this.username = username;
    }

    public static AccountOpenFailedEvent create(SagaId sagaId, String correlationId,
                                                String userId, String username, String errorMessage) {
        return new AccountOpenFailedEvent(
            java.util.UUID.randomUUID().toString(),
            sagaId,
            correlationId,
            Instant.now(),
            false,
            errorMessage,
            userId,
            username
        );
    }
}
