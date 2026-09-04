package com.retailhr.ems.controller.employee;

import com.retailhr.ems.controller.SessionContext;
import com.retailhr.ems.model.entity.LeaveRequest;
import com.retailhr.ems.service.ServiceFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.time.format.DateTimeFormatter;

public class MyLeaveController {

    @FXML
    private TableView<LeaveRequest> leaveTable;

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
    private TableColumn<LeaveRequest, String> commentColumn;

    @FXML
    private TableColumn<LeaveRequest, Void> actionsColumn;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private final ObservableList<LeaveRequest> data = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        leaveTypeColumn.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue().getLeaveType().getTypeName()));
        startDateColumn.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue().getStartDate().format(DATE_FMT)));
        endDateColumn.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue().getEndDate().format(DATE_FMT)));
        daysColumn.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(String.valueOf(d.getValue().getTotalDays())));
        reasonColumn.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(
                        d.getValue().getReason() == null ? "" : d.getValue().getReason()));
        statusColumn.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue().getStatus().name()));
        commentColumn.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(
                        d.getValue().getReviewComment() == null ? "" : d.getValue().getReviewComment()));

        addActionButtons();
        loadData();
    }

    private void addActionButtons() {
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button cancelBtn = new Button("Cancel");
            private final HBox box = new HBox(cancelBtn);

            {
                cancelBtn.getStyleClass().add("table-action-button-danger");
                cancelBtn.setOnAction(e -> handleCancel(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                LeaveRequest request = getTableView().getItems().get(getIndex());
                cancelBtn.setDisable(request.getStatus() != LeaveRequest.Status.PENDING);
                setGraphic(box);
            }
        });
    }

    private void loadData() {
        data.setAll(ServiceFactory.leaveService().getHistoryFor(SessionContext.getCurrentEmployee()));
        leaveTable.setItems(data);
    }

    @FXML
    private void handleNewRequest() {
        LeaveRequestDialog dialog = new LeaveRequestDialog(SessionContext.getCurrentEmployee());
        dialog.showAndWait().ifPresent(result -> loadData());
    }

    private void handleCancel(LeaveRequest request) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Cancel this leave request?", ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(button -> {
            if (button == ButtonType.YES) {
                try {
                    ServiceFactory.leaveService().cancel(request, SessionContext.getCurrentUser());
                    loadData();
                } catch (IllegalStateException e) {
                    showError(e.getMessage());
                }
            }
        });
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setHeaderText("Action Failed");
        alert.showAndWait();
    }
}