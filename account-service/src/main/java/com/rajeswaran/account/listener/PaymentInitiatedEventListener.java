package com.rajeswaran.account.listener;

import com.rajeswaran.account.service.AccountService;
import com.rajeswaran.common.events.PaymentFailedEvent;
import com.rajeswaran.common.events.PaymentInitiatedEvent;
import com.rajeswaran.common.events.PaymentValidatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentInitiatedEventListener {
    private final AccountService accountService;
    private final StreamBridge streamBridge;

    public Consumer<PaymentInitiatedEvent> paymentInitiatedEvent() {
        return event -> {
            log.info("Received PaymentInitiatedEvent for paymentId={}, sourceAccountNumber={}, amount={}",
                    event.getPaymentId(), event.getSourceAccountNumber(), event.getAmount());
            boolean valid = accountService.validateSourceAccount(event.getSourceAccountNumber(), event.getAmount());
            String correlationId = MDC.get("correlationId");
            if (valid) {
                PaymentValidatedEvent validatedEvent = PaymentValidatedEvent.builder()
                        .paymentId(event.getPaymentId())
                        .valid(true)
                        .reason(null)
                        .username(event.getUsername())
                        .timestamp(java.time.Instant.now())
                        .details("Payment validated for paymentId: " + event.getPaymentId())
                        .correlationId(correlationId)
                        .serviceName(com.rajeswaran.common.AppConstants.ServiceName.ACCOUNT_SERVICE)
                        .eventType(com.rajeswaran.common.AppConstants.SagaEventType.PAYMENT_VALIDATED)
                        .build();
                streamBridge.send("paymentValidatedEvent-out-0", validatedEvent);
                streamBridge.send("auditEvent-out-0", validatedEvent);
                log.info("Published PaymentValidatedEvent for paymentId={}", event.getPaymentId());
            } else {
                PaymentFailedEvent failedEvent = PaymentFailedEvent.builder()
                        .paymentId(event.getPaymentId())
                        .reason("Account validation failed or insufficient balance")
                        .username(event.getUsername())
                        .timestamp(java.time.Instant.now())
                        .details("Payment validation failed for paymentId: " + event.getPaymentId())
                        .correlationId(correlationId)
                        .serviceName(com.rajeswaran.common.AppConstants.ServiceName.ACCOUNT_SERVICE)
                        .eventType(com.rajeswaran.common.AppConstants.SagaEventType.PAYMENT_FAILED)
                        .build();
                streamBridge.send("paymentFailedEvent-out-0", failedEvent);
                streamBridge.send("auditEvent-out-0", failedEvent);
                log.info("Published PaymentFailedEvent for paymentId={}", event.getPaymentId());
            }
        };
    }
}
