package com.rajeswaran.account.service;

import com.rajeswaran.account.entity.Account;
import com.rajeswaran.account.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AccountService {
    @Autowired
    private AccountRepository accountRepository;

    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }


    //Get account by account number
    public Optional<Account> getAccountByAccountNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber);
    }

    public Account createAccount(Account account) {
        // mimic exception for testing
         /*if (!account.getAccountNumber().equals("1234567890")) {
             throw new RuntimeException("Simulated exception for testing");
         }*/
        return accountRepository.save(account);
    }

    public void deleteAccount(String id) {
        accountRepository.deleteById(id);
    }

    // Validate if account exists and has sufficient balance
    public boolean validateSourceAccount(String accountNumber, double amount) {
        Optional<Account> accountOpt = getAccountByAccountNumber(accountNumber);
        if (accountOpt.isPresent()) {
            Account account = accountOpt.get();
            return account.getBalance() >= amount;
        }
        return false;
    }

    // Deduct amount from account balance
    public boolean deductFromAccount(String accountNumber, double amount) {
        Optional<Account> accountOpt = getAccountByAccountNumber(accountNumber);
        if (accountOpt.isPresent()) {
            Account account = accountOpt.get();
            if (account.getBalance() >= amount) {
                account.setBalance(account.getBalance() - amount);
                accountRepository.save(account);
                return true;
            }
        }
        return false;
    }

    // Add amount to account balance
    public boolean addToAccount(String accountNumber, double amount) {
        Optional<Account> accountOpt = getAccountByAccountNumber(accountNumber);
        if (accountOpt.isPresent()) {
            Account account = accountOpt.get();
            account.setBalance(account.getBalance() + amount);
            accountRepository.save(account);
            return true;
        }
        return false;
    }

    // Get accounts by user ID
    public List<Account> getAccountsByUserId(String userId) {
        return accountRepository.findByUserId(userId);
    }

    // Get accounts by user name
    public List<Account> getAccountsByUserName(String userName) {
        return accountRepository.findByUserName(userName);
    }

    // Validate if account belongs to the specified user
    public boolean validateAccountOwnership(String accountNumber, String userName) {
        Optional<Account> accountOpt = getAccountByAccountNumber(accountNumber);
        if (accountOpt.isPresent()) {
            Account account = accountOpt.get();
            return account.getUserName().equals(userName);
        }
        return false;
    }
}
