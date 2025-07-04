package com.rajeswaran.common.saga.payment.events;

import com.rajeswaran.common.saga.event.BaseEvent;
import com.rajeswaran.common.saga.SagaId;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Event indicating account balances have been successfully updated.
 */
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class AccountBalanceUpdatedEvent extends BaseEvent {

    @NotBlank
    private String paymentId;

    @NotBlank
    private String sourceAccountNumber;

    @NotBlank
    private String destinationAccountNumber;

    @Positive
    private double amount;

    private String description;

    public static AccountBalanceUpdatedEvent create(SagaId sagaId, String correlationId, String paymentId,
                                                   String sourceAccountNumber, String destinationAccountNumber,
                                                   double amount, String description) {
        return AccountBalanceUpdatedEvent.builder()
            .eventId(java.util.UUID.randomUUID().toString())
            .sagaId(sagaId)
            .correlationId(correlationId)
            .timestamp(java.time.Instant.now())
            .paymentId(paymentId)
            .sourceAccountNumber(sourceAccountNumber)
            .destinationAccountNumber(destinationAccountNumber)
            .amount(amount)
            .description(description)
            .build();
    }
}
