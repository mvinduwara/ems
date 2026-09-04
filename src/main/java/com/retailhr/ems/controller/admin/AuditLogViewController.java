package com.retailhr.ems.controller.admin;

import com.retailhr.ems.model.entity.AuditLog;
import com.retailhr.ems.service.ServiceFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class AuditLogViewController {

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> entityFilter;

    @FXML
    private TableView<AuditLog> auditTable;

    @FXML
    private TableColumn<AuditLog, String> timestampColumn;

    @FXML
    private TableColumn<AuditLog, String> actorColumn;

    @FXML
    private TableColumn<AuditLog, String> actionColumn;

    @FXML
    private TableColumn<AuditLog, String> entityColumn;

    @FXML
    private TableColumn<AuditLog, String> entityIdColumn;

    @FXML
    private TableColumn<AuditLog, String> detailsColumn;

    private static final DateTimeFormatter TIMESTAMP_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy hh:mm:ss a");
    private static final int LOAD_LIMIT = 500;

    private final ObservableList<AuditLog> masterData = FXCollections.observableArrayList();
    private FilteredList<AuditLog> filteredData;

    @FXML
    private void initialize() {
        timestampColumn.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue().getCreatedAt().format(TIMESTAMP_FMT)));
        actorColumn.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(
                        d.getValue().getActor() == null ? "System" : d.getValue().getActor().getUsername()));
        actionColumn.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue().getAction()));
        entityColumn.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue().getEntityName()));
        entityIdColumn.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(
                        d.getValue().getEntityId() == null ? "—" : String.valueOf(d.getValue().getEntityId())));
        detailsColumn.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(
                        d.getValue().getDetails() == null ? "" : d.getValue().getDetails()));

        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        entityFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        loadData();
    }

    private void loadData() {
        masterData.setAll(ServiceFactory.auditService().getRecent(LOAD_LIMIT));

        ObservableList<String> entities = FXCollections.observableArrayList("ALL");
        masterData.stream()
                .map(AuditLog::getEntityName)
                .distinct()
                .sorted()
                .forEach(entities::add);
        entityFilter.setItems(entities);
        entityFilter.setValue("ALL");

        filteredData = new FilteredList<>(masterData, a -> true);
        auditTable.setItems(filteredData);
        applyFilters();
    }

    private void applyFilters() {
        String search = searchField.getText() == null ? "" : searchField.getText().toLowerCase(Locale.ROOT).trim();
        String entity = entityFilter.getValue();

        filteredData.setPredicate(log -> {
            boolean matchesSearch = search.isEmpty()
                    || log.getAction().toLowerCase(Locale.ROOT).contains(search)
                    || log.getEntityName().toLowerCase(Locale.ROOT).contains(search)
                    || (log.getDetails() != null && log.getDetails().toLowerCase(Locale.ROOT).contains(search));
            boolean matchesEntity = entity == null || "ALL".equals(entity)
                    || log.getEntityName().equals(entity);
            return matchesSearch && matchesEntity;
        });
    }

    @FXML
    private void handleRefresh() {
        loadData();
    }
}