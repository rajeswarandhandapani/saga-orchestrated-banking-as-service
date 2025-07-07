package com.rajeswaran.common.saga.useronboarding.commands;

import com.rajeswaran.common.saga.command.BaseCommand;
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

    public DeleteUserCommand(String commandId, Long sagaId, Instant timestamp, String username) {
        super(commandId, sagaId, timestamp);
        this.username = username;
    }

    public static DeleteUserCommand create(Long sagaId, String username) {
        return new DeleteUserCommand(
            java.util.UUID.randomUUID().toString(),
            sagaId,
            Instant.now(),
            username
        );
    }
}
