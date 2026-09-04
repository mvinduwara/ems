package com.retailhr.ems.service;

import com.retailhr.ems.model.entity.*;
import com.retailhr.ems.repository.EmployeeRepository;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final AuditService auditService;

    public EmployeeService(EmployeeRepository employeeRepository, AuditService auditService) {
        this.employeeRepository = employeeRepository;
        this.auditService = auditService;
    }

    public Employee onboardEmployee(User user, String employeeCode, String firstName, String lastName,
                                    String email, String phone, String address, LocalDate dateOfBirth,
                                    LocalDate dateHired, Department department, Position position,
                                    User actor) {
        Employee employee = new Employee();
        employee.setUser(user);
        employee.setEmployeeCode(employeeCode);
        employee.setFirstName(firstName);
        employee.setLastName(lastName);
        employee.setEmail(email);
        employee.setPhone(phone);
        employee.setAddress(address);
        employee.setDateOfBirth(dateOfBirth);
        employee.setDateHired(dateHired);
        employee.setDepartment(department);
        employee.setPosition(position);
        employee.setQrCodeHash(generateQrCodeHash());
        employee.setStatus(Employee.Status.ACTIVE);
        Employee saved = employeeRepository.save(employee);
        auditService.log(actor, "EMPLOYEE_ONBOARDED", "Employee", saved.getEmployeeId(),
                "Onboarded " + saved.getFullName());
        return saved;
    }

    public Employee terminateEmployee(Employee employee, LocalDate terminationDate, User actor) {
        employee.setStatus(Employee.Status.TERMINATED);
        employee.setDateTerminated(terminationDate);
        Employee updated = employeeRepository.update(employee);
        auditService.log(actor, "EMPLOYEE_TERMINATED", "Employee", employee.getEmployeeId(),
                "Terminated effective " + terminationDate);
        return updated;
    }

    public Employee updateStatus(Employee employee, Employee.Status status, User actor) {
        employee.setStatus(status);
        Employee updated = employeeRepository.update(employee);
        auditService.log(actor, "EMPLOYEE_STATUS_CHANGED", "Employee", employee.getEmployeeId(),
                "Status changed to " + status);
        return updated;
    }

    public Optional<Employee> findByQrCodeHash(String qrCodeHash) {
        return employeeRepository.findByQrCodeHash(qrCodeHash);
    }

    public Optional<Employee> findByEmployeeCode(String employeeCode) {
        return employeeRepository.findByEmployeeCode(employeeCode);
    }

    public List<Employee> findByDepartment(Department department) {
        return employeeRepository.findByDepartment(department);
    }

    public List<Employee> findAllActive() {
        return employeeRepository.findByStatus(Employee.Status.ACTIVE);
    }

    public List<Employee> findAll() {
        return employeeRepository.findAll();
    }

    private String generateQrCodeHash() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[24];
        random.nextBytes(bytes);
        return UUID.nameUUIDFromBytes(bytes).toString().replace("-", "");
    }

    public Optional<Employee> findByUserId(Integer userId) {
        return employeeRepository.findByUserId(userId);
    }
}