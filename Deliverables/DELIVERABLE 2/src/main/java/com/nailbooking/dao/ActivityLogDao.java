package com.nailbooking.dao;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ActivityLogDao {

    public void recordActivity(String username, String action) {
        String sql = """
            INSERT INTO activity_logs
            (username, action, activity_date)
            VALUES (?, ?, ?)
            """;

        String date = LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        );

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, username);
            p.setString(2, action);
            p.setString(3, date);
            p.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Could not save activity log: "
                    + e.getMessage());
        }
    }

    public void viewActivityLogs() {
        String sql = """
            SELECT * FROM activity_logs ORDER BY id DESC
            """;

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet r = p.executeQuery()) {

            System.out.println("\n========== ACTIVITY LOGS ==========");

            while (r.next()) {
                System.out.printf(
                        "ID: %d | User: %s | Action: %s | Date: %s%n",
                        r.getInt("id"),
                        r.getString("username"),
                        r.getString("action"),
                        r.getString("activity_date")
                );
            }
        } catch (SQLException e) {
            System.out.println("Error reading logs: " + e.getMessage());
        }
    }
}
