package com.rajeswaran.audit.listener;

import com.rajeswaran.audit.entity.AuditLog;
import com.rajeswaran.audit.service.AuditLogService;
import com.rajeswaran.common.events.AccountOpenedEvent;
import com.rajeswaran.common.events.AccountOpeningFailedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
public class AuditEventListener {

    private static final Logger logger = LoggerFactory.getLogger(AuditEventListener.class);
    private final AuditLogService auditLogService;

    public AuditEventListener(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @Bean
    public Consumer<Message<Object>> auditEvent() {
        return message -> {
            logger.info("Received message in auditEvent consumer: Headers: {}, Payload Type: {}", message.getHeaders(), message.getPayload().getClass().getName());
            Object eventPayload = message.getPayload();

            if (eventPayload instanceof AccountOpenedEvent event) {
                logger.info("Processing AccountOpenedEvent: {}", event);
                AuditLog auditLog = AuditLog.builder()
                        .eventTimestamp(event.timestamp()) // Changed from eventTimestamp()
                        .eventType(event.getClass().getSimpleName())
                        .userId(event.userId())
                        .accountId(event.accountId())
                        .details(event.message())
                        .status("SUCCESS")
                        .serviceName("saga-orchestrator")
                        .build();
                auditLogService.createLog(auditLog);
            } else if (eventPayload instanceof AccountOpeningFailedEvent event) {
                logger.info("Processing AccountOpeningFailedEvent: {}", event);
                AuditLog auditLog = AuditLog.builder()
                        .eventTimestamp(event.timestamp()) // Changed from eventTimestamp()
                        .eventType(event.getClass().getSimpleName())
                        .userId(event.userId())
                        .accountId(event.accountId())
                        .details("Reason: " + event.reason() + ". Message: " + event.message()) // Changed from failureReason()
                        .status("FAILED")
                        .serviceName("saga-orchestrator")
                        .build();
                auditLogService.createLog(auditLog);
            } else {
                logger.warn("Received unhandled event type: {}", eventPayload.getClass().getName());
            }
        };
    }
}
