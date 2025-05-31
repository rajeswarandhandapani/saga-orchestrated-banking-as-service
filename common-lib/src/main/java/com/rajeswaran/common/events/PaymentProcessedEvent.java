package com.rajeswaran.common.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class PaymentProcessedEvent extends SagaEvent {
    private String paymentId;
    private String sourceAccountNumber;
    private String destinationAccountNumber;
    private Double amount;
    private boolean processed;
    private String reason;
    private String recipientUsername; // Added field to track the recipient of the payment
}

