package com.retailhr.ems.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "attendance_logs")
public class AttendanceLog {

    public enum ScanType {
        CLOCK_IN, CLOCK_OUT
    }

    public enum ScanMethod {
        QR, MANUAL
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attendance_id")
    private Long attendanceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(name = "scan_type", nullable = false, length = 20)
    private ScanType scanType;

    @Column(name = "scanned_at", nullable = false)
    private LocalDateTime scannedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "scan_method", nullable = false, length = 20)
    private ScanMethod scanMethod = ScanMethod.QR;

    @Column(name = "device_info", length = 150)
    private String deviceInfo;

    public AttendanceLog() {
    }

    @PrePersist
    protected void onCreate() {
        if (scannedAt == null) {
            scannedAt = LocalDateTime.now();
        }
    }

    public Long getAttendanceId() {
        return attendanceId;
    }

    public void setAttendanceId(Long attendanceId) {
        this.attendanceId = attendanceId;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public ScanType getScanType() {
        return scanType;
    }

    public void setScanType(ScanType scanType) {
        this.scanType = scanType;
    }

    public LocalDateTime getScannedAt() {
        return scannedAt;
    }

    public void setScannedAt(LocalDateTime scannedAt) {
        this.scannedAt = scannedAt;
    }

    public ScanMethod getScanMethod() {
        return scanMethod;
    }

    public void setScanMethod(ScanMethod scanMethod) {
        this.scanMethod = scanMethod;
    }

    public String getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AttendanceLog that)) return false;
        return Objects.equals(attendanceId, that.attendanceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(attendanceId);
    }
}