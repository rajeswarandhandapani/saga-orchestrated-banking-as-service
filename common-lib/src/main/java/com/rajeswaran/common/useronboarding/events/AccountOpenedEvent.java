package com.rajeswaran.common.useronboarding.events;

import com.rajeswaran.common.event.BaseEvent;
import com.rajeswaran.common.saga.SagaId;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

import java.time.Instant;

/**
 * Event indicating an account was successfully opened.
 */
@Getter
public class AccountOpenedEvent extends BaseEvent {
    
    @NotBlank
    private final String accountId;
    
    @NotBlank
    private final String userId;
    
    @NotBlank
    private final String accountType;
    
    @NotBlank
    private final String accountNumber;
    
    public AccountOpenedEvent(String eventId, SagaId sagaId, String correlationId, Instant timestamp,
                            String accountId, String userId, String accountType, String accountNumber) {
        super(eventId, sagaId, correlationId, timestamp, true, null);
        this.accountId = accountId;
        this.userId = userId;
        this.accountType = accountType;
        this.accountNumber = accountNumber;
    }
    
    public static AccountOpenedEvent create(SagaId sagaId, String correlationId,
                                          String accountId, String userId, String accountType, String accountNumber) {
        return new AccountOpenedEvent(
            java.util.UUID.randomUUID().toString(),
            sagaId,
            correlationId,
            Instant.now(),
            accountId,
            userId,
            accountType,
            accountNumber
        );
    }
}
