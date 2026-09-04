package com.retailhr.ems.controller.admin;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.retailhr.ems.controller.ClosableView;
import com.retailhr.ems.model.entity.AttendanceLog;
import com.retailhr.ems.model.entity.Employee;
import com.retailhr.ems.service.ServiceFactory;
import com.retailhr.ems.util.QrCodeScanner;
import javafx.application.Platform;
import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.StackPane;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AttendanceViewController implements ClosableView {

    @FXML
    private StackPane cameraContainer;

    @FXML
    private Button startButton;

    @FXML
    private Button stopButton;

    @FXML
    private Label statusLabel;

    @FXML
    private Label lastScanLabel;

    @FXML
    private ListView<String> attendanceListView;

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("hh:mm:ss a");

    private QrCodeScanner scanner;
    private SwingNode swingNode;

    @FXML
    private void initialize() {
        swingNode = new SwingNode();
        cameraContainer.getChildren().add(swingNode);
        refreshAttendanceList();
    }

    @FXML
    private void handleStart() {
        try {
            Webcam webcam = QrCodeScanner.getDefaultWebcam();
            scanner = new QrCodeScanner(webcam);

            WebcamPanel panel = scanner.createPanel();
            javax.swing.SwingUtilities.invokeLater(() -> swingNode.setContent(panel));

            scanner.start(this::onQrDecoded, this::onScanError);

            startButton.setDisable(true);
            stopButton.setDisable(false);
            statusLabel.setText("Scanning...");
        } catch (IllegalStateException e) {
            statusLabel.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    private void handleStop() {
        stopScanning();
        statusLabel.setText("Scanner stopped");
    }

    private void stopScanning() {
        if (scanner != null) {
            scanner.stop();
            scanner = null;
        }
        Platform.runLater(() -> swingNode.setContent(null));
        startButton.setDisable(false);
        stopButton.setDisable(true);
    }

    private void onQrDecoded(String qrCodeHash) {
        Platform.runLater(() -> {
            try {
                AttendanceLog log = ServiceFactory.attendanceService()
                        .scanQrCode(qrCodeHash, "Admin Station Webcam");
                Employee employee = log.getEmployee();
                String message = employee.getFullName() + " — " + log.getScanType().name().replace('_', ' ')
                        + " at " + log.getScannedAt().format(TIME_FMT);
                lastScanLabel.setText(message);
                lastScanLabel.setStyle("-fx-text-fill: #1a7f37;");
                refreshAttendanceList();
            } catch (IllegalArgumentException e) {
                lastScanLabel.setText("Unrecognized QR code");
                lastScanLabel.setStyle("-fx-text-fill: #cf222e;");
            } catch (IllegalStateException e) {
                lastScanLabel.setText(e.getMessage());
                lastScanLabel.setStyle("-fx-text-fill: #cf222e;");
            }
        });
    }

    private void onScanError(Throwable error) {
        Platform.runLater(() -> statusLabel.setText("Camera error: " + error.getMessage()));
    }

    private void refreshAttendanceList() {
        List<Employee> activeEmployees = ServiceFactory.employeeService().findAllActive();
        LocalDate today = LocalDate.now();

        attendanceListView.getItems().clear();
        for (Employee employee : activeEmployees) {
            List<AttendanceLog> logs = ServiceFactory.attendanceService().getTodayLogs(employee);
            if (logs.isEmpty()) {
                continue;
            }
            StringBuilder line = new StringBuilder(employee.getFullName()).append("  —  ");
            for (int i = 0; i < logs.size(); i++) {
                AttendanceLog log = logs.get(i);
                line.append(log.getScanType() == AttendanceLog.ScanType.CLOCK_IN ? "IN " : "OUT ")
                        .append(log.getScannedAt().format(TIME_FMT));
                if (i < logs.size() - 1) {
                    line.append("  |  ");
                }
            }
            attendanceListView.getItems().add(line.toString());
        }

        if (attendanceListView.getItems().isEmpty()) {
            attendanceListView.getItems().add("No attendance recorded yet today");
        }
    }

    @Override
    public void onClose() {
        stopScanning();
    }
}