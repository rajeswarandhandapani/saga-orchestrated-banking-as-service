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
 * Event indicating a payment has been initiated.
 */
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class PaymentInitiatedEvent extends BaseEvent {
    
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
    
    private String details;
    
    private String serviceName;
    
    private String eventType;
    
    public static PaymentInitiatedEvent create(SagaId sagaId, String correlationId, String paymentId,
                                               String sourceAccountNumber, String destinationAccountNumber,
                                               double amount, String description) {
        return PaymentInitiatedEvent.builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .sagaId(sagaId)
                .correlationId(correlationId)
                .timestamp(Instant.now())
                .paymentId(paymentId)
                .sourceAccountNumber(sourceAccountNumber)
                .destinationAccountNumber(destinationAccountNumber)
                .amount(amount)
                .description(description)
                .build();
    }
}
