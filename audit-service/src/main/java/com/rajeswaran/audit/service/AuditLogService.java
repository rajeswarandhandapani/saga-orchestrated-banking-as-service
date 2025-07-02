package com.rajeswaran.audit.service;

import com.rajeswaran.common.entity.AuditLog;
import com.rajeswaran.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuditLogService {
    private final AuditLogRepository auditLogRepository;

    public List<AuditLog> getAllLogs() {
        return auditLogRepository.findAll();
    }

    public Optional<AuditLog> getLogById(Long id) {
        return auditLogRepository.findById(id);
    }

    public AuditLog createLog(AuditLog log) {
        // The eventTimestamp should be set by the caller (AuditEventListener)
        // based on the actual event's timestamp.
        return auditLogRepository.save(log);
    }

    public void deleteLog(Long id) {
        auditLogRepository.deleteById(id);
    }
}
