package com.nailbooking.dao;

import com.nailbooking.model.Appointment;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AppointmentDao {

    public boolean addAppointment(
            String name, String contact, int serviceId,
            String date, String time) throws SQLException {

        String sql = """
            INSERT INTO appointments
            (customer_name, contact_number, service_id,
             appointment_date, appointment_time)
            VALUES (?, ?, ?, ?, ?)
            """;

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, name);
            p.setString(2, contact);
            p.setInt(3, serviceId);
            p.setString(4, date);
            p.setString(5, time);
            return p.executeUpdate() > 0;
        }
    }

    public List<Appointment> getAllAppointments() throws SQLException {
        List<Appointment> list = new ArrayList<>();

        String sql = """
            SELECT * FROM appointments
            ORDER BY appointment_date, appointment_time
            """;

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet r = p.executeQuery()) {
            while (r.next()) {
                list.add(new Appointment(
                        r.getInt("id"),
                        r.getString("customer_name"),
                        r.getString("contact_number"),
                        r.getInt("service_id"),
                        r.getString("appointment_date"),
                        r.getString("appointment_time"),
                        r.getString("status")
                ));
            }
        }
        return list;
    }

    public boolean updateAppointment(
            int id, String name, String contact, int serviceId,
            String date, String time, String status)
            throws SQLException {

        String sql = """
            UPDATE appointments
            SET customer_name = ?, contact_number = ?, service_id = ?,
                appointment_date = ?, appointment_time = ?, status = ?
            WHERE id = ?
            """;

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, name);
            p.setString(2, contact);
            p.setInt(3, serviceId);
            p.setString(4, date);
            p.setString(5, time);
            p.setString(6, status.toUpperCase());
            p.setInt(7, id);
            return p.executeUpdate() > 0;
        }
    }

    public boolean updateAppointmentStatus(int id, String status)
            throws SQLException {
        String sql = """
            UPDATE appointments SET status = ? WHERE id = ?
            """;

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, status.toUpperCase());
            p.setInt(2, id);
            return p.executeUpdate() > 0;
        }
    }

    public boolean deleteAppointment(int id) throws SQLException {
        String sql = "DELETE FROM appointments WHERE id = ?";

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, id);
            return p.executeUpdate() > 0;
        }
    }
}
