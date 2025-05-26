package com.rajeswaran.audit.listener;

import com.rajeswaran.audit.entity.AuditLog;
import com.rajeswaran.audit.service.AuditLogService;
import com.rajeswaran.common.events.AccountOpenedEvent;
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
    public Consumer<AccountOpenedEvent> auditEvent() {
        return event -> {
            logger.info("Processing AccountOpenedEvent: {}", event);

            AuditLog auditLog = AuditLog.builder()
                    .eventTimestamp(event.timestamp())
                    .eventType(event.getClass().getSimpleName())
                    .userId(event.userId())
                    .accountId(event.accountId())
                    .details(event.message())
                    .status("SUCCESS")
                    .serviceName("saga-orchestrator")
                    .build();
            auditLogService.createLog(auditLog);
        };
    }
}
