package com.rajeswaran.transaction.repository;

import com.rajeswaran.common.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    // Custom query methods if needed
    List<Transaction> findByUsername(String username);
}
