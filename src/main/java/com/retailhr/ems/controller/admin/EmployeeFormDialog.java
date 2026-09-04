package com.retailhr.ems.controller.admin;

import com.retailhr.ems.controller.SessionContext;
import com.retailhr.ems.model.entity.Department;
import com.retailhr.ems.model.entity.Employee;
import com.retailhr.ems.model.entity.Position;
import com.retailhr.ems.model.entity.Role;
import com.retailhr.ems.repository.GenericRepository;
import com.retailhr.ems.service.ServiceFactory;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class EmployeeFormDialog extends Dialog<Employee> {

    private final TextField usernameField = new TextField();
    private final PasswordField passwordField = new PasswordField();
    private final TextField employeeCodeField = new TextField();
    private final TextField firstNameField = new TextField();
    private final TextField lastNameField = new TextField();
    private final TextField emailField = new TextField();
    private final TextField phoneField = new TextField();
    private final TextField addressField = new TextField();
    private final DatePicker dateOfBirthPicker = new DatePicker();
    private final DatePicker dateHiredPicker = new DatePicker(LocalDate.now());
    private final ComboBox<Department> departmentBox = new ComboBox<>();
    private final ComboBox<Position> positionBox = new ComboBox<>();

    private final Employee existing;

    public EmployeeFormDialog(Employee existing) {
        this.existing = existing;
        setTitle(existing == null ? "Add Employee" : "Edit Employee");
        setHeaderText(existing == null ? "Onboard a new employee" : "Edit employee details");

        ButtonType saveButtonType = new ButtonType(existing == null ? "Onboard" : "Save", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        buildForm();
        populateIfEditing();

        setResultConverter(buttonType -> {
            if (buttonType == saveButtonType) {
                return submit();
            }
            return null;
        });
    }

    private void buildForm() {
        GridPane grid = new GridPane();
        grid.setHgap(10.0);
        grid.setVgap(10.0);
        grid.setPadding(new Insets(20.0));

        List<Department> departments = new DepartmentRepository().findAll();
        departmentBox.setItems(javafx.collections.FXCollections.observableArrayList(departments));
        departmentBox.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(Department d) {
                return d == null ? "" : d.getDepartmentName();
            }

            @Override
            public Department fromString(String s) {
                return null;
            }
        });
        departmentBox.valueProperty().addListener((obs, oldDept, newDept) -> refreshPositions(newDept));

        positionBox.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(Position p) {
                return p == null ? "" : p.getPositionTitle() + " ($" + p.getBaseSalary() + ")";
            }

            @Override
            public Position fromString(String s) {
                return null;
            }
        });

        int row = 0;
        grid.addRow(row++, new Label("Username"), usernameField);
        grid.addRow(row++, new Label("Password"), passwordField);
        grid.addRow(row++, new Label("Employee Code"), employeeCodeField);
        grid.addRow(row++, new Label("First Name"), firstNameField);
        grid.addRow(row++, new Label("Last Name"), lastNameField);
        grid.addRow(row++, new Label("Email"), emailField);
        grid.addRow(row++, new Label("Phone"), phoneField);
        grid.addRow(row++, new Label("Address"), addressField);
        grid.addRow(row++, new Label("Date of Birth"), dateOfBirthPicker);
        grid.addRow(row++, new Label("Date Hired"), dateHiredPicker);
        grid.addRow(row++, new Label("Department"), departmentBox);
        grid.addRow(row, new Label("Position"), positionBox);

        if (existing != null) {
            usernameField.setDisable(true);
            passwordField.setPromptText("Leave blank to keep current password");
        }

        getDialogPane().setContent(grid);
    }

    private void refreshPositions(Department department) {
        if (department == null) {
            positionBox.setItems(javafx.collections.FXCollections.emptyObservableList());
            return;
        }
        List<Position> positions = new PositionRepository().findByDepartment(department);
        positionBox.setItems(javafx.collections.FXCollections.observableArrayList(positions));
    }

    private void populateIfEditing() {
        if (existing == null) {
            return;
        }
        usernameField.setText(existing.getUser().getUsername());
        employeeCodeField.setText(existing.getEmployeeCode());
        firstNameField.setText(existing.getFirstName());
        lastNameField.setText(existing.getLastName());
        emailField.setText(existing.getEmail());
        phoneField.setText(existing.getPhone());
        addressField.setText(existing.getAddress());
        dateOfBirthPicker.setValue(existing.getDateOfBirth());
        dateHiredPicker.setValue(existing.getDateHired());
        departmentBox.setValue(existing.getDepartment());
        refreshPositions(existing.getDepartment());
        positionBox.setValue(existing.getPosition());
    }

    private Employee submit() {
        String username = usernameField.getText().trim();
        String employeeCode = employeeCodeField.getText().trim();
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String email = emailField.getText().trim();
        Department department = departmentBox.getValue();
        Position position = positionBox.getValue();

        if (username.isEmpty() || employeeCode.isEmpty() || firstName.isEmpty()
                || lastName.isEmpty() || email.isEmpty() || department == null || position == null
                || dateHiredPicker.getValue() == null) {
            showValidationError("All fields except phone, address, and date of birth are required.");
            return null;
        }

        try {
            if (existing == null) {
                if (passwordField.getText() == null || passwordField.getText().isEmpty()) {
                    showValidationError("Password is required for a new employee.");
                    return null;
                }
                Role employeeRole = new RoleRepository().findByName("EMPLOYEE")
                        .orElseThrow(() -> new IllegalStateException("EMPLOYEE role not found"));
                com.retailhr.ems.model.entity.User user = ServiceFactory.authService()
                        .register(username, passwordField.getText(), employeeRole);
                return ServiceFactory.employeeService().onboardEmployee(
                        user, employeeCode, firstName, lastName, email, phoneField.getText(),
                        addressField.getText(), dateOfBirthPicker.getValue(), dateHiredPicker.getValue(),
                        department, position, SessionContext.getCurrentUser());
            } else {
                existing.setFirstName(firstName);
                existing.setLastName(lastName);
                existing.setEmail(email);
                existing.setPhone(phoneField.getText());
                existing.setAddress(addressField.getText());
                existing.setDateOfBirth(dateOfBirthPicker.getValue());
                existing.setDateHired(dateHiredPicker.getValue());
                existing.setDepartment(department);
                existing.setPosition(position);
                if (passwordField.getText() != null && !passwordField.getText().isEmpty()) {
                    ServiceFactory.authService().changePassword(existing.getUser(), passwordField.getText());
                }
                return new EmployeeRepository().update(existing);
            }
        } catch (IllegalArgumentException e) {
            showValidationError(e.getMessage());
            return null;
        }
    }

    private void showValidationError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setHeaderText("Validation Error");
        alert.showAndWait();
    }
}