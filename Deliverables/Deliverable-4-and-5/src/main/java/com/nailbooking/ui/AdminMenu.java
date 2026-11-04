
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
            System.out.println("5. Logout");

            int choice = readInt("Choose: ");

            switch (choice) {
                case 1 -> viewAllUsers();
                case 2 -> viewPendingUsers();
                case 3 -> approveUser();
                case 4 -> applicationMenu.openMenu(admin);
                case 5 -> {
                    System.out.println("Logging out...");
                    return;
                }
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

            int userId = readInt(
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
                        "SUCCESS! User ID " + userId
                                + " has been approved.");
            } else {
                System.out.println(
                        "Approval failed. Please check the account status.");
            }

        } catch (SQLException e) {
            System.out.println(
                    "Could not approve user: " + e.getMessage());
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

    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);

            try {
                return Integer.parseInt(input.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println(
                        "Please enter a valid number.");
            }
        }
    }
}
