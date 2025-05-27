package com.rajeswaran.common.events;

import com.rajeswaran.common.AppConstants.SagaEventType;
import com.rajeswaran.common.AppConstants.ServiceName;

import java.time.Instant;

public record SagaEvent(
    String userId,
    String accountId,
    Instant timestamp,
    String details,
    String correlationId,
    ServiceName serviceName,
    SagaEventType eventType
) {}
