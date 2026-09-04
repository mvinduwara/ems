package com.retailhr.ems.repository;

import com.retailhr.ems.model.entity.LeaveType;

public class LeaveTypeRepository extends GenericRepository<LeaveType, Integer> {

    public LeaveTypeRepository() {
        super(LeaveType.class);
    }
}