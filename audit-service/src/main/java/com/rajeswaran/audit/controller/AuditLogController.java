package com.rajeswaran.audit.controller;

import com.rajeswaran.audit.entity.AuditLog;
import com.rajeswaran.audit.service.AuditLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {
    @Autowired
    private AuditLogService auditLogService;

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

    @PostMapping
    public AuditLog createLog(@RequestBody AuditLog logObj) {
        log.info("Received request: createLog, payload={}", logObj);
        AuditLog created = auditLogService.createLog(logObj);
        log.info("Completed request: createLog, createdId={}", created.getId());
        return created;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLog(@PathVariable Long id) {
        log.info("Received request: deleteLog, id={}", id);
        auditLogService.deleteLog(id);
        log.info("Completed request: deleteLog, id={}", id);
        return ResponseEntity.noContent().build();
    }
}
