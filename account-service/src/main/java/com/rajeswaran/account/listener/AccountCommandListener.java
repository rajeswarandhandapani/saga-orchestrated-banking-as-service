package com.rajeswaran.account.listener;

import com.rajeswaran.account.service.AccountService;
import com.rajeswaran.common.entity.Account;
import com.rajeswaran.common.useronboarding.commands.OpenAccountCommand;
import com.rajeswaran.common.useronboarding.events.AccountOpenedEvent;
import com.rajeswaran.common.useronboarding.events.AccountOpenFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountCommandListener {

    private final AccountService accountService;
    private final StreamBridge streamBridge;
    private static final AtomicInteger accountNumberSequence = new AtomicInteger(10001);

    @Bean
    public Consumer<Message<OpenAccountCommand>> openAccountCommand() {
        return message -> {
            OpenAccountCommand command = message.getPayload();
            log.info("Received OpenAccountCommand for saga {} and userId: {}", 
                    command.getSagaId().value(), command.getUserId());

            try {
                // Create account
                Account account = new Account();
                account.setAccountNumber(generateAccountNumber());
                account.setAccountType(command.getAccountType());
                account.setUserId(command.getUserId());
                account.setUserName(command.getUserDto().getUsername());
                account.setBalance(500.0); // Initial balance
                account.setStatus("ACTIVE");
                account.setCreatedTimestamp(LocalDateTime.now());
                
                Account savedAccount = accountService.createAccount(account);
                
                log.info("Account created successfully for saga {} - accountId: {}, accountNumber: {}", 
                        command.getSagaId().value(), savedAccount.getId(), savedAccount.getAccountNumber());
                
                // Publish success event
                AccountOpenedEvent event = AccountOpenedEvent.create(
                    command.getSagaId(),
                    command.getCorrelationId(),
                    savedAccount.getId().toString(),
                    command.getUserId(),
                    command.getAccountType(),
                    savedAccount.getAccountNumber()
                );
                
                streamBridge.send("accountOpenedEvent-out-0", event);
                log.info("Published AccountOpenedEvent for saga {}", command.getSagaId().value());
                
            } catch (Exception e) {
                log.error("Failed to create account for saga {}, userId: {}", 
                         command.getSagaId().value(), command.getUserId(), e);
                
                // Publish failure event
                AccountOpenFailedEvent event = AccountOpenFailedEvent.create(
                    command.getSagaId(),
                    command.getCorrelationId(),
                    command.getUserId(),
                    command.getUserDto().getUsername(),
                    "Failed to create account: " + e.getMessage()
                );
                
                streamBridge.send("accountOpenFailedEvent-out-0", event);
                log.info("Published AccountOpenFailedEvent for saga {}", command.getSagaId().value());
            }
        };
    }

    private String generateAccountNumber() {
        return String.valueOf(accountNumberSequence.getAndIncrement());
    }
}
