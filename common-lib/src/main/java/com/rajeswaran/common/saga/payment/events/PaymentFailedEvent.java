package com.rajeswaran.common.saga.payment.events;

import com.rajeswaran.common.saga.event.BaseEvent;
import com.rajeswaran.common.saga.SagaId;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Event indicating a payment processing has failed.
 */
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class PaymentFailedEvent extends BaseEvent {

    @NotBlank
    private String paymentId;

    @NotBlank
    private String sourceAccountNumber;

    @NotBlank
    private String destinationAccountNumber;

    private double amount;

    private String reason;

    private String username;

    public static PaymentFailedEvent create(SagaId sagaId, String correlationId, String paymentId,
                                           String sourceAccountNumber, String destinationAccountNumber,
                                           double amount, String reason, String username) {
        return PaymentFailedEvent.builder()
            .eventId(java.util.UUID.randomUUID().toString())
            .sagaId(sagaId)
            .correlationId(correlationId)
            .timestamp(java.time.Instant.now())
            .paymentId(paymentId)
            .sourceAccountNumber(sourceAccountNumber)
            .destinationAccountNumber(destinationAccountNumber)
            .amount(amount)
            .reason(reason)
            .username(username)
            .build();
    }
}
