package com.retailhr.ems.controller;

import com.retailhr.ems.model.entity.Employee;
import com.retailhr.ems.model.entity.User;

public class SessionContext {

    private static User currentUser;
    private static Employee currentEmployee;

    private SessionContext() {
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static Employee getCurrentEmployee() {
        return currentEmployee;
    }

    public static void setCurrentEmployee(Employee employee) {
        currentEmployee = employee;
    }

    public static boolean isAdmin() {
        return currentUser != null && "ADMIN".equals(currentUser.getRole().getRoleName());
    }

    public static void clear() {
        currentUser = null;
        currentEmployee = null;
    }
}