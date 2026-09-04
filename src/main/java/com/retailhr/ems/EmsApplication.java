package com.retailhr.ems;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class EmsApplication extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        Parent root = loadFxml("fxml/login");
        Scene scene = new Scene(root, 1000, 650);
        scene.getStylesheets().add(
                Objects.requireNonNull(EmsApplication.class.getResource("css/app.css")).toExternalForm()
        );
        stage.setTitle("Employee Management System");
        stage.setScene(scene);
        stage.setMinWidth(1000);
        stage.setMinHeight(650);
        stage.show();
    }

    public static Parent loadFxml(String fxmlName) throws IOException {
        FXMLLoader loader = new FXMLLoader(EmsApplication.class.getResource(fxmlName + ".fxml"));
        return loader.load();
    }

    public static void setRoot(String fxmlName) throws IOException {
        primaryStage.getScene().setRoot(loadFxml(fxmlName));
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}