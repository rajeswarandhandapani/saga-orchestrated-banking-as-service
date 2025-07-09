package com.rajeswaran.common;

/**
 * Application constants for the Banking as a Service platform.
 * Contains only essential shared values across microservices.
 */
public final class AppConstants {
    private AppConstants() {}

    // Header Constants for distributed tracing
    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    
    // MDC Keys for structured logging
    public static final String CORRELATION_ID_MDC_KEY = "correlationId";
    
    // Security Roles
    public static final String ROLE_BAAS_ADMIN = "ROLE_BAAS_ADMIN";
    public static final String ROLE_ACCOUNT_HOLDER = "ROLE_ACCOUNT_HOLDER";
}
