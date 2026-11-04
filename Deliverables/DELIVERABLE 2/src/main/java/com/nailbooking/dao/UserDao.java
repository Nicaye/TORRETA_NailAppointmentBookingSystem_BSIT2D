package com.nailbooking.dao;

import com.nailbooking.model.User;
import java.sql.*;

public class UserDao {

    public boolean registerUser(
            String username, String password, String roles)
            throws SQLException {

        String sql = """
            INSERT INTO users (username, password, roles)
            VALUES (?, ?, ?)
            """;

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, username);
            p.setString(2, password);
            p.setString(3, roles.toUpperCase());
            return p.executeUpdate() > 0;
        }
    }

    public User loginUser(String username, String password)
            throws SQLException {

        String sql = """
            SELECT id, username, password, roles, status
            FROM users
            WHERE username = ? AND password = ?
            """;

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, username);
            p.setString(2, password);

            try (ResultSet r = p.executeQuery()) {
                if (r.next()) {
                    User user = new User(
                            r.getInt("id"),
                            r.getString("username"),
                            r.getString("password"),
                            r.getString("roles"),
                            r.getString("status")
                    );

                    if (!user.status().equalsIgnoreCase("ACTIVE")) {
                        return null;
                    }

                    return user;
                }
            }
        }
        return null;
    }
}
