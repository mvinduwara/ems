package com.retailhr.ems.controller;

import com.retailhr.ems.EmsApplication;
import com.retailhr.ems.model.entity.Employee;
import com.retailhr.ems.model.entity.User;
import com.retailhr.ems.service.ServiceFactory;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.util.Optional;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    @FXML
    private Button loginButton;

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password");
            return;
        }

        loginButton.setDisable(true);
        Optional<User> userOpt = ServiceFactory.authService().login(username, password);
        loginButton.setDisable(false);

        if (userOpt.isEmpty()) {
            showError("Invalid username or password");
            passwordField.clear();
            return;
        }

        User user = userOpt.get();
        SessionContext.setCurrentUser(user);

        boolean isAdmin = "ADMIN".equals(user.getRole().getRoleName());

        if (!isAdmin) {
            Optional<Employee> employeeOpt = ServiceFactory.employeeService().findByUserId(user.getUserId());
            if (employeeOpt.isEmpty()) {
                showError("No employee record is linked to this account. Contact an administrator.");
                SessionContext.clear();
                return;
            }
            SessionContext.setCurrentEmployee(employeeOpt.get());
        }

        try {
            if (isAdmin) {
                EmsApplication.setRoot("fxml/admin_dashboard");
            } else {
                EmsApplication.setRoot("fxml/employee_dashboard");
            }
        } catch (IOException e) {
            e.printStackTrace();
            showError("Failed to load dashboard: " + e.getMessage());
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
}