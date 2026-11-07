
package com.nailbooking.ui;

import com.nailbooking.dao.ActivityLogDao;
import com.nailbooking.dao.UserDao;
import com.nailbooking.model.ActivityLog;
import com.nailbooking.model.User;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class AdminMenu {

    private final UserDao userDao;
    private final ActivityLogDao logDao;
    private final Scanner input;
    private final ApplicationMenu applicationMenu;

    public AdminMenu(
            UserDao userDao,
            Scanner input,
            ApplicationMenu applicationMenu,
            ActivityLogDao logDao) {

        this.userDao = userDao;
        this.input = input;
        this.applicationMenu = applicationMenu;
        this.logDao = logDao;
    }

    public void openMenu(User admin) {

        while (true) {

            System.out.println("\n========== ADMIN MENU ==========");
            System.out.println("1. View All System Users");
            System.out.println("2. View Pending Registrations");
            System.out.println("3. Approve User Account");
            System.out.println("4. Service and Appointment Management");
            System.out.println("5. Update User Role and Status");
            System.out.println("6. View System Audit Logs");
            System.out.println("7. Logout");

            int choice = readIntInput("Choose (0 to cancel): ");

            switch (choice) {

                case 1 -> viewAllUsers();

                case 2 -> viewPendingUsers();

                case 3 -> approveUser(admin);

                case 4 -> applicationMenu.openMenu(admin);

                case 5 -> updateUserRoleAndStatus(admin);

                case 6 -> auditLogMenu();

                case 7 -> {
                    System.out.println("Logging out...");
                    return;
                }

                case 0 -> System.out.println("No action selected.");

                default -> System.out.println("Invalid choice.");
            }
        }
    }

    // ==================== USER MANAGEMENT ====================

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

    // Approve a pending account and record the action
    private void approveUser(User admin) {

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

            User target = pendingUsers.stream()
                    .filter(user -> user.id() == userId)
                    .findFirst()
                    .orElse(null);

            if (target == null) {
                System.out.println(
                        "That ID is not in the pending registrations list.");
                return;
            }

            if (userDao.approveUser(userId)) {

                logDao.logAction(
                        admin.id(),
                        "USER_APPROVED",
                        "Admin " + admin.username()
                                + " approved account of User ID "
                                + userId + " (" + target.username() + ")"
                );

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

    // Update a user's role and account status
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

                logDao.logAction(
                        admin.id(),
                        "USER_UPDATED",
                        "Admin " + admin.username()
                                + " updated User ID " + targetUserId
                                + " (" + targetUser.username() + ")"
                                + " | Old Role: " + targetUser.roles()
                                + " | New Role: " + newRole
                                + " | Old Status: " + targetUser.status()
                                + " | New Status: " + newStatus
                );

                System.out.println("\n========== UPDATE SUCCESSFUL ==========");
                System.out.println("User ID: " + targetUserId);
                System.out.println("New Role: " + newRole);
                System.out.println("New Status: " + newStatus);
                System.out.println(
                        "User role and status updated successfully!");

            } else {
                System.out.println(
                        "Update failed. Please check the User ID.");
            }

        } catch (SQLException e) {
            System.out.println(
                    "Could not update user: " + e.getMessage());
        }
    }

    // ==================== AUDIT LOG MENU ====================

    private void auditLogMenu() {

        while (true) {

            System.out.println("\n========== SYSTEM AUDIT LOGS ==========");
            System.out.println("1. View All Logs");
            System.out.println("2. Filter Logs by User ID");
            System.out.println("3. Clear Historical Logs");
            System.out.println("4. Back");

            int choice = readIntInput("Choose: ");

            switch (choice) {

                case 1 -> viewAllLogs();

                case 2 -> filterLogsByUserId();

                case 3 -> clearHistoricalLogs();

                case 4 -> {
                    return;
                }

                default -> System.out.println("Invalid choice.");
            }
        }
    }

    // Display all logs
    private void viewAllLogs() {

        try {

            List<ActivityLog> logs = logDao.getAllLogs();

            printLogTable(logs);

        } catch (SQLException e) {
            System.out.println(
                    "Could not retrieve audit logs: " + e.getMessage());
        }
    }

    // Filter logs using User ID
    private void filterLogsByUserId() {

        try {

            int userId = readIntInput(
                    "Enter User ID to filter (0 to cancel): ");

            if (userId == 0) {
                System.out.println("Filtering cancelled.");
                return;
            }

            List<ActivityLog> logs = logDao.getLogsByUserId(userId);

            System.out.println("\nAUDIT LOGS FOR USER ID: " + userId);

            printLogTable(logs);

        } catch (SQLException e) {
            System.out.println(
                    "Could not filter audit logs: " + e.getMessage());
        }
    }

    // Clear logs only after Admin confirmation
    private void clearHistoricalLogs() {

        try {

            List<ActivityLog> logs = logDao.getAllLogs();

            if (logs.isEmpty()) {
                System.out.println("There are no historical logs to clear.");
                return;
            }

            printLogTable(logs);

            System.out.println(
                    "\nWARNING: This will permanently clear all audit logs.");

            String confirmation = readText(
                    "Type YES to confirm clearing all logs: ");

            if (!confirmation.equalsIgnoreCase("YES")) {
                System.out.println("Clear operation cancelled.");
                return;
            }

            int deleted = logDao.clearAllLogs();

            System.out.println(
                    deleted + " historical audit log(s) cleared successfully.");

        } catch (SQLException e) {
            System.out.println(
                    "Could not clear audit logs: " + e.getMessage());
        }
    }

    // Formatted audit log table
    private void printLogTable(List<ActivityLog> logs) {

        System.out.println("\n==================== SYSTEM AUDIT LOGS ====================");

        System.out.printf(
                "%-5s | %-8s | %-18s | %-35s | %-20s%n",
                "ID",
                "USER ID",
                "ACTION TYPE",
                "DESCRIPTION",
                "TIMESTAMP"
        );

        System.out.println(
                "------+----------+--------------------+-------------------------------------+----------------------");

        if (logs.isEmpty()) {

            System.out.println("No audit logs found.");

        } else {

            for (ActivityLog log : logs) {

                String userId = log.userId() == null
                        ? "N/A"
                        : String.valueOf(log.userId());

                System.out.printf(
                        "%-5d | %-8s | %-18.18s | %-35.35s | %-20s%n",
                        log.id(),
                        userId,
                        log.actionType(),
                        log.description(),
                        log.timestamp()
                );
            }
        }

        System.out.println(
                "------+----------+--------------------+-------------------------------------+----------------------");

        System.out.println("Total logs: " + logs.size());

        System.out.println(
                "============================================================");
    }

    // ==================== INPUT METHODS ====================

    public int readIntInput(String prompt) {

        while (true) {

            System.out.print(prompt);

            String value = input.nextLine().trim();

            try {
                return Integer.parseInt(value);

            } catch (NumberFormatException e) {
                System.out.println(
                        "Invalid input! Please enter numbers only.");
            }
        }
    }

    private String readText(String prompt) {
        System.out.print(prompt);
        return input.nextLine().trim();
    }

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
                    return "NAIL_TECHNICIAN";

                case 3:
                    return "CUSTOMER";

                case 0:
                    return null;

                default:
                    System.out.println("Invalid role choice.");
            }
        }
    }

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
                    System.out.println("Invalid status choice.");
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
                        user.status()
                );
            }
        }

        System.out.println(
                "------+-----------------+--------------------+--------------");

        System.out.println("Total users: " + users.size());

        System.out.println(
                "--------------------------------------------------------------");
    }
}
