package com.nailbooking.dao;

import com.nailbooking.model.NailService;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NailServiceDao {

    public boolean addService(String name, double price, int duration)
            throws SQLException {
        String sql = """
            INSERT INTO nail_services
            (service_name, price, duration_minutes)
            VALUES (?, ?, ?)
            """;

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, name);
            p.setDouble(2, price);
            p.setInt(3, duration);
            return p.executeUpdate() > 0;
        }
    }

    public List<NailService> getAllServices() throws SQLException {
        List<NailService> list = new ArrayList<>();
        String sql = """
            SELECT * FROM nail_services ORDER BY id
            """;

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet r = p.executeQuery()) {
            while (r.next()) {
                list.add(new NailService(
                        r.getInt("id"),
                        r.getString("service_name"),
                        r.getDouble("price"),
                        r.getInt("duration_minutes")
                ));
            }
        }
        return list;
    }

    public boolean updateService(
            int id, String name, double price, int duration)
            throws SQLException {
        String sql = """
            UPDATE nail_services
            SET service_name = ?, price = ?, duration_minutes = ?
            WHERE id = ?
            """;

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, name);
            p.setDouble(2, price);
            p.setInt(3, duration);
            p.setInt(4, id);
            return p.executeUpdate() > 0;
        }
    }

    public boolean deleteService(int id) throws SQLException {
        String sql = "DELETE FROM nail_services WHERE id = ?";

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, id);
            return p.executeUpdate() > 0;
        }
    }
}
