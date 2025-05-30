package com.rajeswaran.common;

public final class AppConstants {
    private AppConstants() {}

    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    public static final String CORRELATION_ID_MDC_KEY = "correlationId";
    public static final String ROLE_BAAS_ADMIN = "ROLE_BAAS_ADMIN";
    public static final String ROLE_ACCOUNT_HOLDER = "ROLE_ACCOUNT_HOLDER";

    public enum SagaEventType {
        USER_REGISTERED,
        USER_REGISTRATION_FAILED,
        ACCOUNT_OPENED,
        ACCOUNT_OPEN_FAILED,
        PAYMENT_INITIATED,
        PAYMENT_VALIDATED,
        PAYMENT_FAILED,
        PAYMENT_PROCESSED,
        ACCOUNT_BALANCE_UPDATED,
        TRANSACTION_RECORDED
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
