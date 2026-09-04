package com.retailhr.ems.service;

import com.retailhr.ems.repository.*;

public class ServiceFactory {

    private static final AuditLogRepository AUDIT_LOG_REPOSITORY = new AuditLogRepository();
    private static final UserRepository USER_REPOSITORY = new UserRepository();
    private static final EmployeeRepository EMPLOYEE_REPOSITORY = new EmployeeRepository();
    private static final AttendanceLogRepository ATTENDANCE_LOG_REPOSITORY = new AttendanceLogRepository();
    private static final LeaveRequestRepository LEAVE_REQUEST_REPOSITORY = new LeaveRequestRepository();
    private static final PayrollRecordRepository PAYROLL_RECORD_REPOSITORY = new PayrollRecordRepository();

    private static final AuditService AUDIT_SERVICE = new AuditService(AUDIT_LOG_REPOSITORY);
    private static final AuthService AUTH_SERVICE = new AuthService(USER_REPOSITORY, AUDIT_SERVICE);
    private static final EmployeeService EMPLOYEE_SERVICE = new EmployeeService(EMPLOYEE_REPOSITORY, AUDIT_SERVICE);
    private static final AttendanceService ATTENDANCE_SERVICE =
            new AttendanceService(ATTENDANCE_LOG_REPOSITORY, EMPLOYEE_SERVICE, AUDIT_SERVICE);
    private static final LeaveService LEAVE_SERVICE = new LeaveService(LEAVE_REQUEST_REPOSITORY, AUDIT_SERVICE);
    private static final PayrollService PAYROLL_SERVICE =
            new PayrollService(PAYROLL_RECORD_REPOSITORY, ATTENDANCE_SERVICE, AUDIT_SERVICE);

    private ServiceFactory() {
    }

    public static AuditService auditService() {
        return AUDIT_SERVICE;
    }

    public static AuthService authService() {
        return AUTH_SERVICE;
    }

    public static EmployeeService employeeService() {
        return EMPLOYEE_SERVICE;
    }

    public static AttendanceService attendanceService() {
        return ATTENDANCE_SERVICE;
    }

    public static LeaveService leaveService() {
        return LEAVE_SERVICE;
    }

    public static PayrollService payrollService() {
        return PAYROLL_SERVICE;
    }
}