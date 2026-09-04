package com.retailhr.ems.controller.employee;

import com.retailhr.ems.controller.SessionContext;
import com.retailhr.ems.model.entity.Employee;
import com.retailhr.ems.util.QrCodeGenerator;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.nio.file.Path;

public class MyQrBadgeController {

    @FXML
    private Label employeeInfoLabel;

    @FXML
    private Label employeeCodeLabel;

    @FXML
    private ImageView qrImageView;

    private Employee employee;

    @FXML
    private void initialize() {
        employee = SessionContext.getCurrentEmployee();
        if (employee == null) {
            employeeInfoLabel.setText("No employee record found for this account.");
            return;
        }

        employeeInfoLabel.setText(employee.getFullName() + " — " + employee.getPosition().getPositionTitle());
        employeeCodeLabel.setText("Code: " + employee.getEmployeeCode());

        Image qrImage = QrCodeGenerator.toFxImage(employee.getQrCodeHash());
        qrImageView.setImage(qrImage);
    }

    @FXML
    private void handleSaveAsPng() {
        if (employee == null) {
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save QR Badge");
        fileChooser.setInitialFileName(employee.getEmployeeCode() + "_qr.png");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PNG Image", "*.png"));

        Stage stage = (Stage) qrImageView.getScene().getWindow();
        java.io.File file = fileChooser.showSaveDialog(stage);
        if (file == null) {
            return;
        }

        try {
            QrCodeGenerator.writeToPng(employee.getQrCodeHash(), Path.of(file.getAbsolutePath()));
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "QR badge saved successfully.", ButtonType.OK);
            alert.setHeaderText(null);
            alert.showAndWait();
        } catch (IllegalStateException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to save: " + e.getMessage(), ButtonType.OK);
            alert.setHeaderText(null);
            alert.showAndWait();
        }
    }
}