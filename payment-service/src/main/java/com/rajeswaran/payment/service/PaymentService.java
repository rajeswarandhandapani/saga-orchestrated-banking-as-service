package com.rajeswaran.payment.service;

import com.rajeswaran.common.events.PaymentInitiatedEvent;
import com.rajeswaran.common.util.SagaEventBuilderUtil;
import com.rajeswaran.common.util.SecurityUtil;
import com.rajeswaran.payment.entity.Payment;
import com.rajeswaran.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final StreamBridge streamBridge;

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    public List<Payment> getPaymentsByUsername(String username) {
        return paymentRepository.findByCreatedBy(username);
    }

    public Optional<Payment> getPaymentById(Long id) {
        return paymentRepository.findById(id);
    }

    public Payment createPayment(Payment payment) {
        // Use SecurityContextHolder here, not in common-lib
        String username = SecurityUtil.getCurrentUsername();

        // Set the createdBy field to track which user created this payment
        payment.setCreatedBy(username);
        
        // Set the timestamp when creating the payment
        payment.setTimestamp(LocalDateTime.now());
        
        // Set initial status as PENDING
        payment.setStatus("PENDING");

        Payment created = paymentRepository.save(payment);

        PaymentInitiatedEvent event = PaymentInitiatedEvent.builder()
                .paymentId(String.valueOf(created.getId()))
                .sourceAccountNumber(created.getSourceAccountNumber())
                .destinationAccountNumber(created.getDestinationAccountNumber())
                .amount(created.getAmount())
                .username(username)
                .timestamp(SagaEventBuilderUtil.now())
                .details("Payment initiated for paymentId: " + created.getId())
                .correlationId(SagaEventBuilderUtil.getCurrentCorrelationId())
                .serviceName(com.rajeswaran.common.AppConstants.ServiceName.PAYMENT_SERVICE)
                .eventType(com.rajeswaran.common.AppConstants.SagaEventType.PAYMENT_INITIATED)
                .build();
        streamBridge.send("paymentInitiatedEvent-out-0", event);
        streamBridge.send("auditEvent-out-0", event);
        return created;
    }

    public void deletePayment(Long id) {
        paymentRepository.deleteById(id);
    }
}
