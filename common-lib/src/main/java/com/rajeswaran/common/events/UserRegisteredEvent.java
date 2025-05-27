package com.rajeswaran.common.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public final class UserRegisteredEvent extends SagaEvent {
    private String username;
    private String email;
    private String fullName;
}
