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
 * Command to open an account for a user.
 */
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class OpenAccountCommand extends BaseCommand {
    
    @NotBlank
    private String userId;
    
    @NotBlank
    private String accountType;
    
    public OpenAccountCommand(String commandId, SagaId sagaId, String correlationId, Instant timestamp,
                            String userId, String accountType) {
        super(commandId, sagaId, correlationId, timestamp);
        this.userId = userId;
        this.accountType = accountType;
    }
    
    public static OpenAccountCommand create(SagaId sagaId, String correlationId, 
                                          String userId, String accountType) {
        return new OpenAccountCommand(
            java.util.UUID.randomUUID().toString(),
            sagaId,
            correlationId,
            Instant.now(),
            userId,
            accountType
        );
    }
}
