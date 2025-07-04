package com.rajeswaran.payment.listener;

import com.rajeswaran.common.saga.payment.commands.ProcessPaymentCommand;
import com.rajeswaran.common.saga.payment.commands.ValidatePaymentCommand;
import com.rajeswaran.common.saga.payment.events.PaymentValidatedEvent;
import com.rajeswaran.common.saga.payment.events.PaymentValidationFailedEvent;
import com.rajeswaran.common.saga.payment.events.PaymentProcessedEvent;
import com.rajeswaran.common.saga.payment.events.PaymentFailedEvent;
import com.rajeswaran.payment.client.AccountServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;

/**
 * Handles incoming ValidatePaymentCommand messages for payment validation.
 *
 * This is the first step in the payment processing saga.
 */
@Component
@Slf4j
public class PaymentCommandListener {

    @Autowired
    private StreamBridge streamBridge;

    @Autowired
    private AccountServiceClient accountServiceClient;

    /**
     * Consumes ValidatePaymentCommand, performs basic validation, and publishes result event.
     */
    @Bean
    public Consumer<Message<ValidatePaymentCommand>> validatePaymentCommand() {
        return message -> {
            ValidatePaymentCommand command = message.getPayload();
            log.info("Received ValidatePaymentCommand for saga {} and payment: {}", command.getSagaId(), command.getPaymentId());

            // Basic validation placeholder (always succeeds for now)
            boolean valid = command.getAmount() > 0 && command.getSourceAccountNumber() != null && command.getDestinationAccountNumber() != null;

            if (valid) {
                PaymentValidatedEvent event = PaymentValidatedEvent.create(
                    command.getSagaId(),
                    command.getCorrelationId(),
                    command.getPaymentId(),
                    command.getSourceAccountNumber(),
                    command.getDestinationAccountNumber(),
                    command.getAmount(),
                    command.getDescription(),
                    command.getUsername()
                );
                streamBridge.send("paymentValidatedEvent-out-0", event);
                log.info("Published PaymentValidatedEvent for saga {} and payment: {}", command.getSagaId(), command.getPaymentId());
            } else {
                PaymentValidationFailedEvent event = PaymentValidationFailedEvent.create(
                    command.getSagaId(),
                    command.getCorrelationId(),
                    command.getPaymentId(),
                    command.getSourceAccountNumber(),
                    command.getDestinationAccountNumber(),
                    command.getAmount(),
                    "Validation failed: Invalid amount or account information",
                    command.getUsername()
                );
                streamBridge.send("paymentValidationFailedEvent-out-0", event);
                log.warn("Published PaymentValidationFailedEvent for saga {} and payment: {}", command.getSagaId(), command.getPaymentId());
            }
        };
    }

    /**
     * Consumes ProcessPaymentCommand, fetches account, and logs result (incremental step).
     */
    @Bean
    public Consumer<Message<ProcessPaymentCommand>> processPaymentCommand() {
        return message -> {
            ProcessPaymentCommand command = message.getPayload();
            log.info("Received ProcessPaymentCommand for saga {} and payment: {}", command.getSagaId(), command.getPaymentId());

            Mono<Account> accountMono = accountServiceClient.getAccountByNumber(command.getSourceAccountNumber());
            Account account = accountMono.block();

            if (account == null) {
                log.warn("Account not found for number {}", command.getSourceAccountNumber());
                PaymentFailedEvent event = PaymentFailedEvent.create(
                    command.getSagaId(),
                    command.getCorrelationId(),
                    command.getPaymentId(),
                    command.getSourceAccountNumber(),
                    command.getDestinationAccountNumber(),
                    command.getAmount(),
                    "Source account not found",
                    command.getUsername()
                );
                streamBridge.send("paymentFailedEvent-out-0", event);
                return;
            }
            if (!"ACTIVE".equalsIgnoreCase(account.getStatus())) {
                log.warn("Account {} is not active", account.getAccountNumber());
                PaymentFailedEvent event = PaymentFailedEvent.create(
                    command.getSagaId(),
                    command.getCorrelationId(),
                    command.getPaymentId(),
                    command.getSourceAccountNumber(),
                    command.getDestinationAccountNumber(),
                    command.getAmount(),
                    "Source account is not active",
                    command.getUsername()
                );
                streamBridge.send("paymentFailedEvent-out-0", event);
                return;
            }
            if (account.getBalance() < command.getAmount()) {
                log.warn("Insufficient balance in account {}", account.getAccountNumber());
                PaymentFailedEvent event = PaymentFailedEvent.create(
                    command.getSagaId(),
                    command.getCorrelationId(),
                    command.getPaymentId(),
                    command.getSourceAccountNumber(),
                    command.getDestinationAccountNumber(),
                    command.getAmount(),
                    "Insufficient balance",
                    command.getUsername()
                );
                streamBridge.send("paymentFailedEvent-out-0", event);
                return;
            }
            // All checks passed, publish PaymentProcessedEvent
            PaymentProcessedEvent event = PaymentProcessedEvent.create(
                command.getSagaId(),
                command.getCorrelationId(),
                command.getPaymentId(),
                command.getSourceAccountNumber(),
                command.getDestinationAccountNumber(),
                command.getAmount(),
                command.getDescription(),
                command.getUsername()
            );
            streamBridge.send("paymentProcessedEvent-out-0", event);
            log.info("Published PaymentProcessedEvent for saga {} and payment: {}", command.getSagaId(), command.getPaymentId());
        };
    }
}
