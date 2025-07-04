package com.rajeswaran.account.listener;

import com.rajeswaran.account.service.AccountService;
import com.rajeswaran.common.entity.Account;
import com.rajeswaran.common.entity.Payment;
import com.rajeswaran.common.entity.User;
import com.rajeswaran.common.saga.payment.commands.ProcessPaymentCommand;
import com.rajeswaran.common.saga.payment.events.PaymentFailedEvent;
import com.rajeswaran.common.saga.payment.events.PaymentProcessedEvent;
import com.rajeswaran.common.saga.useronboarding.commands.OpenAccountCommand;
import com.rajeswaran.common.saga.useronboarding.events.AccountOpenFailedEvent;
import com.rajeswaran.common.saga.useronboarding.events.AccountOpenedEvent;
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
    public Consumer<Message<OpenAccountCommand>> accountOpenCommand() {
        return message -> {
            OpenAccountCommand command = message.getPayload();
            User user = command.getUser();
            log.info("Received OpenAccountCommand for saga {} and userId: {}",
                    command.getSagaId(), user.getUserId());

            try {
                // Create account
                Account account = new Account();
                account.setAccountNumber(generateAccountNumber());
                account.setAccountType(command.getAccountType());
                account.setUserId(String.valueOf(user.getUserId()));
                account.setUserName(user.getUsername());
                account.setBalance(500.0); // Initial balance
                account.setStatus("ACTIVE");
                account.setCreatedTimestamp(LocalDateTime.now());
                
                Account savedAccount = accountService.createAccount(account);
                
                log.info("Account created successfully for saga {} - accountId: {}, accountNumber: {}", 
                        command.getSagaId(), savedAccount.getId(), savedAccount.getAccountNumber());
                
                // Publish success event
                AccountOpenedEvent event = AccountOpenedEvent.create(
                    command.getSagaId(),
                    command.getCorrelationId(),
                    savedAccount,
                    user
                );
                
                streamBridge.send("accountOpenedEvent-out-0", event);
                log.info("Published AccountOpenedEvent for saga {}", command.getSagaId());
                
            } catch (Exception e) {
                log.error("Failed to create account for saga {}, userId: {}", 
                         command.getSagaId(), user.getUserId(), e);
                
                // Publish failure event
                AccountOpenFailedEvent event = AccountOpenFailedEvent.create(
                    command.getSagaId(),
                    command.getCorrelationId(),
                    String.valueOf(user.getUserId()),
                    user.getUsername(),
                    "Failed to create account: " + e.getMessage()
                );
                
                streamBridge.send("accountOpenFailedEvent-out-0", event);
                log.info("Published AccountOpenFailedEvent for saga {}", command.getSagaId());
            }
        };
    }

    /**
     * Consumes ProcessPaymentCommand, fetches account, and logs result (incremental step).
     */
    @Bean
    public Consumer<Message<ProcessPaymentCommand>> processPaymentCommand() {
        return message -> {
            ProcessPaymentCommand cmd = message.getPayload();
            Payment payment = cmd.getPayment();
            log.info("[Account] Received ProcessPaymentCommand for saga {} and payment: {}", cmd.getSagaId(), payment);

            var srcOpt = accountService.getAccountByAccountNumber(payment.getSourceAccountNumber());
            if (srcOpt.isEmpty()) {
                publishFailed(cmd, "Source account not found", payment);
                return;
            }
            var src = srcOpt.get();
            if (!"ACTIVE".equalsIgnoreCase(src.getStatus())) {
                publishFailed(cmd, "Source account is not active", payment);
                return;
            }
            if (src.getBalance() < payment.getAmount()) {
                publishFailed(cmd, "Insufficient balance", payment);
                return;
            }
            var destOpt = accountService.getAccountByAccountNumber(payment.getDestinationAccountNumber());
            if (destOpt.isEmpty()) {
                publishFailed(cmd, "Destination account not found", payment);
                return;
            }
            var dest = destOpt.get();
            if (!"ACTIVE".equalsIgnoreCase(dest.getStatus())) {
                publishFailed(cmd, "Destination account is not active", payment);
                return;
            }
            // All checks passed: update balances
            boolean debited = accountService.deductFromAccount(payment.getSourceAccountNumber(), payment.getAmount());
            boolean credited = accountService.addToAccount(payment.getDestinationAccountNumber(), payment.getAmount());
            if (!debited || !credited) {
                publishFailed(cmd, "Failed to update account balances", payment);
                return;
            }
            log.info("[Account] Debited {} from {} and credited to {}", payment.getAmount(), payment.getSourceAccountNumber(), payment.getDestinationAccountNumber());
            streamBridge.send("paymentProcessedEvent-out-0", PaymentProcessedEvent.create(
                cmd.getSagaId(), cmd.getCorrelationId(), payment
            ));
            log.info("[Account] Published PaymentProcessedEvent for saga {} and payment: {}", cmd.getSagaId(), payment.getId());
        };
    }

    private void publishFailed(ProcessPaymentCommand cmd, String reason, Payment payment) {
        streamBridge.send("paymentFailedEvent-out-0", PaymentFailedEvent.create(
            cmd.getSagaId(), cmd.getCorrelationId(), payment, reason
        ));
        log.warn("[Account] Payment failed for saga {} and payment {}: {}", cmd.getSagaId(), payment.getId(), reason);
    }

    private String generateAccountNumber() {
        return String.valueOf(accountNumberSequence.getAndIncrement());
    }
}
