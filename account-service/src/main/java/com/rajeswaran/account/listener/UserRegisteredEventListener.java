package com.rajeswaran.account.listener;

import com.rajeswaran.account.entity.Account;
import com.rajeswaran.account.service.AccountService;
import com.rajeswaran.common.AppConstants;
import com.rajeswaran.common.events.AccountOpenFailedEvent;
import com.rajeswaran.common.events.AccountOpenedEvent;
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
    public Consumer<UserRegisteredEvent> userRegisteredEvent() {
        return event -> {
            log.info("Received UserRegisteredEvent for userId={}, username={}, email={}", event.getUserId(), event.getUsername(), event.getEmail());
            try {
                // Create a new account for the registered user
                Account account = new Account();
                account.setAccountNumber(generateAccountNumber());
                account.setAccountType("SAVINGS");
                account.setUserName(event.getUsername());
                account.setUserId(event.getUserId());
                account.setBalance(500.0);
                account.setStatus("ACTIVE");
                accountService.createAccount(account);
                log.info("Created new account for userId={}, accountNumber={}", event.getUserId(), account.getAccountNumber());

                AccountOpenedEvent accountOpenedEvent = AccountOpenedEvent.builder()
                        .userId(event.getUserId())
                        .username(event.getUsername())
                        .email(event.getEmail())
                        .fullName(event.getFullName())
                        .timestamp(Instant.now())
                        .details("Account opened for user: " + event.getUsername())
                        .correlationId(event.getCorrelationId())
                        .serviceName(AppConstants.ServiceName.ACCOUNT_SERVICE)
                        .eventType(AppConstants.SagaEventType.ACCOUNT_OPENED)
                        .accountType(account.getAccountType())
                        .accountNumber(account.getAccountNumber())
                        .balance(account.getBalance())
                        .status(account.getStatus())
                        .build();

                streamBridge.send("auditEvent-out-0", accountOpenedEvent);
                streamBridge.send("notificationEvent-out-0", accountOpenedEvent);
            } catch (Exception ex) {
                log.error("Account creation failed for userId={}, username={}, reason={}", event.getUserId(), event.getUsername(), ex.getMessage());
                AccountOpenFailedEvent failedEvent = AccountOpenFailedEvent.builder()
                        .userId(event.getUserId())
                        .username(event.getUsername())
                        .email(event.getEmail())
                        .fullName(event.getFullName())
                        .details(ex.getMessage())
                        .timestamp(Instant.now())
                        .correlationId(event.getCorrelationId())
                        .serviceName(AppConstants.ServiceName.ACCOUNT_SERVICE)
                        .eventType(AppConstants.SagaEventType.ACCOUNT_OPEN_FAILED)
                        .build();
                streamBridge.send("accountOpenFailedEvent-out-0", failedEvent);
                streamBridge.send("auditEvent-out-0", failedEvent);
            }
        };
    }

    private String generateAccountNumber() {
        SecureRandom random = new SecureRandom();
        int number = 100000 + random.nextInt(900000); // 6 digit number
        return String.valueOf(number);
    }
}
