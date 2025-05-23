package com.rajeswaran.common.events;

import java.time.Instant;

public record AccountOpenedEvent(
    String userId,
    String accountId,
    String email,
    String fullName,
    Instant timestamp,
    String message
) {}
