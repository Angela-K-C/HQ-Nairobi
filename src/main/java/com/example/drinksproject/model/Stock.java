package com.example.drinksproject.model;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

import java.io.Serializable;

public class Stock implements Serializable {

    private static final long serialVersionUID = 1L;

    // These fields are NOT Serializable, so we need extra fields for serialization
    private transient SimpleStringProperty branchName;
    private transient SimpleStringProperty drinkName;
    private transient SimpleIntegerProperty currentStock;

    // For RMI serialization
    private String _branchName;
    private String _drinkName;
    private int _currentStock;

    public Stock(String branchName, String drinkName, int currentStock) {
        this.branchName = new SimpleStringProperty(branchName);
        this.drinkName = new SimpleStringProperty(drinkName);
        this.currentStock = new SimpleIntegerProperty(currentStock);

        this._branchName = branchName;
        this._drinkName = drinkName;
        this._currentStock = currentStock;
    }

    // Use these for JavaFX bindings
    public String getBranchName() {
        return branchName != null ? branchName.get() : _branchName;
    }

    public SimpleStringProperty branchNameProperty() {
        if (branchName == null) branchName = new SimpleStringProperty(_branchName);
        return branchName;
    }

    public String getDrinkName() {
        return drinkName != null ? drinkName.get() : _drinkName;
    }

    public SimpleStringProperty drinkNameProperty() {
        if (drinkName == null) drinkName = new SimpleStringProperty(_drinkName);
        return drinkName;
    }

    public int getCurrentStock() {
        return currentStock != null ? currentStock.get() : _currentStock;
    }

    public SimpleIntegerProperty currentStockProperty() {
        if (currentStock == null) currentStock = new SimpleIntegerProperty(_currentStock);
        return currentStock;
    }
}
