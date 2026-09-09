package com.retailhr.ems.db;

import com.retailhr.ems.model.entity.*;
import com.retailhr.ems.repository.*;
import org.mindrot.jbcrypt.BCrypt;

import java.math.BigDecimal;
import java.util.Optional;

public class DataSeeder {

    private DataSeeder() {
    }

    public static void seedIfEmpty() {
        RoleRepository roleRepository = new RoleRepository();
        UserRepository userRepository = new UserRepository();
        DepartmentRepository departmentRepository = new DepartmentRepository();
        PositionRepository positionRepository = new PositionRepository();

        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseThrow(() -> new IllegalStateException("ADMIN role missing — run schema.sql first"));

        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPasswordHash(BCrypt.hashpw("Admin@123", BCrypt.gensalt(12)));
            admin.setRole(adminRole);
            admin.setIsActive(true);
            userRepository.save(admin);
            System.out.println("Seeded default admin account -> username: admin | password: Admin@123");
        }

        Optional<Department> retailOps = departmentRepository.findByName("Retail Operations");
        Department department = retailOps.orElseGet(() -> {
            Department d = new Department();
            d.setDepartmentName("Retail Operations");
            return departmentRepository.save(d);
        });

        if (positionRepository.findByDepartment(department).isEmpty()) {
            Position cashier = new Position();
            cashier.setPositionTitle("Cashier");
            cashier.setDepartment(department);
            cashier.setBaseSalary(new BigDecimal("35000.00"));
            positionRepository.save(cashier);

            Position supervisor = new Position();
            supervisor.setPositionTitle("Shift Supervisor");
            supervisor.setDepartment(department);
            supervisor.setBaseSalary(new BigDecimal("52000.00"));
            positionRepository.save(supervisor);

            System.out.println("Seeded default department 'Retail Operations' with 2 positions");
        }
    }
}