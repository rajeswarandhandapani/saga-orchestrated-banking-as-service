package com.rajeswaran.common;

public final class AppConstants {
    private AppConstants() {}

    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    public static final String CORRELATION_ID_MDC_KEY = "correlationId";

    public enum SagaEventType {
        USER_REGISTERED,
        USER_REGISTRATION_FAILED,
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
}
