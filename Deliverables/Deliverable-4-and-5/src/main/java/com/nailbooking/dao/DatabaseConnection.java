
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
                    roles IN ('ADMIN', 'NAIL_TECHNICIAN', 'CUSTOMER')
                ),
                status TEXT NOT NULL DEFAULT 'PENDING'
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
                customer_username TEXT,
                FOREIGN KEY (service_id) REFERENCES nail_services(id),
                FOREIGN KEY (customer_username) REFERENCES users(username)
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

            // Add the customer account column to an existing table
            if (!hasColumn(connection, "appointments", "customer_username")) {
                statement.execute("""
                    ALTER TABLE appointments
                    ADD COLUMN customer_username TEXT
                    REFERENCES users(username)
                    """);
            }

            // Add CUSTOMER to the existing users table if needed
            boolean customerAllowed = false;

            try (ResultSet result = statement.executeQuery(
                    "SELECT sql FROM sqlite_master " +
                            "WHERE type = 'table' AND name = 'users'")) {

                if (result.next()) {
                    String sql = result.getString("sql");
                    customerAllowed = sql != null
                            && sql.toUpperCase().contains("'CUSTOMER'");
                }
            }

            if (!customerAllowed) {
                statement.execute("PRAGMA foreign_keys = OFF");
                statement.execute("BEGIN");

                try {
                    statement.execute("""
                        CREATE TABLE users_new (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            username TEXT NOT NULL UNIQUE,
                            password TEXT NOT NULL,
                            roles TEXT NOT NULL CHECK (
                                roles IN (
                                    'ADMIN',
                                    'NAIL_TECHNICIAN',
                                    'CUSTOMER'
                                )
                            ),
                            status TEXT NOT NULL DEFAULT 'PENDING'
                        )
                        """);

                    statement.execute("""
                        INSERT INTO users_new
                            (id, username, password, roles, status)
                        SELECT id, username, password, roles, status
                        FROM users
                        """);

                    statement.execute("DROP TABLE users");
                    statement.execute(
                            "ALTER TABLE users_new RENAME TO users");

                    statement.execute("COMMIT");

                } catch (SQLException e) {
                    statement.execute("ROLLBACK");
                    throw e;

                } finally {
                    statement.execute("PRAGMA foreign_keys = ON");
                }
            }

            // Add default services only if the table is empty
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

    private static boolean hasColumn(
            Connection connection, String tableName, String columnName)
            throws SQLException {

        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(
                     "PRAGMA table_info(" + tableName + ")")) {

            while (result.next()) {
                if (columnName.equalsIgnoreCase(
                        result.getString("name"))) {
                    return true;
                }
            }
        }

        return false;
    }
}
