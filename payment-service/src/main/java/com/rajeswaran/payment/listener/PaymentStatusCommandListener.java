package com.rajeswaran.payment.listener;

import com.rajeswaran.common.saga.payment.commands.UpdatePaymentStatusCommand;
import com.rajeswaran.common.saga.payment.events.PaymentStatusUpdatedEvent;
import com.rajeswaran.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.function.Consumer;
import com.rajeswaran.common.entity.Payment;

/**
 * Handles UpdatePaymentStatusCommand to update payment status.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentStatusCommandListener {

    private final PaymentService paymentService;
    private final StreamBridge streamBridge;

    /**
     * Consumes UpdatePaymentStatusCommand and updates the entire payment object.
     */
    @Bean
    public Consumer<Message<UpdatePaymentStatusCommand>> updatePaymentStatusCommand() {
        return message -> {
            UpdatePaymentStatusCommand command = message.getPayload();
            Payment payment = command.getPayment();
            
            log.info("Received UpdatePaymentStatusCommand for saga {} and payment: {} to status: {}", 
                    command.getSagaId(), payment.getId(), payment.getStatus());

            try {
                Payment updatedPayment = paymentService.updatePayment(payment);
                
                log.info("Successfully updated payment {} to status: {}", payment.getId(), payment.getStatus());
                
                // Publish success event
                PaymentStatusUpdatedEvent event = PaymentStatusUpdatedEvent.create(
                    command.getSagaId(),
                    updatedPayment
                );
                streamBridge.send("paymentStatusUpdatedEvent-out-0", event);
                
            } catch (Exception e) {
                log.error("Error updating payment {}: {}", 
                        payment.getId(), e.getMessage(), e);
                // Could publish a failure event here if needed
            }
        };
    }
}
