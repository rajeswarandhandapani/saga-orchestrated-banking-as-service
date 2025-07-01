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
public class AccountOpenFailedEvent extends BaseEvent {rajeswaran.common.useronboarding.events;

import com.rajeswaran.common.event.BaseEvent;
import com.rajeswaran.common.saga.SagaId;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

/**
 * Event indicating account opening failed.
 */
public class AccountOpenFailedEvent extends BaseEvent {
    
    @NotBlank
    private final String userId;
    
    public AccountOpenFailedEvent(String eventId, SagaId sagaId, String correlationId, Instant timestamp,
                                String userId, String errorMessage) {
        super(eventId, sagaId, correlationId, timestamp, false, errorMessage);
        this.userId = userId;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public static AccountOpenFailedEvent create(SagaId sagaId, String correlationId, 
                                              String userId, String errorMessage) {
        return new AccountOpenFailedEvent(
            java.util.UUID.randomUUID().toString(),
            sagaId,
            correlationId,
            Instant.now(),
            userId,
            errorMessage
        );
    }
}
