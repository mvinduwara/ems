package com.retailhr.ems.repository;

import com.retailhr.ems.model.entity.Employee;
import com.retailhr.ems.model.entity.PayrollRecord;
import jakarta.persistence.EntityManager;

import java.time.LocalDate;
import java.util.List;

public class PayrollRecordRepository extends GenericRepository<PayrollRecord, Long> {

    public PayrollRecordRepository() {
        super(PayrollRecord.class);
    }

    public List<PayrollRecord> findByEmployee(Employee employee) {
        try (EntityManager em = entityManager()) {
            return em.createQuery(
                            "SELECT p FROM PayrollRecord p WHERE p.employee = :employee " +
                                    "ORDER BY p.payPeriodStart DESC",
                            PayrollRecord.class)
                    .setParameter("employee", employee)
                    .getResultList();
        }
    }

    public List<PayrollRecord> findByPeriod(LocalDate periodStart, LocalDate periodEnd) {
        try (EntityManager em = entityManager()) {
            return em.createQuery(
                            "SELECT p FROM PayrollRecord p WHERE p.payPeriodStart = :start " +
                                    "AND p.payPeriodEnd = :end",
                            PayrollRecord.class)
                    .setParameter("start", periodStart)
                    .setParameter("end", periodEnd)
                    .getResultList();
        }
    }
}