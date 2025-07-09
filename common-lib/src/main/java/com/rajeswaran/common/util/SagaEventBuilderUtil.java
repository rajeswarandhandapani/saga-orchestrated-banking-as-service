package com.rajeswaran.common.util;

import java.time.LocalDateTime;

/**
 * Utility class for saga event building operations.
 */
public class SagaEventBuilderUtil {
    private SagaEventBuilderUtil() {
        // Utility class - prevent instantiation
    }

    /**
     * Gets the current timestamp for saga events.
     *
     * @return current LocalDateTime
     */
    public static LocalDateTime now() {
        return LocalDateTime.now();
    }
}

