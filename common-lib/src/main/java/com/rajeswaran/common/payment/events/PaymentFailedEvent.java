package com.rajeswaran.common.payment.events;

import com.rajeswaran.common.event.BaseEvent;
import com.rajeswaran.common.saga.SagaId;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

/**
 * Event indicating a payment has failed.
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
    
    @Positive
    private double amount;
    
    @NotBlank
    private String reason;
    
    private String details;
    
    public static PaymentFailedEvent create(SagaId sagaId, String correlationId, String paymentId,
                                            String sourceAccountNumber, String destinationAccountNumber,
                                            double amount, String reason, String details) {
        return PaymentFailedEvent.builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .sagaId(sagaId)
                .correlationId(correlationId)
                .timestamp(Instant.now())
                .paymentId(paymentId)
                .sourceAccountNumber(sourceAccountNumber)
                .destinationAccountNumber(destinationAccountNumber)
                .amount(amount)
                .reason(reason)
                .details(details)
                .build();
    }
}
