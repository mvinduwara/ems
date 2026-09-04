package com.retailhr.ems.service;

import com.retailhr.ems.model.entity.Employee;
import com.retailhr.ems.model.entity.PayrollRecord;
import com.retailhr.ems.model.entity.User;
import com.retailhr.ems.repository.PayrollRecordRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class PayrollService {

    private final PayrollRecordRepository payrollRecordRepository;
    private final AttendanceService attendanceService;
    private final AuditService auditService;

    public PayrollService(PayrollRecordRepository payrollRecordRepository,
                          AttendanceService attendanceService,
                          AuditService auditService) {
        this.payrollRecordRepository = payrollRecordRepository;
        this.attendanceService = attendanceService;
        this.auditService = auditService;
    }

    public PayrollRecord generatePayroll(Employee employee, LocalDate periodStart, LocalDate periodEnd,
                                         BigDecimal overtimeHours, BigDecimal overtimeRate,
                                         BigDecimal deductions, BigDecimal bonuses, User generatedBy) {
        long totalPeriodDays = ChronoUnit.DAYS.between(periodStart, periodEnd) + 1;
        long daysWorked = attendanceService.countWorkedDays(employee, periodStart, periodEnd);
        long daysOnLeave = Math.max(0, totalPeriodDays - daysWorked);

        BigDecimal baseSalary = employee.getPosition().getBaseSalary();
        BigDecimal dailyRate = baseSalary.divide(BigDecimal.valueOf(totalPeriodDays), 4, RoundingMode.HALF_UP);
        BigDecimal earnedBase = dailyRate.multiply(BigDecimal.valueOf(daysWorked));
        BigDecimal overtimePay = overtimeHours.multiply(overtimeRate);
        BigDecimal netPay = earnedBase.add(overtimePay).add(bonuses).subtract(deductions)
                .setScale(2, RoundingMode.HALF_UP);

        PayrollRecord record = new PayrollRecord();
        record.setEmployee(employee);
        record.setPayPeriodStart(periodStart);
        record.setPayPeriodEnd(periodEnd);
        record.setBaseSalary(baseSalary);
        record.setDaysWorked((int) daysWorked);
        record.setDaysOnLeave((int) daysOnLeave);
        record.setOvertimeHours(overtimeHours);
        record.setOvertimeRate(overtimeRate);
        record.setDeductions(deductions);
        record.setBonuses(bonuses);
        record.setNetPay(netPay);
        record.setStatus(PayrollRecord.Status.DRAFT);
        record.setGeneratedBy(generatedBy);

        PayrollRecord saved = payrollRecordRepository.save(record);
        auditService.log(generatedBy, "PAYROLL_GENERATED", "PayrollRecord", saved.getPayrollId().intValue(),
                "Generated payroll for " + employee.getFullName() + ": " + netPay);
        return saved;
    }

    public PayrollRecord finalizePayroll(PayrollRecord record, User actor) {
        if (record.getStatus() != PayrollRecord.Status.DRAFT) {
            throw new IllegalStateException("Only draft payroll can be finalized");
        }
        record.setStatus(PayrollRecord.Status.FINALIZED);
        PayrollRecord updated = payrollRecordRepository.update(record);
        auditService.log(actor, "PAYROLL_FINALIZED", "PayrollRecord", record.getPayrollId().intValue(), "Payroll finalized");
        return updated;
    }

    public PayrollRecord markAsPaid(PayrollRecord record, User actor) {
        if (record.getStatus() != PayrollRecord.Status.FINALIZED) {
            throw new IllegalStateException("Only finalized payroll can be marked as paid");
        }
        record.setStatus(PayrollRecord.Status.PAID);
        PayrollRecord updated = payrollRecordRepository.update(record);
        auditService.log(actor, "PAYROLL_PAID", "PayrollRecord", record.getPayrollId().intValue(), "Payroll marked as paid");
        return updated;
    }

    public List<PayrollRecord> getHistoryFor(Employee employee) {
        return payrollRecordRepository.findByEmployee(employee);
    }
}