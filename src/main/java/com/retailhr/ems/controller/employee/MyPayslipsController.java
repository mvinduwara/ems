package com.retailhr.ems.controller.employee;

import com.retailhr.ems.controller.SessionContext;
import com.retailhr.ems.model.entity.PayrollRecord;
import com.retailhr.ems.service.ServiceFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.time.format.DateTimeFormatter;

public class MyPayslipsController {

    @FXML
    private TableView<PayrollRecord> payslipTable;

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

    private final ObservableList<PayrollRecord> data = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        periodColumn.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(
                        d.getValue().getPayPeriodStart().format(DATE_FMT) + " – "
                                + d.getValue().getPayPeriodEnd().format(DATE_FMT)));
        daysWorkedColumn.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(String.valueOf(d.getValue().getDaysWorked())));
        daysLeaveColumn.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(String.valueOf(d.getValue().getDaysOnLeave())));
        overtimeColumn.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue().getOvertimeHours().toPlainString()));
        deductionsColumn.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue().getDeductions().toPlainString()));
        bonusesColumn.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue().getBonuses().toPlainString()));
        netPayColumn.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue().getNetPay().toPlainString()));
        statusColumn.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue().getStatus().name()));

        addActionButtons();
        loadData();
    }

    private void addActionButtons() {
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button viewBtn = new Button("View");
            private final HBox box = new HBox(viewBtn);

            {
                viewBtn.getStyleClass().add("table-action-button");
                viewBtn.setOnAction(e -> showDetail(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private void loadData() {
        data.setAll(ServiceFactory.payrollService().getHistoryFor(SessionContext.getCurrentEmployee()));
        payslipTable.setItems(data);
    }

    @FXML
    private void handleRefresh() {
        loadData();
    }

    private void showDetail(PayrollRecord record) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Payslip Detail");
        dialog.setHeaderText(record.getPayPeriodStart().format(DATE_FMT) + " – "
                + record.getPayPeriodEnd().format(DATE_FMT));
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        GridPane grid = new GridPane();
        grid.setHgap(12.0);
        grid.setVgap(8.0);
        grid.setPadding(new javafx.geometry.Insets(20.0));

        int row = 0;
        grid.addRow(row++, new Label("Base Salary"), new Label(record.getBaseSalary().toPlainString()));
        grid.addRow(row++, new Label("Days Worked"), new Label(String.valueOf(record.getDaysWorked())));
        grid.addRow(row++, new Label("Days on Leave"), new Label(String.valueOf(record.getDaysOnLeave())));
        grid.addRow(row++, new Label("Overtime Hours"), new Label(record.getOvertimeHours().toPlainString()));
        grid.addRow(row++, new Label("Overtime Rate"), new Label(record.getOvertimeRate().toPlainString()));
        grid.addRow(row++, new Label("Deductions"), new Label(record.getDeductions().toPlainString()));
        grid.addRow(row++, new Label("Bonuses"), new Label(record.getBonuses().toPlainString()));
        grid.addRow(row++, new Label("Net Pay"), new Label(record.getNetPay().toPlainString()));
        grid.addRow(row, new Label("Status"), new Label(record.getStatus().name()));

        dialog.getDialogPane().setContent(grid);
        dialog.showAndWait();
    }
}