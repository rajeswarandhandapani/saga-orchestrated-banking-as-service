package com.rajeswaran.user.listener;

import com.rajeswaran.common.entity.User;
import com.rajeswaran.common.useronboarding.commands.CreateUserCommand;
import com.rajeswaran.common.useronboarding.commands.DeleteUserCommand;
import com.rajeswaran.common.useronboarding.events.UserCreatedEvent;
import com.rajeswaran.common.useronboarding.events.UserCreationFailedEvent;
import com.rajeswaran.common.useronboarding.events.UserDeletedEvent;
import com.rajeswaran.common.useronboarding.events.UserDeletionFailedEvent;
import com.rajeswaran.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserCommandListener {

    private final UserService userService;
    private final StreamBridge streamBridge;

    @Bean
    public Consumer<CreateUserCommand> createUserCommand() {
        return command -> {
            User user = command.getUser();
            log.info("Received createUserCommand for saga {} and user: {}", command.getSagaId().value(), user.getUsername());
            
            try {
                User createdUser = userService.createUserFromJwt(user.getUsername(), user.getEmail(), user.getFullName());
                log.info("User {} created successfully for saga {}", user.getUsername(), command.getSagaId().value());
                
                UserCreatedEvent event = UserCreatedEvent.create(
                    command.getSagaId(),
                    command.getCorrelationId(),
                    createdUser
                );
                
                streamBridge.send("userCreatedEvent-out-0", event);
                log.info("Published UserCreatedEvent for saga {} and userId: {}", command.getSagaId().value(), createdUser.getUserId());
                
            } catch (Exception e) {
                log.error("Failed to create user {} for saga {}: {}", user.getUsername(), command.getSagaId().value(), e.getMessage(), e);
                
                // Publish UserCreationFailedEvent
                UserCreationFailedEvent event = UserCreationFailedEvent.create(
                    command.getSagaId(),
                    command.getCorrelationId(),
                    "Failed to create user: " + e.getMessage()
                );
                
                streamBridge.send("userCreationFailedEvent-out-0", event);
                log.info("Published UserCreationFailedEvent for saga {} and user: {}", command.getSagaId().value(), user.getUsername());
            }
        };
    }

    @Bean
    public Consumer<DeleteUserCommand> deleteUserCommand() {
        return command -> {
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
