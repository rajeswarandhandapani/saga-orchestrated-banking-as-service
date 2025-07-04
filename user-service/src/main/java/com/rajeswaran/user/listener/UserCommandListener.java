package com.rajeswaran.user.listener;

import com.rajeswaran.common.entity.User;
import com.rajeswaran.common.saga.useronboarding.commands.CreateUserCommand;
import com.rajeswaran.common.saga.useronboarding.commands.DeleteUserCommand;
import com.rajeswaran.common.saga.useronboarding.events.UserCreatedEvent;
import com.rajeswaran.common.saga.useronboarding.events.UserCreationFailedEvent;
import com.rajeswaran.common.saga.useronboarding.events.UserDeletedEvent;
import com.rajeswaran.common.saga.useronboarding.events.UserDeletionFailedEvent;
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
            log.info("Received createUserCommand for saga {} and user: {}", command.getSagaId(), user.getUsername());
            
            try {
                User createdUser = userService.createUser(user);
                log.info("User {} created successfully for saga {}", user.getUsername(), command.getSagaId());
                
                UserCreatedEvent event = UserCreatedEvent.create(
                    command.getSagaId(),
                    command.getCorrelationId(),
                    createdUser
                );
                
                streamBridge.send("userCreatedEvent-out-0", event);
                log.info("Published UserCreatedEvent for saga {} and userId: {}", command.getSagaId(), createdUser.getUserId());
                
            } catch (Exception e) {
                log.error("Failed to create user {} for saga {}: {}", user.getUsername(), command.getSagaId(), e.getMessage(), e);
                
                // Publish UserCreationFailedEvent
                UserCreationFailedEvent event = UserCreationFailedEvent.create(
                    command.getSagaId(),
                    command.getCorrelationId(),
                    "Failed to create user: " + e.getMessage()
                );
                
                streamBridge.send("userCreationFailedEvent-out-0", event);
                log.info("Published UserCreationFailedEvent for saga {} and user: {}", command.getSagaId(), user.getUsername());
            }
        };
    }

    @Bean
    public Consumer<DeleteUserCommand> deleteUserCommand() {
        return command -> {
            String username = command.getUsername();
            log.info("Received deleteUserCommand for saga {} and user: {} (compensation)", command.getSagaId(), username);
            
            try {
                userService.deleteUserByUsername(username);
                log.info("User {} deleted successfully for saga {} (compensation completed)", username, command.getSagaId());
                
                // Publish UserDeletedEvent
                UserDeletedEvent event = UserDeletedEvent.create(
                    command.getSagaId(),
                    command.getCorrelationId(),
                    username
                );
                
                streamBridge.send("userDeletedEvent-out-0", event);
                log.info("Published UserDeletedEvent for saga {} and user: {}", command.getSagaId(), username);
                
            } catch (Exception e) {
                log.error("Failed to delete user {} for saga {} (compensation failed): {}", username, command.getSagaId(), e.getMessage(), e);
                
                // Publish UserDeletionFailedEvent
                UserDeletionFailedEvent event = UserDeletionFailedEvent.create(
                    command.getSagaId(),
                    command.getCorrelationId(),
                    username,
                    "Failed to delete user: " + e.getMessage()
                );
                
                streamBridge.send("userDeletionFailedEvent-out-0", event);
                log.info("Published UserDeletionFailedEvent for saga {} and user: {}", command.getSagaId(), username);
            }
        };
    }
}
