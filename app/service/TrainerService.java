package app.service;

import models.PTSession;
import models.Trainer;
import models.TrainerAvailability;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.LocalDateTime;
import java.util.List;

public class TrainerService {

    /**
     * T1 - Set Availability
     * Adds a new availability interval if it does not overlap
     * with existing availability for the same trainer.
     */
    public TrainerAvailability addAvailability(long trainerId,
                                               LocalDateTime start,
                                               LocalDateTime end,
                                               String status) {

        if (!end.isAfter(start)) {
            System.out.println("End time must be after start time.");
            return null;
        }

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            Trainer trainer = session.get(Trainer.class, trainerId);
            if (trainer == null) {
                tx.rollback();
                System.out.println("Trainer not found: " + trainerId);
                return null;
            }

            // check overlap with existing availability
            Long overlapCount = session.createQuery(
                            "select count(a) " +
                                    "from TrainerAvailability a " +
                                    "where a.trainer.trainerId = :tid " +
                                    "and :start < a.endTime " +
                                    "and :end > a.startTime",
                            Long.class)
                    .setParameter("tid", trainerId)
                    .setParameter("start", start)
                    .setParameter("end", end)
                    .uniqueResult();

            if (overlapCount != null && overlapCount > 0) {
                tx.rollback();
                System.out.println("Availability overlaps with existing slots.");
                return null;
            }

            TrainerAvailability availability = new TrainerAvailability();
            availability.setTrainer(trainer);
            availability.setStartTime(start);
            availability.setEndTime(end);
            availability.setStatus(status != null ? status : "ACTIVE");

            session.persist(availability);
            tx.commit();
            return availability;
        }
    }

    /**
     * T2 - Schedule View
     * Returns upcoming PT sessions for this trainer .
     */
    public List<PTSession> getScheduleForTrainer(long trainerId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "from PTSession s " +
                                    "where s.trainer.trainerId = :tid " +
                                    "and s.status <> 'CANCELLED' " +
                                    "order by s.startTime",
                            PTSession.class)
                    .setParameter("tid", trainerId)
                    .getResultList();
        }
    }

    /**
     * Helper for M4 – list all ACTIVE availabilities for all trainers.
     * Used by the Member UI so users can pick an availability slot by ID.
     */
    public List<TrainerAvailability> getAllActiveAvailabilities() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "from TrainerAvailability a " +
                                    "where a.status = 'ACTIVE' " +
                                    "order by a.trainer.fullName, a.startTime",
                            TrainerAvailability.class)
                    .getResultList();
        }
    }

}

