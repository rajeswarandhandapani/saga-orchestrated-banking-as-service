package com.rajeswaran.common.saga.useronboarding.commands;

import com.rajeswaran.common.saga.command.BaseCommand;
import com.rajeswaran.common.entity.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

/**
 * Command to open an account for a user.
 */
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class OpenAccountCommand extends BaseCommand {
    
    @NotBlank
    private String accountType;
    
    @NotNull
    private User user;
    
    public OpenAccountCommand(String commandId, Long sagaId, Instant timestamp,
                            String accountType, User user) {
        super(commandId, sagaId, timestamp);
        this.accountType = accountType;
        this.user = user;
    }
    
    public static OpenAccountCommand create(Long sagaId, String accountType, User user) {
        return new OpenAccountCommand(
            java.util.UUID.randomUUID().toString(),
            sagaId,
            Instant.now(),
            accountType,
            user
        );
    }
}
