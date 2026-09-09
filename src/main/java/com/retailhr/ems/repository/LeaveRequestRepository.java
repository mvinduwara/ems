package com.retailhr.ems.repository;

import com.retailhr.ems.model.entity.Employee;
import com.retailhr.ems.model.entity.LeaveRequest;
import jakarta.persistence.EntityManager;
import com.retailhr.ems.repository.LeaveTypeRepository;

import java.util.List;

public class LeaveRequestRepository extends GenericRepository<LeaveRequest, Integer> {

    public LeaveRequestRepository() {
        super(LeaveRequest.class);
    }

    public List<LeaveRequest> findByEmployee(Employee employee) {
        try (EntityManager em = entityManager()) {
            return em.createQuery(
                            "SELECT l FROM LeaveRequest l WHERE l.employee = :employee " +
                                    "ORDER BY l.createdAt DESC",
                            LeaveRequest.class)
                    .setParameter("employee", employee)
                    .getResultList();
        }
    }

    public List<LeaveRequest> findByStatus(LeaveRequest.Status status) {
        try (EntityManager em = entityManager()) {
            return em.createQuery(
                            "SELECT l FROM LeaveRequest l WHERE l.status = :status " +
                                    "ORDER BY l.createdAt ASC",
                            LeaveRequest.class)
                    .setParameter("status", status)
                    .getResultList();
        }
    }

    public List<LeaveRequest> findPendingForApproval() {
        return findByStatus(LeaveRequest.Status.PENDING);
    }

    public List<LeaveRequest> findAll() {
        return super.findAll();
    }
}