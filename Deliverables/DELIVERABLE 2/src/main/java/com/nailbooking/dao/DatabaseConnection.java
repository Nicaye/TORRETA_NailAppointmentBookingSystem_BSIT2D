
package com.nailbooking.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

public class DatabaseConnection {

    private static final String URL = "jdbc:sqlite:nailbooking.db";

    public static Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(URL);
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        }
        return connection;
    }

    public static void initializeDatabase() {
        String[] tables = {
                """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL UNIQUE,
                password TEXT NOT NULL,
                roles TEXT NOT NULL CHECK (
                    roles IN ('ADMIN', 'NAIL_TECHNICIAN')
                ),
                status TEXT NOT NULL DEFAULT 'ACTIVE'
            )
            """,
                """
            CREATE TABLE IF NOT EXISTS nail_services (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                service_name TEXT NOT NULL,
                price REAL NOT NULL CHECK (price >= 0),
                duration_minutes INTEGER NOT NULL
                    CHECK (duration_minutes > 0)
            )
            """,
                """
            CREATE TABLE IF NOT EXISTS appointments (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                customer_name TEXT NOT NULL,
                contact_number TEXT NOT NULL,
                service_id INTEGER NOT NULL,
                appointment_date TEXT NOT NULL,
                appointment_time TEXT NOT NULL,
                status TEXT NOT NULL DEFAULT 'PENDING',
                FOREIGN KEY (service_id)
                    REFERENCES nail_services(id)
            )
            """,
                """
            CREATE TABLE IF NOT EXISTS activity_logs (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL,
                action TEXT NOT NULL,
                activity_date TEXT NOT NULL
            )
            """
        };

        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {

            for (String table : tables) {
                statement.execute(table);
            }

            try (ResultSet result = statement.executeQuery(
                    "SELECT COUNT(*) FROM nail_services")) {
                if (result.next() && result.getInt(1) == 0) {
                    statement.executeUpdate("""
                        INSERT INTO nail_services
                        (service_name, price, duration_minutes)
                        VALUES
                        ('Basic Manicure', 150, 30),
                        ('Basic Pedicure', 200, 45),
                        ('Gel Polish', 300, 60),
                        ('Nail Extensions', 500, 90),
                        ('Nail Art', 100, 30)
                        """);
                }
            }

            System.out.println("Database tables are ready.");

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }
}
