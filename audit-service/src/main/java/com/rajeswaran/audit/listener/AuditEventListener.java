package com.rajeswaran.audit.listener;

import com.rajeswaran.audit.entity.AuditLog;
import com.rajeswaran.audit.service.AuditLogService;
import com.rajeswaran.common.events.AccountOpenedEvent;
import com.rajeswaran.common.events.SagaEvent;
import com.rajeswaran.common.events.UserRegisteredEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
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
    public Consumer<SagaEvent> auditEvent() {
        return event -> {
            logger.info("Processing SagaEvent: {}", event);

            AuditLog.AuditLogBuilder builder = AuditLog.builder()
                    .eventTimestamp(event.getTimestamp())
                    .eventType(event.getEventType().name())
                    .userId(event.getUserId())
                    .accountId(event.getAccountId())
                    .details(event.getDetails())
                    .serviceName(event.getServiceName().name())
                    .correlationId(event.getCorrelationId());

            // Optionally handle event-specific fields
            if (event instanceof UserRegisteredEvent ure) {
                // ure.getUsername(), ure.getEmail(), ure.getFullName() can be used if needed
            } else if (event instanceof AccountOpenedEvent aoe) {
                // aoe.getAccountType(), aoe.getBalance(), aoe.getStatus() can be used if needed
            }

            auditLogService.createLog(builder.build());
        };
    }
}
