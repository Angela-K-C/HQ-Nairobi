package com.example.drinksproject.controller;

import com.example.drinksproject.HelloApplication;
import com.example.drinksproject.Session;
import com.example.drinksproject.dao.CustomerDao;
import com.example.drinksproject.dao.OrderDao;
import com.example.drinksproject.model.*;
import com.example.drinksproject.rmi.shared.*;

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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DashboardController implements Initializable, CustomerService {

    // ========== FXML UI Bindings ==========
    @FXML
    private TableView<Order> ordersTable;
    @FXML
    private TableColumn<Order, String> orderIdCol;
    @FXML
    private TableColumn<Order, String> customerCol;
    @FXML
    private TableColumn<Order, String> branchCol;
    @FXML
    private TableColumn<Order, String> itemsCol;
    @FXML
    private TableColumn<Order, Double> amountCol;
    @FXML
    private TableColumn<Order, String> dateCol;

    @FXML
    private TabPane tabPane;
    @FXML
    private Tab addOrderTab, customersTab, viewReportsTab;
    @FXML
    private HBox quickActions;
    @FXML
    private Button viewReportsLink;

    @FXML
    private TextField customerNameField, customerPhoneField;
    @FXML
    private Label customerStatsLabel;
    @FXML
    private TableView<CustomerOrder> customersTable;
    @FXML
    private TableColumn<CustomerOrder, String> colCustomerName;
    @FXML
    private TableColumn<CustomerOrder, String> colCustomerID;
    @FXML
    private TableColumn<CustomerOrder, String> colCustomerPhone;
    @FXML
    private TableColumn<CustomerOrder, Integer> colTotalOrders;


    @FXML
    private ChoiceBox<Customer> customerChoiceBox;
    @FXML
    private ChoiceBox<Drink> drinkChoiceBox;
    @FXML
    private TextField quantityField;
    @FXML
    private Label itemPriceLabel;
    @FXML
    private VBox orderItemsList;
    @FXML
    private Label orderTotalLabel;

    @FXML
    private Label todaySalesLabel, ordersCountLabel, customersCountLabel;
    @FXML
    private TextField searchField;
    @FXML
    private Label branchNameLabel;


    @FXML private TableView<Stock> stocksTable;
    @FXML private TableColumn<Stock, String> stockBranchNameCol;
    @FXML private TableColumn<Stock, String> stockDrinkNameCol;
    @FXML private TableColumn<Stock, String> currentStockCol;

    @FXML private ChoiceBox<Drink> stockDrinkChoiceBox;
    @FXML private ChoiceBox<Branch> stockBranchChoiceBox;
    @FXML private TextField stockAddQuantityField;
    @FXML private Button restockButton;


    // ========== Internal Variables ==========
    private final List<OrderItem> orderItems = new ArrayList<>();
    private double totalOrderCost = 0.0;
    private boolean isHeadquarters = true;

    // RMI Services
    private OrderService orderService;
    private DrinkService drinkService;
    private CustomerService customerService;
    private StockService stockService;


    // Auto-refresh task
    private ScheduledExecutorService scheduler;

    // ========== Initialization ==========
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            orderService = (OrderService) Naming.lookup(RMIConfig.getURL("OrderService"));
            customerService = (CustomerService) Naming.lookup(RMIConfig.getURL("CustomerService"));
            drinkService = (DrinkService) Naming.lookup(RMIConfig.getURL("DrinkService"));
            stockService = (StockService) Naming.lookup(RMIConfig.getURL("StockService"));

        } catch (Exception e) {
            showAlert("❗ Could not connect to RMI services: " + e.getMessage());
            e.printStackTrace();
        }

        initializeTableColumns();
        setupUIBindings();
        loadOrders("");
        loadCustomersFromRMI();
        loadDrinksFromRMI();
        updateDashboardStats();
        loadStocksFromRMI();
        loadBranchesToChoiceBox();
        showLowStockWarnings();



        startAutoRefresh();

        if (!isHeadquarters) {
            tabPane.getTabs().remove(viewReportsTab);
            quickActions.getChildren().remove(viewReportsLink);
        }
    }

    private void initializeTableColumns() {
        orderIdCol.setCellValueFactory(cell -> cell.getValue().orderIdProperty().asString());
        customerCol.setCellValueFactory(cell -> cell.getValue().customerProperty());
        branchCol.setCellValueFactory(cell -> cell.getValue().branchProperty());
        itemsCol.setCellValueFactory(cell -> cell.getValue().itemsProperty());
        amountCol.setCellValueFactory(cell -> cell.getValue().amountProperty().asObject());
        dateCol.setCellValueFactory(cell -> cell.getValue().dateProperty());

//        table initialization for stocks
        stockBranchNameCol.setCellValueFactory(cell -> cell.getValue().branchNameProperty());
        stockDrinkNameCol.setCellValueFactory(cell -> cell.getValue().drinkNameProperty());
        currentStockCol.setCellValueFactory(cell -> cell.getValue().currentStockProperty().asString());

    }

    private void setupUIBindings() {
        branchNameLabel.setText(Session.getBranchName().toUpperCase() + " BRANCH");

        drinkChoiceBox.setOnAction(event -> {
            Drink selected = drinkChoiceBox.getValue();
            if (selected != null) {
                itemPriceLabel.setText("Ksh " + selected.getPrice());
            }
        });
    }

    private void loadOrders(String search) {
        try {
            List<OrderDTO> dtoList = orderService.getAllOrders("",search);
            List<Order> fxOrders = convertToJavaFXOrders(dtoList);
            ordersTable.setItems(FXCollections.observableArrayList(fxOrders));
        } catch (Exception e) {
            showAlert("❗ Failed to load orders: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadCustomersFromRMI() {
        try {
            List<Customer> customers = getAllCustomers();
            customerChoiceBox.setItems(FXCollections.observableArrayList(customers));
        } catch (Exception e) {
            showAlert("⚠️ Could not load customers: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadDrinksFromRMI() {
        try {
            List<Drink> drinks = drinkService.getAllDrinks();
            drinkChoiceBox.setItems(FXCollections.observableArrayList(drinks));
            stockDrinkChoiceBox.setItems(FXCollections.observableArrayList(drinks));

        } catch (Exception e) {
            showAlert("⚠️ Could not load drinks: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private List<Order> convertToJavaFXOrders(List<OrderDTO> dtoList) {
        List<Order> fxOrders = new ArrayList<>();
        for (OrderDTO dto : dtoList) {
            fxOrders.add(new Order(dto.getOrderId(), dto.getCustomer(), dto.getBranch(), dto.getItems(), dto.getAmount(), dto.getDate()));
        }
        return fxOrders;
    }

    private void updateDashboardStats() {
        try {
            customersCountLabel.setText(String.valueOf(getCustomerCount()));
            customerStatsLabel.setText(String.valueOf(getCustomerCount()) + " customers have been registered!");
            ordersCountLabel.setText(String.valueOf(orderService.getOrderCount()));
            todaySalesLabel.setText(String.format("Ksh %.2f", orderService.getTotalSales()));

            List<Customer> customers = getAllCustomers();
            List<CustomerOrder> customerorders = new ArrayList<>();

            colCustomerID.setCellValueFactory(new PropertyValueFactory<>("id"));
            colCustomerName.setCellValueFactory(new PropertyValueFactory<>("customerName"));
            colCustomerPhone.setCellValueFactory(new PropertyValueFactory<>("customerPhone"));
            colTotalOrders.setCellValueFactory(new PropertyValueFactory<>("totalOrders"));


            for (Customer customer : customers) {
                try {
                    int orderCount = orderService.getAllOrders("", customer.getName()).size();

                    CustomerOrder order = new CustomerOrder(
                            String.valueOf(customer.getId()),
                            customer.getName(),
                            customer.getPhone(),
                            orderCount
                    );

                    customerorders.add(order);
                } catch (RemoteException e) {
                    showAlert("Failed to load orders for " + customer.getName() + ": " + e.getMessage());
                }
            }


            customersTable.setItems(FXCollections.observableArrayList(customerorders));

        } catch (Exception e) {
            showAlert("⚠️ Failed to load dashboard stats: " + e.getMessage());
            System.out.print(e.getMessage());
        }
    }

    // ========== Auto-Refresh Logic ==========
    private void startAutoRefresh() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            Platform.runLater(() -> {
                try {
                    loadOrders(searchField.getText().trim());
                    loadCustomersFromRMI();
                    loadDrinksFromRMI();
                    updateDashboardStats();
                    loadStocksFromRMI();
                } catch (Exception e) {
                    System.err.println("Auto-refresh error: " + e.getMessage());
                }
            });
        }, 0, 10, TimeUnit.SECONDS); // Refresh every 10 seconds
    }

    public List<Customer> getAllCustomers() {
        try {
            return customerService.getAllCustomers(); // RMI call
        } catch (RemoteException e) {
            showAlert("Failed to retrieve customers: " + e.getMessage());
            return new ArrayList<>(); // return empty list on failure
        }
    }

    public boolean registerCustomer(String name, String phone) {
        try {
            return customerService.registerCustomer(name, phone); // RMI call
        } catch (RemoteException e) {
            showAlert("Failed to register customer: " + e.getMessage());
            return false;
        }
    }

    public int getCustomerCount() {
        try {
            return customerService.getAllCustomers().size(); // RMI call
        } catch (RemoteException e) {
            showAlert("Failed to get customer count: " + e.getMessage());
            return 0;
        }
    }


    // ========== UI Button Actions ==========

    @FXML
    private void searchOrders(ActionEvent event) {
        loadOrders(searchField.getText().trim());
    }

    @FXML
    private void goToAddOrder(ActionEvent event) {
        tabPane.getSelectionModel().select(addOrderTab);
    }

    @FXML
    private void goToAddCustomer(ActionEvent event) {
        tabPane.getSelectionModel().select(customersTab);
    }

    @FXML
    private void goToViewReports(ActionEvent event) {
        tabPane.getSelectionModel().select(viewReportsTab);
    }

    @FXML
    private void logout(ActionEvent event) throws IOException {
        // Stop scheduler
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }

        Session.clear();
        Parent root = FXMLLoader.load(HelloApplication.class.getResource("login.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        Platform.runLater(() -> stage.setMaximized(true));
        stage.show();
    }

    @FXML
    private void registerUser(ActionEvent event) {
        String name = customerNameField.getText().trim();
        String phone = customerPhoneField.getText().trim();

        if (name.isEmpty() || phone.isEmpty()) {
            customerStatsLabel.setText("❗ Please fill in all fields.");
            return;
        }

        try {
            boolean success = registerCustomer(name, phone);
            if (success) {
                customerStatsLabel.setText("✅ Customer registered remotely!");
                customerNameField.clear();
                customerPhoneField.clear();
                loadCustomersFromRMI();
            } else {
                customerStatsLabel.setText("❌ Registration failed on server.");
            }
        } catch (Exception e) {
            customerStatsLabel.setText("⚠️ RMI Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddItem(ActionEvent event) {
        Drink selectedDrink = drinkChoiceBox.getValue();
        String quantityText = quantityField.getText().trim();

        if (selectedDrink == null || quantityText.isEmpty()) {
            showAlert("❗ Please select a drink and enter quantity.");
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(quantityText);
            if (quantity <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showAlert("❗ Quantity must be a positive number.");
            return;
        }

        double price = selectedDrink.getPrice();
        double itemTotal = price * quantity;

        OrderItem item = new OrderItem(selectedDrink.getId(), selectedDrink.getName(), quantity, itemTotal);

        orderItems.add(item);

        Label itemLabel = new Label(selectedDrink.getName() + " x" + quantity + " - Ksh " + itemTotal);
        itemLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #374151;");
        orderItemsList.getChildren().add(itemLabel);

        totalOrderCost += itemTotal;
        orderTotalLabel.setText("Ksh " + totalOrderCost);

        drinkChoiceBox.setValue(null);
        quantityField.clear();
        itemPriceLabel.setText("Ksh 0");
    }

    @FXML
    private void handlePlaceOrder(ActionEvent event) {
        Customer selectedCustomer = customerChoiceBox.getValue();

        if (selectedCustomer == null) {
            showAlert("❗ Please select a customer.");
            return;
        }

        if (orderItems.isEmpty()) {
            showAlert("❗ Please add at least one item to the order.");
            return;
        }

        try {
            int branchId = Session.getBranchId();
            int orderId = orderService.placeOrder(selectedCustomer.getId(), branchId);

            if (orderId == -1) {
                showAlert("❌ Failed to place order.");
                return;
            }

            boolean allItemsInserted = true;
            for (OrderItem item : orderItems) {
                boolean success = orderService.addOrderItem(orderId, item.getDrinkId(), item.getQuantity(), item.getTotalPrice());

                if (!success) {
                    allItemsInserted = false;
                    break;
                }

                // Reduce stock for the branch
                stockService.deductStock(item.getDrinkId(), branchId, item.getQuantity());
            }

            if (allItemsInserted) {
                showAlert("✅ Order placed successfully!");
                resetOrderForm(); // this now preserves selected customer
                loadOrders(searchField.getText().trim());
                showLowStockWarnings();
            } else {
                showAlert("⚠️ Order saved but failed to save one or more items.");
            }

        } catch (Exception e) {
            showAlert("⚠️ RMI Error: " + e.getMessage());
            e.printStackTrace();
        }
    }


    //    load stocks from RMI
private void loadStocksFromRMI() {
    try {
        String currentBranch = Session.getBranchName(); // replace with your actual session class
        List<Stock> stockList = stockService.getAllStocks(currentBranch);
        stocksTable.setItems(FXCollections.observableArrayList(stockList));
    } catch (Exception e) {
        showAlert("⚠️ Failed to load stock data: " + e.getMessage());
    }
}


    private void loadBranchesToChoiceBox() {
        try {
            List<Branch> branches = stockService.getAllBranches();
            stockBranchChoiceBox.setItems(FXCollections.observableArrayList(branches));
        } catch (Exception e) {
            showAlert("⚠️ Could not load branches: " + e.getMessage());
        }
    }

//    Add restock Handler via RMI

    @FXML
    private void handleRestock(ActionEvent event) {
        String currentBranch = Session.getBranchName(); // Replace with your actual session class/method

        // ✅ Block non-Nairobi branches
        if (!"Nairobi".equalsIgnoreCase(currentBranch)) {
            showAlert("⛔ Only Nairobi branch is allowed to restock items.");
            return;
        }

        Branch selectedBranch = stockBranchChoiceBox.getValue();
        Drink selectedDrink = stockDrinkChoiceBox.getValue();
        String qtyText = stockAddQuantityField.getText().trim();

        if (selectedBranch == null || selectedDrink == null || qtyText.isEmpty()) {
            showAlert("❗ Please select a branch, a drink, and enter quantity.");
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(qtyText);
            if (quantity <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showAlert("❗ Quantity must be a positive number.");
            return;
        }

        try {
            boolean success = stockService.addStock(
                    selectedBranch.getBranchId(),
                    selectedDrink.getId(),
                    quantity
            );
            if (success) {
                showAlert("✅ Restocked successfully.");
                stockAddQuantityField.clear();
                stockBranchChoiceBox.setValue(null);
                stockDrinkChoiceBox.setValue(null);
                loadStocksFromRMI();
            } else {
                showAlert("❌ Failed to restock.");
            }
        } catch (RemoteException e) {
            showAlert("⚠️ RMI Error during restock: " + e.getMessage());
        }
    }



    //    show low stock warning
private void showLowStockWarnings() {
    try {
        String currentBranch = Session.getBranchName(); // Example
        List<Stock> lowStocks = stockService.getLowStockItems(10, currentBranch);

        if (!lowStocks.isEmpty()) {
            StringBuilder message = new StringBuilder("Low stock alerts:\n\n");
            for (Stock s : lowStocks) {
                message.append(String.format("- %s at %s: %d left\n", s.getDrinkName(), s.getBranchName(), s.getCurrentStock()));
            }

            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Low Stock Warning");
            alert.setHeaderText("Some items are running low!");
            alert.setContentText(message.toString());
            alert.showAndWait();
        }
    } catch (RemoteException e) {
        showAlert("⚠️ Could not check low stock: " + e.getMessage());
    }
}



    // ========== Utility Methods ==========

    private void resetOrderForm() {
        orderItems.clear();
        orderItemsList.getChildren().clear();
        orderTotalLabel.setText("Ksh 0");
        totalOrderCost = 0.0;
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}


