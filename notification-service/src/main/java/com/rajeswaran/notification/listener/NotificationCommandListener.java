package com.rajeswaran.notification.listener;

import com.rajeswaran.common.useronboarding.commands.SendNotificationCommand;
import com.rajeswaran.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationCommandListener {

    private final NotificationService notificationService;

    @Bean
    public Consumer<Message<SendNotificationCommand>> sendNotificationCommand() {
        return message -> {
            SendNotificationCommand command = message.getPayload();
            log.info("Received send notification command: {}", command);
            try {
                notificationService.sendNotification(command);
            } catch (Exception e) {
                log.error("Failed to send notification for command: {}", command, e);
            }
        };
    }

}
