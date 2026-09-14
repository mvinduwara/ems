package com.retailhr.ems.controller.admin;

import com.retailhr.ems.controller.SessionContext;
import com.retailhr.ems.model.entity.Employee;
import com.retailhr.ems.model.entity.PayrollRecord;
import com.retailhr.ems.service.ServiceFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class PayrollViewController {

    @FXML
    private ComboBox<Employee> employeeFilter;

    @FXML
    private TableView<PayrollRecord> payrollTable;

    @FXML
    private TableColumn<PayrollRecord, String> employeeColumn;

    @FXML
    private TableColumn<PayrollRecord, String> periodColumn;

    @FXML
    private TableColumn<PayrollRecord, String> daysWorkedColumn;

    @FXML
    private TableColumn<PayrollRecord, String> daysLeaveColumn;

    @FXML
    private TableColumn<PayrollRecord, String> overtimeColumn;

    @FXML
    private TableColumn<PayrollRecord, String> deductionsColumn;

    @FXML
    private TableColumn<PayrollRecord, String> bonusesColumn;

    @FXML
    private TableColumn<PayrollRecord, String> netPayColumn;

    @FXML
    private TableColumn<PayrollRecord, String> statusColumn;

    @FXML
    private TableColumn<PayrollRecord, Void> actionsColumn;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private final ObservableList<PayrollRecord> masterData = FXCollections.observableArrayList();

    private final Button finalizeBtn = new Button("✓  Finalize");

    private final Button payBtn = new Button("$  Mark Paid");

    @FXML
    private void initialize() {
        employeeColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getEmployee().getFullName()));
        periodColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getPayPeriodStart().format(DATE_FMT) + " – "
                                + data.getValue().getPayPeriodEnd().format(DATE_FMT)));
        daysWorkedColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(String.valueOf(data.getValue().getDaysWorked())));
        daysLeaveColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(String.valueOf(data.getValue().getDaysOnLeave())));
        overtimeColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getOvertimeHours().toPlainString()));
        deductionsColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getDeductions().toPlainString()));
        bonusesColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getBonuses().toPlainString()));
        netPayColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getNetPay().toPlainString()));
        statusColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getStatus().name()));

        addActionButtons();

        payrollTable.setPlaceholder(new Label("No payroll records for this employee yet."));

        employeeFilter.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(Employee e) {
                return e == null ? "" : e.getFullName();
            }

            @Override
            public Employee fromString(String s) {
                return null;
            }
        });
        employeeFilter.setItems(FXCollections.observableArrayList(
                ServiceFactory.employeeService().findAllActive()));
        employeeFilter.valueProperty().addListener((obs, oldVal, newVal) -> loadData());

        if (!employeeFilter.getItems().isEmpty()) {
            employeeFilter.setValue(employeeFilter.getItems().get(0));
        }
    }

    private void addActionButtons() {
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button finalizeBtn = new Button("Finalize", new org.kordamp.ikonli.javafx.FontIcon("fea-check-circle"));
            private final Button payBtn = new Button("Mark Paid", new org.kordamp.ikonli.javafx.FontIcon("fea-dollar-sign"));
            private final HBox box = new HBox(6.0, finalizeBtn, payBtn);

            {
                finalizeBtn.getStyleClass().add("table-action-button");
                payBtn.getStyleClass().add("table-action-button");
                finalizeBtn.setOnAction(e -> handleFinalize(getTableView().getItems().get(getIndex())));
                payBtn.setOnAction(e -> handleMarkPaid(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                PayrollRecord record = getTableView().getItems().get(getIndex());
                finalizeBtn.setDisable(record.getStatus() != PayrollRecord.Status.DRAFT);
                payBtn.setDisable(record.getStatus() != PayrollRecord.Status.FINALIZED);
                setGraphic(box);
            }
        });
    }

    private void loadData() {
        Employee selected = employeeFilter.getValue();
        if (selected == null) {
            masterData.clear();
            payrollTable.setItems(masterData);
            return;
        }
        List<PayrollRecord> records = ServiceFactory.payrollService().getHistoryFor(selected);
        masterData.setAll(records);
        payrollTable.setItems(masterData);
    }

    @FXML
    private void handleGenerate() {
        Employee selected = employeeFilter.getValue();
        PayrollGenerationDialog dialog = new PayrollGenerationDialog(selected, employeeFilter.getItems());
        dialog.showAndWait().ifPresent(result -> loadData());
    }

    private void handleFinalize(PayrollRecord record) {
        try {
            ServiceFactory.payrollService().finalizePayroll(record, SessionContext.getCurrentUser());
            loadData();
        } catch (IllegalStateException e) {
            showError(e.getMessage());
        }
    }

    private void handleMarkPaid(PayrollRecord record) {
        try {
            ServiceFactory.payrollService().markAsPaid(record, SessionContext.getCurrentUser());
            loadData();
        } catch (IllegalStateException e) {
            showError(e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setHeaderText("Action Failed");
        alert.showAndWait();
    }
}