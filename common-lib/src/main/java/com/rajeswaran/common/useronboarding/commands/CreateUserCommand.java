package com.rajeswaran.common.useronboarding.commands;

import com.rajeswaran.common.command.BaseCommand;
import com.rajeswaran.common.saga.SagaId;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

import java.time.Instant;

/**
 * Command to create a new user in the user service.
 */
@Getter
public class CreateUserCommand extends BaseCommand {
    
    @NotBlank
    private final String username;
    
    @Email
    private final String email;
    
    @NotBlank
    private final String fullName;
    
    @NotBlank
    private final String password;
    
    public CreateUserCommand(String commandId, SagaId sagaId, String correlationId, Instant timestamp,
                           String username, String email, String fullName, String password) {
        super(commandId, sagaId, correlationId, timestamp);
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.password = password;
    }
    
    public static CreateUserCommand create(SagaId sagaId, String correlationId, 
                                         String username, String email, String fullName, String password) {
        return new CreateUserCommand(
            java.util.UUID.randomUUID().toString(),
            sagaId,
            correlationId,
            Instant.now(),
            username,
            email,
            fullName,
            password
        );
    }
}
