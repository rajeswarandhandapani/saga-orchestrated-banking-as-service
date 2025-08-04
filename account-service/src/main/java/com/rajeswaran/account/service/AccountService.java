package com.rajeswaran.account.service;

import com.rajeswaran.account.repository.AccountRepository;
import com.rajeswaran.common.entity.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;

    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }


    //Get account by account number
    public Optional<Account> getAccountByAccountNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber);
    }

    public Account createAccount(Account account) {
        // Set created timestamp if not already set
        if (account.getCreatedTimestamp() == null) {
            account.setCreatedTimestamp(LocalDateTime.now());
        }
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

    @Transactional
    public void closeAccountByUserId(String userId) {
        accountRepository.deleteByUserId(userId);
    }

    /**
     * Atomically transfers money between two accounts with pessimistic locking.
     * This method ensures that both debit and credit operations happen within a single transaction
     * with proper locking to prevent concurrent access issues.
     * 
     * @param sourceAccountNumber the source account number
     * @param destinationAccountNumber the destination account number
     * @param amount the amount to transfer
     * @return TransferResult containing success status and updated balances
     */
    @Transactional
    public TransferResult transferMoney(String sourceAccountNumber, String destinationAccountNumber, double amount) {
        // Fetch both accounts within the transaction with pessimistic locking
        // Lock accounts in a consistent order to prevent deadlocks (alphabetical order)
        String firstAccount = sourceAccountNumber.compareTo(destinationAccountNumber) < 0 ? 
            sourceAccountNumber : destinationAccountNumber;
        String secondAccount = sourceAccountNumber.compareTo(destinationAccountNumber) < 0 ? 
            destinationAccountNumber : sourceAccountNumber;
            
        // Lock accounts in consistent order to prevent deadlocks
        Optional<Account> firstOpt = accountRepository.findByAccountNumberWithLock(firstAccount);
        Optional<Account> secondOpt = accountRepository.findByAccountNumberWithLock(secondAccount);
        
        // Map back to source and destination
        Optional<Account> sourceOpt = sourceAccountNumber.equals(firstAccount) ? firstOpt : secondOpt;
        Optional<Account> destOpt = destinationAccountNumber.equals(firstAccount) ? firstOpt : secondOpt;
        
        if (sourceOpt.isEmpty()) {
            throw new IllegalArgumentException("Source account not found: " + sourceAccountNumber);
        }
        if (destOpt.isEmpty()) {
            throw new IllegalArgumentException("Destination account not found: " + destinationAccountNumber);
        }
        
        Account sourceAccount = sourceOpt.get();
        Account destAccount = destOpt.get();
        
        // Validate account status
        if (!"ACTIVE".equalsIgnoreCase(sourceAccount.getStatus())) {
            throw new IllegalStateException("Source account is not active: " + sourceAccountNumber);
        }
        if (!"ACTIVE".equalsIgnoreCase(destAccount.getStatus())) {
            throw new IllegalStateException("Destination account is not active: " + destinationAccountNumber);
        }
        
        // Check sufficient balance (now with locked balance)
        if (sourceAccount.getBalance() < amount) {
            throw new IllegalStateException("Insufficient balance. Available: " + sourceAccount.getBalance() + ", Required: " + amount);
        }
        
        // Perform the transfer atomically
        sourceAccount.setBalance(sourceAccount.getBalance() - amount);
        destAccount.setBalance(destAccount.getBalance() + amount);
        
        // Save both accounts - if either fails, entire transaction rolls back
        accountRepository.save(sourceAccount);
        accountRepository.save(destAccount);
        
        return new TransferResult(true, sourceAccount.getBalance(), destAccount.getBalance());
    }

    /**
     * Alternative transfer method using optimistic locking instead of pessimistic locking.
     * This approach is more performant but may require retry logic in case of concurrent access.
     * 
     * @param sourceAccountNumber the source account number
     * @param destinationAccountNumber the destination account number
     * @param amount the amount to transfer
     * @param maxRetries maximum number of retry attempts for optimistic lock failures
     * @return TransferResult containing success status and updated balances
     */
    @Transactional
    public TransferResult transferMoneyOptimistic(String sourceAccountNumber, String destinationAccountNumber, 
                                                 double amount, int maxRetries) {
        int attempts = 0;
        
        while (attempts <= maxRetries) {
            try {
                // Fetch both accounts (optimistic locking via @Version)
                Optional<Account> sourceOpt = getAccountByAccountNumber(sourceAccountNumber);
                Optional<Account> destOpt = getAccountByAccountNumber(destinationAccountNumber);
                
                if (sourceOpt.isEmpty()) {
                    throw new IllegalArgumentException("Source account not found: " + sourceAccountNumber);
                }
                if (destOpt.isEmpty()) {
                    throw new IllegalArgumentException("Destination account not found: " + destinationAccountNumber);
                }
                
                Account sourceAccount = sourceOpt.get();
                Account destAccount = destOpt.get();
                
                // Validate account status
                if (!"ACTIVE".equalsIgnoreCase(sourceAccount.getStatus())) {
                    throw new IllegalStateException("Source account is not active: " + sourceAccountNumber);
                }
                if (!"ACTIVE".equalsIgnoreCase(destAccount.getStatus())) {
                    throw new IllegalStateException("Destination account is not active: " + destinationAccountNumber);
                }
                
                // Check sufficient balance
                if (sourceAccount.getBalance() < amount) {
                    throw new IllegalStateException("Insufficient balance. Available: " + sourceAccount.getBalance() + ", Required: " + amount);
                }
                
                // Perform the transfer
                sourceAccount.setBalance(sourceAccount.getBalance() - amount);
                destAccount.setBalance(destAccount.getBalance() + amount);
                
                // Save both accounts - @Version will handle optimistic locking
                accountRepository.save(sourceAccount);
                accountRepository.save(destAccount);
                
                return new TransferResult(true, sourceAccount.getBalance(), destAccount.getBalance());
                
            } catch (org.springframework.orm.ObjectOptimisticLockingFailureException e) {
                attempts++;
                if (attempts > maxRetries) {
                    throw new IllegalStateException("Transfer failed after " + maxRetries + " retries due to concurrent access. Please try again.", e);
                }
                // Brief pause before retry to reduce contention
                try {
                    Thread.sleep(10 + (attempts * 5)); // Progressive backoff
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Transfer interrupted during retry", ie);
                }
            }
        }
        
        throw new IllegalStateException("Transfer failed unexpectedly");
    }

    /**
     * Result object for money transfer operations.
     */
    public static class TransferResult {
        private final boolean success;
        private final double sourceBalance;
        private final double destinationBalance;
        
        public TransferResult(boolean success, double sourceBalance, double destinationBalance) {
            this.success = success;
            this.sourceBalance = sourceBalance;
            this.destinationBalance = destinationBalance;
        }
        
        public boolean isSuccess() { return success; }
        public double getSourceBalance() { return sourceBalance; }
        public double getDestinationBalance() { return destinationBalance; }
    }
}
