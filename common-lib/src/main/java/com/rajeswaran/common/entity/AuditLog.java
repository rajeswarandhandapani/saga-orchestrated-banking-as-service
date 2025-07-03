package com.rajeswaran.common.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String username;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(length = 1024)
    private String details; // Summary or specific message from the event

    @Column(length = 128)
    private String correlationId;

    private String serviceName; // Service that originated the event or is related to the saga

    @Column(nullable = false)
    private String eventType; // e.g., AccountOpenedEvent, AccountOpeningFailedEvent
}
