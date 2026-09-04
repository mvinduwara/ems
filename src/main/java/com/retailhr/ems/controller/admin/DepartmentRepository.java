package com.retailhr.ems.controller.admin;

import com.retailhr.ems.model.entity.Department;
import com.retailhr.ems.repository.GenericRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

import java.util.Optional;

public class DepartmentRepository extends GenericRepository<Department, Integer> {

    public DepartmentRepository() {
        super(Department.class);
    }

    public Optional<Department> findByName(String name) {
        try (EntityManager em = entityManager()) {
            Department department = em.createQuery(
                            "SELECT d FROM Department d WHERE d.departmentName = :name", Department.class)
                    .setParameter("name", name)
                    .getSingleResult();
            return Optional.of(department);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
}