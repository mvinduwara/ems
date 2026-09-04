package com.retailhr.ems.service;

import com.retailhr.ems.model.entity.AttendanceLog;
import com.retailhr.ems.model.entity.Employee;
import com.retailhr.ems.repository.AttendanceLogRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class AttendanceService {

    private final AttendanceLogRepository attendanceLogRepository;
    private final EmployeeService employeeService;
    private final AuditService auditService;

    public AttendanceService(AttendanceLogRepository attendanceLogRepository,
                             EmployeeService employeeService,
                             AuditService auditService) {
        this.attendanceLogRepository = attendanceLogRepository;
        this.employeeService = employeeService;
        this.auditService = auditService;
    }

    public AttendanceLog scanQrCode(String qrCodeHash, String deviceInfo) {
        Employee employee = employeeService.findByQrCodeHash(qrCodeHash)
                .orElseThrow(() -> new IllegalArgumentException("Unrecognized QR code"));

        if (employee.getStatus() == Employee.Status.TERMINATED
                || employee.getStatus() == Employee.Status.SUSPENDED) {
            throw new IllegalStateException("Employee is not permitted to clock in/out: " + employee.getStatus());
        }

        LocalDate today = LocalDate.now();
        Optional<AttendanceLog> lastLog = attendanceLogRepository.findLastLogForEmployeeToday(employee, today);

        AttendanceLog.ScanType nextType = (lastLog.isEmpty() || lastLog.get().getScanType() == AttendanceLog.ScanType.CLOCK_OUT)
                ? AttendanceLog.ScanType.CLOCK_IN
                : AttendanceLog.ScanType.CLOCK_OUT;

        AttendanceLog log = new AttendanceLog();
        log.setEmployee(employee);
        log.setScanType(nextType);
        log.setScannedAt(LocalDateTime.now());
        log.setScanMethod(AttendanceLog.ScanMethod.QR);
        log.setDeviceInfo(deviceInfo);

        AttendanceLog saved = attendanceLogRepository.save(log);
        auditService.log(employee.getUser(), "ATTENDANCE_" + nextType, "AttendanceLog", saved.getAttendanceId().intValue(),
                employee.getFullName() + " " + nextType.name().toLowerCase().replace('_', ' '));
        return saved;
    }

    public List<AttendanceLog> getTodayLogs(Employee employee) {
        return attendanceLogRepository.findByEmployeeAndDate(employee, LocalDate.now());
    }

    public List<AttendanceLog> getLogsBetween(Employee employee, LocalDateTime start, LocalDateTime end) {
        return attendanceLogRepository.findByEmployeeBetween(employee, start, end);
    }

    public long countWorkedDays(Employee employee, LocalDate periodStart, LocalDate periodEnd) {
        List<AttendanceLog> logs = attendanceLogRepository.findByEmployeeBetween(
                employee, periodStart.atStartOfDay(), periodEnd.plusDays(1).atStartOfDay());
        return logs.stream()
                .filter(l -> l.getScanType() == AttendanceLog.ScanType.CLOCK_IN)
                .map(l -> l.getScannedAt().toLocalDate())
                .distinct()
                .count();
    }
}