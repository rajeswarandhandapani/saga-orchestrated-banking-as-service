package com.rajeswaran.payment.listener;

import com.rajeswaran.common.events.PaymentFailedEvent;
import com.rajeswaran.common.entity.Payment;
import com.rajeswaran.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
public class PaymentFailedEventListener {
    private static final Logger log = LoggerFactory.getLogger(PaymentFailedEventListener.class);

    private final PaymentRepository paymentRepository;

    @Bean
    public Consumer<PaymentFailedEvent> paymentFailedEvent() {
        return event -> {
            log.info("Received PaymentFailedEvent for paymentId={}", event.getPaymentId());

            Optional<Payment> paymentOpt = paymentRepository.findById(Long.valueOf(event.getPaymentId()));
            if (paymentOpt.isPresent()) {
                Payment payment = paymentOpt.get();
                payment.setStatus("FAILED");
                payment.setTimestamp(LocalDateTime.now()); // Update timestamp when payment fails
                paymentRepository.save(payment);
                log.info("Updated payment status to FAILED for paymentId={}", event.getPaymentId());
            } else {
                log.error("Payment not found for paymentId={}", event.getPaymentId());
            }
        };
    }
}
