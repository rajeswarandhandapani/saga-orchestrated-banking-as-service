package com.rajeswaran.user.listener;

import com.rajeswaran.common.model.dto.UserDTO;
import com.rajeswaran.common.useronboarding.commands.CreateUserCommand;
import com.rajeswaran.common.useronboarding.commands.DeleteUserCommand;
import com.rajeswaran.common.useronboarding.events.UserCreatedEvent;
import com.rajeswaran.common.useronboarding.events.UserCreationFailedEvent;
import com.rajeswaran.common.useronboarding.events.UserDeletedEvent;
import com.rajeswaran.common.useronboarding.events.UserDeletionFailedEvent;
import com.rajeswaran.user.entity.User;
import com.rajeswaran.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserCommandListener {

    private final UserService userService;
    private final StreamBridge streamBridge;

    @Bean
    public Consumer<Message<CreateUserCommand>> createUserCommand() {
        return message -> {
            CreateUserCommand command = message.getPayload();
            UserDTO userDTO = command.getUser();
            log.info("Received createUserCommand for saga {} and user: {}", command.getSagaId().value(), userDTO.getUsername());
            
            try {
                User createdUser = userService.createUserFromJwt(userDTO.getUsername(), userDTO.getEmail(), userDTO.getFullName());
                log.info("User {} created successfully for saga {}", userDTO.getUsername(), command.getSagaId().value());
                
                // Publish UserCreatedEvent
                UserCreatedEvent event = UserCreatedEvent.create(
                    command.getSagaId(),
                    command.getCorrelationId(),
                    String.valueOf(createdUser.getUserId()),
                    createdUser.getUsername(),
                    createdUser.getEmail(),
                    createdUser.getFullName()
                );
                
                streamBridge.send("userCreatedEvent-out-0", event);
                log.info("Published UserCreatedEvent for saga {} and userId: {}", command.getSagaId().value(), createdUser.getUserId());
                
            } catch (Exception e) {
                log.error("Failed to create user {} for saga {}: {}", userDTO.getUsername(), command.getSagaId().value(), e.getMessage(), e);
                
                // Publish UserCreationFailedEvent
                UserCreationFailedEvent event = UserCreationFailedEvent.create(
                    command.getSagaId(),
                    command.getCorrelationId(),
                    "Failed to create user: " + e.getMessage()
                );
                
                streamBridge.send("userCreationFailedEvent-out-0", event);
                log.info("Published UserCreationFailedEvent for saga {} and user: {}", command.getSagaId().value(), userDTO.getUsername());
            }
        };
    }

    @Bean
    public Consumer<Message<DeleteUserCommand>> deleteUserCommand() {
        return message -> {
            DeleteUserCommand command = message.getPayload();
            String username = command.getUsername();
            log.info("Received deleteUserCommand for saga {} and user: {} (compensation)", command.getSagaId().value(), username);
            
            try {
                userService.deleteUserByUsername(username);
                log.info("User {} deleted successfully for saga {} (compensation completed)", username, command.getSagaId().value());
                
                // Publish UserDeletedEvent
                UserDeletedEvent event = UserDeletedEvent.create(
                    command.getSagaId(),
                    command.getCorrelationId(),
                    username
                );
                
                streamBridge.send("userDeletedEvent-out-0", event);
                log.info("Published UserDeletedEvent for saga {} and user: {}", command.getSagaId().value(), username);
                
            } catch (Exception e) {
                log.error("Failed to delete user {} for saga {} (compensation failed): {}", username, command.getSagaId().value(), e.getMessage(), e);
                
                // Publish UserDeletionFailedEvent
                UserDeletionFailedEvent event = UserDeletionFailedEvent.create(
                    command.getSagaId(),
                    command.getCorrelationId(),
                    username,
                    "Failed to delete user: " + e.getMessage()
                );
                
                streamBridge.send("userDeletionFailedEvent-out-0", event);
                log.info("Published UserDeletionFailedEvent for saga {} and user: {}", command.getSagaId().value(), username);
            }
        };
    }
}
