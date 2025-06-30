package com.rajeswaran.account.repository;

import com.rajeswaran.account.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {

    Optional<Account> findByAccountNumber(String accountNumber);

    List<Account> findByUserId(String userId);

    List<Account> findByUserName(String userName);

    void deleteByUserId(String userId);
}
