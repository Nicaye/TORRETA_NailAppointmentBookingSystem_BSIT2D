
package com.nailbooking.dao;

import com.nailbooking.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDao {

    // Register a new user with PENDING status
    public boolean registerUser(
            String username, String password, String roles)
            throws SQLException {

        String sql = """
            INSERT INTO users (username, password, roles, status)
            VALUES (?, ?, ?, 'PENDING')
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);
            statement.setString(2, password);
            statement.setString(3, roles.toUpperCase());

            return statement.executeUpdate() > 0;
        }
    }

    // Log in approved accounts and preserve access for existing ACTIVE accounts
    public User loginUser(String username, String password)
            throws SQLException {

        String sql = """
            SELECT id, username, password, roles, status
            FROM users
            WHERE username = ? AND password = ?
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);
            statement.setString(2, password);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    User user = new User(
                            result.getInt("id"),
                            result.getString("username"),
                            result.getString("password"),
                            result.getString("roles"),
                            result.getString("status")
                    );

                    if (user.status().equalsIgnoreCase("APPROVED")
                            || user.status().equalsIgnoreCase("ACTIVE")) {
                        return user;
                    }
                }
            }
        }

        return null;
    }

    // Get every registered user, regardless of role or status
    public List<User> getAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();

        String sql = """
            SELECT id, username, password, roles, status
            FROM users
            ORDER BY id
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                users.add(new User(
                        result.getInt("id"),
                        result.getString("username"),
                        result.getString("password"),
                        result.getString("roles"),
                        result.getString("status")
                ));
            }
        }

        return users;
    }

    // Get all pending users across every role
    public List<User> getPendingUsers() throws SQLException {
        List<User> users = new ArrayList<>();

        String sql = """
            SELECT id, username, password, roles, status
            FROM users
            WHERE status = 'PENDING'
            ORDER BY id
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                users.add(new User(
                        result.getInt("id"),
                        result.getString("username"),
                        result.getString("password"),
                        result.getString("roles"),
                        result.getString("status")
                ));
            }
        }

        return users;
    }

    // Approve a pending user by ID
    public boolean approveUser(int userId) throws SQLException {
        String sql = """
            UPDATE users
            SET status = 'APPROVED'
            WHERE id = ? AND status = 'PENDING'
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            return statement.executeUpdate() > 0;
        }
    }
}
