package com.example.drinksproject.controller;

import com.example.drinksproject.HelloApplication;
import com.example.drinksproject.Session;
import com.example.drinksproject.rmi.shared.LoginResponseDTO;
import com.example.drinksproject.rmi.shared.LoginService;
import com.example.drinksproject.rmi.shared.RMIConfig;

import com.example.drinksproject.rmi.shared.StockService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.rmi.Naming;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    private LoginService loginService;

    @FXML private ChoiceBox<String> branchComboBox;
    @FXML private TextField usernameTextField;
    @FXML private PasswordField passwordTextField;
    @FXML private Label statusLabel;

    private Stage stage;
    private Scene scene;
    private Parent root;

    private final String[] branches = {"Select your branch", "Nairobi", "Nakuru", "Mombasa", "Kisumu"};

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        branchComboBox.setItems(FXCollections.observableArrayList(branches));
        branchComboBox.setValue(branches[0]);
        statusLabel.setVisible(false);

        try {
            loginService = (LoginService) Naming.lookup(RMIConfig.getURL("LoginService"));
        } catch (Exception e) {
            statusLabel.setText("❗ Could not connect to login service.");
            e.printStackTrace();
        }
    }

    public void onSubmit(ActionEvent event) {
        String username = usernameTextField.getText().trim();
        String password = passwordTextField.getText().trim();
        String branch = branchComboBox.getValue();

        if (username.isEmpty() || password.isEmpty() || branch.equals("Select your branch")) {
            statusLabel.setText("⚠️ Please fill in all fields correctly.");
            statusLabel.setVisible(true);
            return;
        }

        try {
            LoginResponseDTO response = loginService.login(username, password, branch);
            if (response != null) {
                Session.set(response.getUsername(), response.getBranchId(), response.getBranchName());
                statusLabel.setText("✅ Login successful!");
                statusLabel.setVisible(true);

                new Thread(() -> {
                    try {
                        Thread.sleep(1000);
                        Platform.runLater(() -> {
                            try {
                                FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("dashboard.fxml"));
                                Parent root = loader.load();

// Get the DashboardController instance
                                DashboardController dashboardController = loader.getController();

                                try {
                                    // Manually inject the stockService here
                                    StockService stockService = (StockService) Naming.lookup(RMIConfig.getURL("StockService"));
                                    dashboardController.setStockService(stockService);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    statusLabel.setText("❗ Could not load StockService.");
                                    statusLabel.setVisible(true);
                                    return; // Stop if the StockService setup fails
                                }

// Continue to load the dashboard scene
                                stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                                scene = new Scene(root);
                                stage.setScene(scene);
                                stage.setMaximized(true);
                                stage.show();

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();

            } else {
                statusLabel.setText("❌ Invalid username, password, or branch.");
                statusLabel.setVisible(true);
            }

        } catch (Exception e) {
            statusLabel.setText("⚠️ Login error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
