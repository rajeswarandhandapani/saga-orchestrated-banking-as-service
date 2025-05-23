package com.rajeswaran.account.controller;

import com.rajeswaran.account.entity.Account;
import com.rajeswaran.account.service.AccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/accounts")
public class AccountController {
    @Autowired
    private AccountService accountService;

    @GetMapping
    public List<Account> getAllAccounts() {
        log.info("Received request: getAllAccounts");
        List<Account> accounts = accountService.getAllAccounts();
        log.info("Completed request: getAllAccounts, count={}", accounts.size());
        return accounts;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Account> getAccountById(@PathVariable String id) {
        log.info("Received request: getAccountById, id={}", id);
        Optional<Account> account = accountService.getAccountById(id);
        if (account.isPresent()) {
            log.info("Completed request: getAccountById, found accountId={}", id);
            return ResponseEntity.ok(account.get());
        } else {
            log.info("Completed request: getAccountById, accountId={} not found", id);
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public Account createAccount(@RequestBody Account account) {
        log.info("Received request: createAccount, payload={}", account);
        Account created = accountService.createAccount(account);
        log.info("Completed request: createAccount, createdId={}", created.getAccountId());
        return created;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccount(@PathVariable String id) {
        log.info("Received request: deleteAccount, id={}", id);
        accountService.deleteAccount(id);
        log.info("Completed request: deleteAccount, id={}", id);
        return ResponseEntity.noContent().build();
    }
}
