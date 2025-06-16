package com.rajeswaran.payment.listener;

import com.rajeswaran.common.events.AccountBalanceUpdatedEvent;
import com.rajeswaran.common.events.PaymentProcessedEvent;
import com.rajeswaran.common.util.SagaEventBuilderUtil;
import com.rajeswaran.payment.entity.Payment;
import com.rajeswaran.payment.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Consumer;

@Component
public class AccountBalanceUpdatedEventListener {
    private static final Logger log = LoggerFactory.getLogger(AccountBalanceUpdatedEventListener.class);

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private StreamBridge streamBridge;

    @Bean
    public Consumer<AccountBalanceUpdatedEvent> accountBalanceUpdatedEvent() {
        return event -> {
            log.info("Received AccountBalanceUpdatedEvent for paymentId={}, sourceAccountNumber={}, destinationAccountNumber={}, amount={}",
                    event.getPaymentId(), event.getSourceAccountNumber(), event.getDestinationAccountNumber(), event.getAmount());

            Optional<Payment> paymentOpt = paymentRepository.findById(Long.valueOf(event.getPaymentId()));
            if (paymentOpt.isPresent()) {
                Payment payment = paymentOpt.get();
                payment.setStatus("PROCESSED");
                payment.setTimestamp(LocalDateTime.now()); // Update timestamp when payment is processed
                paymentRepository.save(payment);

                PaymentProcessedEvent processedEvent = PaymentProcessedEvent.builder()
                        .paymentId(event.getPaymentId())
                        .userId(event.getUserId())
                        .username(event.getUsername())
                        .sourceAccountNumber(event.getSourceAccountNumber())
                        .destinationAccountNumber(event.getDestinationAccountNumber())
                        .amount(event.getAmount())
                        .recipientUsername(event.getRecipientUsername()) // Pass through the recipient username from AccountBalanceUpdatedEvent
                        .timestamp(SagaEventBuilderUtil.now())
                        .details("Payment processed for paymentId: " + event.getPaymentId())
                        .correlationId(event.getCorrelationId())
                        .serviceName(com.rajeswaran.common.AppConstants.ServiceName.PAYMENT_SERVICE)
                        .eventType(com.rajeswaran.common.AppConstants.SagaEventType.PAYMENT_PROCESSED)
                        .build();
                streamBridge.send("paymentProcessedEvent-out-0", processedEvent);
                streamBridge.send("auditEvent-out-0", processedEvent);
                log.info("Published PaymentProcessedEvent for paymentId={}", event.getPaymentId());
            } else {
                log.error("Payment not found for paymentId={}", event.getPaymentId());
            }
        };
    }
}

