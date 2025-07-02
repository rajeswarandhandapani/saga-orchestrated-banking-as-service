package com.rajeswaran.common.useronboarding.commands;

import com.rajeswaran.common.command.BaseCommand;
import com.rajeswaran.common.model.dto.UserDTO;
import com.rajeswaran.common.saga.SagaId;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

/**
 * Command to create a new user in the user service.
 */
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class CreateUserCommand extends BaseCommand {
    
    @NotBlank
    private UserDTO user;
    
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
