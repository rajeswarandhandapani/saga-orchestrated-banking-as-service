package com.rajeswaran.common.util;

import com.rajeswaran.common.AppConstants;
import org.slf4j.MDC;

import java.time.Instant;

public class SagaEventBuilderUtil {
    private SagaEventBuilderUtil() {
    }

    public static String getCurrentCorrelationId() {
        return MDC.get(AppConstants.CORRELATION_ID_MDC_KEY);
    }

    public static Instant now() {
        return Instant.now();
    }
}

