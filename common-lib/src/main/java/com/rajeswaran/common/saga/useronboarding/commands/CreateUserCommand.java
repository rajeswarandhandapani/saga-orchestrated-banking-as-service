package com.rajeswaran.common.saga.useronboarding.commands;

import com.rajeswaran.common.saga.command.BaseCommand;
import com.rajeswaran.common.entity.User;
import jakarta.validation.constraints.NotNull;
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
    
    @NotNull
    private User user;
    
    public CreateUserCommand(String commandId, Long sagaId, Instant timestamp, User user) {
        super(commandId, sagaId, timestamp);
        this.user = user;
    }
    
    public static CreateUserCommand create(Long sagaId, User user) {
        return new CreateUserCommand(
            java.util.UUID.randomUUID().toString(),
            sagaId,
            Instant.now(),
            user
        );
    }
}
