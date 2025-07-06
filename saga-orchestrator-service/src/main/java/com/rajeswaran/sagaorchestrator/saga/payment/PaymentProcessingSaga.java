package com.rajeswaran.sagaorchestrator.saga.payment;

import com.rajeswaran.common.entity.Payment;
import com.rajeswaran.common.saga.notification.commands.SendNotificationCommand;
import com.rajeswaran.common.saga.payment.commands.ProcessPaymentCommand;
import com.rajeswaran.common.saga.payment.commands.RecordTransactionCommand;
import com.rajeswaran.common.saga.payment.commands.UpdatePaymentStatusCommand;
import com.rajeswaran.common.saga.payment.commands.ValidatePaymentCommand;
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
 * Happy Path Flow:
 * 1. Produces ValidatePaymentCommand → Listens for PaymentValidatedEvent/PaymentValidationFailedEvent
 * 2. Produces ProcessPaymentCommand → Listens for PaymentProcessedEvent/PaymentFailedEvent
 * 3. Produces RecordTransactionCommand → Listens for TransactionRecordedEvent/TransactionFailedEvent
 * 4. Produces UpdatePaymentStatusCommand → Listens for PaymentStatusUpdatedEvent
 * 5. Produces SendNotificationCommand → Completes saga
 *
 * Failure Flow:
 * - If validation fails → Fails saga immediately
 * - If payment processing fails → Fails saga immediately  
 * - If transaction recording fails → Sends failure notification
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
        if (payload instanceof Payment payment) {
            log.info("Starting payment processing saga flow {} for payment: {}", sagaId, payment);
            triggerValidatePaymentCommand(sagaId, payment);
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

    private void triggerValidatePaymentCommand(Long sagaId, Payment payment) {
        log.info("Triggering ValidatePaymentCommand for saga {} and payment: {}", sagaId, payment);

        ValidatePaymentCommand command = ValidatePaymentCommand.create(
            sagaId,
            SagaEventBuilderUtil.getCurrentCorrelationId(),
            payment
        );

        // Record step as STARTED before publishing command
        startStep(sagaId, PaymentProcessingSteps.VALIDATE_PAYMENT.getStepName(), command);

        streamBridge.send("validatePaymentCommand-out-0", command);
    }

    private void triggerProcessPaymentCommand(Long sagaId, Payment payment) {
        log.info("Triggering ProcessPaymentCommand for saga {} and payment: {}", sagaId, payment);

        ProcessPaymentCommand command = ProcessPaymentCommand.create(
            sagaId,
            SagaEventBuilderUtil.getCurrentCorrelationId(),
            payment
        );

        // Record step as STARTED before publishing command
        startStep(sagaId, PaymentProcessingSteps.PROCESS_PAYMENT.getStepName(), command);

        streamBridge.send("processPaymentCommand-out-0", command);
    }

    private void triggerRecordTransactionCommand(Long sagaId, Payment payment) {
        log.info("Triggering RecordTransactionCommand for saga {} and payment: {}", sagaId, payment);

        RecordTransactionCommand command = RecordTransactionCommand.create(
            sagaId,
            SagaEventBuilderUtil.getCurrentCorrelationId(),
            payment
        );

        // Record step as STARTED before publishing command
        startStep(sagaId, PaymentProcessingSteps.RECORD_TRANSACTION.getStepName(), command);

        streamBridge.send("recordTransactionCommand-out-0", command);
    }

    private void triggerUpdatePaymentStatusCommand(Long sagaId, Payment payment, String status) {
        log.info("Triggering UpdatePaymentStatusCommand for saga {} and payment: {} to status: {}", sagaId, payment.getId(), status);

        // Set the payment status in the saga
        payment.setStatus(status);

        UpdatePaymentStatusCommand command = UpdatePaymentStatusCommand.create(
            sagaId,
            SagaEventBuilderUtil.getCurrentCorrelationId(),
            payment
        );

        // Record step as STARTED before publishing command
        startStep(sagaId, PaymentProcessingSteps.UPDATE_PAYMENT_STATUS.getStepName(), command);

        streamBridge.send("updatePaymentStatusCommand-out-0", command);
    }



    // === EVENT LISTENERS (Responds to events from other services) ===

    /**
     * Listens for PaymentValidatedEvent to proceed to next step or PaymentValidationFailedEvent to handle failure.
     */
    @Bean
    public Consumer<Message<PaymentValidatedEvent>> paymentValidatedEvent() {
        return message -> {
            PaymentValidatedEvent event = message.getPayload();
            Payment payment = event.getPayment();
            Long sagaId = event.getSagaId();

            log.info("Received PaymentValidatedEvent for saga {}, payment: {}", sagaId, payment.getId());

            try {
                // Complete current step
                completeStep(sagaId, PaymentProcessingSteps.VALIDATE_PAYMENT.getStepName(), event);

                // Proceed to next step: Process Payment
                triggerProcessPaymentCommand(sagaId, payment);

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
    public Consumer<Message<PaymentValidationFailedEvent>> paymentValidationFailedEvent() {
        return message -> {
            PaymentValidationFailedEvent event = message.getPayload();
            Long sagaId = event.getSagaId();

            log.error("Received PaymentValidationFailedEvent for saga {}, payment: {}, reason: {}",
                sagaId, event.getPayment().getId(), event.getReason());

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
    public Consumer<Message<PaymentProcessedEvent>> paymentProcessedEvent() {
        return message -> {
            PaymentProcessedEvent event = message.getPayload();
            Payment payment = event.getPayment();
            Long sagaId = event.getSagaId();

            log.info("Received PaymentProcessedEvent for saga {}, payment: {}", sagaId, payment);

            try {
                // Complete current step
                completeStep(sagaId, PaymentProcessingSteps.PROCESS_PAYMENT.getStepName(), event);

                // Proceed to next step: Record Transaction (skip UpdateAccountBalanceCommand)
                triggerRecordTransactionCommand(sagaId, payment);

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
                sagaId, event.getPayment().getId(), event.getReason());

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
     * Listens for TransactionRecordedEvent to proceed to next step or TransactionFailedEvent to handle failure.
     */
    @Bean
    public Consumer<Message<TransactionRecordedEvent>> transactionRecordedEvent() {
        return message -> {
            TransactionRecordedEvent event = message.getPayload();
            Payment payment = event.getPayment();
            Long sagaId = event.getSagaId();

            log.info("Received TransactionRecordedEvent for saga {}, payment: {}", sagaId, payment);

            try {
                // Complete current step
                completeStep(sagaId, PaymentProcessingSteps.RECORD_TRANSACTION.getStepName(), event);

                // Trigger payment status update to COMPLETED
                triggerUpdatePaymentStatusCommand(sagaId, payment, "COMPLETED");

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
    public Consumer<Message<TransactionFailedEvent>> transactionFailedEvent() {
        return message -> {
            TransactionFailedEvent event = message.getPayload();
            Payment payment = event.getPayment();
            Long sagaId = event.getSagaId();

            log.error("Received TransactionFailedEvent for saga {}, payment: {}, reason: {}",
                sagaId, payment.getId(), event.getReason());

            try {
                // Mark step as failed
                failStep(sagaId, PaymentProcessingSteps.RECORD_TRANSACTION.getStepName(), event);


                String subject = "Payment Processing Failed";
                String notificationMessage = String.format(
                        "Your payment of $%.2f from account %s to account %s failed to process. Payment ID: %s",
                        payment.getAmount(), payment.getSourceAccountNumber(), payment.getDestinationAccountNumber(), payment.getId()
                );

                triggerSendNotificationCommand(sagaId, payment.getCreatedBy(), subject, notificationMessage);

            } catch (Exception e) {
                log.error("Error processing TransactionFailedEvent for saga {}: {}", sagaId, e.getMessage(), e);
            }
        };
    }

    /**
     * Listens for PaymentStatusUpdatedEvent to proceed to final step.
     */
    @Bean
    public Consumer<Message<PaymentStatusUpdatedEvent>> paymentStatusUpdatedEvent() {
        return message -> {
            PaymentStatusUpdatedEvent event = message.getPayload();
            Payment payment = event.getPayment();
            Long sagaId = event.getSagaId();

            log.info("Received PaymentStatusUpdatedEvent for saga {}, payment: {} with status: {}", 
                    sagaId, payment.getId(), payment.getStatus());

            try {
                // Complete the update payment status step
                completeStep(sagaId, PaymentProcessingSteps.UPDATE_PAYMENT_STATUS.getStepName(), event);

                String subject = "Payment Processed Successfully";
                String notificationMessage = String.format(
                        "Your payment of $%.2f from account %s to account %s has been processed successfully and marked as %s. Payment ID: %s",
                        payment.getAmount(), payment.getSourceAccountNumber(), payment.getDestinationAccountNumber(), 
                        payment.getStatus(), payment.getId()
                );

                // Trigger notification (fire-and-forget)
                triggerSendNotificationCommand(sagaId, payment.getCreatedBy(), subject, notificationMessage);
                
                // Mark saga as complete
                completeSaga(sagaId);

            } catch (Exception e) {
                log.error("Error processing PaymentStatusUpdatedEvent for saga {}: {}", sagaId, e.getMessage(), e);
                failSaga(sagaId);
            }
        };
    }
}
