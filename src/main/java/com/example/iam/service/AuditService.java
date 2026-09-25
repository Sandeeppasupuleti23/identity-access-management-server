package com.example.iam.service;

import com.example.iam.entity.AuditLog;
import com.example.iam.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public void record(String username, String eventType, String ipAddress, String userAgent, boolean success, String details) {
        AuditLog log = new AuditLog(username, eventType, ipAddress, userAgent, success, details);
        auditLogRepository.save(log);
    }
}
