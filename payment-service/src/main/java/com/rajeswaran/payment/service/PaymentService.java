package com.rajeswaran.payment.service;

import com.rajeswaran.common.events.PaymentInitiatedEvent;
import com.rajeswaran.common.util.SagaEventBuilderUtil;
import com.rajeswaran.payment.entity.Payment;
import com.rajeswaran.payment.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PaymentService {
    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private StreamBridge streamBridge;

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    public Optional<Payment> getPaymentById(Long id) {
        return paymentRepository.findById(id);
    }

    public Payment createPayment(Payment payment) {
        Payment created = paymentRepository.save(payment);
        // Use SecurityContextHolder here, not in common-lib
        String username = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            username = authentication.getName();
        }
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
