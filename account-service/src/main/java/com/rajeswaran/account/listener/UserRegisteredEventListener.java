package com.rajeswaran.account.listener;

import com.rajeswaran.account.entity.Account;
import com.rajeswaran.account.service.AccountService;
import com.rajeswaran.common.events.UserRegisteredEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.util.function.Consumer;

@Component
public class UserRegisteredEventListener {
    @Autowired
    private AccountService accountService;

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
        };
    }

    private String generateAccountNumber() {
        SecureRandom random = new SecureRandom();
        int number = 100000 + random.nextInt(900000); // 6 digit number
        return String.valueOf(number);
    }
}
