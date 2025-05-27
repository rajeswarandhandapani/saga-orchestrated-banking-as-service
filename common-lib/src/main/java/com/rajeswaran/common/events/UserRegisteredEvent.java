package com.rajeswaran.common.events;

import java.time.Instant;

public record UserRegisteredEvent(
    String userId,
    String username,
    String email,
    String fullName,
    Instant timestamp
) {}

