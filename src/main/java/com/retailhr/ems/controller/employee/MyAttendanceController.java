package com.retailhr.ems.controller.employee;

import com.retailhr.ems.controller.SessionContext;
import com.retailhr.ems.model.entity.AttendanceLog;
import com.retailhr.ems.service.ServiceFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MyAttendanceController {

    @FXML
    private DatePicker fromDatePicker;

    @FXML
    private DatePicker toDatePicker;

    @FXML
    private Label daysPresentLabel;

    @FXML
    private Label totalHoursLabel;

    @FXML
    private TableView<DayAttendanceRow> attendanceTable;

    @FXML
    private TableColumn<DayAttendanceRow, String> dateColumn;

    @FXML
    private TableColumn<DayAttendanceRow, String> clockInColumn;

    @FXML
    private TableColumn<DayAttendanceRow, String> clockOutColumn;

    @FXML
    private TableColumn<DayAttendanceRow, String> hoursColumn;

    @FXML
    private TableColumn<DayAttendanceRow, String> methodColumn;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("hh:mm a");

    private final ObservableList<DayAttendanceRow> rows = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        dateColumn.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue().date));
        clockInColumn.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue().clockIn));
        clockOutColumn.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue().clockOut));
        hoursColumn.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue().hours));
        methodColumn.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue().method));

        attendanceTable.setItems(rows);

        toDatePicker.setValue(LocalDate.now());
        fromDatePicker.setValue(LocalDate.now().withDayOfMonth(1));

        loadData();
    }

    @FXML
    private void handleFilter() {
        loadData();
    }

    private void loadData() {
        LocalDate from = fromDatePicker.getValue();
        LocalDate to = toDatePicker.getValue();
        if (from == null || to == null || to.isBefore(from)) {
            return;
        }

        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.plusDays(1).atStartOfDay();

        List<AttendanceLog> logs = ServiceFactory.attendanceService()
                .getLogsBetween(SessionContext.getCurrentEmployee(), start, end);

        Map<LocalDate, List<AttendanceLog>> byDay = new LinkedHashMap<>();
        for (AttendanceLog log : logs) {
            byDay.computeIfAbsent(log.getScannedAt().toLocalDate(), k -> new ArrayList<>()).add(log);
        }

        List<DayAttendanceRow> result = new ArrayList<>();
        long totalMinutes = 0;
        int daysPresent = 0;

        for (Map.Entry<LocalDate, List<AttendanceLog>> entry : byDay.entrySet()) {
            List<AttendanceLog> dayLogs = entry.getValue();
            AttendanceLog clockIn = dayLogs.stream()
                    .filter(l -> l.getScanType() == AttendanceLog.ScanType.CLOCK_IN)
                    .findFirst().orElse(null);
            AttendanceLog clockOut = dayLogs.stream()
                    .filter(l -> l.getScanType() == AttendanceLog.ScanType.CLOCK_OUT)
                    .reduce((first, second) -> second).orElse(null);

            String hoursDisplay = "—";
            if (clockIn != null && clockOut != null) {
                Duration worked = Duration.between(clockIn.getScannedAt(), clockOut.getScannedAt());
                long minutes = worked.toMinutes();
                totalMinutes += minutes;
                hoursDisplay = String.format("%d h %02d m", minutes / 60, minutes % 60);
            }

            if (clockIn != null) {
                daysPresent++;
            }

            result.add(new DayAttendanceRow(
                    entry.getKey().format(DATE_FMT),
                    clockIn != null ? clockIn.getScannedAt().format(TIME_FMT) : "—",
                    clockOut != null ? clockOut.getScannedAt().format(TIME_FMT) : "—",
                    hoursDisplay,
                    clockIn != null ? clockIn.getScanMethod().name() : "—"
            ));
        }

        result.sort((a, b) -> b.date.compareTo(a.date));
        rows.setAll(result);

        daysPresentLabel.setText("Days Present: " + daysPresent);
        totalHoursLabel.setText(String.format("Total Hours: %d h %02d m", totalMinutes / 60, totalMinutes % 60));
    }

    private static class DayAttendanceRow {
        final String date;
        final String clockIn;
        final String clockOut;
        final String hours;
        final String method;

        DayAttendanceRow(String date, String clockIn, String clockOut, String hours, String method) {
            this.date = date;
            this.clockIn = clockIn;
            this.clockOut = clockOut;
            this.hours = hours;
            this.method = method;
        }
    }
}