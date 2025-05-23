package com.rajeswaran.notification.controller;

import com.rajeswaran.notification.entity.Notification;
import com.rajeswaran.notification.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public List<Notification> getAllNotifications() {
        log.info("Received request: getAllNotifications");
        List<Notification> notifications = notificationService.getAllNotifications();
        log.info("Completed request: getAllNotifications, count={}", notifications.size());
        return notifications;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Notification> getNotificationById(@PathVariable Long id) {
        log.info("Received request: getNotificationById, id={}", id);
        Optional<Notification> notification = notificationService.getNotificationById(id);
        if (notification.isPresent()) {
            log.info("Completed request: getNotificationById, found notificationId={}", id);
            return ResponseEntity.ok(notification.get());
        } else {
            log.info("Completed request: getNotificationById, notificationId={} not found", id);
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public Notification createNotification(@RequestBody Notification notification) {
        log.info("Received request: createNotification, payload={}", notification);
        Notification created = notificationService.createNotification(notification);
        log.info("Completed request: createNotification, createdId={}", created.getId());
        return created;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        log.info("Received request: deleteNotification, id={}", id);
        notificationService.deleteNotification(id);
        log.info("Completed request: deleteNotification, id={}", id);
        return ResponseEntity.noContent().build();
    }
}
