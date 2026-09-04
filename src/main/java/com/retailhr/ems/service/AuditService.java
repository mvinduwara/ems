package com.retailhr.ems.service;

import com.retailhr.ems.model.entity.AuditLog;
import com.retailhr.ems.model.entity.User;
import com.retailhr.ems.repository.AuditLogRepository;

import java.util.List;

public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void log(User actor, String action, String entityName, Integer entityId, String details) {
        auditLogRepository.log(actor, action, entityName, entityId, details, null);
    }

    public void log(User actor, String action, String entityName, Integer entityId, String details, String ipAddress) {
        auditLogRepository.log(actor, action, entityName, entityId, details, ipAddress);
    }

    public List<AuditLog> getRecent(int limit) {
        return auditLogRepository.findRecent(limit);
    }

    public List<AuditLog> getHistoryFor(String entityName, Integer entityId) {
        return auditLogRepository.findByEntity(entityName, entityId);
    }
}