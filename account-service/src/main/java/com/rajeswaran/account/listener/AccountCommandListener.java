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
     * Consumes ProcessPaymentCommand, validates accounts, and processes payment atomically.
     * Uses transactional money transfer to ensure data consistency.
     */
    @Bean
    public Consumer<Message<ProcessPaymentCommand>> processPaymentCommand() {
        return message -> {
            ProcessPaymentCommand cmd = message.getPayload();
            Payment payment = cmd.getPayment();
            log.info("[Account] Received ProcessPaymentCommand for saga {} and payment: {}", cmd.getSagaId(), payment);

            try {
                // Perform atomic money transfer
                AccountService.TransferResult result = accountService.transferMoney(
                    payment.getSourceAccountNumber(),
                    payment.getDestinationAccountNumber(), 
                    payment.getAmount()
                );
                
                // Get destination account user name for the payment
                var destOpt = accountService.getAccountByAccountNumber(payment.getDestinationAccountNumber());
                if (destOpt.isPresent()) {
                    payment.setDestinationAccountUserName(destOpt.get().getUserName());
                }
                
                // Set the updated balances on the payment object
                payment.setSourceAccountBalance(result.getSourceBalance());
                payment.setDestinationAccountBalance(result.getDestinationBalance());

                log.info("[Account] Successfully transferred {} from {} to {}", 
                    payment.getAmount(), payment.getSourceAccountNumber(), payment.getDestinationAccountNumber());
                
                // Publish success event
                streamBridge.send("paymentProcessedEvent-out-0", PaymentProcessedEvent.create(
                    cmd.getSagaId(), payment
                ));
                log.info("[Account] Published PaymentProcessedEvent for saga {} and payment: {}", cmd.getSagaId(), payment.getId());
                
            } catch (IllegalArgumentException | IllegalStateException e) {
                // Business logic errors (account not found, insufficient balance, etc.)
                log.warn("[Account] Payment validation failed for saga {} and payment {}: {}", 
                    cmd.getSagaId(), payment.getId(), e.getMessage());
                publishFailed(cmd, e.getMessage(), payment);
                
            } catch (Exception e) {
                // Unexpected errors (database failures, etc.)
                log.error("[Account] Unexpected error processing payment for saga {} and payment {}: {}", 
                    cmd.getSagaId(), payment.getId(), e.getMessage(), e);
                publishFailed(cmd, "Failed to process payment: " + e.getMessage(), payment);
            }
        };
    }

    private void publishFailed(ProcessPaymentCommand cmd, String reason, Payment payment) {
        streamBridge.send("paymentFailedEvent-out-0", PaymentFailedEvent.create(
            cmd.getSagaId(), payment, reason
        ));
        log.warn("[Account] Payment failed for saga {} and payment {}: {}", cmd.getSagaId(), payment.getId(), reason);
    }

    private String generateAccountNumber() {
        return String.valueOf(accountNumberSequence.getAndIncrement());
    }
}
