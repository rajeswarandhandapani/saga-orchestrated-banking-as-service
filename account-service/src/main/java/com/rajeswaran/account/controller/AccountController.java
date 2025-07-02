package com.rajeswaran.account.controller;

import com.rajeswaran.account.service.AccountService;
import com.rajeswaran.common.entity.Account;
import com.rajeswaran.common.util.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;

    @PreAuthorize("hasRole(T(com.rajeswaran.common.AppConstants).ROLE_BAAS_ADMIN)")
    @GetMapping
    public List<Account> getAllAccounts() {
        log.info("Received request: getAllAccounts");
        List<Account> accounts = accountService.getAllAccounts();
        log.info("Completed request: getAllAccounts, count={}", accounts.size());
        return accounts;
    }

    @PreAuthorize("hasRole(T(com.rajeswaran.common.AppConstants).ROLE_BAAS_ADMIN) or hasRole(T(com.rajeswaran.common.AppConstants).ROLE_ACCOUNT_HOLDER)")
    @GetMapping("/{accountNumber}")
    public ResponseEntity<Account> getAccountByNumber(@PathVariable String accountNumber) {
        log.info("Received request: getAccountByNumber, accountNumber={}", accountNumber);
        Optional<Account> account = accountService.getAccountByAccountNumber(accountNumber);
        if (account.isPresent()) {
            log.info("Completed request: getAccountByNumber, found accountNumber={}", accountNumber);
            return ResponseEntity.ok(account.get());
        } else {
            log.info("Completed request: getAccountByNumber, accountNumber={} not found", accountNumber);
            return ResponseEntity.notFound().build();
        }
    }

    @PreAuthorize("hasRole(T(com.rajeswaran.common.AppConstants).ROLE_BAAS_ADMIN)")
    @PostMapping
    public Account createAccount(@RequestBody Account account) {
        log.info("Received request: createAccount, payload={}", account);
        Account created = accountService.createAccount(account);
        log.info("Completed request: createAccount, account number={}", created.getAccountNumber());
        return created;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/my-accounts")
    public List<Account> getMyAccounts() {
        log.info("Received request: getMyAccounts");
        String username = SecurityUtil.getCurrentUsername();

        List<Account> accounts = accountService.getAccountsByUserName(username);

        log.info("Completed request: getMyAccounts, found {} accounts for user {}", accounts.size(), username);
        return accounts;
    }
}
