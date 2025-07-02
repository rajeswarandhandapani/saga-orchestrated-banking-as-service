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
public class SendNotificationCommand extends BaseCommand {
    
    @NotBlank
    private String email;
    
    @NotBlank
    private String subject;

    @NotBlank
    private String message;

    public SendNotificationCommand(String commandId, SagaId sagaId, String correlationId, Instant timestamp, String email, String subject, String message) {
        super(commandId, sagaId, correlationId, timestamp);
        this.email = email;
        this.subject = subject;
        this.message = message;
    }

    public static SendNotificationCommand create(SagaId sagaId, String correlationId, String email, String subject, String message) {
        return new SendNotificationCommand(
            java.util.UUID.randomUUID().toString(),
            sagaId,
            correlationId,
            Instant.now(),
            email,
            subject,
            message
        );
    }
}
