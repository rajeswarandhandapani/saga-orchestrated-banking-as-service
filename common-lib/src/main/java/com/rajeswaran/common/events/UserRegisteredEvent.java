package com.rajeswaran.common.events;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public final class UserRegisteredEvent extends SagaEvent {
    private final String username;
    private final String email;
    private final String fullName;
}
