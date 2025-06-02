package com.rajeswaran.account.listener;

import com.rajeswaran.account.entity.Account;
import com.rajeswaran.account.service.AccountService;
import com.rajeswaran.common.events.AccountBalanceUpdatedEvent;
import com.rajeswaran.common.events.PaymentFailedEvent;
import com.rajeswaran.common.events.PaymentInitiatedEvent;
import com.rajeswaran.common.events.PaymentValidatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.function.Consumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentInitiatedEventListener {
    private final AccountService accountService;
    private final StreamBridge streamBridge;

    @Bean
    public Consumer<PaymentInitiatedEvent> paymentInitiatedEvent() {
        return event -> {
            log.info("Received PaymentInitiatedEvent for paymentId={}, sourceAccountNumber={}, amount={}",
                    event.getPaymentId(), event.getSourceAccountNumber(), event.getAmount());

            // First validate that the source account belongs to the logged-in user
            boolean isAccountOwner = accountService.validateAccountOwnership(
                    event.getSourceAccountNumber(), event.getUsername());

            if (!isAccountOwner) {
                log.error("Payment validation failed: Source account {} does not belong to user {}",
                        event.getSourceAccountNumber(), event.getUsername());

                String details = "Payment validation failed: Unauthorized access to account " + event.getSourceAccountNumber();
                publishPaymentFailedEvent(event, details);
                log.info("Published PaymentFailedEvent for paymentId={} due to unauthorized account access", event.getPaymentId());
                return;
            }

            // Then validate if the account has sufficient balance
            boolean hasSufficientBalance = accountService.validateSourceAccount(event.getSourceAccountNumber(), event.getAmount());
            if (hasSufficientBalance) {
                PaymentValidatedEvent validatedEvent = PaymentValidatedEvent.builder()
                        .paymentId(event.getPaymentId())
                        .valid(true)
                        .reason(null)
                        .userId(event.getUserId())
                        .username(event.getUsername())
                        .timestamp(java.time.Instant.now())
                        .details("Payment validated for paymentId: " + event.getPaymentId())
                        .correlationId(event.getCorrelationId())
                        .serviceName(com.rajeswaran.common.AppConstants.ServiceName.ACCOUNT_SERVICE)
                        .eventType(com.rajeswaran.common.AppConstants.SagaEventType.PAYMENT_VALIDATED)
                        .build();
                streamBridge.send("auditEvent-out-0", validatedEvent);
                log.info("Published PaymentValidatedEvent for paymentId={}", event.getPaymentId());
                // Deduct amount from source account
                boolean deducted = accountService.deductFromAccount(event.getSourceAccountNumber(), event.getAmount());
                // Add amount to destination account
                boolean added = accountService.addToAccount(event.getDestinationAccountNumber(), event.getAmount());
                if (deducted && added) {
                    // Look up the recipient's username from the destination account
                    String recipientUsername = null;
                    Optional<Account> destinationAccount = accountService.getAccountByAccountNumber(event.getDestinationAccountNumber());
                    if (destinationAccount.isPresent()) {
                        recipientUsername = destinationAccount.get().getUserName();
                    }

                    // Publish AccountBalanceUpdatedEvent
                    AccountBalanceUpdatedEvent balanceUpdatedEvent = AccountBalanceUpdatedEvent.builder()
                            .paymentId(event.getPaymentId())
                            .sourceAccountNumber(event.getSourceAccountNumber())
                            .destinationAccountNumber(event.getDestinationAccountNumber())
                            .amount(event.getAmount())
                            .userId(event.getUserId())
                            .username(event.getUsername())
                            .recipientUsername(recipientUsername) // Add recipient username
                            .timestamp(Instant.now())
                            .details("Source and destination account balances updated for paymentId: " + event.getPaymentId())
                            .correlationId(event.getCorrelationId())
                            .serviceName(com.rajeswaran.common.AppConstants.ServiceName.ACCOUNT_SERVICE)
                            .eventType(com.rajeswaran.common.AppConstants.SagaEventType.ACCOUNT_BALANCE_UPDATED)
                            .build();
                    streamBridge.send("accountBalanceUpdatedEvent-out-0", balanceUpdatedEvent);
                    streamBridge.send("auditEvent-out-0", balanceUpdatedEvent);
                    log.info("Published AccountBalanceUpdatedEvent for paymentId={}", event.getPaymentId());
                } else {
                    log.error("Failed to update both source and destination account balances for paymentId={}", event.getPaymentId());
                    // Optionally, you may want to implement compensation logic here
                }
            } else {
                String details = "Payment validation failed due to insufficient balance for paymentId: " + event.getPaymentId();
                publishPaymentFailedEvent(event, details);
                log.info("Published PaymentFailedEvent for paymentId={}", event.getPaymentId());
            }
        };
    }

    /**
     * Helper method to create and publish a PaymentFailedEvent
     *
     * @param event   The original PaymentInitiatedEvent
     * @param details Details about the failure reason
     */
    private void publishPaymentFailedEvent(PaymentInitiatedEvent event, String details) {
        PaymentFailedEvent failedEvent = PaymentFailedEvent.builder()
                .paymentId(event.getPaymentId())
                .username(event.getUsername())
                .timestamp(java.time.Instant.now())
                .details(details)
                .correlationId(event.getCorrelationId())
                .serviceName(com.rajeswaran.common.AppConstants.ServiceName.ACCOUNT_SERVICE)
                .eventType(com.rajeswaran.common.AppConstants.SagaEventType.PAYMENT_FAILED)
                .build();
        streamBridge.send("paymentFailedEvent-out-0", failedEvent);
        streamBridge.send("auditEvent-out-0", failedEvent);
    }
}
