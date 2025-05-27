package com.rajeswaran.common.events;

import com.rajeswaran.common.AppConstants;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Getter
@AllArgsConstructor
@SuperBuilder
public abstract class SagaEvent {
    private final String userId;
    private final String accountId;
    private final Instant timestamp;
    private final String details;
    private final String correlationId;
    private final AppConstants.ServiceName serviceName;
    private final AppConstants.SagaEventType eventType;
}
