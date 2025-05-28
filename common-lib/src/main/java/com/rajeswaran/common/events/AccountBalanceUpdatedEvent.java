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
public class AccountBalanceUpdatedEvent extends SagaEvent {
    private String accountNumber;
    private double newBalance;
    private String paymentId;
}
