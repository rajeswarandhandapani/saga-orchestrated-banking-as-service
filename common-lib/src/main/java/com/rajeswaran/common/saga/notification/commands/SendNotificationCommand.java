package com.rajeswaran.common.saga.notification.commands;

import com.rajeswaran.common.saga.command.BaseCommand;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

/**
 * Command to send notification to a user.
 * This is a shared command that can be used by any saga for sending notifications.
 */
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class SendNotificationCommand extends BaseCommand {

    @NotBlank
    private String userName;

    @NotBlank
    private String subject;

    @NotBlank
    private String message;

    public SendNotificationCommand(String commandId, Long sagaId, String correlationId, Instant timestamp, String userName, String subject, String message) {
        super(commandId, sagaId, correlationId, timestamp);
        this.userName = userName;
        this.subject = subject;
        this.message = message;
    }

    public static SendNotificationCommand create(Long sagaId, String correlationId, String userName, String subject, String message) {
        return new SendNotificationCommand(
            java.util.UUID.randomUUID().toString(),
            sagaId,
            correlationId,
            Instant.now(),
            userName,
            subject,
            message
        );
    }
}
