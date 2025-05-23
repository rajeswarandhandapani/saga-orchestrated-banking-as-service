package com.rajeswaran.common.events;

import java.time.Instant;

public record AccountOpeningFailedEvent(
    String userId,
    String accountId,
    String email,
    String fullName,
    Instant timestamp,
    String reason,
    String message
) {}

