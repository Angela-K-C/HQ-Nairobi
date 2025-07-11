package com.example.drinksproject.rmi.server;

import com.example.drinksproject.DBConnection;
import com.example.drinksproject.rmi.shared.LoginResponseDTO;
import com.example.drinksproject.rmi.shared.LoginService;

import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginServiceImpl extends UnicastRemoteObject implements LoginService {

    public LoginServiceImpl() throws RemoteException {
        super();
    }

    @Override
    public boolean registerUser(String username, String password, String branchName) throws RemoteException {
        try (Connection connection = DBConnection.getConnection()){
                String getBranchIdQuery = "SELECT branch_id FROM branch WHERE (branch_name) = ? ";
                String checkUserExists = "SELECT * FROM admin WHERE username = ?";
                String insertUserQuery = "INSERT INTO admin (username, password, branch_id) VALUES (?,?,?)";

                int branchId = -1;

                try (PreparedStatement statement = connection.prepareStatement(getBranchIdQuery)) {
                    statement.setString(1, branchName);

                    ResultSet resultSet = statement.executeQuery();
                    if (rs.next()) {
                        branchId = rs.getInt("branch_id");
                    }
                }
        }

                {

        }
    }

    @Override
    public LoginResponseDTO login(String username, String password, String branchName) throws RemoteException {
        String query = """
            SELECT u.username, b.branch_id, b.branch_name
            FROM admin u
            JOIN branch b ON u.branch_id = b.branch_id
            WHERE u.username = ? AND u.password = ? AND b.branch_name = ?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, branchName);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new LoginResponseDTO(
                        rs.getString("username"),
                        rs.getInt("branch_id"),
                        rs.getString("branch_name")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
