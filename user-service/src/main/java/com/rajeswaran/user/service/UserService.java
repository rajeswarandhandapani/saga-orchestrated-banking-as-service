package com.rajeswaran.user.service;

import com.rajeswaran.common.AppConstants;
import com.rajeswaran.common.events.UserRegisteredEvent;
import com.rajeswaran.common.util.SagaEventBuilderUtil;
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


    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }


    public User createUserFromJwt(String username, String email, String fullName) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setFullName(fullName);
        User savedUser = userRepository.save(user);

        UserRegisteredEvent event = UserRegisteredEvent.builder()
                .userId(String.valueOf(savedUser.getUserId()))
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .fullName(savedUser.getFullName())
                .timestamp(Instant.now())
                .details("User registered: " + savedUser.getUsername())
                .correlationId(SagaEventBuilderUtil.getCurrentCorrelationId())
                .serviceName(AppConstants.ServiceName.USER_SERVICE)
                .eventType(AppConstants.SagaEventType.USER_REGISTERED)
                .build();

        streamBridge.send("userRegisteredEvent-out-0", event);
        streamBridge.send("auditEvent-out-0", event);

        return savedUser;
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
