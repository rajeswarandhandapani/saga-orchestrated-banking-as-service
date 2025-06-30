package com.rajeswaran.user.service;

import com.rajeswaran.common.AppConstants;
import com.rajeswaran.common.events.UserRegisteredEvent;
import com.rajeswaran.common.util.SagaEventBuilderUtil;
import com.rajeswaran.common.util.SecurityUtil;
import com.rajeswaran.user.entity.User;
import com.rajeswaran.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final StreamBridge streamBridge;

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

        // Extract roles from JWT using the common SecurityUtil method
        Set<String> roles = SecurityUtil.extractRolesFromJwt();

        UserRegisteredEvent event = UserRegisteredEvent.builder()
                .userId(String.valueOf(savedUser.getUserId()))
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .fullName(savedUser.getFullName())
                .timestamp(LocalDateTime.now())
                .details("User registered: " + savedUser.getUsername())
                .correlationId(SagaEventBuilderUtil.getCurrentCorrelationId())
                .serviceName(AppConstants.ServiceName.USER_SERVICE)
                .eventType(AppConstants.SagaEventType.USER_REGISTERED)
                .roles(roles) // Include roles in the event
                .build();

        streamBridge.send("userRegisteredEvent-out-0", event);
        streamBridge.send("auditEvent-out-0", event);

        return savedUser;
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public void deleteUserByUsername(String username) {
        userRepository.findByUsername(username).ifPresent(userRepository::delete);
    }
}
