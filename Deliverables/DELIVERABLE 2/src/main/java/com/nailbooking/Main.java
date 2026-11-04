
package com.nailbooking;

import com.nailbooking.dao.*;
import com.nailbooking.model.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static final Scanner input = new Scanner(System.in);
    private static final UserDao userDao = new UserDao();
    private static final NailServiceDao serviceDao = new NailServiceDao();
    private static final AppointmentDao appointmentDao =
            new AppointmentDao();
    private static final ActivityLogDao logDao = new ActivityLogDao();

    public static void main(String[] args) {
        DatabaseConnection.initializeDatabase();

        while (true) {
            System.out.println("\n==================================");
            System.out.println(" NAIL APPOINTMENT BOOKING SYSTEM");
            System.out.println("==================================");
            System.out.println("1. Register Account");
            System.out.println("2. Login");
            System.out.println("3. Exit1" +
                    "1");

            int choice = readInt("Choose: ");

            switch (choice) {
                case 1 -> register();
                case 2 -> login();
                case 3 -> {
                    System.out.println("Thank you for using the system!");
                    input.close();
                    return;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private static void register() {
        try {
            System.out.println("\n--- REGISTER ACCOUNT ---");
            String username = readText("Username: ");
            String password = readText("Password: ");

            System.out.println("Account role:");
            System.out.println("1. Admin");
            System.out.println("2. Nail Technician");
            int roleChoice = readInt("Choose role: ");

            String role;
            if (roleChoice == 1) {
                role = "ADMIN";
            } else if (roleChoice == 2) {
                role = "NAIL_TECHNICIAN";
            } else {
                System.out.println("Invalid role.");
                return;
            }

            if (username.isBlank() || password.isBlank()) {
                System.out.println("Username and password are required.");
                return;
            }

            if (userDao.registerUser(username, password, role)) {
                System.out.println("Account registered successfully.");
                logDao.recordActivity(username, "Registered account");
            }
        } catch (SQLException e) {
            System.out.println("Registration failed. The username may "
                    + "already exist. " + e.getMessage());
        }
    }

    private static void login() {
        try {
            System.out.println("\n--- LOGIN ---");
            String username = readText("Username: ");
            String password = readText("Password: ");

            User user = userDao.loginUser(username, password);

            if (user == null) {
                System.out.println(
                        "Login failed. Check your details or account status."
                );
                return;
            }

            System.out.println("Welcome, " + user.username() + "!");
            logDao.recordActivity(user.username(), "Logged in");

            if (user.roles().equalsIgnoreCase("ADMIN")) {
                adminMenu(user);
            } else {
                technicianMenu(user);
            }

            logDao.recordActivity(user.username(), "Logged out");

        } catch (SQLException e) {
            System.out.println("Login error: " + e.getMessage());
        }
    }

    private static void adminMenu(User user) {
        while (true) {
            System.out.println("\n========== ADMIN MENU ==========");
            System.out.println("1. Add Nail Service");
            System.out.println("2. View Nail Services");
            System.out.println("3. Update Nail Service");
            System.out.println("4. Delete Nail Service");
            System.out.println("5. Add Appointment");
            System.out.println("6. View Appointments");
            System.out.println("7. Update Appointment");
            System.out.println("8. Delete Appointment");
            System.out.println("9. Appointment Summary Report");
            System.out.println("10. View Activity Logs");
            System.out.println("11. Logout");

            int choice = readInt("Choose: ");

            switch (choice) {
                case 1 -> addService(user);
                case 2 -> viewServices();
                case 3 -> updateService(user);
                case 4 -> deleteService(user);
                case 5 -> addAppointment(user);
                case 6 -> viewAppointments();
                case 7 -> updateAppointment(user);
                case 8 -> deleteAppointment(user);
                case 9 -> appointmentReport();
                case 10 -> logDao.viewActivityLogs();
                case 11 -> {
                    return;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private static void technicianMenu(User user) {
        while (true) {
            System.out.println("\n===== NAIL TECHNICIAN MENU =====");
            System.out.println("1. View Nail Services");
            System.out.println("2. View Appointments");
            System.out.println("3. Update Appointment Status");
            System.out.println("4. Logout");

            int choice = readInt("Choose: ");

            switch (choice) {
                case 1 -> viewServices();
                case 2 -> viewAppointments();
                case 3 -> updateAppointmentStatus(user);
                case 4 -> {
                    return;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private static void addService(User user) {
        try {
            String name = readText("Service name: ");
            double price = readDouble("Price: ");
            int duration = readInt("Duration in minutes: ");

            if (name.isBlank() || price < 0 || duration <= 0) {
                System.out.println("Please enter valid service details.");
                return;
            }

            if (serviceDao.addService(name, price, duration)) {
                System.out.println("Service added successfully.");
                logDao.recordActivity(user.username(),
                        "Added service: " + name);
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void viewServices() {
        try {
            List<NailService> services = serviceDao.getAllServices();
            System.out.println("\n========== NAIL SERVICES ==========");

            if (services.isEmpty()) {
                System.out.println("No services found.");
            }

            for (NailService s : services) {
                System.out.printf(
                        "ID: %d | %s | PHP %.2f | %d minutes%n",
                        s.id(), s.serviceName(), s.price(),
                        s.durationMinutes()
                );
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void updateService(User user) {
        try {
            viewServices();
            int id = readInt("Service ID to update: ");
            String name = readText("New service name: ");
            double price = readDouble("New price: ");
            int duration = readInt("New duration in minutes: ");

            if (name.isBlank() || price < 0 || duration <= 0) {
                System.out.println("Invalid service details.");
                return;
            }

            if (serviceDao.updateService(id, name, price, duration)) {
                System.out.println("Service updated.");
                logDao.recordActivity(user.username(),
                        "Updated service ID " + id);
            } else {
                System.out.println("Service ID not found.");
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void deleteService(User user) {
        try {
            viewServices();
            int id = readInt("Service ID to delete: ");

            if (serviceDao.deleteService(id)) {
                System.out.println("Service deleted.");
                logDao.recordActivity(user.username(),
                        "Deleted service ID " + id);
            } else {
                System.out.println("Service ID not found.");
            }
        } catch (SQLException e) {
            System.out.println("Cannot delete this service. It may be "
                    + "used by an appointment. " + e.getMessage());
        }
    }

    private static void addAppointment(User user) {
        try {
            viewServices();
            String name = readText("Customer name: ");
            String contact = readText("Contact number: ");
            int serviceId = readInt("Service ID: ");
            String date = readText("Date (YYYY-MM-DD): ");
            String time = readText("Time (HH:MM): ");

            if (name.isBlank() || contact.isBlank()
                    || date.isBlank() || time.isBlank()) {
                System.out.println("All fields are required.");
                return;
            }

            if (appointmentDao.addAppointment(
                    name, contact, serviceId, date, time)) {
                System.out.println("Appointment added successfully.");
                logDao.recordActivity(user.username(),
                        "Added appointment for " + name);
            }
        } catch (SQLException e) {
            System.out.println("Could not add appointment. Check the "
                    + "service ID. " + e.getMessage());
        }
    }

    private static void viewAppointments() {
        try {
            List<Appointment> appointments =
                    appointmentDao.getAllAppointments();

            System.out.println("\n========== APPOINTMENTS ==========");

            if (appointments.isEmpty()) {
                System.out.println("No appointments found.");
            }

            for (Appointment a : appointments) {
                System.out.printf(
                        "ID: %d | Customer: %s | Contact: %s | "
                                + "Service ID: %d | Date: %s | Time: %s | Status: %s%n",
                        a.id(), a.customerName(), a.contactNumber(),
                        a.serviceId(), a.appointmentDate(),
                        a.appointmentTime(), a.status()
                );
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void updateAppointment(User user) {
        try {
            viewAppointments();
            int id = readInt("Appointment ID to update: ");
            String name = readText("New customer name: ");
            String contact = readText("New contact number: ");

            viewServices();
            int serviceId = readInt("New service ID: ");
            String date = readText("New date (YYYY-MM-DD): ");
            String time = readText("New time (HH:MM): ");
            String status = readText(
                    "Status (PENDING, CONFIRMED, COMPLETED, CANCELLED): "
            );

            if (name.isBlank() || contact.isBlank()
                    || date.isBlank() || time.isBlank()
                    || !validStatus(status)) {
                System.out.println("Please enter valid details and status.");
                return;
            }

            if (appointmentDao.updateAppointment(
                    id, name, contact, serviceId, date, time, status)) {
                System.out.println("Appointment updated.");
                logDao.recordActivity(user.username(),
                        "Updated appointment ID " + id);
            } else {
                System.out.println("Appointment ID not found.");
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void updateAppointmentStatus(User user) {
        try {
            viewAppointments();
            int id = readInt("Appointment ID: ");
            String status = readText(
                    "New status (PENDING, CONFIRMED, COMPLETED, CANCELLED): "
            );

            if (!validStatus(status)) {
                System.out.println("Invalid status.");
                return;
            }

            if (appointmentDao.updateAppointmentStatus(id, status)) {
                System.out.println("Appointment status updated.");
                logDao.recordActivity(user.username(),
                        "Changed appointment " + id + " to " + status);
            } else {
                System.out.println("Appointment ID not found.");
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void deleteAppointment(User user) {
        try {
            viewAppointments();
            int id = readInt("Appointment ID to delete: ");

            if (appointmentDao.deleteAppointment(id)) {
                System.out.println("Appointment deleted.");
                logDao.recordActivity(user.username(),
                        "Deleted appointment ID " + id);
            } else {
                System.out.println("Appointment ID not found.");
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void appointmentReport() {
        try {
            List<Appointment> appointments =
                    appointmentDao.getAllAppointments();

            int pending = 0;
            int confirmed = 0;
            int completed = 0;
            int cancelled = 0;

            for (Appointment a : appointments) {
                switch (a.status().toUpperCase()) {
                    case "PENDING" -> pending++;
                    case "CONFIRMED" -> confirmed++;
                    case "COMPLETED" -> completed++;
                    case "CANCELLED" -> cancelled++;
                }
            }

            System.out.println("\n===== APPOINTMENT SUMMARY REPORT =====");
            System.out.println("Total appointments: " + appointments.size());
            System.out.println("Pending: " + pending);
            System.out.println("Confirmed: " + confirmed);
            System.out.println("Completed: " + completed);
            System.out.println("Cancelled: " + cancelled);
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static boolean validStatus(String status) {
        return status.equalsIgnoreCase("PENDING")
                || status.equalsIgnoreCase("CONFIRMED")
                || status.equalsIgnoreCase("COMPLETED")
                || status.equalsIgnoreCase("CANCELLED");
    }

    private static String readText(String prompt) {
        System.out.print(prompt);
        return input.nextLine().trim();
    }

    private static int readInt(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Integer.parseInt(input.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a whole number.");
            }
        }
    }

    private static double readDouble(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Double.parseDouble(input.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }
}
