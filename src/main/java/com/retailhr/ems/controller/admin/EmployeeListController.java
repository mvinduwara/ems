package com.retailhr.ems.controller.admin;

import com.retailhr.ems.model.entity.Department;
import com.retailhr.ems.model.entity.Employee;
import com.retailhr.ems.service.ServiceFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import com.retailhr.ems.util.IconFactory;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class EmployeeListController {

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> statusFilter;

    @FXML
    private ComboBox<String> departmentFilter;

    @FXML
    private TableView<Employee> employeeTable;

    @FXML
    private TableColumn<Employee, String> codeColumn;

    @FXML
    private TableColumn<Employee, String> nameColumn;

    @FXML
    private TableColumn<Employee, String> emailColumn;

    @FXML
    private TableColumn<Employee, String> departmentColumn;

    @FXML
    private TableColumn<Employee, String> positionColumn;

    @FXML
    private TableColumn<Employee, String> statusColumn;

    @FXML
    private TableColumn<Employee, String> hiredColumn;

    @FXML
    private TableColumn<Employee, Void> actionsColumn;

    @FXML
    private Button addEmployeeButton;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private final ObservableList<Employee> masterData = FXCollections.observableArrayList();
    private final FilteredList<Employee> filteredData = new FilteredList<>(masterData, e -> true);
    private final Button editBtn = new Button("✎  Edit");
    private final Button statusBtn = new Button("↻  Status");

    @FXML
    private void initialize() {
        codeColumn.setCellValueFactory(new PropertyValueFactory<>("employeeCode"));
        nameColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getFullName()));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        departmentColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getDepartment().getDepartmentName()));
        positionColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getPosition().getPositionTitle()));
        statusColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getStatus().name()));
        hiredColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getDateHired().format(DATE_FMT)));

        addActionButtons();
        employeeTable.setItems(filteredData);
        addEmployeeButton.setGraphic(IconFactory.plusCircle(14));
        employeeTable.setPlaceholder(new Label("No employees found. Click \"+ Add Employee\" to onboard your first one."));

        statusFilter.setItems(FXCollections.observableArrayList(
                "ALL", "ACTIVE", "ON_LEAVE", "SUSPENDED", "TERMINATED"));
        statusFilter.setValue("ALL");
        statusFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        departmentFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        loadData();
    }

    private void addActionButtons() {
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Edit", new org.kordamp.ikonli.javafx.FontIcon("fea-edit-2"));
            private final Button statusBtn = new Button("Status", new org.kordamp.ikonli.javafx.FontIcon("fea-refresh-cw"));
            private final HBox box = new HBox(6.0, editBtn, statusBtn);

            {
                editBtn.getStyleClass().add("table-action-button");
                statusBtn.getStyleClass().add("table-action-button");
                editBtn.setOnAction(e -> handleEditEmployee(getTableView().getItems().get(getIndex())));
                statusBtn.setOnAction(e -> handleChangeStatus(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private void loadData() {
        masterData.setAll(ServiceFactory.employeeService().findAll());

        ObservableList<String> departments = FXCollections.observableArrayList("ALL");
        masterData.stream()
                .map(e -> e.getDepartment().getDepartmentName())
                .distinct()
                .sorted()
                .forEach(departments::add);
        departmentFilter.setItems(departments);
        departmentFilter.setValue("ALL");

        applyFilters();
    }

    private void applyFilters() {
        String search = searchField.getText() == null ? "" : searchField.getText().toLowerCase(Locale.ROOT).trim();
        String status = statusFilter.getValue();
        String department = departmentFilter.getValue();

        filteredData.setPredicate(employee -> {
            boolean matchesSearch = search.isEmpty()
                    || employee.getFullName().toLowerCase(Locale.ROOT).contains(search)
                    || employee.getEmployeeCode().toLowerCase(Locale.ROOT).contains(search);
            boolean matchesStatus = status == null || "ALL".equals(status)
                    || employee.getStatus().name().equals(status);
            boolean matchesDepartment = department == null || "ALL".equals(department)
                    || employee.getDepartment().getDepartmentName().equals(department);
            return matchesSearch && matchesStatus && matchesDepartment;
        });
    }

    @FXML
    private void handleAddEmployee() {
        openEmployeeDialog(null);
    }

    private void handleEditEmployee(Employee employee) {
        openEmployeeDialog(employee);
    }

    private void handleChangeStatus(Employee employee) {
        ChoiceDialog<Employee.Status> dialog = new ChoiceDialog<>(employee.getStatus(), Employee.Status.values());
        dialog.setTitle("Change Status");
        dialog.setHeaderText("Update status for " + employee.getFullName());
        dialog.setContentText("New status:");

        dialog.showAndWait().ifPresent(newStatus -> {
            ServiceFactory.employeeService().updateStatus(employee, newStatus,
                    com.retailhr.ems.controller.SessionContext.getCurrentUser());
            loadData();
        });
    }

    private void openEmployeeDialog(Employee existing) {
        EmployeeFormDialog dialog = new EmployeeFormDialog(existing);
        dialog.showAndWait().ifPresent(result -> loadData());
    }
}