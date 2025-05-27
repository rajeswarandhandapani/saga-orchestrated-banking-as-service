package com.rajeswaran.common.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public final class AccountOpenedEvent extends SagaEvent {
    private String accountType;
    private String accountNumber;
    private Double balance;
    private String status;
}

