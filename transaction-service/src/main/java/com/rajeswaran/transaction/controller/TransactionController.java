package com.rajeswaran.transaction.controller;

import com.rajeswaran.transaction.entity.Transaction;
import com.rajeswaran.transaction.service.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @GetMapping
    public List<Transaction> getAllTransactions() {
        log.info("Received request: getAllTransactions");
        List<Transaction> transactions = transactionService.getAllTransactions();
        log.info("Completed request: getAllTransactions, count={}", transactions.size());
        return transactions;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getTransactionById(@PathVariable Long id) {
        log.info("Received request: getTransactionById, id={}", id);
        Optional<Transaction> transaction = transactionService.getTransactionById(id);
        if (transaction.isPresent()) {
            log.info("Completed request: getTransactionById, found transactionId={}", id);
            return ResponseEntity.ok(transaction.get());
        } else {
            log.info("Completed request: getTransactionById, transactionId={} not found", id);
            return ResponseEntity.notFound().build();
        }
    }


}

