package com.rajeswaran.common.events;

import java.time.Instant;

public record SagaEvent(
    String userId,
    String accountId,
    Instant timestamp,
    String details,
    CorrelationId correlationId,
    ServiceName serviceName,
    SagaEventType eventType
) {
    public enum SagaEventType {
        ACCOUNT_OPENED,
        ACCOUNT_OPEN_FAILED
    }
    public enum ServiceName {
        SAGA_ORCHESTRATOR,
        USER_SERVICE,
        ACCOUNT_SERVICE,
        PAYMENT_SERVICE,
        NOTIFICATION_SERVICE,
        AUDIT_SERVICE
    }
    public record CorrelationId(String value) {}
}
