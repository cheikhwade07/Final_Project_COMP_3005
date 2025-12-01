package app;

import app.service.HibernateUtil;
import models.Admin;
import models.Equipment;
import models.Manage;
import models.Room;
import models.Trainer;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.LocalDate;

public class DataSeeder {

    /**
     * Seed minimal base data:
     * - 2 admins
     * - 2 trainers
     * - 2 rooms
     * - equipment in each room
     * - Manage(admin, room) links
     *
     * Safe to call multiple times: if admins already exist, it skips seeding.
     */
    public void seedBaseData() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            Long adminCount = session.createQuery(
                            "select count(a) from Admin a",
                            Long.class)
                    .uniqueResult();

            if (adminCount != null && adminCount > 0) {
                System.out.println("Base data already present. Skipping seeding.");
                tx.rollback();
                return;
            }

            System.out.println("Seeding base data...");

            // Admins
            Admin admin1 = new Admin();
            admin1.setFullName("Alice Admin");
            admin1.setEmail("alice.admin@example.com");
            admin1.setPasswordHash("admin123");   // plain for demo
            admin1.setStatus("ACTIVE");

            Admin admin2 = new Admin();
            admin2.setFullName("Bob Admin");
            admin2.setEmail("bob.admin@example.com");
            admin2.setPasswordHash("admin123");
            admin2.setStatus("ACTIVE");

            session.persist(admin1);
            session.persist(admin2);

            // Trainers
            Trainer trainer1 = new Trainer();
            trainer1.setFullName("Tom Trainer");
            trainer1.setEmail("tom.trainer@example.com");
            trainer1.setPasswordHash("trainer123");
            trainer1.setHireDate(LocalDate.now().minusYears(1));
            trainer1.setStatus("ACTIVE");

            Trainer trainer2 = new Trainer();
            trainer2.setFullName("Tina Trainer");
            trainer2.setEmail("tina.trainer@example.com");
            trainer2.setPasswordHash("trainer123");
            trainer2.setHireDate(LocalDate.now().minusMonths(6));
            trainer2.setStatus("ACTIVE");

            session.persist(trainer1);
            session.persist(trainer2);

            // Rooms
            Room room1 = new Room();
            room1.setRoomType("PT_ROOM");
            room1.setCapacity(1);
            room1.setStatus("AVAILABLE");

            Room room2 = new Room();
            room2.setRoomType("PT_ROOM");
            room2.setCapacity(1);
            room2.setStatus("AVAILABLE");

            session.persist(room1);
            session.persist(room2);

            // Equipment
            Equipment eq1 = new Equipment();
            eq1.setName("Treadmill A");
            eq1.setCategory("CARDIO");
            eq1.setStatus("OK");
            eq1.setRoom(room1);

            Equipment eq2 = new Equipment();
            eq2.setName("Bench Press");
            eq2.setCategory("STRENGTH");
            eq2.setStatus("OK");
            eq2.setRoom(room1);

            Equipment eq3 = new Equipment();
            eq3.setName("Elliptical X");
            eq3.setCategory("CARDIO");
            eq3.setStatus("OK");
            eq3.setRoom(room2);

            session.persist(eq1);
            session.persist(eq2);
            session.persist(eq3);

            // Make sure ids for admins/rooms are generated
            session.flush();

            // Manage relationships: admins manage rooms
            Manage m1 = new Manage(admin1, room1);
            Manage m2 = new Manage(admin2, room2);


            session.persist(m1);
            session.persist(m2);




            tx.commit();
            System.out.println("Base data seeding complete.");
        } catch (Exception e) {
            System.out.println("Error while seeding data: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
