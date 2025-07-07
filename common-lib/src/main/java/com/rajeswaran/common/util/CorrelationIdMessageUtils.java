package com.rajeswaran.common.util;

import com.rajeswaran.common.AppConstants;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.StringUtils;

import java.util.UUID;

/**
 * Utility class for handling correlation ID in Spring Cloud Stream messages.
 * Provides transport-agnostic methods for correlation ID propagation.
 */
@Slf4j
public class CorrelationIdMessageUtils {

    private CorrelationIdMessageUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Adds correlation ID to message headers from current MDC context.
     * If no correlation ID exists in MDC, generates a new one.
     *
     * @param message the message to enhance with correlation ID
     * @return enhanced message with correlation ID header
     */
    public static <T> Message<T> addCorrelationIdToMessage(Message<T> message) {
        String correlationId = getCurrentCorrelationId();
        
        if (!StringUtils.hasText(correlationId)) {
            correlationId = generateCorrelationId();
            setCorrelationIdInMDC(correlationId);
            log.debug("Generated new correlation ID for outgoing message: {}", correlationId);
        }
        
        return MessageBuilder.fromMessage(message)
                .setHeader(AppConstants.CORRELATION_ID_MESSAGE_HEADER, correlationId)
                .build();
    }

    /**
     * Extracts correlation ID from message headers and sets it in MDC.
     * If no correlation ID exists in headers, generates a new one.
     *
     * @param message the incoming message to extract correlation ID from
     */
    public static void extractCorrelationIdFromMessage(Message<?> message) {
        MessageHeaders headers = message.getHeaders();
        String correlationId = (String) headers.get(AppConstants.CORRELATION_ID_MESSAGE_HEADER);
        
        if (StringUtils.hasText(correlationId)) {
            setCorrelationIdInMDC(correlationId);
            log.debug("Extracted correlation ID from incoming message: {}", correlationId);
        } else {
            // Generate new correlation ID if missing
            correlationId = generateCorrelationId();
            setCorrelationIdInMDC(correlationId);
            log.warn("No correlation ID found in message headers, generated new one: {}", correlationId);
        }
    }

    /**
     * Gets current correlation ID from MDC.
     *
     * @return correlation ID from MDC, or null if not set
     */
    public static String getCurrentCorrelationId() {
        return MDC.get(AppConstants.CORRELATION_ID_MDC_KEY);
    }

    /**
     * Sets correlation ID in MDC for structured logging.
     *
     * @param correlationId the correlation ID to set
     */
    public static void setCorrelationIdInMDC(String correlationId) {
        if (StringUtils.hasText(correlationId)) {
            MDC.put(AppConstants.CORRELATION_ID_MDC_KEY, correlationId);
        }
    }

    /**
     * Clears correlation ID from MDC.
     */
    public static void clearCorrelationIdFromMDC() {
        MDC.remove(AppConstants.CORRELATION_ID_MDC_KEY);
    }

    /**
     * Generates a new correlation ID using UUID.
     *
     * @return new correlation ID
     */
    public static String generateCorrelationId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Checks if correlation ID exists in message headers.
     *
     * @param message the message to check
     * @return true if correlation ID header exists, false otherwise
     */
    public static boolean hasCorrelationId(Message<?> message) {
        return message.getHeaders().containsKey(AppConstants.CORRELATION_ID_MESSAGE_HEADER);
    }

    /**
     * Gets correlation ID from message headers.
     *
     * @param message the message to extract correlation ID from
     * @return correlation ID from message headers, or null if not present
     */
    public static String getCorrelationIdFromMessage(Message<?> message) {
        return (String) message.getHeaders().get(AppConstants.CORRELATION_ID_MESSAGE_HEADER);
    }

    /**
     * Executes a block of code with correlation ID context.
     * Automatically sets up and cleans up MDC context.
     *
     * @param correlationId the correlation ID to use
     * @param runnable the code to execute
     */
    public static void withCorrelationId(String correlationId, Runnable runnable) {
        String originalCorrelationId = getCurrentCorrelationId();
        try {
            setCorrelationIdInMDC(correlationId);
            runnable.run();
        } finally {
            if (originalCorrelationId != null) {
                setCorrelationIdInMDC(originalCorrelationId);
            } else {
                clearCorrelationIdFromMDC();
            }
        }
    }
}
