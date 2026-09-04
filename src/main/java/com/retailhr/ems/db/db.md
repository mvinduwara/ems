CREATE DATABASE IF NOT EXISTS ems_db
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE ems_db;

CREATE TABLE roles (
role_id INT AUTO_INCREMENT PRIMARY KEY,
role_name VARCHAR(30) NOT NULL UNIQUE
);

INSERT INTO roles (role_name) VALUES ('ADMIN'), ('EMPLOYEE');

CREATE TABLE departments (
department_id INT AUTO_INCREMENT PRIMARY KEY,
department_name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE positions (
position_id INT AUTO_INCREMENT PRIMARY KEY,
position_title VARCHAR(100) NOT NULL,
department_id INT NOT NULL,
base_salary DECIMAL(12,2) NOT NULL,
FOREIGN KEY (department_id) REFERENCES departments(department_id)
ON DELETE RESTRICT ON UPDATE CASCADE
);

CREATE TABLE users (
user_id INT AUTO_INCREMENT PRIMARY KEY,
username VARCHAR(50) NOT NULL UNIQUE,
password_hash VARCHAR(60) NOT NULL,
role_id INT NOT NULL,
is_active BOOLEAN NOT NULL DEFAULT TRUE,
last_login DATETIME NULL,
created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
FOREIGN KEY (role_id) REFERENCES roles(role_id)
ON DELETE RESTRICT ON UPDATE CASCADE
);

CREATE TABLE employees (
employee_id INT AUTO_INCREMENT PRIMARY KEY,
user_id INT NOT NULL UNIQUE,
employee_code VARCHAR(20) NOT NULL UNIQUE,
first_name VARCHAR(50) NOT NULL,
last_name VARCHAR(50) NOT NULL,
email VARCHAR(150) NOT NULL UNIQUE,
phone VARCHAR(20),
address VARCHAR(255),
date_of_birth DATE,
date_hired DATE NOT NULL,
date_terminated DATE NULL,
department_id INT NOT NULL,
position_id INT NOT NULL,
qr_code_hash VARCHAR(64) NOT NULL UNIQUE,
status ENUM('ACTIVE', 'ON_LEAVE', 'SUSPENDED', 'TERMINATED') NOT NULL DEFAULT 'ACTIVE',
created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
FOREIGN KEY (user_id) REFERENCES users(user_id)
ON DELETE CASCADE ON UPDATE CASCADE,
FOREIGN KEY (department_id) REFERENCES departments(department_id)
ON DELETE RESTRICT ON UPDATE CASCADE,
FOREIGN KEY (position_id) REFERENCES positions(position_id)
ON DELETE RESTRICT ON UPDATE CASCADE,
INDEX idx_employees_status (status),
INDEX idx_employees_department (department_id)
);

CREATE TABLE attendance_logs (
attendance_id BIGINT AUTO_INCREMENT PRIMARY KEY,
employee_id INT NOT NULL,
scan_type ENUM('CLOCK_IN', 'CLOCK_OUT') NOT NULL,
scanned_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
scan_method ENUM('QR', 'MANUAL') NOT NULL DEFAULT 'QR',
device_info VARCHAR(150),
FOREIGN KEY (employee_id) REFERENCES employees(employee_id)
ON DELETE CASCADE ON UPDATE CASCADE,
INDEX idx_attendance_employee_date (employee_id, scanned_at)
);

CREATE TABLE leave_types (
leave_type_id INT AUTO_INCREMENT PRIMARY KEY,
type_name VARCHAR(50) NOT NULL UNIQUE,
max_days_per_year INT NOT NULL,
requires_approval BOOLEAN NOT NULL DEFAULT TRUE
);

INSERT INTO leave_types (type_name, max_days_per_year, requires_approval) VALUES
('ANNUAL', 14, TRUE),
('SICK', 7, TRUE),
('CASUAL', 7, TRUE),
('UNPAID', 30, TRUE);

CREATE TABLE leave_requests (
leave_request_id INT AUTO_INCREMENT PRIMARY KEY,
employee_id INT NOT NULL,
leave_type_id INT NOT NULL,
start_date DATE NOT NULL,
end_date DATE NOT NULL,
total_days INT NOT NULL,
reason VARCHAR(500),
status ENUM('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED') NOT NULL DEFAULT 'PENDING',
reviewed_by INT NULL,
reviewed_at DATETIME NULL,
review_comment VARCHAR(500),
created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
FOREIGN KEY (employee_id) REFERENCES employees(employee_id)
ON DELETE CASCADE ON UPDATE CASCADE,
FOREIGN KEY (leave_type_id) REFERENCES leave_types(leave_type_id)
ON DELETE RESTRICT ON UPDATE CASCADE,
FOREIGN KEY (reviewed_by) REFERENCES users(user_id)
ON DELETE SET NULL ON UPDATE CASCADE,
INDEX idx_leave_employee_status (employee_id, status),
CONSTRAINT chk_leave_dates CHECK (end_date >= start_date)
);

CREATE TABLE payroll_records (
payroll_id BIGINT AUTO_INCREMENT PRIMARY KEY,
employee_id INT NOT NULL,
pay_period_start DATE NOT NULL,
pay_period_end DATE NOT NULL,
base_salary DECIMAL(12,2) NOT NULL,
days_worked INT NOT NULL DEFAULT 0,
days_on_leave INT NOT NULL DEFAULT 0,
overtime_hours DECIMAL(6,2) NOT NULL DEFAULT 0,
overtime_rate DECIMAL(10,2) NOT NULL DEFAULT 0,
deductions DECIMAL(12,2) NOT NULL DEFAULT 0,
bonuses DECIMAL(12,2) NOT NULL DEFAULT 0,
net_pay DECIMAL(12,2) NOT NULL,
status ENUM('DRAFT', 'FINALIZED', 'PAID') NOT NULL DEFAULT 'DRAFT',
generated_by INT NOT NULL,
generated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
FOREIGN KEY (employee_id) REFERENCES employees(employee_id)
ON DELETE CASCADE ON UPDATE CASCADE,
FOREIGN KEY (generated_by) REFERENCES users(user_id)
ON DELETE RESTRICT ON UPDATE CASCADE,
INDEX idx_payroll_employee_period (employee_id, pay_period_start, pay_period_end),
CONSTRAINT chk_payroll_period CHECK (pay_period_end >= pay_period_start)
);

CREATE TABLE audit_logs (
audit_id BIGINT AUTO_INCREMENT PRIMARY KEY,
actor_user_id INT NULL,
action VARCHAR(100) NOT NULL,
entity_name VARCHAR(100) NOT NULL,
entity_id INT NULL,
details TEXT,
ip_address VARCHAR(45),
created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
FOREIGN KEY (actor_user_id) REFERENCES users(user_id)
ON DELETE SET NULL ON UPDATE CASCADE,
INDEX idx_audit_entity (entity_name, entity_id),
INDEX idx_audit_created (created_at)
);