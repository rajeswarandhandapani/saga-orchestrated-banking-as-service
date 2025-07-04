package com.rajeswaran.payment.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.rajeswaran.common.entity.Payment;
import com.rajeswaran.common.util.SecurityUtil;
import com.rajeswaran.payment.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;

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

        return created;
    }

    public void deletePayment(Long id) {
        paymentRepository.deleteById(id);
    }
}
