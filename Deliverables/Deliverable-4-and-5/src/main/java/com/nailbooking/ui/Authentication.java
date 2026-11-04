package com.nailbooking.ui;

import com.nailbooking.dao.ActivityLogDao;
import com.nailbooking.dao.UserDao;
import com.nailbooking.model.User;

import java.sql.SQLException;
import java.util.Scanner;

public class Authentication {

    private final UserDao userDao;
    private final Scanner input;
    private final ApplicationMenu applicationMenu;
    private final ActivityLogDao logDao;
    private final AdminMenu adminMenu;

    public Authentication(
            UserDao userDao,
            Scanner input,
            ApplicationMenu applicationMenu,
            ActivityLogDao logDao) {

        this.userDao = userDao;
        this.input = input;
        this.applicationMenu = applicationMenu;
        this.logDao = logDao;

        // Connect the new Admin Menu
        this.adminMenu = new AdminMenu(userDao, input, applicationMenu);
    }
    public void start() {
        while (true) {
            System.out.println("\n==================================");
            System.out.println(" NAIL APPOINTMENT BOOKING SYSTEM");
            System.out.println("==================================");
            System.out.println("1. Register Account");
            System.out.println("2. Login");
            System.out.println("3. Exit");

            String choice = readText("Choose: ");

            switch (choice) {
                case "1" -> register();
                case "2" -> login();
                case "3" -> {
                    System.out.println(
                            "Thank you for using the system!");
                    return;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private void register() {
        try {
            System.out.println("\n--- REGISTER ACCOUNT ---");

            String username = readText("Username: ");
            String password = readText("Password: ");

            System.out.println("Account role:");
            System.out.println("1. Customer");
            System.out.println("2. Nail Technician");

            String roleChoice = readText("Choose role: ");

            String role;

            switch (roleChoice) {
                case "1" -> role = "CUSTOMER";
                case "2" -> role = "NAIL_TECHNICIAN";
                default -> {
                    System.out.println("Invalid role.");
                    return;
                }
            }

            if (username.isBlank() || password.isBlank()) {
                System.out.println(
                        "Username and password are required.");
                return;
            }

            if (userDao.registerUser(username, password, role)) {
                System.out.println(
                        "Account registered successfully. Status: PENDING");

                logDao.recordActivity(
                        username, "Registered account");
            }

        } catch (SQLException e) {
            System.out.println(
                    "Registration failed. The username may already exist. "
                            + e.getMessage());
        }
    }

    private void login() {
        try {
            System.out.println("\n--- LOGIN ---");

            String username = readText("Username: ");
            String password = readText("Password: ");

            User user = userDao.loginUser(username, password);

            if (user == null) {
                System.out.println(
                        "Login failed. Check your details or account status.");
                return;
            }

            System.out.println("Welcome, " + user.username() + "!");

            logDao.recordActivity(user.username(), "Logged in");

            // Route users to the menu for their role
            if (user.roles().equalsIgnoreCase("ADMIN")) {
                adminMenu.openMenu(user);
            } else {
                applicationMenu.openMenu(user);
            }

            logDao.recordActivity(user.username(), "Logged out");

        } catch (SQLException e) {
            System.out.println("Login error: " + e.getMessage());
        }
    }

    private String readText(String prompt) {
        System.out.print(prompt);
        return input.nextLine().trim();
    }
}
