package com.rajeswaran.common.useronboarding.events;

import com.rajeswaran.common.event.BaseEvent;
import com.rajeswaran.common.saga.SagaId;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

/**
 * Event indicating an account was successfully opened.
 */
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class AccountOpenedEvent extends BaseEvent {
    @NotBlank
    private String accountId;
    @NotBlank
    private String userId;
    @NotBlank
    private String accountType;
    @NotBlank
    private String accountNumber;

    public static AccountOpenedEvent create(SagaId sagaId, String correlationId,
                                            String accountId, String userId, String accountType, String accountNumber) {
        return AccountOpenedEvent.builder()
            .eventId(java.util.UUID.randomUUID().toString())
            .sagaId(sagaId)
            .correlationId(correlationId)
            .timestamp(Instant.now())
            .success(true)
            .errorMessage(null)
            .accountId(accountId)
            .userId(userId)
            .accountType(accountType)
            .accountNumber(accountNumber)
            .build();
    }
}
