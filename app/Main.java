package app;

import app.service.DatabaseResetService;
import app.service.HibernateUtil;

public class Main {

    public static void main(String[] args) {
        System.out.println("Starting Health and Fitness Club Management demo...");

        // 1) Make sure SessionFactory is initialized
        HibernateUtil.getSessionFactory();

        // 2) Seed base data (admins, trainers, rooms, equipment, manage)
        DataSeeder seeder = new DataSeeder();
        seeder.seedBaseData();

        // 3) Create reset service and start the main console app
        DatabaseResetService resetService = new DatabaseResetService();
        ConsoleApp consoleApp = new ConsoleApp(resetService);
        consoleApp.run();

        // 4) Close SessionFactory on exit
        HibernateUtil.getSessionFactory().close();
        System.out.println("Application finished.");
    }
}
