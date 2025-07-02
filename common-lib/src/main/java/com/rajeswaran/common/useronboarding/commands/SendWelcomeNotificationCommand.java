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
 * Command to send welcome notification to a user.
 */
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class SendWelcomeNotificationCommand extends BaseCommand {
    
    @NotBlank
    private String userId;
    
    @NotBlank
    private String email;
    
    @NotBlank
    private String fullName;

    @NotBlank
    private String accountNumber;
    
    public SendWelcomeNotificationCommand(String commandId, SagaId sagaId, String correlationId, Instant timestamp,
                                        String userId, String email, String fullName, String accountNumber) {
        super(commandId, sagaId, correlationId, timestamp);
        this.userId = userId;
        this.email = email;
        this.fullName = fullName;
        this.accountNumber = accountNumber;
    }
    
    public static SendWelcomeNotificationCommand create(SagaId sagaId, String correlationId, 
                                                      String userId, String email, String fullName, String accountNumber) {
        return new SendWelcomeNotificationCommand(
            java.util.UUID.randomUUID().toString(),
            sagaId,
            correlationId,
            Instant.now(),
            userId,
            email,
            fullName,
            accountNumber
        );
    }
}
