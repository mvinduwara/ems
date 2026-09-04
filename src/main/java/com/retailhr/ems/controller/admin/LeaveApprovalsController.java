package com.retailhr.ems.controller.admin;

import com.retailhr.ems.controller.SessionContext;
import com.retailhr.ems.model.entity.LeaveRequest;
import com.retailhr.ems.service.ServiceFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class LeaveApprovalsController {

    @FXML
    private ComboBox<String> statusFilter;

    @FXML
    private TableView<LeaveRequest> leaveTable;

    @FXML
    private TableColumn<LeaveRequest, String> employeeColumn;

    @FXML
    private TableColumn<LeaveRequest, String> leaveTypeColumn;

    @FXML
    private TableColumn<LeaveRequest, String> startDateColumn;

    @FXML
    private TableColumn<LeaveRequest, String> endDateColumn;

    @FXML
    private TableColumn<LeaveRequest, String> daysColumn;

    @FXML
    private TableColumn<LeaveRequest, String> reasonColumn;

    @FXML
    private TableColumn<LeaveRequest, String> statusColumn;

    @FXML
    private TableColumn<LeaveRequest, String> reviewedColumn;

    @FXML
    private TableColumn<LeaveRequest, Void> actionsColumn;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private final ObservableList<LeaveRequest> masterData = FXCollections.observableArrayList();
    private FilteredList<LeaveRequest> filteredData;

    @FXML
    private void initialize() {
        employeeColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getEmployee().getFullName()));
        leaveTypeColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getLeaveType().getTypeName()));
        startDateColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getStartDate().format(DATE_FMT)));
        endDateColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getEndDate().format(DATE_FMT)));
        daysColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(String.valueOf(data.getValue().getTotalDays())));
        reasonColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getReason() == null ? "" : data.getValue().getReason()));
        statusColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getStatus().name()));
        reviewedColumn.setCellValueFactory(data -> {
            LeaveRequest request = data.getValue();
            String reviewer = request.getReviewedBy() == null ? "—" : request.getReviewedBy().getUsername();
            return new javafx.beans.property.SimpleStringProperty(reviewer);
        });

        addActionButtons();

        statusFilter.setItems(FXCollections.observableArrayList(
                "PENDING", "APPROVED", "REJECTED", "CANCELLED", "ALL"));
        statusFilter.setValue("PENDING");
        statusFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilter());

        loadData();
    }

    private void addActionButtons() {
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button approveBtn = new Button("Approve");
            private final Button rejectBtn = new Button("Reject");
            private final HBox box = new HBox(6.0, approveBtn, rejectBtn);

            {
                approveBtn.getStyleClass().add("table-action-button");
                rejectBtn.getStyleClass().add("table-action-button-danger");
                approveBtn.setOnAction(e -> handleApprove(getTableView().getItems().get(getIndex())));
                rejectBtn.setOnAction(e -> handleReject(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                LeaveRequest request = getTableView().getItems().get(getIndex());
                boolean pending = request.getStatus() == LeaveRequest.Status.PENDING;
                approveBtn.setDisable(!pending);
                rejectBtn.setDisable(!pending);
                setGraphic(box);
            }
        });
    }

    private void loadData() {
        masterData.setAll(ServiceFactory.leaveService().getPendingForApproval());
        filteredData = new FilteredList<>(masterData, r -> true);
        leaveTable.setItems(filteredData);
        applyFilter();
    }

    private void loadAllData() {
        List<LeaveRequest> all = ServiceFactory.leaveService().getPendingForApproval();
        masterData.setAll(all);
    }

    private void applyFilter() {
        String status = statusFilter.getValue();
        if (status == null || "PENDING".equals(status)) {
            masterData.setAll(ServiceFactory.leaveService().getPendingForApproval());
        }
        filteredData = new FilteredList<>(masterData, request ->
                status == null || "ALL".equals(status) || request.getStatus().name().equals(status));
        leaveTable.setItems(filteredData);
    }

    @FXML
    private void handleRefresh() {
        loadData();
    }

    private void handleApprove(LeaveRequest request) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Approve Leave Request");
        dialog.setHeaderText("Approve " + request.getEmployee().getFullName() + "'s "
                + request.getLeaveType().getTypeName() + " request?");
        dialog.setContentText("Comment (optional):");

        dialog.showAndWait().ifPresent(comment -> {
            try {
                ServiceFactory.leaveService().approve(request, SessionContext.getCurrentUser(), comment);
                loadData();
            } catch (IllegalStateException e) {
                showError(e.getMessage());
            }
        });
    }

    private void handleReject(LeaveRequest request) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Reject Leave Request");
        dialog.setHeaderText("Reject " + request.getEmployee().getFullName() + "'s "
                + request.getLeaveType().getTypeName() + " request?");
        dialog.setContentText("Reason for rejection:");

        dialog.showAndWait().ifPresent(comment -> {
            if (comment.isBlank()) {
                showError("A rejection reason is required.");
                return;
            }
            try {
                ServiceFactory.leaveService().reject(request, SessionContext.getCurrentUser(), comment);
                loadData();
            } catch (IllegalStateException e) {
                showError(e.getMessage());
            }
        });
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setHeaderText("Action Failed");
        alert.showAndWait();
    }
}