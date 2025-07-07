module com.example.drinksproject {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;
    requires mysql.connector.j;
    requires java.rmi;

    opens com.example.drinksproject to javafx.fxml;
    exports com.example.drinksproject;

    opens com.example.drinksproject.controller to javafx.fxml;
    exports com.example.drinksproject.controller;

    opens com.example.drinksproject.rmi.shared to java.rmi;
    exports com.example.drinksproject.rmi.shared;

    exports com.example.drinksproject.model;
    opens com.example.drinksproject.model to javafx.fxml;
}
