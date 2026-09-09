package com.retailhr.ems.repository;

import com.retailhr.ems.model.entity.Department;
import com.retailhr.ems.model.entity.Position;
import jakarta.persistence.EntityManager;

import java.util.List;

public class PositionRepository extends GenericRepository<Position, Integer> {

    public PositionRepository() {
        super(Position.class);
    }

    public List<Position> findByDepartment(Department department) {
        try (EntityManager em = entityManager()) {
            return em.createQuery(
                            "SELECT p FROM Position p WHERE p.department = :department", Position.class)
                    .setParameter("department", department)
                    .getResultList();
        }
    }
}