package com.retailhr.ems.controller;

import com.retailhr.ems.EmsApplication;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class AdminDashboardController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private VBox contentArea;

    private Object currentSubController;

    @FXML
    private void initialize() {
        welcomeLabel.setText("Welcome, " + SessionContext.getCurrentUser().getUsername());
        showEmployees();
    }

    @FXML
    private void showEmployees() {
        loadIntoContent("fxml/admin/employee_list");
    }

    @FXML
    private void showAttendance() {
        loadIntoContent("fxml/admin/attendance_view");
    }

    @FXML
    private void showLeaveApprovals() {
        loadIntoContent("fxml/admin/leave_approvals");
    }

    @FXML
    private void showPayroll() {
        loadIntoContent("fxml/admin/payroll_view");
    }

    @FXML
    private void showAuditLog() {
        loadIntoContent("fxml/admin/audit_log_view");
    }

    @FXML
    private void handleLogout() {
        closeCurrentSubController();
        SessionContext.clear();
        try {
            EmsApplication.setRoot("fxml/login");
        } catch (IOException e) {
            throw new IllegalStateException("Failed to return to login screen", e);
        }
    }

    private void loadIntoContent(String fxmlName) {
        closeCurrentSubController();
        try {
            FXMLLoader loader = new FXMLLoader(EmsApplication.class.getResource(fxmlName + ".fxml"));
            Parent view = loader.load();
            currentSubController = loader.getController();
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load view: " + fxmlName, e);
        }
    }

    private void closeCurrentSubController() {
        if (currentSubController instanceof ClosableView closable) {
            closable.onClose();
        }
        currentSubController = null;
    }
}