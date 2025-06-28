package com.rajeswaran.audit.controller;

import com.rajeswaran.audit.entity.AuditLog;
import com.rajeswaran.audit.service.AuditLogService;
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
@RequestMapping("/api/audit-logs")
@PreAuthorize("hasRole(T(com.rajeswaran.common.AppConstants).ROLE_BAAS_ADMIN)")
@RequiredArgsConstructor
public class AuditLogController {
    private final AuditLogService auditLogService;

    @GetMapping
    public List<AuditLog> getAllLogs() {
        log.info("Received request: getAllLogs");
        List<AuditLog> logs = auditLogService.getAllLogs();
        log.info("Completed request: getAllLogs, count={}", logs.size());
        return logs;
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuditLog> getLogById(@PathVariable Long id) {
        log.info("Received request: getLogById, id={}", id);
        Optional<AuditLog> logOpt = auditLogService.getLogById(id);
        if (logOpt.isPresent()) {
            log.info("Completed request: getLogById, found logId={}", id);
            return ResponseEntity.ok(logOpt.get());
        } else {
            log.info("Completed request: getLogById, logId={} not found", id);
            return ResponseEntity.notFound().build();
        }
    }
}
