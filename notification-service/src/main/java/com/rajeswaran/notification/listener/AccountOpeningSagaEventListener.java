package com.rajeswaran.notification.listener;

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
    public Consumer<AccountOpenedEvent> notificationAccountOpenedEvent() {
        return event -> {
            log.info("[NOTIFICATION] AccountOpenedEvent received: {}", event);
            // TODO: Send notification (email/SMS/etc.) if required
        };
    }

    @Bean
    public Consumer<AccountOpeningFailedEvent> notificationAccountOpeningFailedEvent() {
        return event -> {
            log.info("[NOTIFICATION] AccountOpeningFailedEvent received: {}", event);
            // TODO: Send notification (email/SMS/etc.) if required
        };
    }
}

