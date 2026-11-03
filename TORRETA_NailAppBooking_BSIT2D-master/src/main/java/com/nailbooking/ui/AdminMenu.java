package com.nailbooking.ui;

import com.nailbooking.dao.UserDao;
import com.nailbooking.model.User;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class AdminMenu {

    private final UserDao userDao;
    private final Scanner input;
    private final ApplicationMenu applicationMenu;

    public AdminMenu(
            UserDao userDao,
            Scanner input,
            ApplicationMenu applicationMenu) {

        this.userDao = userDao;
        this.input = input;
        this.applicationMenu = applicationMenu;
    }

    public void openMenu(User admin) {
        while (true) {
            System.out.println("\n========== ADMIN MENU ==========");
            System.out.println("1. View All System Users");
            System.out.println("2. View Pending Registrations");
            System.out.println("3. Approve User Account");
            System.out.println("4. Service and Appointment Management");
            System.out.println("5. Update User Role and Status");
            System.out.println("6. Logout");

            int choice = readIntInput("Choose (0 to cancel): ");

            switch (choice) {
                case 1 -> viewAllUsers();
                case 2 -> viewPendingUsers();
                case 3 -> approveUser();
                case 4 -> applicationMenu.openMenu(admin);
                case 5 -> updateUserRoleAndStatus(admin);
                case 6 -> {
                    System.out.println("Logging out...");
                    return;
                }
                case 0 -> System.out.println("No action selected.");
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private void viewAllUsers() {
        try {
            List<User> users = userDao.getAllUsers();
            printUserTable(users, "ALL SYSTEM USERS");
        } catch (SQLException e) {
            System.out.println("Could not load users: " + e.getMessage());
        }
    }

    private void viewPendingUsers() {
        try {
            List<User> users = userDao.getPendingUsers();
            printUserTable(users, "PENDING REGISTRATIONS");
        } catch (SQLException e) {
            System.out.println(
                    "Could not load pending users: " + e.getMessage());
        }
    }

    private void approveUser() {
        try {
            List<User> pendingUsers = userDao.getPendingUsers();

            printUserTable(pendingUsers, "PENDING REGISTRATIONS");

            if (pendingUsers.isEmpty()) {
                System.out.println(
                        "There are no accounts waiting for approval.");
                return;
            }

            int userId = readIntInput(
                    "Enter the User ID to approve (0 to cancel): ");

            if (userId == 0) {
                System.out.println("Approval cancelled.");
                return;
            }

            boolean found = pendingUsers.stream()
                    .anyMatch(user -> user.id() == userId);

            if (!found) {
                System.out.println(
                        "That ID is not in the pending registrations list.");
                return;
            }

            if (userDao.approveUser(userId)) {
                System.out.println(
                        "SUCCESS! User ID " + userId + " has been approved.");
            } else {
                System.out.println(
                        "Approval failed. Please check the account status.");
            }

        } catch (SQLException e) {
            System.out.println(
                    "Could not approve user: " + e.getMessage());
        }
    }

    // NEW: Update a user's role and account status
    private void updateUserRoleAndStatus(User admin) {
        try {
            List<User> users = userDao.getAllUsers();

            printUserTable(users, "UPDATE USER ROLE AND STATUS");

            if (users.isEmpty()) {
                System.out.println("No users available to update.");
                return;
            }

            int targetUserId = readIntInput(
                    "Enter the User ID to update (0 to cancel): ");

            if (targetUserId == 0) {
                System.out.println("Update cancelled.");
                return;
            }

            User targetUser = users.stream()
                    .filter(user -> user.id() == targetUserId)
                    .findFirst()
                    .orElse(null);

            if (targetUser == null) {
                System.out.println("User ID not found. No changes made.");
                return;
            }

            // Prevent the currently active admin from modifying their account
            if (targetUserId == admin.id()
                    && admin.roles().equalsIgnoreCase("ADMIN")
                    && (admin.status().equalsIgnoreCase("ACTIVE")
                    || admin.status().equalsIgnoreCase("APPROVED"))) {

                System.out.println(
                        "WARNING: Modifying the currently active admin account is restricted.");
                return;
            }

            String newRole = readValidRole();

            if (newRole == null) {
                System.out.println("Update cancelled.");
                return;
            }

            String newStatus = readValidStatus();

            if (newStatus == null) {
                System.out.println("Update cancelled.");
                return;
            }

            boolean updated = userDao.updateUserRoleAndStatus(
                    targetUserId, newRole, newStatus);

            if (updated) {
                System.out.println("\n========== UPDATE SUCCESSFUL ==========");
                System.out.println("User ID: " + targetUserId);
                System.out.println("New Role: " + newRole);
                System.out.println("New Status: " + newStatus);
                System.out.println("User role and status updated successfully!");
            } else {
                System.out.println(
                        "Update failed. Please check the User ID.");
            }

        } catch (SQLException e) {
            System.out.println(
                    "Could not update user: " + e.getMessage());
        }
    }

    // Read integer safely; return 0 to cancel
    public int readIntInput(String prompt) {
        while (true) {
            System.out.print(prompt);

            String value = input.nextLine().trim();

            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                System.out.println(
                        "Invalid input! Please enter numbers only, or 0 to cancel.");
            }
        }
    }

    // Only accept roles used by the Nail Booking System
    private String readValidRole() {
        while (true) {
            System.out.println("\nAvailable Roles:");
            System.out.println("1. ADMIN");
            System.out.println("2. NAIL TECHNICIAN");
            System.out.println("3. CUSTOMER");
            System.out.println("0. Cancel");

            int choice = readIntInput("Choose the new role: ");

            switch (choice) {
                case 1:
                    return "ADMIN";
                case 2:
                    return "NAIL TECHNICIAN";
                case 3:
                    return "CUSTOMER";
                case 0:
                    return null;
                default:
                    System.out.println(
                            "Invalid role choice. Please select 1, 2, 3, or 0.");
            }
        }
    }

    // Only accept APPROVED or PENDING
    private String readValidStatus() {
        while (true) {
            System.out.println("\nAvailable Statuses:");
            System.out.println("1. APPROVED");
            System.out.println("2. PENDING");
            System.out.println("0. Cancel");

            int choice = readIntInput("Choose the new status: ");

            switch (choice) {
                case 1:
                    return "APPROVED";
                case 2:
                    return "PENDING";
                case 0:
                    return null;
                default:
                    System.out.println(
                            "Invalid status choice. Please select 1, 2, or 0.");
            }
        }
    }

    private void printUserTable(List<User> users, String title) {
        System.out.println();
        System.out.println(
                "--------------------------------------------------------------");
        System.out.printf("%38s%n", title);
        System.out.println(
                "--------------------------------------------------------------");

        System.out.printf(
                "%-5s | %-15s | %-18s | %-12s%n",
                "ID", "Username", "Role", "Status");

        System.out.println(
                "------+-----------------+--------------------+--------------");

        if (users.isEmpty()) {
            System.out.println("No users found.");
        } else {
            for (User user : users) {
                System.out.printf(
                        "%-5d | %-15.15s | %-18.18s | %-12.12s%n",
                        user.id(),
                        user.username(),
                        user.roles(),
                        user.status());
            }
        }

        System.out.println(
                "------+-----------------+--------------------+--------------");
        System.out.println("Total users: " + users.size());
        System.out.println(
                "--------------------------------------------------------------");
    }
}