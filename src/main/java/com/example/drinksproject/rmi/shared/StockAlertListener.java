package com.example.drinksproject.rmi.shared;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import com.example.drinksproject.model.Stock;

public interface StockAlertListener extends Remote {
    void onLowStock(List<Stock> lowStockItems) throws RemoteException;
}
