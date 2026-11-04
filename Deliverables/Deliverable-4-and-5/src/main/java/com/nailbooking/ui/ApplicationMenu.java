
package com.nailbooking.ui;

import com.nailbooking.dao.*;
import com.nailbooking.model.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class ApplicationMenu {

    private final Scanner input;
    private final NailServiceDao serviceDao;
    private final AppointmentDao appointmentDao;
    private final ActivityLogDao logDao;

    public ApplicationMenu(
            Scanner input,
            NailServiceDao serviceDao,
            AppointmentDao appointmentDao,
            ActivityLogDao logDao) {

        this.input = input;
        this.serviceDao = serviceDao;
        this.appointmentDao = appointmentDao;
        this.logDao = logDao;
    }

    public void openMenu(User user) {
        if (user.roles().equalsIgnoreCase("ADMIN")) {
            adminMenu(user);
        } else if (user.roles().equalsIgnoreCase("NAIL_TECHNICIAN")) {
            technicianMenu(user);
        } else if (user.roles().equalsIgnoreCase("CUSTOMER")) {
            customerMenu(user);
        } else {
            System.out.println("Unknown account role.");
        }
    }

    // ==================== ADMIN MENU ====================

    private void adminMenu(User user) {
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

    // ==================== TECHNICIAN MENU ====================

    private void technicianMenu(User user) {
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

    // ==================== CUSTOMER MENU ====================

    private void customerMenu(User user) {
        while (true) {
            System.out.println("\n===== CUSTOMER MENU =====");
            System.out.println("1. View Nail Services");
            System.out.println("2. Book Appointment");
            System.out.println("3. View My Appointments");
            System.out.println("4. Cancel My Appointment");
            System.out.println("5. Logout");

            int choice = readInt("Choose: ");

            switch (choice) {
                case 1 -> viewServices();
                case 2 -> bookCustomerAppointment(user);
                case 3 -> viewCustomerAppointments(user);
                case 4 -> cancelCustomerAppointment(user);
                case 5 -> {
                    return;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    // ==================== NAIL SERVICES ====================

    private void addService(User user) {
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
                logDao.recordActivity(
                        user.username(), "Added service: " + name);
            }

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void viewServices() {
        try {
            List<NailService> services = serviceDao.getAllServices();

            System.out.println("\n==============================================================");
            System.out.println("                       NAIL SERVICES");
            System.out.println("==============================================================");

            if (services.isEmpty()) {
                System.out.println("No services found.");
                return;
            }

            System.out.printf(
                    "%-5s | %-25s | %12s | %10s%n",
                    "ID", "Service Name", "Price (PHP)", "Duration");

            System.out.println(
                    "------+---------------------------+--------------+-----------");

            for (NailService s : services) {
                System.out.printf(
                        "%-5d | %-25.25s | %12.2f | %7d min%n",
                        s.id(),
                        s.serviceName(),
                        s.price(),
                        s.durationMinutes());
            }

            System.out.println(
                    "------+---------------------------+--------------+-----------");
            System.out.println("Total services: " + services.size());

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void updateService(User user) {
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
                logDao.recordActivity(
                        user.username(), "Updated service ID " + id);
            } else {
                System.out.println("Service ID not found.");
            }

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void deleteService(User user) {
        try {
            viewServices();

            int id = readInt("Service ID to delete: ");

            if (serviceDao.deleteService(id)) {
                System.out.println("Service deleted.");
                logDao.recordActivity(
                        user.username(), "Deleted service ID " + id);
            } else {
                System.out.println("Service ID not found.");
            }

        } catch (SQLException e) {
            System.out.println(
                    "Cannot delete this service. It may be used by an appointment. "
                            + e.getMessage());
        }
    }

    // ==================== ADMIN APPOINTMENTS ====================

    private void addAppointment(User user) {
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
                logDao.recordActivity(
                        user.username(), "Added appointment for " + name);
            }

        } catch (SQLException e) {
            System.out.println(
                    "Could not add appointment. Check the service ID. "
                            + e.getMessage());
        }
    }

    // Formatted table for Admin and Technician
    private void viewAppointments() {
        try {
            List<Appointment> appointments =
                    appointmentDao.getAllAppointments();

            printAppointmentTable(appointments, "ALL APPOINTMENTS");

        } catch (SQLException e) {
            System.out.println(
                    "Error loading appointments: " + e.getMessage());
        }
    }

    // ==================== CUSTOMER BOOKING ====================

    private void bookCustomerAppointment(User user) {
        try {
            viewServices();

            String name = readText("Your full name: ");
            String contact = readText("Contact number: ");
            int serviceId = readInt("Enter the service ID you want: ");
            String date = readText("Appointment date (YYYY-MM-DD): ");
            String time = readText("Appointment time (HH:MM): ");

            if (name.isBlank() || contact.isBlank()
                    || date.isBlank() || time.isBlank()) {
                System.out.println("All fields are required.");
                return;
            }

            if (appointmentDao.addCustomerAppointment(
                    user.username(), name, contact,
                    serviceId, date, time)) {

                System.out.println(
                        "Appointment booked successfully! Status: PENDING");

                logDao.recordActivity(
                        user.username(),
                        "Booked an appointment for " + date);
            } else {
                System.out.println("Appointment could not be booked.");
            }

        } catch (SQLException e) {
            System.out.println(
                    "Could not book appointment. Check the service ID. "
                            + e.getMessage());
        }
    }

    private void viewCustomerAppointments(User user) {
        try {
            List<Appointment> appointments =
                    appointmentDao.getCustomerAppointments(user.username());

            printAppointmentTable(appointments, "MY APPOINTMENTS");

        } catch (SQLException e) {
            System.out.println(
                    "Could not load your appointments: " + e.getMessage());
        }
    }

    private void cancelCustomerAppointment(User user) {
        try {
            viewCustomerAppointments(user);

            int id = readInt("Enter the appointment ID to cancel: ");

            if (appointmentDao.cancelCustomerAppointment(
                    id, user.username())) {

                System.out.println("Your appointment has been cancelled.");

                logDao.recordActivity(
                        user.username(), "Cancelled appointment ID " + id);

            } else {
                System.out.println(
                        "Appointment not found, already cancelled, "
                                + "or no longer eligible for cancellation.");
            }

        } catch (SQLException e) {
            System.out.println(
                    "Could not cancel appointment: " + e.getMessage());
        }
    }

    // ==================== APPOINTMENT TABLE ====================

    private void printAppointmentTable(
            List<Appointment> appointments, String title) {

        System.out.println();
        System.out.println(
                "==============================================================================================");
        System.out.printf("%48s%n", title);
        System.out.println(
                "==============================================================================================");

        System.out.printf(
                "%-5s | %-20s | %-15s | %-10s | %-12s | %-8s | %-12s%n",
                "ID",
                "Customer Name",
                "Contact Number",
                "Service ID",
                "Date",
                "Time",
                "Status");

        System.out.println(
                "------+----------------------+-----------------+------------+--------------+----------+--------------");

        if (appointments.isEmpty()) {
            System.out.println("No appointments found.");
            System.out.println(
                    "==============================================================================================");
            return;
        }

        for (Appointment a : appointments) {
            System.out.printf(
                    "%-5d | %-20.20s | %-15.15s | %-10d | %-12.12s | %-8.8s | %-12.12s%n",
                    a.id(),
                    a.customerName(),
                    a.contactNumber(),
                    a.serviceId(),
                    a.appointmentDate(),
                    a.appointmentTime(),
                    a.status());
        }

        System.out.println(
                "------+----------------------+-----------------+------------+--------------+----------+--------------");

        System.out.println("Total appointments: " + appointments.size());

        System.out.println(
                "==============================================================================================");
    }

    // ==================== UPDATE APPOINTMENTS ====================

    private void updateAppointment(User user) {
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
                    "Status (PENDING, CONFIRMED, COMPLETED, CANCELLED): ");

            if (name.isBlank() || contact.isBlank()
                    || date.isBlank() || time.isBlank()
                    || !validStatus(status)) {

                System.out.println(
                        "Please enter valid details and status.");
                return;
            }

            if (appointmentDao.updateAppointment(
                    id, name, contact, serviceId, date, time, status)) {

                System.out.println("Appointment updated.");

                logDao.recordActivity(
                        user.username(), "Updated appointment ID " + id);

            } else {
                System.out.println("Appointment ID not found.");
            }

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void updateAppointmentStatus(User user) {
        try {
            viewAppointments();

            int id = readInt("Appointment ID: ");
            String status = readText(
                    "New status (PENDING, CONFIRMED, COMPLETED, CANCELLED): ");

            if (!validStatus(status)) {
                System.out.println("Invalid status.");
                return;
            }

            if (appointmentDao.updateAppointmentStatus(id, status)) {
                System.out.println("Appointment status updated.");

                logDao.recordActivity(
                        user.username(),
                        "Changed appointment " + id + " to " + status);

            } else {
                System.out.println("Appointment ID not found.");
            }

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void deleteAppointment(User user) {
        try {
            viewAppointments();

            int id = readInt("Appointment ID to delete: ");

            if (appointmentDao.deleteAppointment(id)) {
                System.out.println("Appointment deleted.");

                logDao.recordActivity(
                        user.username(), "Deleted appointment ID " + id);

            } else {
                System.out.println("Appointment ID not found.");
            }

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ==================== SUMMARY REPORT ====================

    private void appointmentReport() {
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

    // ==================== HELPER METHODS ====================

    private boolean validStatus(String status) {
        return status.equalsIgnoreCase("PENDING")
                || status.equalsIgnoreCase("CONFIRMED")
                || status.equalsIgnoreCase("COMPLETED")
                || status.equalsIgnoreCase("CANCELLED");
    }

    private String readText(String prompt) {
        System.out.print(prompt);
        return input.nextLine().trim();
    }

    private int readInt(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Integer.parseInt(input.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a whole number.");
            }
        }
    }

    private double readDouble(String prompt) {
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
