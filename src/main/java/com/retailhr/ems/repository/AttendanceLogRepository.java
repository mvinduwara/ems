package com.retailhr.ems.repository;

import com.retailhr.ems.model.entity.AttendanceLog;
import com.retailhr.ems.model.entity.Employee;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class AttendanceLogRepository extends GenericRepository<AttendanceLog, Long> {

    public AttendanceLogRepository() {
        super(AttendanceLog.class);
    }

    public List<AttendanceLog> findByEmployeeAndDate(Employee employee, LocalDate date) {
        try (EntityManager em = entityManager()) {
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.plusDays(1).atStartOfDay();
            return em.createQuery(
                            "SELECT a FROM AttendanceLog a WHERE a.employee = :employee " +
                                    "AND a.scannedAt >= :start AND a.scannedAt < :end " +
                                    "ORDER BY a.scannedAt ASC",
                            AttendanceLog.class)
                    .setParameter("employee", employee)
                    .setParameter("start", start)
                    .setParameter("end", end)
                    .getResultList();
        }
    }

    public Optional<AttendanceLog> findLastLogForEmployeeToday(Employee employee, LocalDate date) {
        List<AttendanceLog> logs = findByEmployeeAndDate(employee, date);
        return logs.isEmpty() ? Optional.empty() : Optional.of(logs.get(logs.size() - 1));
    }

    public List<AttendanceLog> findByEmployeeBetween(Employee employee, LocalDateTime start, LocalDateTime end) {
        try (EntityManager em = entityManager()) {
            return em.createQuery(
                            "SELECT a FROM AttendanceLog a WHERE a.employee = :employee " +
                                    "AND a.scannedAt >= :start AND a.scannedAt < :end " +
                                    "ORDER BY a.scannedAt ASC",
                            AttendanceLog.class)
                    .setParameter("employee", employee)
                    .setParameter("start", start)
                    .setParameter("end", end)
                    .getResultList();
        }
    }
}