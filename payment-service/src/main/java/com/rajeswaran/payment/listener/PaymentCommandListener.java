package com.rajeswaran.payment.listener;

import com.rajeswaran.common.entity.Payment;
import com.rajeswaran.common.saga.payment.commands.ValidatePaymentCommand;
import com.rajeswaran.common.saga.payment.events.PaymentValidatedEvent;
import com.rajeswaran.common.saga.payment.events.PaymentValidationFailedEvent;
import com.rajeswaran.payment.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

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

    /**
     * Consumes ValidatePaymentCommand, performs basic validation, and publishes result event.
     */
    @Bean
    public Consumer<Message<ValidatePaymentCommand>> validatePaymentCommand(PaymentService paymentService) {
        return message -> {
            ValidatePaymentCommand command = message.getPayload();
            Payment payment = command.getPayment();
            log.info("Received ValidatePaymentCommand for saga {} and payment: {}", command.getSagaId(), payment);

            // Basic validation placeholder (always succeeds for now)
            boolean valid = payment.getAmount() > 0 && payment.getSourceAccountNumber() != null && payment.getDestinationAccountNumber() != null;

            if (valid) {
                PaymentValidatedEvent event = PaymentValidatedEvent.create(
                    command.getSagaId(),
                    payment
                );
                paymentService.createPayment(payment);
                streamBridge.send("paymentValidatedEvent-out-0", event);
                log.info("Published PaymentValidatedEvent for saga {} and payment: {}", command.getSagaId(), payment);
            } else {
                PaymentValidationFailedEvent event = PaymentValidationFailedEvent.create(
                    command.getSagaId(),
                    payment,
                    "Validation failed: Invalid amount or account information"
                );
                streamBridge.send("paymentValidationFailedEvent-out-0", event);
                log.warn("Published PaymentValidationFailedEvent for saga {} and payment: {}", command.getSagaId(), payment.getId());
            }
        };
    }
}
