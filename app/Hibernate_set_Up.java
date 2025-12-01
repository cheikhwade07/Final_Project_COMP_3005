package app;

import app.service.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

public class Hibernate_set_Up {
    public static void main(String[] args) {
        System.out.println("Starting Hibernate test...");

        SessionFactory sf = HibernateUtil.getSessionFactory();

        try (Session session = sf.openSession()) {
            System.out.println("Successfully opened Hibernate session!");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            HibernateUtil.shutdown();
        }

        System.out.println("Done.");
    }
}
