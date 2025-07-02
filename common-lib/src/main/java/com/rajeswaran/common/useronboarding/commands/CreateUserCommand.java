package com.rajeswaran.common.useronboarding.commands;

import com.rajeswaran.common.command.BaseCommand;
import com.rajeswaran.common.model.dto.UserDTO;
import com.rajeswaran.common.saga.SagaId;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.ToString;

import java.time.Instant;

/**
 * Command to create a new user in the user service.
 */
@Getter
@ToString
public class CreateUserCommand extends BaseCommand {
    
    @NotBlank
    private final UserDTO user;
    
    public CreateUserCommand(String commandId, SagaId sagaId, String correlationId, Instant timestamp, UserDTO user) {
        super(commandId, sagaId, correlationId, timestamp);
        this.user = user;
    }
    
    public static CreateUserCommand create(SagaId sagaId, String correlationId, UserDTO user) {
        return new CreateUserCommand(
            java.util.UUID.randomUUID().toString(),
            sagaId,
            correlationId,
            Instant.now(),
            user
        );
    }
}
