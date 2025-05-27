package com.rajeswaran.user.service;

import com.rajeswaran.common.AppConstants;
import com.rajeswaran.common.events.UserRegisteredEvent;
import com.rajeswaran.user.entity.User;
import com.rajeswaran.user.repository.UserRepository;
import org.slf4j.MDC;
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
        String correlationId = MDC.get(AppConstants.CORRELATION_ID_MDC_KEY);
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setFullName(fullName);
        User savedUser = userRepository.save(user);

        UserRegisteredEvent event = UserRegisteredEvent.builder()
                .userId(String.valueOf(savedUser.getId()))
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .fullName(savedUser.getFullName())
                .details("User registered: " + savedUser.getUsername())
                .serviceName(AppConstants.ServiceName.USER_SERVICE)
                .eventType(AppConstants.SagaEventType.USER_REGISTERED)
                .timestamp(Instant.now())
                .correlationId(correlationId)
                .build();

        streamBridge.send("userRegistered-out-0", event);
        streamBridge.send("auditEvent-out-0", event);

        return savedUser;
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
