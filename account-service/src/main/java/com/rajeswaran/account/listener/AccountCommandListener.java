package com.rajeswaran.account.listener;

import com.rajeswaran.account.entity.Account;
import com.rajeswaran.account.service.AccountService;
import com.rajeswaran.common.AppConstants;
import com.rajeswaran.common.model.command.CloseAccountCommand;
import com.rajeswaran.common.model.command.OpenAccountCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountCommandListener {

    private final AccountService accountService;
    private static final AtomicInteger accountNumberSequence = new AtomicInteger(10001);

    @Bean
    public Function<Message<OpenAccountCommand>, Message<String>> accountOpenCommand() {
        return message -> {
            OpenAccountCommand command = message.getPayload();
            log.info("Received openAccountCommand for user: {}", command.getUsername());

            if (command.getRoles() != null && command.getRoles().contains(AppConstants.ROLE_BAAS_ADMIN)) {
                log.info("Skipping account creation for admin user: {}", command.getUsername());
                // For admin users, we can consider the step as successful without creating an account.
                return MessageBuilder.withPayload("SUCCESS").build();
            }

            try {
                Account account = new Account();
                account.setAccountNumber(generateAccountNumber());
                account.setAccountType("SAVINGS");
                account.setUserName(command.getUsername());
                account.setUserId(command.getUserId());
                account.setBalance(500.0);
                account.setStatus("ACTIVE");
                account.setCreatedTimestamp(LocalDateTime.now());
                accountService.createAccount(account);
                log.info("Created new account for userId={}, accountNumber={}", command.getUserId(), account.getAccountNumber());
                return MessageBuilder.withPayload("SUCCESS").build();
            } catch (Exception e) {
                log.error("Failed to create account for user: {}", command.getUsername(), e);
                return MessageBuilder.withPayload("FAILURE").build();
            }
        };
    }

    @Bean
    public Function<Message<CloseAccountCommand>, Message<String>> closeAccountCommand() {
        return message -> {
            CloseAccountCommand command = message.getPayload();
            log.info("Received closeAccountCommand for user: {}", command.getUserId());
            try {
                accountService.closeAccountByUserId(command.getUserId());
                log.info("Account closed successfully for compensation for user: {}", command.getUserId());
                return MessageBuilder.withPayload("SUCCESS").build();
            } catch (Exception e) {
                log.error("Failed to close account for compensation for user: {}", command.getUserId(), e);
                return MessageBuilder.withPayload("SUCCESS").build(); // Do not block saga
            }
        };
    }

    private String generateAccountNumber() {
        return String.valueOf(accountNumberSequence.getAndIncrement());
    }
}
