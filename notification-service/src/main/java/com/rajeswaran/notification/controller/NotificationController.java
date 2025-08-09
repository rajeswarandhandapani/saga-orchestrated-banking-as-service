package com.rajeswaran.notification.controller;

import com.rajeswaran.common.entity.Notification;
import com.rajeswaran.common.util.SecurityUtil;
import com.rajeswaran.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;
    
    @GetMapping
    @PreAuthorize("hasRole(T(com.rajeswaran.common.AppConstants).ROLE_BAAS_ADMIN)")
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

    @GetMapping("/my-notifications")
    public ResponseEntity<List<Notification>> getMyNotifications() {
        log.info("Received request: getMyNotifications");
        String username = SecurityUtil.getCurrentUsername();
        List<Notification> notifications = notificationService.getNotificationByUserName(username);
        log.info("Completed request: getMyNotifications, count={}", notifications.size());
        return ResponseEntity.ok(notifications);
    }

}
