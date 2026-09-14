package com.retailhr.ems.controller;

import com.retailhr.ems.EmsApplication;
import com.retailhr.ems.util.ThemeManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import com.retailhr.ems.util.IconFactory;

import java.io.IOException;

public class AdminDashboardController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private VBox contentArea;

    @FXML
    private Button themeToggleButton;

    private Object currentSubController;

    @FXML
    private Button logoutButton;
    @FXML
    private Button employeesNavButton;
    @FXML
    private Button attendanceNavButton;
    @FXML
    private Button leaveNavButton;
    @FXML
    private Button payrollNavButton;
    @FXML
    private Button auditNavButton;

    @FXML
    private void initialize() {
        welcomeLabel.setText("Welcome, " + SessionContext.getCurrentUser().getUsername());
        updateThemeToggleIcon();
        logoutButton.setGraphic(IconFactory.logout(14));
        employeesNavButton.setGraphic(IconFactory.users(16));
        attendanceNavButton.setGraphic(IconFactory.clock(16));
        leaveNavButton.setGraphic(IconFactory.checkSquare(16));
        payrollNavButton.setGraphic(IconFactory.dollar(16));
        auditNavButton.setGraphic(IconFactory.document(16));
        showEmployees();
    }

    @FXML
    private void handleToggleTheme() {
        ThemeManager.toggleTheme(themeToggleButton.getScene());
        updateThemeToggleIcon();
    }

    private void updateThemeToggleIcon() {
        themeToggleButton.setText(ThemeManager.isDarkMode() ? "☀" : "🌙");
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