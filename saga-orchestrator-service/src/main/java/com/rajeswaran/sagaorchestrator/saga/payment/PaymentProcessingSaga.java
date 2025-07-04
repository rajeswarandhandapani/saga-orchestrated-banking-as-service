package com.rajeswaran.sagaorchestrator.saga.payment;/*
 * PaymentProcessingSaga temporarily commented out during SagaId migration
 * TODO: Update this saga to use long sagaId instead of SagaId value object
 */


import com.rajeswaran.common.saga.payment.commands.*;
import com.rajeswaran.common.saga.notification.commands.SendNotificationCommand;
import com.rajeswaran.common.saga.payment.events.*;
import com.rajeswaran.common.util.SagaEventBuilderUtil;
import com.rajeswaran.sagaorchestrator.constants.SagaConstants;
import com.rajeswaran.sagaorchestrator.saga.Saga;
import com.rajeswaran.sagaorchestrator.service.SagaStateManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

/**
 * Self-orchestrating Payment Processing Saga using command/event pattern.
 *
 * Flow:
 * 1. Produces ValidatePaymentCommand → Listens for PaymentValidatedEvent/PaymentValidationFailedEvent
 * 2. Produces ProcessPaymentCommand → Listens for PaymentProcessedEvent/PaymentFailedEvent
 * 3. Produces UpdateAccountBalanceCommand → Listens for AccountBalanceUpdatedEvent/AccountBalanceUpdateFailedEvent
 * 4. Produces RecordTransactionCommand → Listens for TransactionRecordedEvent/TransactionFailedEvent
 * 5. Produces SendNotificationCommand → Listens for NotificationSentEvent/NotificationFailedEvent
 */
@Component
@Slf4j
public class PaymentProcessingSaga extends Saga {

    public PaymentProcessingSaga(SagaStateManager sagaStateManager, StreamBridge streamBridge) {
        super(sagaStateManager, streamBridge);
    }

    @Override
    public String getSagaName() {
        return SagaConstants.PAYMENT_PROCESSING_SAGA;
    }

    @Override
    public void startSagaFlow(Long sagaId, Object payload) {
        if (payload instanceof PaymentRequest paymentRequest) {
            log.info("Starting payment processing saga flow {} for payment: {}", sagaId, paymentRequest.getPaymentId());
            triggerValidatePaymentCommand(sagaId, paymentRequest);
        } else {
            throw new IllegalArgumentException("PaymentProcessingSaga requires PaymentRequest as payload, got: " +
                (payload != null ? payload.getClass().getSimpleName() : "null"));
        }
    }

    @Override
    public void completeSagaFlow(Long sagaId) {
        log.info("Payment processing saga {} completed successfully", sagaId);
    }

    // === COMMAND PRODUCERS (Triggers commands to other services) ===

    private void triggerValidatePaymentCommand(Long sagaId, PaymentRequest paymentRequest) {
        log.info("Triggering ValidatePaymentCommand for saga {} and payment: {}", sagaId, paymentRequest.getPaymentId());

        ValidatePaymentCommand command = ValidatePaymentCommand.create(
            sagaId,
            SagaEventBuilderUtil.getCurrentCorrelationId(),
            paymentRequest.getPaymentId(),
            paymentRequest.getSourceAccountNumber(),
            paymentRequest.getDestinationAccountNumber(),
            paymentRequest.getAmount(),
            paymentRequest.getDescription(),
            paymentRequest.getUsername()
        );

        // Record step as STARTED before publishing command
        startStep(sagaId, PaymentProcessingSteps.VALIDATE_PAYMENT.getStepName(), command);

        streamBridge.send("validatePaymentCommand-out-0", command);
    }

    private void triggerProcessPaymentCommand(Long sagaId, String paymentId, String sourceAccount,
                                             String destinationAccount, double amount, String description, String username) {
        log.info("Triggering ProcessPaymentCommand for saga {} and payment: {}", sagaId, paymentId);

        ProcessPaymentCommand command = ProcessPaymentCommand.create(
            sagaId,
            SagaEventBuilderUtil.getCurrentCorrelationId(),
            paymentId,
            sourceAccount,
            destinationAccount,
            amount,
            description,
            username
        );

        // Record step as STARTED before publishing command
        startStep(sagaId, PaymentProcessingSteps.PROCESS_PAYMENT.getStepName(), command);

        streamBridge.send("processPaymentCommand-out-0", command);
    }

    private void triggerUpdateAccountBalanceCommand(Long sagaId, String paymentId, String sourceAccount,
                                                   String destinationAccount, double amount, String description) {
        log.info("Triggering UpdateAccountBalanceCommand for saga {} and payment: {}", sagaId, paymentId);

        UpdateAccountBalanceCommand command = UpdateAccountBalanceCommand.create(
            sagaId,
            SagaEventBuilderUtil.getCurrentCorrelationId(),
            paymentId,
            sourceAccount,
            destinationAccount,
            amount,
            description
        );

        // Record step as STARTED before publishing command
        startStep(sagaId, PaymentProcessingSteps.UPDATE_ACCOUNT_BALANCE.getStepName(), command);

        streamBridge.send("updateAccountBalanceCommand-out-0", command);
    }

    private void triggerRecordTransactionCommand(Long sagaId, String paymentId, String sourceAccount,
                                                String destinationAccount, double amount, String description) {
        log.info("Triggering RecordTransactionCommand for saga {} and payment: {}", sagaId, paymentId);

        RecordTransactionCommand command = RecordTransactionCommand.create(
            sagaId,
            SagaEventBuilderUtil.getCurrentCorrelationId(),
            paymentId,
            sourceAccount,
            destinationAccount,
            amount,
            description,
            "PAYMENT"
        );

        // Record step as STARTED before publishing command
        startStep(sagaId, PaymentProcessingSteps.RECORD_TRANSACTION.getStepName(), command);

        streamBridge.send("recordTransactionCommand-out-0", command);
    }

    private void triggerSendNotificationCommand(Long sagaId, String userEmail, String paymentId,
                                               double amount, String sourceAccount, String destinationAccount) {
        log.info("Triggering SendNotificationCommand for saga {} and payment: {}", sagaId, paymentId);

        String subject = "Payment Processed Successfully";
        String message = String.format(
            "Your payment of $%.2f from account %s to account %s has been processed successfully. Payment ID: %s",
            amount, sourceAccount, destinationAccount, paymentId
        );

        SendNotificationCommand command = SendNotificationCommand.create(
            sagaId,
            SagaEventBuilderUtil.getCurrentCorrelationId(),
            userEmail,
            subject,
            message
        );

        // Record step as STARTED before publishing command
        startStep(sagaId, PaymentProcessingSteps.SEND_NOTIFICATION.getStepName(), command);

        streamBridge.send("sendNotificationCommand-out-0", command);
    }

    // === EVENT LISTENERS (Responds to events from other services) ===

    /**
     * Listens for PaymentValidatedEvent to proceed to next step or PaymentValidationFailedEvent to handle failure.
     */
    @Bean
    public Consumer<Message<PaymentValidatedEvent>> paymentValidatedEventListener() {
        return message -> {
            PaymentValidatedEvent event = message.getPayload();
            Long sagaId = event.getSagaId();

            log.info("Received PaymentValidatedEvent for saga {}, payment: {}", sagaId, event.getPaymentId());

            try {
                // Complete current step
                completeStep(sagaId, PaymentProcessingSteps.VALIDATE_PAYMENT.getStepName(), event);

                // Proceed to next step: Process Payment
                triggerProcessPaymentCommand(sagaId, event.getPaymentId(), event.getSourceAccountNumber(),
                    event.getDestinationAccountNumber(), event.getAmount(), event.getDescription(), event.getUsername());

            } catch (Exception e) {
                log.error("Error processing PaymentValidatedEvent for saga {}: {}", sagaId, e.getMessage(), e);
                failSaga(sagaId);
            }
        };
    }

    /**
     * Listens for PaymentValidationFailedEvent to handle payment validation failure.
     */
    @Bean
    public Consumer<Message<PaymentValidationFailedEvent>> paymentValidationFailedEventListener() {
        return message -> {
            PaymentValidationFailedEvent event = message.getPayload();
            Long sagaId = event.getSagaId();

            log.error("Received PaymentValidationFailedEvent for saga {}, payment: {}, reason: {}",
                sagaId, event.getPaymentId(), event.getReason());

            try {
                // Mark step as failed
                failStep(sagaId, PaymentProcessingSteps.VALIDATE_PAYMENT.getStepName(), event);

                // Handle saga failure (no compensation needed for validation failure)
                failSaga(sagaId);

            } catch (Exception e) {
                log.error("Error processing PaymentValidationFailedEvent for saga {}: {}", sagaId, e.getMessage(), e);
            }
        };
    }

    /**
     * Listens for PaymentProcessedEvent to proceed to next step or PaymentFailedEvent to handle failure.
     */
    @Bean
    public Consumer<Message<PaymentProcessedEvent>> paymentProcessedEventListener() {
        return message -> {
            PaymentProcessedEvent event = message.getPayload();
            Long sagaId = event.getSagaId();

            log.info("Received PaymentProcessedEvent for saga {}, payment: {}", sagaId, event.getPaymentId());

            try {
                // Complete current step
                completeStep(sagaId, PaymentProcessingSteps.PROCESS_PAYMENT.getStepName(), event);

                // Proceed to next step: Update Account Balance
                triggerUpdateAccountBalanceCommand(sagaId, event.getPaymentId(), event.getSourceAccountNumber(),
                    event.getDestinationAccountNumber(), event.getAmount(), event.getDescription());

            } catch (Exception e) {
                log.error("Error processing PaymentProcessedEvent for saga {}: {}", sagaId, e.getMessage(), e);
                failSaga(sagaId);
            }
        };
    }

    /**
     * Listens for PaymentFailedEvent to handle payment processing failure.
     */
    @Bean
    public Consumer<Message<PaymentFailedEvent>> paymentFailedEventListener() {
        return message -> {
            PaymentFailedEvent event = message.getPayload();
            Long sagaId = event.getSagaId();

            log.error("Received PaymentFailedEvent for saga {}, payment: {}, reason: {}",
                sagaId, event.getPaymentId(), event.getReason());

            try {
                // Mark step as failed
                failStep(sagaId, PaymentProcessingSteps.PROCESS_PAYMENT.getStepName(), event);

                // Handle saga failure (no compensation needed as payment wasn't actually processed)
                failSaga(sagaId);

            } catch (Exception e) {
                log.error("Error processing PaymentFailedEvent for saga {}: {}", sagaId, e.getMessage(), e);
            }
        };
    }

    /**
     * Listens for AccountBalanceUpdatedEvent to proceed to next step or AccountBalanceUpdateFailedEvent to handle failure.
     */
    @Bean
    public Consumer<Message<AccountBalanceUpdatedEvent>> accountBalanceUpdatedEventListener() {
        return message -> {
            AccountBalanceUpdatedEvent event = message.getPayload();
            Long sagaId = event.getSagaId();

            log.info("Received AccountBalanceUpdatedEvent for saga {}, payment: {}", sagaId, event.getPaymentId());

            try {
                // Complete current step
                completeStep(sagaId, PaymentProcessingSteps.UPDATE_ACCOUNT_BALANCE.getStepName(), event);

                // Proceed to next step: Record Transaction
                triggerRecordTransactionCommand(sagaId, event.getPaymentId(), event.getSourceAccountNumber(),
                    event.getDestinationAccountNumber(), event.getAmount(), event.getDescription());

            } catch (Exception e) {
                log.error("Error processing AccountBalanceUpdatedEvent for saga {}: {}", sagaId, e.getMessage(), e);
                failSaga(sagaId);
            }
        };
    }

    /**
     * Listens for AccountBalanceUpdateFailedEvent to handle account balance update failure.
     */
    @Bean
    public Consumer<Message<AccountBalanceUpdateFailedEvent>> accountBalanceUpdateFailedEventListener() {
        return message -> {
            AccountBalanceUpdateFailedEvent event = message.getPayload();
            Long sagaId = event.getSagaId();

            log.error("Received AccountBalanceUpdateFailedEvent for saga {}, payment: {}, reason: {}",
                sagaId, event.getPaymentId(), event.getReason());

            try {
                // Mark step as failed
                failStep(sagaId, PaymentProcessingSteps.UPDATE_ACCOUNT_BALANCE.getStepName(), event);

                // Start compensation: Reverse the payment since it was processed but balance update failed
                triggerReversePaymentCommand(sagaId, event.getPaymentId(), event.getSourceAccountNumber(),
                    event.getDestinationAccountNumber(), event.getAmount(),
                    "Account balance update failed: " + event.getReason());

            } catch (Exception e) {
                log.error("Error processing AccountBalanceUpdateFailedEvent for saga {}: {}", sagaId, e.getMessage(), e);
            }
        };
    }

    /**
     * Listens for TransactionRecordedEvent to proceed to next step or TransactionFailedEvent to handle failure.
     */
    @Bean
    public Consumer<Message<TransactionRecordedEvent>> transactionRecordedEventListener() {
        return message -> {
            TransactionRecordedEvent event = message.getPayload();
            Long sagaId = event.getSagaId();

            log.info("Received TransactionRecordedEvent for saga {}, payment: {}", sagaId, event.getPaymentId());

            try {
                // Complete current step
                completeStep(sagaId, PaymentProcessingSteps.RECORD_TRANSACTION.getStepName(), event);

                // Get payment request details from saga context to send notification
                // For now, we'll use the event data and construct a generic notification
                triggerSendNotificationCommand(sagaId, "getUserEmailFromSagaContext(sagaId)",
                    event.getPaymentId(), event.getAmount(),
                    event.getSourceAccountNumber(), event.getDestinationAccountNumber());

            } catch (Exception e) {
                log.error("Error processing TransactionRecordedEvent for saga {}: {}", sagaId, e.getMessage(), e);
                failSaga(sagaId);
            }
        };
    }

    /**
     * Listens for TransactionFailedEvent to handle transaction recording failure.
     */
    @Bean
    public Consumer<Message<TransactionFailedEvent>> transactionFailedEventListener() {
        return message -> {
            TransactionFailedEvent event = message.getPayload();
            Long sagaId = event.getSagaId();

            log.error("Received TransactionFailedEvent for saga {}, payment: {}, reason: {}",
                sagaId, event.getPaymentId(), event.getReason());

            try {
                // Mark step as failed
                failStep(sagaId, PaymentProcessingSteps.RECORD_TRANSACTION.getStepName(), event);

                // Transaction recording failed but payment and balance update succeeded
                // Continue with notification but include warning about audit trail
                String userEmail = "getUserEmailFromSagaContext(sagaId)";
                triggerSendNotificationCommand(sagaId, userEmail, event.getPaymentId(),
                    event.getAmount(), event.getSourceAccountNumber(), event.getDestinationAccountNumber());

            } catch (Exception e) {
                log.error("Error processing TransactionFailedEvent for saga {}: {}", sagaId, e.getMessage(), e);
            }
        };
    }

    /**
     * Listens for NotificationSentEvent to complete the saga or NotificationFailedEvent to handle failure.
     */
    /*@Bean
    public Consumer<Message<NotificationSentEvent>> notificationSentEventListener() {
        return message -> {
            NotificationSentEvent event = message.getPayload();
            Long sagaId = event.getSagaId();

            log.info("Received NotificationSentEvent for saga {}", sagaId);

            try {
                // Complete current step
                completeStep(sagaId, PaymentProcessingSteps.SEND_NOTIFICATION.getStepName(), event);

                // Complete the entire saga
                completeSaga(sagaId);

            } catch (Exception e) {
                log.error("Error processing NotificationSentEvent for saga {}: {}", sagaId, e.getMessage(), e);
            }
        };
    }*/

    /**
     * Listens for NotificationFailedEvent to handle notification failure.
     */
    /*@Bean
    public Consumer<Message<NotificationFailedEvent>> notificationFailedEventListener() {
        return message -> {
            NotificationFailedEvent event = message.getPayload();
            Long sagaId = event.getSagaId();

            log.error("Received NotificationFailedEvent for saga {}, reason: {}", sagaId, event.getReason());

            try {
                // Mark step as failed
                failStep(sagaId, PaymentProcessingSteps.SEND_NOTIFICATION.getStepName(), event);

                // Payment was successful but notification failed - complete saga anyway
                log.warn("Payment processing completed successfully but notification failed for saga {}", sagaId);
                completeSaga(sagaId);

            } catch (Exception e) {
                log.error("Error processing NotificationFailedEvent for saga {}: {}", sagaId, e.getMessage(), e);
            }
        };
    }*/

    // === COMPENSATION METHODS (Rollback/Reverse operations) ===

    /**
     * Triggers reverse payment command for compensation.
     */
    private void triggerReversePaymentCommand(Long sagaId, String paymentId, String sourceAccount,
                                             String destinationAccount, double amount, String reason) {
        log.info("Triggering ReversePaymentCommand for saga {} and payment: {}", sagaId, paymentId);

        ReversePaymentCommand command = ReversePaymentCommand.create(
            sagaId,
            SagaEventBuilderUtil.getCurrentCorrelationId(),
            paymentId,
            sourceAccount,
            destinationAccount,
            amount,
            reason,
            "getUsernameFromSagaContext(sagaId)"
        );

        // Record compensation step
        startStep(sagaId, "reverse-payment", command);

        streamBridge.send("reversePaymentCommand-out-0", command);
    }

    /**
     * Listens for PaymentReversedEvent to handle successful payment reversal.
     */
    @Bean
    public Consumer<Message<PaymentReversedEvent>> paymentReversedEventListener() {
        return message -> {
            PaymentReversedEvent event = message.getPayload();
            Long sagaId = event.getSagaId();

            log.info("Received PaymentReversedEvent for saga {}, payment: {}", sagaId, event.getPaymentId());

            try {
                // Complete compensation step
                completeStep(sagaId, "reverse-payment", event);

                // Send failure notification to user
                String userEmail = "getUserEmailFromSagaContext(sagaId)";
                String subject = "Payment Processing Failed";
                String notificationMessage = String.format(
                    "Your payment of $%.2f could not be completed and has been reversed. Reason: %s. Payment ID: %s",
                    event.getAmount(), event.getReason(), event.getPaymentId()
                );

                SendNotificationCommand command = SendNotificationCommand.create(
                    sagaId,
                    SagaEventBuilderUtil.getCurrentCorrelationId(),
                    userEmail,
                    subject,
                    notificationMessage
                );

                streamBridge.send("sendNotificationCommand-out-0", command);

                // Mark saga as compensated
                failSaga(sagaId);
                log.info("Payment reversed successfully for saga {}", sagaId);

            } catch (Exception e) {
                log.error("Error processing PaymentReversedEvent for saga {}: {}", sagaId, e.getMessage(), e);
            }
        };
    }


}
