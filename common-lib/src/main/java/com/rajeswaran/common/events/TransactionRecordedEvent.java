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
public class TransactionRecordedEvent extends SagaEvent {
    private String paymentId;
    private String sourceAccountNumber;
    private String destinationAccountNumber;
    private Double amount;
    private String debitTransactionId;
    private String creditTransactionId;
}
