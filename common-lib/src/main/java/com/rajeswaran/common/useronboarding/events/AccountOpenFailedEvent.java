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

    public AccountOpenFailedEvent(String eventId, SagaId sagaId, String correlationId, Instant timestamp,
                                  boolean success, String errorMessage, String userId) {
        super(eventId, sagaId, correlationId, timestamp, success, errorMessage);
        this.userId = userId;
    }

    public static AccountOpenFailedEvent create(SagaId sagaId, String correlationId,
                                                String userId, String errorMessage) {
        return new AccountOpenFailedEvent(
            java.util.UUID.randomUUID().toString(),
            sagaId,
            correlationId,
            Instant.now(),
            false,
            errorMessage,
            userId
        );
    }
}
