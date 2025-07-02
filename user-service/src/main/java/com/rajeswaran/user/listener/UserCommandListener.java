package com.rajeswaran.user.listener;

import com.rajeswaran.common.model.dto.UserDTO;
import com.rajeswaran.common.useronboarding.commands.CreateUserCommand;
import com.rajeswaran.common.useronboarding.commands.DeleteUserCommand;
import com.rajeswaran.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserCommandListener {

    private final UserService userService;

    @Bean
    public Function<Message<CreateUserCommand>, Message<String>> createUserCommand() {
        return message -> {
            CreateUserCommand command = message.getPayload();
            UserDTO userDTO = command.getUser();
            log.info("Received createUserCommand for user: {}", userDTO.getUsername());
            try {
                userService.createUserFromJwt(userDTO.getUsername(), userDTO.getEmail(), userDTO.getFullName());
                log.info("User {} created successfully", userDTO.getUsername());
                return MessageBuilder.withPayload("SUCCESS").build();
            } catch (Exception e) {
                log.error("Failed to create user: {}", userDTO.getUsername(), e);
                return MessageBuilder.withPayload("FAILURE").build();
            }
        };
    }

    @Bean
    public Function<Message<DeleteUserCommand>, Message<String>> deleteUserCommand() {
        return message -> {
            DeleteUserCommand command = message.getPayload();
            String username = command.getUsername();
            log.info("Received deleteUserCommand for user: {}", username);
            try {
                userService.deleteUserByUsername(username);
                log.info("User {} deleted successfully for compensation", username);
                return MessageBuilder.withPayload("SUCCESS").build();
            } catch (Exception e) {
                log.error("Failed to delete user for compensation: {}", username, e);
                // Even if deletion fails, we should not block the saga.
                // The system should have other means to handle this (e.g., manual intervention).
                return MessageBuilder.withPayload("SUCCESS").build();
            }
        };
    }
}
