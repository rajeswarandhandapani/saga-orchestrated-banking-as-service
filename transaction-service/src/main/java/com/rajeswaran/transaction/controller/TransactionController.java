package com.rajeswaran.transaction.controller;

import com.rajeswaran.common.util.SecurityUtil;
import com.rajeswaran.transaction.entity.Transaction;
import com.rajeswaran.transaction.service.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @PreAuthorize("hasRole(T(com.rajeswaran.common.AppConstants).ROLE_BAAS_ADMIN)")
    @GetMapping
    public List<Transaction> getAllTransactions() {
        log.info("Received request: getAllTransactions");
        List<Transaction> transactions = transactionService.getAllTransactions();
        log.info("Completed request: getAllTransactions, count={}", transactions.size());
        return transactions;
    }

    @PreAuthorize("hasRole(T(com.rajeswaran.common.AppConstants).ROLE_BAAS_ADMIN) or hasRole(T(com.rajeswaran.common.AppConstants).ROLE_ACCOUNT_HOLDER)")
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

    @PreAuthorize("hasRole(T(com.rajeswaran.common.AppConstants).ROLE_ACCOUNT_HOLDER)")
    @GetMapping("/my-transactions")
    public List<Transaction> getMyTransactions() {
        log.info("Received request: getMyTransactions");
        String username = SecurityUtil.getCurrentUsername();
        List<Transaction> transactions = transactionService.getTransactionsByUsername(username);
        log.info("User {} requesting their transactions, count={}", username, transactions.size());
        return transactions;
    }
}
