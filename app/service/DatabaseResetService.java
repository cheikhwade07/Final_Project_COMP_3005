package app.service;

import app.DataSeeder;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 * Utility service to reset the database back to the base seed state.
 *
 * Steps:
 *  1) TRUNCATE all tables managed by Hibernate (CASCADE + restart identity)
 *  2) Call DataSeeder.seedBaseData() to recreate admins, trainers, rooms, equipment, manage
 *
 * This is useful for demos and tests: you can play with the app,
 * then reset everything back to a clean baseline without dropping the schema.
 */
public class DatabaseResetService {

    public void resetToBaseSeed() {
        // 1) Wipe all data using a native TRUNCATE
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            // Order does not matter with CASCADE, but table names must be correct.
            // These names match what Hibernate created in your logs.
            session.createNativeQuery(
                    """
                    TRUNCATE TABLE 
                        pt_session,
                        health_metric,
                        fitness_goal,
                        trainer_availability,
                        manage,
                        equipment,
                        room,
                        trainer,
                        member,
                        admin
                    RESTART IDENTITY CASCADE
                    """
            ).executeUpdate();

            tx.commit();
            System.out.println("All data truncated. Identities reset.");
        } catch (Exception e) {
            System.out.println("Error while truncating tables: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        // 2) Reseed base data in a clean DB
        DataSeeder seeder = new DataSeeder();
        seeder.seedBaseData();

        System.out.println("Database reset to base seed state.");
    }
}
