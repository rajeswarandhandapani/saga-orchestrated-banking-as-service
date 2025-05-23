package com.rajeswaran.payment.controller;

import com.rajeswaran.payment.entity.Payment;
import com.rajeswaran.payment.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    @Autowired
    private PaymentService paymentService;

    @GetMapping
    public List<Payment> getAllPayments() {
        log.info("Received request: getAllPayments");
        List<Payment> payments = paymentService.getAllPayments();
        log.info("Completed request: getAllPayments, count={}", payments.size());
        return payments;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Payment> getPaymentById(@PathVariable Long id) {
        log.info("Received request: getPaymentById, id={}", id);
        Optional<Payment> payment = paymentService.getPaymentById(id);
        if (payment.isPresent()) {
            log.info("Completed request: getPaymentById, found paymentId={}", id);
            return ResponseEntity.ok(payment.get());
        } else {
            log.info("Completed request: getPaymentById, paymentId={} not found", id);
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public Payment createPayment(@RequestBody Payment payment) {
        log.info("Received request: createPayment, payload={}", payment);
        Payment created = paymentService.createPayment(payment);
        log.info("Completed request: createPayment, createdId={}", created.getId());
        return created;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePayment(@PathVariable Long id) {
        log.info("Received request: deletePayment, id={}", id);
        paymentService.deletePayment(id);
        log.info("Completed request: deletePayment, id={}", id);
        return ResponseEntity.noContent().build();
    }
}
