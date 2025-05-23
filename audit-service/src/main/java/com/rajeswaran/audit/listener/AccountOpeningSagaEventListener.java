package com.rajeswaran.audit.listener;

import com.rajeswaran.common.events.AccountOpenedEvent;
import com.rajeswaran.common.events.AccountOpeningFailedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Slf4j
@Component
public class AccountOpeningSagaEventListener {

    @Bean
    public Consumer<AccountOpenedEvent> auditEvent() {
        return event -> {
            log.info("[AUDIT] AccountOpenedEvent received: {}", event);
            // TODO: Persist audit event if required
        };
    }

    @Bean
    public Consumer<AccountOpeningFailedEvent> auditAccountOpeningFailedEvent() {
        return event -> {
            log.info("[AUDIT] AccountOpeningFailedEvent received: {}", event);
            // TODO: Persist audit event if required
        };
    }
}

