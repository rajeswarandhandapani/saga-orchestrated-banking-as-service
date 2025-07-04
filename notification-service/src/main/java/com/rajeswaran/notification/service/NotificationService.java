package com.rajeswaran.notification.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.rajeswaran.common.entity.Notification;
import com.rajeswaran.common.saga.notification.commands.SendNotificationCommand;
import com.rajeswaran.notification.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;

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
                command.getEmail(),
                command.getSubject(),
                command.getMessage(),
                LocalDateTime.now()
        );
        notificationRepository.save(notification);
    }


}
