package com.rajeswaran.common.events;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public final class AccountOpenedEvent extends SagaEvent {
    private final String accountType;
    private final Double balance;
    private final String status;
}

