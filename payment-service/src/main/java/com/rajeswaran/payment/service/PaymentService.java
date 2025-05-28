package com.rajeswaran.payment.service;

import com.rajeswaran.common.events.PaymentInitiatedEvent;
import com.rajeswaran.payment.entity.Payment;
import com.rajeswaran.payment.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
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
        PaymentInitiatedEvent event = PaymentInitiatedEvent.builder()
                .paymentId(String.valueOf(created.getId()))
                .sourceAccountNumber(created.getSourceAccountNumber())
                .destinationAccountNumber(created.getDestinationAccountNumber())
                .amount(created.getAmount())
                .build();
        streamBridge.send("paymentInitiatedEvent-out-0", event);
        return created;
    }

    public void deletePayment(Long id) {
        paymentRepository.deleteById(id);
    }
}
