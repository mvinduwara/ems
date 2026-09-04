package com.retailhr.ems.controller.employee;

import com.retailhr.ems.model.entity.Employee;
import com.retailhr.ems.model.entity.LeaveRequest;
import com.retailhr.ems.model.entity.LeaveType;
import com.retailhr.ems.repository.GenericRepository;
import com.retailhr.ems.service.ServiceFactory;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import com.retailhr.ems.repository.LeaveTypeRepository;

import java.time.LocalDate;
import java.util.List;

public class LeaveRequestDialog extends Dialog<LeaveRequest> {

    private final ComboBox<LeaveType> leaveTypeBox = new ComboBox<>();
    private final DatePicker startDatePicker = new DatePicker(LocalDate.now());
    private final DatePicker endDatePicker = new DatePicker(LocalDate.now());
    private final TextArea reasonArea = new TextArea();

    private final Employee employee;

    public LeaveRequestDialog(Employee employee) {
        this.employee = employee;
        setTitle("Request Leave");
        setHeaderText("Submit a new leave request");

        ButtonType submitButtonType = new ButtonType("Submit", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(submitButtonType, ButtonType.CANCEL);

        List<LeaveType> leaveTypes = new LeaveTypeRepository().findAll();
        leaveTypeBox.setItems(javafx.collections.FXCollections.observableArrayList(leaveTypes));
        leaveTypeBox.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(LeaveType t) {
                return t == null ? "" : t.getTypeName() + " (max " + t.getMaxDaysPerYear() + " days/yr)";
            }

            @Override
            public LeaveType fromString(String s) {
                return null;
            }
        });

        reasonArea.setPrefRowCount(3);
        reasonArea.setWrapText(true);

        GridPane grid = new GridPane();
        grid.setHgap(10.0);
        grid.setVgap(10.0);
        grid.setPadding(new Insets(20.0));
        grid.addRow(0, new Label("Leave Type"), leaveTypeBox);
        grid.addRow(1, new Label("Start Date"), startDatePicker);
        grid.addRow(2, new Label("End Date"), endDatePicker);
        grid.addRow(3, new Label("Reason"), reasonArea);

        getDialogPane().setContent(grid);

        setResultConverter(buttonType -> {
            if (buttonType == submitButtonType) {
                return submit();
            }
            return null;
        });
    }

    private LeaveRequest submit() {
        LeaveType leaveType = leaveTypeBox.getValue();
        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();

        if (leaveType == null || start == null || end == null) {
            showValidationError("Leave type, start date, and end date are required.");
            return null;
        }

        try {
            return ServiceFactory.leaveService().submitRequest(
                    employee, leaveType, start, end, reasonArea.getText());
        } catch (IllegalArgumentException | IllegalStateException e) {
            showValidationError(e.getMessage());
            return null;
        }
    }

    private void showValidationError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setHeaderText("Cannot Submit Request");
        alert.showAndWait();
    }
}