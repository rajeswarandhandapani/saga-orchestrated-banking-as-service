package com.rajeswaran.common.useronboarding.commands;

import com.rajeswaran.common.command.BaseCommand;
import com.rajeswaran.common.model.dto.UserDTO;
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
    private String userId;
    
    @NotBlank
    private String accountType;
    
    @NotNull
    private UserDTO userDto;
    
    public OpenAccountCommand(String commandId, SagaId sagaId, String correlationId, Instant timestamp,
                            String userId, String accountType, UserDTO userDto) {
        super(commandId, sagaId, correlationId, timestamp);
        this.userId = userId;
        this.accountType = accountType;
        this.userDto = userDto;
    }
    
    public static OpenAccountCommand create(SagaId sagaId, String correlationId, 
                                          String userId, String accountType, UserDTO userDto) {
        return new OpenAccountCommand(
            java.util.UUID.randomUUID().toString(),
            sagaId,
            correlationId,
            Instant.now(),
            userId,
            accountType,
            userDto
        );
    }
}
