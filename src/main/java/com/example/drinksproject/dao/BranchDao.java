package com.example.drinksproject.dao;

import com.example.drinksproject.DBConnection;
import com.example.drinksproject.model.Branch;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BranchDao {

    public static List<Branch> getAllBranches() {
        List<Branch> branches = new ArrayList<>();

        String sql = "SELECT branch_id, branch_name FROM branch";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("branch_id");
                String name = rs.getString("branch_name");

                branches.add(new Branch(id, name));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return branches;
    }
}
