package com.retailhr.ems.controller;

import com.retailhr.ems.EmsApplication;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class EmployeeDashboardController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private VBox contentArea;

    private Object currentSubController;

    @FXML
    private void initialize() {
        welcomeLabel.setText("Welcome, " + SessionContext.getCurrentUser().getUsername());
        showAttendance();
    }

    @FXML
    private void showAttendance() {
        loadIntoContent("fxml/employee/my_attendance");
    }

    @FXML
    private void showLeaveRequests() {
        loadIntoContent("fxml/employee/my_leave");
    }

    @FXML
    private void showPayslips() {
        loadIntoContent("fxml/employee/my_payslips");
    }

    @FXML
    private void showMyQrCode() {
        loadIntoContent("fxml/employee/my_qr_badge");
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