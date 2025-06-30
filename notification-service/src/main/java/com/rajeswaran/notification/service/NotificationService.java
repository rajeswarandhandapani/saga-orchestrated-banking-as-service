package com.rajeswaran.notification.service;

import com.rajeswaran.common.model.command.CancelNotificationCommand;
import com.rajeswaran.common.model.command.SendNotificationCommand;
import com.rajeswaran.notification.entity.Notification;
import com.rajeswaran.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll();
    }

    public Optional<Notification> getNotificationById(Long id) {
        return notificationRepository.findById(id);
    }

    public Notification createNotification(Notification notification) {
        return notificationRepository.save(notification);
    }

    public void deleteNotification(Long id) {
        notificationRepository.deleteById(id);
    }

    public void sendNotification(SendNotificationCommand command) {
        Notification notification = new Notification(
                null,
                command.getUser().getUsername(),
                command.getNotificationType(),
                command.getMessage(),
                "NEW",
                command.getReferenceId(),
                LocalDateTime.now()
        );
        notificationRepository.save(notification);
    }

    public void cancelNotification(CancelNotificationCommand command) {
        notificationRepository.findByReferenceId(command.getReferenceId()).ifPresent(notification -> {
            notification.setStatus("CANCELLED");
            notificationRepository.save(notification);
        });
    }

    public <T> Message<String> buildReply(Message<T> message, String replyType) {
        String correlationId = (String) message.getHeaders().get("correlationId");
        return MessageBuilder.withPayload("SUCCESS")
                .setHeader("correlationId", correlationId)
                .setHeader("replyType", replyType)
                .build();
    }
}
