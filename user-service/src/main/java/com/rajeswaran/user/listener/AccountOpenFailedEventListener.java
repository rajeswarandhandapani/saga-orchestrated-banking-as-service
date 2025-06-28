package com.rajeswaran.user.listener;

import com.rajeswaran.common.events.AccountOpenFailedEvent;
import com.rajeswaran.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
public class AccountOpenFailedEventListener {
    private static final Logger log = LoggerFactory.getLogger(AccountOpenFailedEventListener.class);

    private final UserService userService;

    @Bean
    public Consumer<AccountOpenFailedEvent> accountOpenFailedEvent() {
        return event -> {
            log.info("Received AccountOpenFailedEvent for userId={}, username={}, reason={}", event.getUserId(), event.getUsername(), event.getDetails());
            try {
                userService.deleteUser(Long.valueOf(event.getUserId()));
                log.info("Compensation: Deleted user with userId={} due to account creation failure.", event.getUserId());
            } catch (Exception ex) {
                log.error("Compensation failed: Could not delete user with userId={}, reason={}", event.getUserId(), ex.getMessage());
            }
        };
    }
}

