package com.nailbooking;

import com.nailbooking.dao.ActivityLogDao;
import com.nailbooking.dao.AppointmentDao;
import com.nailbooking.dao.DatabaseInitializer;
import com.nailbooking.dao.NailServiceDao;
import com.nailbooking.dao.UserDao;
import com.nailbooking.ui.ApplicationMenu;
import com.nailbooking.ui.Authentication;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);

        // Initialize the database before starting the application.
        DatabaseInitializer initializer = new DatabaseInitializer();
        initializer.initialize();

        // Create the DAO instances.
        UserDao userDao = new UserDao();
        NailServiceDao serviceDao = new NailServiceDao();
        AppointmentDao appointmentDao = new AppointmentDao();
        ActivityLogDao logDao = new ActivityLogDao();

        // Create the application menus.
        ApplicationMenu applicationMenu = new ApplicationMenu(
                input,
                serviceDao,
                appointmentDao,
                logDao
        );

        // Start authentication.
        Authentication authentication = new Authentication(
                userDao,
                input,
                applicationMenu,
                logDao
        );

        authentication.start();

        input.close();
    }
}
