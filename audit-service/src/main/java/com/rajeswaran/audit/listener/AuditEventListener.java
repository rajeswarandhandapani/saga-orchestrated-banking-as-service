/* package com.rajeswaran.audit.listener;

import com.rajeswaran.common.entity.AuditLog;
import com.rajeswaran.audit.service.AuditLogService;
import com.rajeswaran.common.events.SagaEvent;
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
                    .username(event.getUsername())
                    .timestamp(event.getTimestamp())
                    .details(event.getDetails())
                    .correlationId(event.getCorrelationId())
                    .serviceName(event.getServiceName().name())
                    .eventType(event.getEventType().name());

            auditLogService.createLog(builder.build());
        };
    }
}
 */