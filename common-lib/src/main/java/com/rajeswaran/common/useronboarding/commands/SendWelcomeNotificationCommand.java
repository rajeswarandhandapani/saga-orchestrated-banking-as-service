package com.rajeswaran.common.useronboarding.commands;

import com.rajeswaran.common.command.BaseCommand;
import com.rajeswaran.common.saga.SagaId;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

import java.time.Instant;

/**
 * Command to send welcome notification to a user.
 */
@Getter
public class SendWelcomeNotificationCommand extends BaseCommand {
    
    @NotBlank
    private final String userId;
    
    @NotBlank
    private final String email;
    
    @NotBlank
    private final String fullName;
    
    public SendWelcomeNotificationCommand(String commandId, SagaId sagaId, String correlationId, Instant timestamp,
                                        String userId, String email, String fullName) {
        super(commandId, sagaId, correlationId, timestamp);
        this.userId = userId;
        this.email = email;
        this.fullName = fullName;
    }
    
    public static SendWelcomeNotificationCommand create(SagaId sagaId, String correlationId, 
                                                      String userId, String email, String fullName) {
        return new SendWelcomeNotificationCommand(
            java.util.UUID.randomUUID().toString(),
            sagaId,
            correlationId,
            Instant.now(),
            userId,
            email,
            fullName
        );
    }
}
