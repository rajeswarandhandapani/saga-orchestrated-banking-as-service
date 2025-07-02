package com.rajeswaran.common.useronboarding.commands;

import com.rajeswaran.common.command.BaseCommand;
import com.rajeswaran.common.saga.SagaId;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

/**
 * Command to delete a user in the user service (compensation action).
 */
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class DeleteUserCommand extends BaseCommand {
    
    @NotBlank
    private String username;

    public DeleteUserCommand(String commandId, SagaId sagaId, String correlationId, Instant timestamp, String username) {
        super(commandId, sagaId, correlationId, timestamp);
        this.username = username;
    }

    public static DeleteUserCommand create(SagaId sagaId, String correlationId, String username) {
        return new DeleteUserCommand(
            java.util.UUID.randomUUID().toString(),
            sagaId,
            correlationId,
            Instant.now(),
            username
        );
    }
}
