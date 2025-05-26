package com.rajeswaran.audit.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Instant eventTimestamp;

    @Column(nullable = false)
    private String eventType; // e.g., AccountOpenedEvent, AccountOpeningFailedEvent

    private String userId;

    private String accountId;

    @Column(length = 1024)
    private String details; // Summary or specific message from the event

    private String serviceName; // Service that originated the event or is related to the saga

    @Column(length = 128)
    private String correlationId;
}
