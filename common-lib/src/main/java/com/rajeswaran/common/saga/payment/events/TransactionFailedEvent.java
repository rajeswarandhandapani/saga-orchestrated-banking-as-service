package com.rajeswaran.common.saga.payment.events;

import com.rajeswaran.common.saga.event.BaseEvent;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Event indicating transaction recording has failed.
 */
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class TransactionFailedEvent extends BaseEvent {

    @NotBlank
    private String paymentId;

    @NotBlank
    private String sourceAccountNumber;

    @NotBlank
    private String destinationAccountNumber;

    private double amount;

    private String reason;

    private String transactionType;

    public static TransactionFailedEvent create(Long sagaId, String correlationId, String paymentId,
                                               String sourceAccountNumber, String destinationAccountNumber,
                                               double amount, String reason, String transactionType) {
        return TransactionFailedEvent.builder()
            .eventId(java.util.UUID.randomUUID().toString())
            .sagaId(sagaId)
            .correlationId(correlationId)
            .timestamp(java.time.Instant.now())
            .paymentId(paymentId)
            .sourceAccountNumber(sourceAccountNumber)
            .destinationAccountNumber(destinationAccountNumber)
            .amount(amount)
            .reason(reason)
            .transactionType(transactionType)
            .build();
    }
}
