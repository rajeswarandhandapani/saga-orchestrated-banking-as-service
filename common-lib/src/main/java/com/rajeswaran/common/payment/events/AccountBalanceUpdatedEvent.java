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
 * Event indicating account balance has been updated after a transaction.
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
    
    private double sourceNewBalance;
    
    private double destinationNewBalance;
    
    private String userId;
    
    private String username;
    
    private String recipientUsername;
    
    public static AccountBalanceUpdatedEvent create(SagaId sagaId, String correlationId, String paymentId,
                                                    String sourceAccountNumber, String destinationAccountNumber,
                                                    double amount, double sourceNewBalance,
                                                    double destinationNewBalance) {
        return AccountBalanceUpdatedEvent.builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .sagaId(sagaId)
                .correlationId(correlationId)
                .timestamp(Instant.now())
                .paymentId(paymentId)
                .sourceAccountNumber(sourceAccountNumber)
                .destinationAccountNumber(destinationAccountNumber)
                .amount(amount)
                .sourceNewBalance(sourceNewBalance)
                .destinationNewBalance(destinationNewBalance)
                .build();
    }
}
