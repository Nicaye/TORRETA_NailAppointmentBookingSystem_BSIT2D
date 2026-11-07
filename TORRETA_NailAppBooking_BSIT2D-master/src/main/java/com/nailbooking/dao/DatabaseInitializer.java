
package com.nailbooking.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

public class DatabaseInitializer {

    public void initialize() {
        DatabaseConnection.initializeDatabase();
        migrateActivityLogs();
    }

    private void migrateActivityLogs() {
        try (Connection connection = DatabaseConnection.getConnection()) {

            Set<String> columns = new HashSet<>();

            try (Statement statement = connection.createStatement();
                 ResultSet result = statement.executeQuery(
                         "PRAGMA table_info(activity_logs)")) {

                while (result.next()) {
                    columns.add(result.getString("name").toLowerCase());
                }
            }

            if (columns.contains("user_id")
                    && columns.contains("action_type")
                    && columns.contains("description")
                    && columns.contains("timestamp")) {

                System.out.println("Activity logs table is ready.");
                return;
            }

            connection.setAutoCommit(false);

            try (Statement statement = connection.createStatement()) {

                statement.execute("""
                    CREATE TABLE activity_logs_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        user_id INTEGER,
                        action_type TEXT NOT NULL,
                        description TEXT NOT NULL,
                        timestamp TEXT NOT NULL,
                        FOREIGN KEY (user_id)
                            REFERENCES users(id)
                            ON DELETE SET NULL
                    )
                    """);

                if (columns.contains("username")
                        && columns.contains("action")
                        && columns.contains("activity_date")) {

                    statement.execute("""
                        INSERT INTO activity_logs_new
                            (id, user_id, action_type, description, timestamp)
                        SELECT
                            old.id,
                            (
                                SELECT users.id
                                FROM users
                                WHERE users.username = old.username
                            ),
                            'LEGACY',
                            'Previous user: ' || old.username
                                || ' | ' || old.action,
                            old.activity_date
                        FROM activity_logs old
                        """);
                }

                statement.execute("DROP TABLE activity_logs");
                statement.execute(
                        "ALTER TABLE activity_logs_new RENAME TO activity_logs");

                connection.commit();

                System.out.println("Activity logs table migrated successfully.");

            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }

        } catch (SQLException e) {
            System.out.println(
                    "Activity logs initialization failed: " + e.getMessage());
        }
    }
}
