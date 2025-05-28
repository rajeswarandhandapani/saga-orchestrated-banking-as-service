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
}
