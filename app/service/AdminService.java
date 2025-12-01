package app.service;

import models.Admin;
import models.Equipment;
import models.Manage;
import models.PTSession;
import models.Room;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.LocalDateTime;

public class AdminService {

    /**
     * A1 - Room Booking Management:
     *
     * Admin assigns or changes the room for an existing PT session.
     * This finalizes the booking by:
     *  - checking room conflicts
     *  - linking the room and admin
     *  - setting status to "VALIDATED"
     */
    public PTSession assignRoomToSession(long adminId,
                                         long sessionId,
                                         long roomId) {

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            Admin admin = session.get(Admin.class, adminId);
            PTSession pt = session.get(PTSession.class, sessionId);
            Room room = session.get(Room.class, roomId);

            if (admin == null || pt == null || room == null) {
                tx.rollback();
                System.out.println("Invalid admin, session, or room id.");
                return null;
            }

            LocalDateTime start = pt.getStartTime();
            LocalDateTime end = pt.getEndTime();

            // room double-booking check (excluding this session)
            Long conflicts = session.createQuery(
                            "select count(s) " +
                                    "from PTSession s " +
                                    "where s.status <> 'CANCELLED' " +
                                    "and s.room.roomId = :rid " +
                                    "and s.sessionId <> :sid " +
                                    "and :start < s.endTime " +
                                    "and :end > s.startTime",
                            Long.class)
                    .setParameter("rid", roomId)
                    .setParameter("sid", sessionId)
                    .setParameter("start", start)
                    .setParameter("end", end)
                    .uniqueResult();

            if (conflicts != null && conflicts > 0) {
                tx.rollback();
                System.out.println("Room already booked for this time.");
                return null;
            }

            pt.setRoom(room);
            pt.setAdmin(admin);
            pt.setStatus("VALIDATED");

            session.merge(pt);
            tx.commit();
            return pt;
        }
    }

    /**
     * A2 - Equipment Maintenance:
     * Update the status of equipment (for example: OK, OUT_OF_SERVICE, UNDER_MAINTENANCE).
     * This method can be used to log an issue or mark it as repaired.
     */
    public Equipment updateEquipmentStatus(long adminId,
                                           long equipmentId,
                                           String newStatus) {

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            Admin admin = session.get(Admin.class, adminId);
            Equipment eq = session.get(Equipment.class, equipmentId);

            if (admin == null || eq == null) {
                tx.rollback();
                System.out.println("Invalid admin or equipment id.");
                return null;
            }

            // optional: check that this admin manages the room of this equipment
            Long manageCount = session.createQuery(
                            "select count(m) " +
                                    "from Manage m " +
                                    "where m.admin.adminId = :aid " +
                                    "and m.room.roomId = :rid",
                            Long.class)
                    .setParameter("aid", adminId)
                    .setParameter("rid", eq.getRoom().getRoomId())
                    .uniqueResult();

            if (manageCount == null || manageCount == 0) {
                tx.rollback();
                System.out.println("Admin does not manage this room; cannot update equipment.");
                return null;
            }

            eq.setStatus(newStatus);
            session.merge(eq);

            tx.commit();
            return eq;
        }
    }
}
