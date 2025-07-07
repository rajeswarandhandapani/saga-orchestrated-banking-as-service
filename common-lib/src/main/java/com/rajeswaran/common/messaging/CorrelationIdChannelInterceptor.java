package com.rajeswaran.common.messaging;

import com.rajeswaran.common.util.CorrelationIdMessageUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

/**
 * Channel interceptor that automatically adds correlation ID to outgoing messages.
 * Works with any Spring Cloud Stream binder (Kafka, RabbitMQ, etc.).
 */
@Slf4j
@Component
public class CorrelationIdChannelInterceptor implements ChannelInterceptor {

    /**
     * Intercepts outgoing messages and adds correlation ID header.
     * The correlation ID is taken from the current MDC context.
     * If no correlation ID exists in MDC, a new one is generated.
     *
     * @param message the message being sent
     * @param channel the channel to which the message is being sent
     * @return the message with correlation ID header added
     */
    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        try {
            // Check if correlation ID already exists in message headers
            if (CorrelationIdMessageUtils.hasCorrelationId(message)) {
                log.debug("Correlation ID already exists in message headers, skipping addition");
                return message;
            }

            // Add correlation ID to message headers
            Message<?> enhancedMessage = CorrelationIdMessageUtils.addCorrelationIdToMessage(message);
            
            log.debug("Added correlation ID to outgoing message: {}", 
                    CorrelationIdMessageUtils.getCorrelationIdFromMessage(enhancedMessage));
            
            return enhancedMessage;
            
        } catch (Exception e) {
            log.error("Failed to add correlation ID to outgoing message", e);
            // Return original message if enhancement fails
            return message;
        }
    }

    /**
     * Called after message is sent successfully.
     * Can be used for cleanup or logging.
     *
     * @param message the message that was sent
     * @param channel the channel to which the message was sent
     * @param sent whether the message was sent successfully
     */
    @Override
    public void postSend(@NonNull Message<?> message, @NonNull MessageChannel channel, boolean sent) {
        if (sent) {
            log.debug("Message sent successfully with correlation ID: {}", 
                    CorrelationIdMessageUtils.getCorrelationIdFromMessage(message));
        } else {
            log.warn("Failed to send message with correlation ID: {}", 
                    CorrelationIdMessageUtils.getCorrelationIdFromMessage(message));
        }
    }

    /**
     * Called before message is received.
     * Extracts correlation ID from incoming message headers and sets it in MDC.
     *
     * @param channel the channel from which the message is being received
     * @return true to continue processing, false to stop
     */
    @Override
    public boolean preReceive(@NonNull MessageChannel channel) {
        log.debug("Preparing to receive message on channel: {}", channel);
        return true;
    }

    /**
     * Called after message is received.
     * Extracts correlation ID from message headers and sets it in MDC context.
     *
     * @param message the received message
     * @param channel the channel from which the message was received
     * @return the message (can be modified if needed)
     */
    @Override
    public Message<?> postReceive(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        if (message != null) {
            try {
                // Extract correlation ID from incoming message and set in MDC
                CorrelationIdMessageUtils.extractCorrelationIdFromMessage(message);
                
                log.debug("Extracted correlation ID from incoming message: {}", 
                        CorrelationIdMessageUtils.getCorrelationIdFromMessage(message));
                
            } catch (Exception e) {
                log.error("Failed to extract correlation ID from incoming message", e);
                // Continue processing even if correlation ID extraction fails
            }
        }
        
        return message;
    }

    /**
     * Called after message processing is complete.
     * Cleans up MDC context to prevent memory leaks.
     *
     * @param message the message that was processed
     * @param channel the channel from which the message was received
     * @param ex any exception that occurred during processing
     */
    @Override
    public void afterReceiveCompletion(@Nullable Message<?> message, @NonNull MessageChannel channel, @Nullable Exception ex) {
        try {
            if (ex != null) {
                String correlationId = message != null ? 
                        CorrelationIdMessageUtils.getCorrelationIdFromMessage(message) : "unknown";
                log.warn("Message processing completed with exception for correlation ID: {}", correlationId, ex);
            } else {
                String correlationId = message != null ? 
                        CorrelationIdMessageUtils.getCorrelationIdFromMessage(message) : "unknown";
                log.debug("Message processing completed successfully for correlation ID: {}", correlationId);
            }
        } finally {
            // Always clean up MDC to prevent memory leaks
            CorrelationIdMessageUtils.clearCorrelationIdFromMDC();
            log.debug("Cleared correlation ID from MDC after message processing");
        }
    }
}
