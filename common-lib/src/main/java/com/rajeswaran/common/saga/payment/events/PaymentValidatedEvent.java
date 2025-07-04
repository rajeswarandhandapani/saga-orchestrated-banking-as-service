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
 * Event indicating a payment has been successfully validated.
 */
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class PaymentValidatedEvent extends BaseEvent {

    @NotBlank
    private String paymentId;

    @NotBlank
    private String sourceAccountNumber;

    @NotBlank
    private String destinationAccountNumber;

    @Positive
    private double amount;

    private String description;

    private String username;

    public static PaymentValidatedEvent create(SagaId sagaId, String correlationId, String paymentId,
                                              String sourceAccountNumber, String destinationAccountNumber,
                                              double amount, String description, String username) {
        return PaymentValidatedEvent.builder()
            .eventId(java.util.UUID.randomUUID().toString())
            .sagaId(sagaId)
            .correlationId(correlationId)
            .timestamp(java.time.Instant.now())
            .paymentId(paymentId)
            .sourceAccountNumber(sourceAccountNumber)
            .destinationAccountNumber(destinationAccountNumber)
            .amount(amount)
            .description(description)
            .username(username)
            .build();
    }
}
