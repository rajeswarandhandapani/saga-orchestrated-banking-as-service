package com.rajeswaran.notification.listener;

import com.rajeswaran.common.events.SagaEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Slf4j
@Component
public class SagaEventListener {
    @Bean
    public Consumer<SagaEvent> notificationEvent() {
        return event -> {
            log.info("[NOTIFICATION] AccountOpenedEvent received: {}", event);
            // TODO: Send notification (email/SMS/etc.) if required
        };
    }


}

