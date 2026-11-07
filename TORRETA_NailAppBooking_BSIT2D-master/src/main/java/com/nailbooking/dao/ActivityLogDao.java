
package com.nailbooking.dao;

import com.nailbooking.model.ActivityLog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ActivityLogDao {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Record a system activity
    public void logAction(
            int userId, String actionType, String description)
            throws SQLException {

        String sql = """
            INSERT INTO activity_logs
                (user_id, action_type, description, timestamp)
            VALUES (?, ?, ?, ?)
            """;

        String timestamp = LocalDateTime.now().format(FORMATTER);

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);
            statement.setString(2, actionType.toUpperCase());
            statement.setString(3, description);
            statement.setString(4, timestamp);

            statement.executeUpdate();
        }
    }

    // Retrieve all activity logs, newest first
    public List<ActivityLog> getAllLogs() throws SQLException {

        List<ActivityLog> logs = new ArrayList<>();

        String sql = """
            SELECT id, user_id, action_type, description, timestamp
            FROM activity_logs
            ORDER BY timestamp DESC, id DESC
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {

                int userIdValue = result.getInt("user_id");
                Integer userId = result.wasNull() ? null : userIdValue;

                logs.add(new ActivityLog(
                        result.getInt("id"),
                        userId,
                        result.getString("action_type"),
                        result.getString("description"),
                        result.getString("timestamp")
                ));
            }
        }

        return logs;
    }

    // Retrieve activity logs belonging to one user
    public List<ActivityLog> getLogsByUserId(int userId)
            throws SQLException {

        List<ActivityLog> logs = new ArrayList<>();

        String sql = """
            SELECT id, user_id, action_type, description, timestamp
            FROM activity_logs
            WHERE user_id = ?
            ORDER BY timestamp DESC, id DESC
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            try (ResultSet result = statement.executeQuery()) {

                while (result.next()) {
                    logs.add(new ActivityLog(
                            result.getInt("id"),
                            result.getInt("user_id"),
                            result.getString("action_type"),
                            result.getString("description"),
                            result.getString("timestamp")
                    ));
                }
            }
        }

        return logs;
    }

    // Clear historical logs
    public int clearAllLogs() throws SQLException {

        String sql = "DELETE FROM activity_logs";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            return statement.executeUpdate();
        }
    }

    // Record existing system activity calls
    public void recordActivity(String username, String action) {

        Integer userId = null;

        String sql = "SELECT id FROM users WHERE username = ?";

        // Step 1: Find the user ID and close the connection.
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {
                    userId = result.getInt("id");
                }
            }

        } catch (SQLException e) {
            System.out.println(
                    "Could not find user for activity log: " + e.getMessage());
            return;
        }

        // Step 2: Check whether the user exists.
        if (userId == null) {
            System.out.println("Activity log skipped: user not found.");
            return;
        }

        // Step 3: Identify the activity type.
        String actionType = "SYSTEM_ACTIVITY";
        String lowerAction = action.toLowerCase();

        if (lowerAction.contains("register")) {
            actionType = "USER_REGISTERED";

        } else if (lowerAction.contains("login")) {
            actionType = "USER_LOGIN";

        } else if (lowerAction.contains("logout")) {
            actionType = "USER_LOGOUT";

        } else if (lowerAction.contains("booked")
                || lowerAction.contains("added appointment")) {
            actionType = "APPOINTMENT_CREATED";

        } else if (lowerAction.contains("updated appointment")
                || lowerAction.contains("changed appointment")) {
            actionType = "APPOINTMENT_UPDATED";

        } else if (lowerAction.contains("cancelled appointment")) {
            actionType = "APPOINTMENT_CANCELLED";

        } else if (lowerAction.contains("deleted appointment")) {
            actionType = "APPOINTMENT_DELETED";

        } else if (lowerAction.contains("added service")) {
            actionType = "SERVICE_CREATED";

        } else if (lowerAction.contains("updated service")) {
            actionType = "SERVICE_UPDATED";

        } else if (lowerAction.contains("deleted service")) {
            actionType = "SERVICE_DELETED";
        }

        // Step 4: Save the activity after the first connection is closed.
        try {
            logAction(userId, actionType, action);

        } catch (SQLException e) {
            System.out.println(
                    "Could not save activity log: " + e.getMessage());
        }
    }
}
