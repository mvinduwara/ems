package com.retailhr.ems.repository;

import com.retailhr.ems.model.entity.AuditLog;
import com.retailhr.ems.model.entity.User;
import jakarta.persistence.EntityManager;

import java.util.List;

public class AuditLogRepository extends GenericRepository<AuditLog, Long> {

    public AuditLogRepository() {
        super(AuditLog.class);
    }

    public void log(User actor, String action, String entityName, Integer entityId, String details, String ipAddress) {
        AuditLog entry = new AuditLog();
        entry.setActor(actor);
        entry.setAction(action);
        entry.setEntityName(entityName);
        entry.setEntityId(entityId);
        entry.setDetails(details);
        entry.setIpAddress(ipAddress);
        save(entry);
    }

    public List<AuditLog> findByEntity(String entityName, Integer entityId) {
        try (EntityManager em = entityManager()) {
            return em.createQuery(
                            "SELECT a FROM AuditLog a WHERE a.entityName = :entityName " +
                                    "AND a.entityId = :entityId ORDER BY a.createdAt DESC",
                            AuditLog.class)
                    .setParameter("entityName", entityName)
                    .setParameter("entityId", entityId)
                    .getResultList();
        }
    }

    public List<AuditLog> findRecent(int limit) {
        try (EntityManager em = entityManager()) {
            return em.createQuery(
                            "SELECT a FROM AuditLog a ORDER BY a.createdAt DESC", AuditLog.class)
                    .setMaxResults(limit)
                    .getResultList();
        }
    }
}