package com.rajeswaran.common.saga.payment.events;

import com.rajeswaran.common.saga.event.BaseEvent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Event indicating transaction has been successfully recorded.
 */
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class TransactionRecordedEvent extends BaseEvent {

    @NotBlank
    private String paymentId;

    @NotBlank
    private String sourceAccountNumber;

    @NotBlank
    private String destinationAccountNumber;

    @Positive
    private double amount;

    private String description;

    private String transactionType;

    public static TransactionRecordedEvent create(Long sagaId, String correlationId, String paymentId,
                                                 String sourceAccountNumber, String destinationAccountNumber,
                                                 double amount, String description, String transactionType) {
        return TransactionRecordedEvent.builder()
            .eventId(java.util.UUID.randomUUID().toString())
            .sagaId(sagaId)
            .correlationId(correlationId)
            .timestamp(java.time.Instant.now())
            .paymentId(paymentId)
            .sourceAccountNumber(sourceAccountNumber)
            .destinationAccountNumber(destinationAccountNumber)
            .amount(amount)
            .description(description)
            .transactionType(transactionType)
            .build();
    }
}
