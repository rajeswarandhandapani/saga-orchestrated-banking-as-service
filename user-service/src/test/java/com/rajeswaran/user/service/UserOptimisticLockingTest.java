package com.rajeswaran.user.service;

import com.rajeswaran.common.entity.User;
import com.rajeswaran.user.listener.UserCommandListener;
import com.rajeswaran.user.repository.UserRepository;
import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ContextConfiguration;

@DataJpaTest
@ContextConfiguration(classes = {})
public class UserOptimisticLockingTest {

    @MockitoBean
    private UserCommandListener userCommandListener;
    @Autowired
    private UserRepository userRepository;

    @Test
    void testOptimisticLocking() {
        // Create and save a user
        User user = User.builder()
                .username("locktest")
                .email("locktest@example.com")
                .fullName("Lock Test")
                .build();
        user = userRepository.save(user);

        // Simulate two concurrent loads
        User user1 = userRepository.findById(user.getUserId()).orElseThrow();
        User user2 = userRepository.findById(user.getUserId()).orElseThrow();

        // First update succeeds
        user1.setFullName("User One Update");
        userRepository.save(user1);

        // Second update should fail
        user2.setFullName("User Two Update");
        Assertions.assertThrows(OptimisticLockException.class, () -> {
            userRepository.saveAndFlush(user2);
        });
    }
}
