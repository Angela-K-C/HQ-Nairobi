package com.example.drinksproject.model;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

import java.io.Serializable;

public class Branch implements Serializable {

    private static final long serialVersionUID = 1L;

    // JavaFX properties (transient to avoid serialization issues)
    private transient SimpleIntegerProperty branchId;
    private transient SimpleStringProperty branchName;

    // Serializable fields (used for RMI)
    private int _branchId;
    private String _branchName;

    public Branch() {
        this(0, "");
    }

    public Branch(int branchId, String branchName) {
        this.branchId = new SimpleIntegerProperty(branchId);
        this.branchName = new SimpleStringProperty(branchName);
        this._branchId = branchId;
        this._branchName = branchName;
    }

    public int getBranchId() {
        return branchId != null ? branchId.get() : _branchId;
    }

    public void setBranchId(int branchId) {
        if (this.branchId != null) {
            this.branchId.set(branchId);
        }
        this._branchId = branchId;
    }

    public SimpleIntegerProperty branchIdProperty() {
        if (branchId == null) branchId = new SimpleIntegerProperty(_branchId);
        return branchId;
    }

    public String getBranchName() {
        return branchName != null ? branchName.get() : _branchName;
    }

    public void setBranchName(String branchName) {
        if (this.branchName != null) {
            this.branchName.set(branchName);
        }
        this._branchName = branchName;
    }

    public SimpleStringProperty branchNameProperty() {
        if (branchName == null) branchName = new SimpleStringProperty(_branchName);
        return branchName;
    }

    @Override
    public String toString() {
        return getBranchName();
    }
}
