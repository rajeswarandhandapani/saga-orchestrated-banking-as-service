package com.rajeswaran.notification.listener;

import com.rajeswaran.common.model.command.CancelNotificationCommand;
import com.rajeswaran.common.model.command.SendNotificationCommand;
import com.rajeswaran.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationCommandListener {

    private final NotificationService notificationService;

    @Bean
    public Function<Message<SendNotificationCommand>, Message<String>> sendNotificationCommand() {
        return message -> {
            SendNotificationCommand command = message.getPayload();
            log.info("Received send notification command: {}", command);
            try {
                notificationService.sendNotification(command);
                return notificationService.buildReply(message, "NotificationSent");
            } catch (Exception e) {
                log.error("Failed to send notification for command: {}", command, e);
                return notificationService.buildReply(message, "NotificationFailed");
            }
        };
    }

    @Bean
    public Function<Message<CancelNotificationCommand>, Message<String>> cancelNotificationCommand() {
        return message -> {
            CancelNotificationCommand command = message.getPayload();
            log.info("Received cancel notification command: {}", command);
            // Implement compensation logic, e.g., marking a notification as cancelled.
            // For now, we'll just log it.
            notificationService.cancelNotification(command);
            return notificationService.buildReply(message, "NotificationCancelled");
        };
    }
}
