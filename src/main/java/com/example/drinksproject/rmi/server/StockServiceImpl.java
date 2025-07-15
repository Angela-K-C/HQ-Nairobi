package com.example.drinksproject.rmi.server;

import com.example.drinksproject.dao.BranchDao;
import com.example.drinksproject.dao.StockDao;
import com.example.drinksproject.model.Branch;
import com.example.drinksproject.model.Stock;
import com.example.drinksproject.rmi.shared.*;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class StockServiceImpl extends UnicastRemoteObject implements StockService {
    private final List<StockAlertListener> listeners = new ArrayList<>();
    public StockServiceImpl() throws RemoteException {
        super();
    }

    @Override
    public synchronized void registerAlertListener(StockAlertListener listener) throws RemoteException {
        if (!listeners.contains(listener)) listeners.add(listener);
    }

    @Override
    public synchronized void unregisterAlertListener(StockAlertListener listener) throws RemoteException {
        listeners.remove(listener);
    }

    private void notifyLowStock() {
        List<Stock> low = StockDao.getGlobalLowStockItems(10);
        for (StockAlertListener l : new ArrayList<>(listeners)) {
            try { l.onLowStock(low); }
            catch (RemoteException e) { listeners.remove(l); }
        }
    }


    @Override
    public List<Stock> getAllStocks(String currentBranch) throws RemoteException {
        return StockDao.getAllStocks(currentBranch);
    }

    @Override
    public List<Stock> getLowStockItems(int threshold, String branchName) throws RemoteException {
        return StockDao.getLowStockItems(threshold, branchName);
    }

    @Override
    public boolean addStock(int branchId, int drinkId, int quantityToAdd) throws RemoteException {
        return StockDao.addStock(branchId, drinkId, quantityToAdd);
    }

    @Override
    public boolean deductStock(int branchId, int drinkId, int quantityUsed) throws RemoteException {
        boolean success = StockDao.deductStock(branchId, drinkId, quantityUsed);
        if (success) {
            notifyLowStock();
        }
        return success;
    }


    @Override
    public boolean isOutOfStock(int branchId, int drinkId) throws RemoteException {
        return StockDao.isOutOfStock(branchId, drinkId);
    }

    @Override
    public List<Branch> getAllBranches() throws RemoteException {
        return BranchDao.getAllBranches();
    }

    public boolean isOutOfStock(int branchId, int drinkId, int qtyToRemove) throws RemoteException {
        return StockDao.isOutOfStock(branchId, drinkId, qtyToRemove);
    }




}
