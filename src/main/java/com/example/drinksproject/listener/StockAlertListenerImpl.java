// com/example/drinksproject/listener/StockAlertListenerImpl.java

package com.example.drinksproject.listener;

import com.example.drinksproject.model.Stock;
import com.example.drinksproject.rmi.shared.StockAlertListener;
import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class StockAlertListenerImpl extends UnicastRemoteObject implements StockAlertListener {

    public StockAlertListenerImpl() throws RemoteException {
        super();
    }

    @Override
    public void onLowStock(List<Stock> lowStockItems) throws RemoteException {
        Platform.runLater(() -> {
            StringBuilder sb = new StringBuilder("⚠️ Global Low Stock Items:\n");
            for (Stock stock : lowStockItems) {
                sb.append(String.format("- %s @ %s: %d left\n",
                        stock.getDrinkName(), stock.getBranchName(), stock.getCurrentStock()));
            }

            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Global Low Stock");
            alert.setHeaderText("Low stock detected across branches");
            alert.setContentText(sb.toString());
            alert.showAndWait();
        });
    }
}
