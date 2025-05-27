package com.rajeswaran.user.service;

import com.rajeswaran.common.events.UserRegisteredEvent;
import com.rajeswaran.user.entity.User;
import com.rajeswaran.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StreamBridge streamBridge;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public User createUser(User user) {
        return userRepository.save(user);
    }

    public User createUserFromJwt(String username, String email, String fullName) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setFullName(fullName);
        User savedUser = userRepository.save(user);
        // Publish UserRegisteredEvent to Kafka
        UserRegisteredEvent event = new UserRegisteredEvent(
            String.valueOf(savedUser.getId()),
            savedUser.getUsername(),
            savedUser.getEmail(),
            savedUser.getFullName(),
            Instant.now()
        );
        streamBridge.send("userRegistered-out-0", event);
        return savedUser;
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
