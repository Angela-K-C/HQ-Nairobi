package com.example.drinksproject.rmi.shared;

import com.example.drinksproject.model.Branch;
import com.example.drinksproject.model.Stock;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface StockService extends Remote {

    List<Stock> getAllStocks(String currentBranch) throws RemoteException;

    List<Stock> getLowStockItems(int threshold, String branchName) throws RemoteException;

    boolean addStock(int branchId, int drinkId, int quantityToAdd) throws RemoteException;

    boolean deductStock(int branchId, int drinkId, int quantityUsed) throws RemoteException;

    boolean isOutOfStock(int branchId, int drinkId) throws RemoteException;

    List<Branch> getAllBranches() throws RemoteException;

    void registerAlertListener(StockAlertListener listener) throws RemoteException;
    void unregisterAlertListener(StockAlertListener listener) throws RemoteException;
}
