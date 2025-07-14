package com.example.drinksproject.rmi.server;

import com.example.drinksproject.DBConnection;
import com.example.drinksproject.rmi.shared.OrderService;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.sql.*;
import java.util.List;
import java.util.ArrayList; // If you're creating new ArrayLists
import com.example.drinksproject.model.OrderDTO;
import com.example.drinksproject.rmi.shared.StockService;


public class OrderServiceImpl extends UnicastRemoteObject implements OrderService {

    public OrderServiceImpl() throws RemoteException {
        super();
    }

    @Override
    public int placeOrder(int customerId, int branchId) throws RemoteException {
        String query = "INSERT INTO `order` (customer_id, branch_id) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, customerId);
            stmt.setInt(2, branchId);
            int affected = stmt.executeUpdate();
            if (affected > 0) {
                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private int getBranchIdByOrder(int orderId) {
        String query = "SELECT branch_id FROM `order` WHERE order_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("branch_id");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }


    @Override
    public boolean addOrderItem(int orderId, int drinkId, int quantity, double totalPrice) throws RemoteException {
        String query = "INSERT INTO orderitem (order_id, drink_id, quantity, total_price) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, orderId);
            stmt.setInt(2, drinkId);
            stmt.setInt(3, quantity);
            stmt.setDouble(4, totalPrice);

            boolean inserted = stmt.executeUpdate() > 0;

            if (inserted) {
                // ✅ Get branchId for the order
                int branchId = getBranchIdByOrder(orderId);
                if (branchId == -1) return false;

                // ✅ Lookup StockService from RMI Registry
                Registry registry = LocateRegistry.getRegistry("localhost", 1099); // Adjust host/port as needed
                StockService stockService = (StockService) registry.lookup("StockService");

                // ✅ Deduct stock using RMI
                return stockService.deductStock(branchId, drinkId, quantity);
            }

        } catch (SQLException | NotBoundException | RemoteException e) {
            e.printStackTrace();
        }

        return false;
    }


    @Override
    public List<OrderDTO> getAllOrders(String searchString, String branchname) throws RemoteException {

        List<OrderDTO> orders = new ArrayList<>();

        String sql = "SELECT \n" +
                "    o.order_id,\n" +
                "    c.customer_name,\n" +
                "    b.branch_name,\n" +
                "    GROUP_CONCAT(CONCAT(oi.quantity, 'x ', d.drink_name) SEPARATOR ', ') AS items,\n" +
                "    SUM(oi.total_price) AS amount,\n" +
                "    o.order_date AS date\n" +
                "FROM `order` o\n" +
                "JOIN customer c ON o.customer_id = c.customer_id\n" +
                "JOIN branch b ON o.branch_id = b.branch_id\n" +
                "JOIN orderitem oi ON o.order_id = oi.order_id\n" +
                "JOIN drink d ON oi.drink_id = d.drink_id\n" +
                "WHERE customer_name LIKE ? OR branch_name LIKE ?\n" +
                "GROUP BY o.order_id, c.customer_name, b.branch_name, o.order_date\n" +
                "ORDER BY o.order_date DESC;\n";

        try {

            Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);

            stmt.setString(1, "%" + searchString + "%");
            stmt.setString(2, "%" + searchString + "%");

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                orders.add(new OrderDTO(
                        rs.getInt("order_id"),
                        rs.getString("customer_name"),
                        rs.getString("branch_name"),
                        rs.getString("items"),
                        rs.getInt("amount"),
                        rs.getString("date")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return orders;

    }

    @Override
    public int getOrderCount() throws RemoteException {
        String query = "SELECT COUNT(order_id) FROM `order`";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public double getTotalSales() throws RemoteException {
        String query = "SELECT SUM(total_price) FROM orderitem";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getDouble(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0;
    }



}
