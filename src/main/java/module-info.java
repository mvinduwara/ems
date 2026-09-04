module com.retailhr.ems {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;
    requires com.mysql.cj;
    requires com.zaxxer.hikaricp;
    requires com.google.zxing;
    requires com.google.zxing.javase;
    requires webcam.capture;
    requires jbcrypt;
    requires org.slf4j;
    requires jakarta.persistence;
    requires org.hibernate.orm.core;
    requires javafx.swing;

    opens com.retailhr.ems to javafx.fxml;
    opens com.retailhr.ems.controller to javafx.fxml;
    opens com.retailhr.ems.model.entity to javafx.fxml;
    opens com.retailhr.ems.util to javafx.fxml;

    exports com.retailhr.ems;
}