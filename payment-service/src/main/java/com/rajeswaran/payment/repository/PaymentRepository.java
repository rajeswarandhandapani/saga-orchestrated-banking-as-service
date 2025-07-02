package com.rajeswaran.payment.repository;

import com.rajeswaran.common.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    // Custom query methods if needed
    List<Payment> findByCreatedBy(String username);
}
