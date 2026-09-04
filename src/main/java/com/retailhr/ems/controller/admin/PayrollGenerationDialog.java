package com.retailhr.ems.controller.admin;

import com.retailhr.ems.controller.SessionContext;
import com.retailhr.ems.model.entity.Employee;
import com.retailhr.ems.model.entity.PayrollRecord;
import com.retailhr.ems.service.ServiceFactory;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PayrollGenerationDialog extends Dialog<PayrollRecord> {

    private final ComboBox<Employee> employeeBox = new ComboBox<>();
    private final DatePicker periodStartPicker = new DatePicker(LocalDate.now().withDayOfMonth(1));
    private final DatePicker periodEndPicker = new DatePicker(LocalDate.now());
    private final TextField overtimeHoursField = new TextField("0");
    private final TextField overtimeRateField = new TextField("0");
    private final TextField deductionsField = new TextField("0");
    private final TextField bonusesField = new TextField("0");

    public PayrollGenerationDialog(Employee preselected, ObservableList<Employee> employees) {
        setTitle("Generate Payroll");
        setHeaderText("Generate a payroll record for a pay period");

        ButtonType generateButtonType = new ButtonType("Generate", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(generateButtonType, ButtonType.CANCEL);

        employeeBox.setItems(employees);
        employeeBox.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(Employee e) {
                return e == null ? "" : e.getFullName();
            }

            @Override
            public Employee fromString(String s) {
                return null;
            }
        });
        if (preselected != null) {
            employeeBox.setValue(preselected);
        }

        GridPane grid = new GridPane();
        grid.setHgap(10.0);
        grid.setVgap(10.0);
        grid.setPadding(new Insets(20.0));
        grid.addRow(0, new Label("Employee"), employeeBox);
        grid.addRow(1, new Label("Period Start"), periodStartPicker);
        grid.addRow(2, new Label("Period End"), periodEndPicker);
        grid.addRow(3, new Label("Overtime Hours"), overtimeHoursField);
        grid.addRow(4, new Label("Overtime Rate"), overtimeRateField);
        grid.addRow(5, new Label("Deductions"), deductionsField);
        grid.addRow(6, new Label("Bonuses"), bonusesField);

        getDialogPane().setContent(grid);

        setResultConverter(buttonType -> {
            if (buttonType == generateButtonType) {
                return submit();
            }
            return null;
        });
    }

    private PayrollRecord submit() {
        Employee employee = employeeBox.getValue();
        LocalDate start = periodStartPicker.getValue();
        LocalDate end = periodEndPicker.getValue();

        if (employee == null || start == null || end == null) {
            showValidationError("Employee, period start, and period end are required.");
            return null;
        }
        if (end.isBefore(start)) {
            showValidationError("Period end cannot be before period start.");
            return null;
        }

        BigDecimal overtimeHours;
        BigDecimal overtimeRate;
        BigDecimal deductions;
        BigDecimal bonuses;
        try {
            overtimeHours = new BigDecimal(overtimeHoursField.getText().trim());
            overtimeRate = new BigDecimal(overtimeRateField.getText().trim());
            deductions = new BigDecimal(deductionsField.getText().trim());
            bonuses = new BigDecimal(bonusesField.getText().trim());
        } catch (NumberFormatException e) {
            showValidationError("Overtime hours, rate, deductions, and bonuses must be valid numbers.");
            return null;
        }

        try {
            return ServiceFactory.payrollService().generatePayroll(
                    employee, start, end, overtimeHours, overtimeRate, deductions, bonuses,
                    SessionContext.getCurrentUser());
        } catch (RuntimeException e) {
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