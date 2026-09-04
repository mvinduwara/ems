package com.retailhr.ems.service;

import com.retailhr.ems.model.entity.Employee;
import com.retailhr.ems.model.entity.LeaveRequest;
import com.retailhr.ems.model.entity.LeaveType;
import com.retailhr.ems.model.entity.User;
import com.retailhr.ems.repository.LeaveRequestRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class LeaveService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final AuditService auditService;

    public LeaveService(LeaveRequestRepository leaveRequestRepository, AuditService auditService) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.auditService = auditService;
    }

    public LeaveRequest submitRequest(Employee employee, LeaveType leaveType, LocalDate startDate,
                                      LocalDate endDate, String reason) {
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }

        int totalDays = (int) ChronoUnit.DAYS.between(startDate, endDate) + 1;

        List<LeaveRequest> existing = leaveRequestRepository.findByEmployee(employee);
        boolean overlaps = existing.stream()
                .filter(r -> r.getStatus() == LeaveRequest.Status.PENDING || r.getStatus() == LeaveRequest.Status.APPROVED)
                .anyMatch(r -> !endDate.isBefore(r.getStartDate()) && !startDate.isAfter(r.getEndDate()));
        if (overlaps) {
            throw new IllegalStateException("Requested dates overlap with an existing leave request");
        }

        int usedDaysThisYear = existing.stream()
                .filter(r -> r.getLeaveType().getLeaveTypeId().equals(leaveType.getLeaveTypeId()))
                .filter(r -> r.getStatus() == LeaveRequest.Status.APPROVED)
                .filter(r -> r.getStartDate().getYear() == startDate.getYear())
                .mapToInt(LeaveRequest::getTotalDays)
                .sum();
        if (usedDaysThisYear + totalDays > leaveType.getMaxDaysPerYear()) {
            throw new IllegalStateException("Request exceeds remaining " + leaveType.getTypeName()
                    + " leave balance for this year");
        }

        LeaveRequest request = new LeaveRequest();
        request.setEmployee(employee);
        request.setLeaveType(leaveType);
        request.setStartDate(startDate);
        request.setEndDate(endDate);
        request.setTotalDays(totalDays);
        request.setReason(reason);
        request.setStatus(LeaveRequest.Status.PENDING);

        LeaveRequest saved = leaveRequestRepository.save(request);
        auditService.log(employee.getUser(), "LEAVE_REQUESTED", "LeaveRequest", saved.getLeaveRequestId(),
                employee.getFullName() + " requested " + totalDays + " day(s) " + leaveType.getTypeName());
        return saved;
    }

    public LeaveRequest approve(LeaveRequest request, User reviewer, String comment) {
        return review(request, LeaveRequest.Status.APPROVED, reviewer, comment);
    }

    public LeaveRequest reject(LeaveRequest request, User reviewer, String comment) {
        return review(request, LeaveRequest.Status.REJECTED, reviewer, comment);
    }

    public LeaveRequest cancel(LeaveRequest request, User actor) {
        if (request.getStatus() != LeaveRequest.Status.PENDING) {
            throw new IllegalStateException("Only pending requests can be cancelled");
        }
        request.setStatus(LeaveRequest.Status.CANCELLED);
        LeaveRequest updated = leaveRequestRepository.update(request);
        auditService.log(actor, "LEAVE_CANCELLED", "LeaveRequest", request.getLeaveRequestId(), "Request cancelled");
        return updated;
    }

    private LeaveRequest review(LeaveRequest request, LeaveRequest.Status status, User reviewer, String comment) {
        if (request.getStatus() != LeaveRequest.Status.PENDING) {
            throw new IllegalStateException("Request has already been reviewed");
        }
        request.setStatus(status);
        request.setReviewedBy(reviewer);
        request.setReviewedAt(LocalDateTime.now());
        request.setReviewComment(comment);
        LeaveRequest updated = leaveRequestRepository.update(request);
        auditService.log(reviewer, "LEAVE_" + status, "LeaveRequest", request.getLeaveRequestId(),
                "Request " + status.name().toLowerCase() + (comment != null ? ": " + comment : ""));
        return updated;
    }

    public List<LeaveRequest> getHistoryFor(Employee employee) {
        return leaveRequestRepository.findByEmployee(employee);
    }

    public List<LeaveRequest> getPendingForApproval() {
        return leaveRequestRepository.findPendingForApproval();
    }

    public List<LeaveRequest> getAll() {
        return leaveRequestRepository.findAll();
    }
}