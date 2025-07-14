package com.example.drinksproject;

import com.example.drinksproject.dao.StockDao;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        Parent root = FXMLLoader.load(HelloApplication.class.getResource("login.fxml"));
        Scene scene = new Scene(root);
        stage.setTitle("Lemonade Farm");
        stage.setMaximized(true);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        // Initialize stock for all branches
        int defaultQuantity = 20;
        StockDao.initializeStockForAllBranches(defaultQuantity);

        launch(args);
    }
}