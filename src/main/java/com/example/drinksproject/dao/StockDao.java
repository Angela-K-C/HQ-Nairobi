package com.example.drinksproject.dao;

import com.example.drinksproject.DBConnection;
import com.example.drinksproject.model.Stock;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StockDao {

    public static List<Stock> getAllStocks(String currentBranch) {
        List<Stock> stocks = new ArrayList<>();
        String sql;

        if ("Nairobi".equalsIgnoreCase(currentBranch)) {
            // Nairobi sees everything
            sql = "SELECT d.drink_name, b.branch_name, s.quantity " +
                    "FROM stock s " +
                    "JOIN drink d ON s.drink_id = d.drink_id " +
                    "JOIN branch b ON s.branch_id = b.branch_id";
        } else {
            // Others only see their own branch's data
            sql = "SELECT d.drink_name, b.branch_name, s.quantity " +
                    "FROM stock s " +
                    "JOIN drink d ON s.drink_id = d.drink_id " +
                    "JOIN branch b ON s.branch_id = b.branch_id " +
                    "WHERE b.branch_name = ?";
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (!"Nairobi".equalsIgnoreCase(currentBranch)) {
                stmt.setString(1, currentBranch);
            }

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                stocks.add(new Stock(
                        rs.getString("branch_name"),
                        rs.getString("drink_name"),
                        rs.getInt("quantity")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return stocks;
    }


    public static List<Stock> getLowStockItems(int threshold, String currentBranch) {
        List<Stock> lowStocks = new ArrayList<>();
        String sql;

        if ("Nairobi".equalsIgnoreCase(currentBranch)) {
            // Nairobi sees all low stock items across all branches
            sql = "SELECT d.drink_name, b.branch_name, s.quantity " +
                    "FROM stock s " +
                    "JOIN drink d ON s.drink_id = d.drink_id " +
                    "JOIN branch b ON s.branch_id = b.branch_id " +
                    "WHERE s.quantity < ?";
        } else {
            // Other branches see only their own low stock items
            sql = "SELECT d.drink_name, b.branch_name, s.quantity " +
                    "FROM stock s " +
                    "JOIN drink d ON s.drink_id = d.drink_id " +
                    "JOIN branch b ON s.branch_id = b.branch_id " +
                    "WHERE s.quantity < ? AND b.branch_name = ?";
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, threshold);
            if (!"Nairobi".equalsIgnoreCase(currentBranch)) {
                stmt.setString(2, currentBranch);
            }

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                lowStocks.add(new Stock(
                        rs.getString("branch_name"),
                        rs.getString("drink_name"),
                        rs.getInt("quantity")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lowStocks;
    }



    public static boolean addStock(int branchId, int drinkId, int quantityToAdd) {
        String sql = "UPDATE stock SET quantity = quantity + ? WHERE branch_id = ? AND drink_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, quantityToAdd);
            stmt.setInt(2, branchId);
            stmt.setInt(3, drinkId);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deductStock(int branchId, int drinkId, int quantityUsed) {
        String sql = "UPDATE stock SET quantity = quantity - ? WHERE branch_id = ? AND drink_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, quantityUsed);
            stmt.setInt(2, branchId);
            stmt.setInt(3, drinkId);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean isOutOfStock(int branchId, int drinkId) {
        String sql = "SELECT quantity FROM stock WHERE branch_id = ? AND drink_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, branchId);
            stmt.setInt(2, drinkId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("quantity") <= 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static List<Stock> getStocksForBranch(int branchId) {
        List<Stock> stocks = new ArrayList<>();
        String sql = "SELECT d.drink_name, b.branch_name, s.quantity " +
                "FROM stock s " +
                "JOIN drink d ON s.drink_id = d.drink_id " +
                "JOIN branch b ON s.branch_id = b.branch_id " +
                "WHERE s.branch_id = ? " +
                "ORDER BY d.drink_name";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, branchId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                stocks.add(new Stock(
                        rs.getString("branch_name"),
                        rs.getString("drink_name"),
                        rs.getInt("quantity")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return stocks;
    }

    public static void initializeStockForAllBranches(int defaultQuantity) {
        String getBranchesQuery = "SELECT branch_id FROM branch";
        String getDrinksQuery = "SELECT drink_id FROM drink";
        String checkStockQuery = "SELECT COUNT(*) FROM stock WHERE branch_id = ? AND drink_id = ?";
        String insertStockQuery = "INSERT INTO stock(branch_id, drink_id, quantity) VALUES (?, ?, ?)";

        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement getBranchesStmt = conn.prepareStatement(getBranchesQuery);
            ResultSet branchRs = getBranchesStmt.executeQuery();

            while (branchRs.next()) {
                int branchId = branchRs.getInt("branch_id");

                try {
                    PreparedStatement getDrinksStmt = conn.prepareStatement(getDrinksQuery);
                    ResultSet drinksRs = getDrinksStmt.executeQuery();

                    while (drinksRs.next()) {
                        int drinkId = drinksRs.getInt("drink_id");

                        // Check if stock already exists
                        try {
                            PreparedStatement checkStmt = conn.prepareStatement(checkStockQuery);
                            checkStmt.setInt(1, branchId);
                            checkStmt.setInt(2, drinkId);

                            ResultSet checkRs = checkStmt.executeQuery();

                            if (checkRs.next() && checkRs.getInt(1) == 0) {
                                // Insert stock
                                try {
                                    PreparedStatement insertStmt = conn.prepareStatement(insertStockQuery);
                                    insertStmt.setInt(1, branchId);
                                    insertStmt.setInt(2, drinkId);
                                    insertStmt.setInt(3, defaultQuantity);
                                    insertStmt.executeUpdate();
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }
                } catch (SQLException | RuntimeException e) {
                    throw new RuntimeException(e);
                }

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<Stock> getLowStockForBranch(int branchId, int threshold) {
        List<Stock> lowStocks = new ArrayList<>();
        String sql = "SELECT d.drink_name, b.branch_name, s.quantity " +
                "FROM stock s " +
                "JOIN drink d ON s.drink_id = d.drink_id " +
                "JOIN branch b ON s.branch_id = b.branch_id " +
                "WHERE s.branch_id = ? AND s.quantity < ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, branchId);
            stmt.setInt(2, threshold);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                lowStocks.add(new Stock(
                        rs.getString("branch_name"),
                        rs.getString("drink_name"),
                        rs.getInt("quantity")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lowStocks;
    }

    // inside StockDao (package dao)
    public static List<Stock> getGlobalLowStockItems(int threshold) {
        List<Stock> lowStocks = new ArrayList<>();
        String sql = "SELECT d.drink_name, b.branch_name, s.quantity " +
                "FROM stock s JOIN drink d ON s.drink_id=d.drink_id " +
                "JOIN branch b ON s.branch_id=b.branch_id " +
                "WHERE s.quantity < ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {
            st.setInt(1, threshold);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    lowStocks.add(new Stock(
                            rs.getString("branch_name"),
                            rs.getString("drink_name"),
                            rs.getInt("quantity")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lowStocks;
    }



}
