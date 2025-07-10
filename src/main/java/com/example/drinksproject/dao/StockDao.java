package com.example.drinksproject.dao;

import com.example.drinksproject.DBConnection;
import com.example.drinksproject.model.Stock;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StockDao {

    public static List<Stock> getAllStocks() {
        List<Stock> stocks = new ArrayList<>();
        String sql = "SELECT d.drink_name, b.branch_name, s.quantity " +
                "FROM stock s " +
                "JOIN drink d ON s.drink_id = d.drink_id " +
                "JOIN branch b ON s.branch_id = b.branch_id " +
                "ORDER BY b.branch_name, d.drink_name";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

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

    public static List<Stock> getLowStockItems(int threshold) {
        List<Stock> lowStocks = new ArrayList<>();
        String sql = "SELECT d.drink_name, b.branch_name, s.quantity " +
                "FROM stock s " +
                "JOIN drink d ON s.drink_id = d.drink_id " +
                "JOIN branch b ON s.branch_id = b.branch_id " +
                "WHERE s.quantity < ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, threshold);
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

}
