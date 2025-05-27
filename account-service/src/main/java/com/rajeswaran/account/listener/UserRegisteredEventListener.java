package com.rajeswaran.account.listener;

import com.rajeswaran.account.entity.Account;
import com.rajeswaran.account.service.AccountService;
import com.rajeswaran.common.AppConstants;
import com.rajeswaran.common.events.SagaEvent;
import com.rajeswaran.common.events.UserRegisteredEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.function.Consumer;

@Component
public class UserRegisteredEventListener {
    @Autowired
    private AccountService accountService;

    @Autowired
    private StreamBridge streamBridge;

    private static final Logger log = LoggerFactory.getLogger(UserRegisteredEventListener.class);

    @Bean
    public Consumer<UserRegisteredEvent> userRegistered() {
        return event -> {
            log.info("Received UserRegisteredEvent for userId={}, username={}, email={}", event.userId(), event.username(), event.email());
            // Create a new account for the registered user
            Account account = new Account();
            account.setAccountNumber(generateAccountNumber());
            account.setAccountType("SAVINGS");
            account.setUserId(event.userId());
            account.setBalance(0.0);
            account.setStatus("ACTIVE");
            accountService.createAccount(account);
            log.info("Created new account for userId={}, accountNumber={}", event.userId(), account.getAccountNumber());

            SagaEvent auditEvent = new SagaEvent(
                event.userId(),
                account.getAccountNumber(),
                Instant.now(),
                "Account opened for user: " + event.username(),
                event.correlationId(),
                AppConstants.ServiceName.ACCOUNT_SERVICE,
                AppConstants.SagaEventType.ACCOUNT_OPENED
            );
            streamBridge.send("auditEvent-out-0", auditEvent);
        };
    }

    private String generateAccountNumber() {
        SecureRandom random = new SecureRandom();
        int number = 100000 + random.nextInt(900000); // 6 digit number
        return String.valueOf(number);
    }
}
