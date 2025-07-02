package com.rajeswaran.common.useronboarding.commands;

import com.rajeswaran.common.command.BaseCommand;
import com.rajeswaran.common.entity.User;
import com.rajeswaran.common.saga.SagaId;
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
    
    public OpenAccountCommand(String commandId, SagaId sagaId, String correlationId, Instant timestamp,
                            String accountType, User user) {
        super(commandId, sagaId, correlationId, timestamp);
        this.accountType = accountType;
        this.user = user;
    }
    
    public static OpenAccountCommand create(SagaId sagaId, String correlationId, 
                                          String accountType, User user) {
        return new OpenAccountCommand(
            java.util.UUID.randomUUID().toString(),
            sagaId,
            correlationId,
            Instant.now(),
            accountType,
            user
        );
    }
}
