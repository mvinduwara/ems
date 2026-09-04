package com.retailhr.ems.repository;

import com.retailhr.ems.model.entity.Department;
import com.retailhr.ems.model.entity.Employee;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import com.retailhr.ems.repository.EmployeeRepository;

import java.util.List;
import java.util.Optional;

public class EmployeeRepository extends GenericRepository<Employee, Integer> {

    public EmployeeRepository() {
        super(Employee.class);
    }

    public Optional<Employee> findByEmployeeCode(String employeeCode) {
        try (EntityManager em = entityManager()) {
            Employee employee = em.createQuery(
                            "SELECT e FROM Employee e WHERE e.employeeCode = :code", Employee.class)
                    .setParameter("code", employeeCode)
                    .getSingleResult();
            return Optional.of(employee);
        } catch (NoResultException ex) {
            return Optional.empty();
        }
    }

    public Optional<Employee> findByQrCodeHash(String qrCodeHash) {
        try (EntityManager em = entityManager()) {
            Employee employee = em.createQuery(
                            "SELECT e FROM Employee e WHERE e.qrCodeHash = :hash", Employee.class)
                    .setParameter("hash", qrCodeHash)
                    .getSingleResult();
            return Optional.of(employee);
        } catch (NoResultException ex) {
            return Optional.empty();
        }
    }

    public List<Employee> findByDepartment(Department department) {
        try (EntityManager em = entityManager()) {
            return em.createQuery(
                            "SELECT e FROM Employee e WHERE e.department = :department", Employee.class)
                    .setParameter("department", department)
                    .getResultList();
        }
    }

    public List<Employee> findByStatus(Employee.Status status) {
        try (EntityManager em = entityManager()) {
            return em.createQuery(
                            "SELECT e FROM Employee e WHERE e.status = :status", Employee.class)
                    .setParameter("status", status)
                    .getResultList();
        }
    }
    public Optional<Employee> findByUserId(Integer userId) {
        try (EntityManager em = entityManager()) {
            Employee employee = em.createQuery(
                            "SELECT e FROM Employee e WHERE e.user.userId = :userId", Employee.class)
                    .setParameter("userId", userId)
                    .getSingleResult();
            return Optional.of(employee);
        } catch (NoResultException ex) {
            return Optional.empty();
        }
    }
}