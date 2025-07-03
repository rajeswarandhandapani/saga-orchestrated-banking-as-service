package com.rajeswaran.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rajeswaran.common.entity.Notification;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
