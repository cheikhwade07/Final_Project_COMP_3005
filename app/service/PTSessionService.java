package app.service;

import models.Member;
import models.PTSession;
import models.Trainer;
import models.TrainerAvailability;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.LocalDateTime;

public class PTSessionService {

    /**
     * M4 - PT Session Scheduling (member request)
     *
     * Member requests a PT session with a trainer at a given time window.
     * At this stage:
     *  - No room is assigned yet
     *  - No admin is assigned
     *  - status = "PENDING"
     *
     * Validation:
     *  - member and trainer exist
     *  - trainer has availability covering [start, end]
     *  - trainer has no conflicting sessions
     */
    public PTSession requestSession(long memberId,
                                    long trainerId,
                                    LocalDateTime start,
                                    LocalDateTime end) {

        if (!end.isAfter(start)) {
            System.out.println("End time must be after start time.");
            return null;
        }

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            Member member = session.get(Member.class, memberId);
            Trainer trainer = session.get(Trainer.class, trainerId);

            if (member == null || trainer == null) {
                tx.rollback();
                System.out.println("Invalid member or trainer id.");
                return null;
            }

            // check trainer availability coverage
            Long covering = session.createQuery(
                            "select count(a) " +
                                    "from TrainerAvailability a " +
                                    "where a.trainer.trainerId = :tid " +
                                    "and a.status = 'ACTIVE' " +
                                    "and a.startTime <= :start " +
                                    "and a.endTime >= :end",
                            Long.class)
                    .setParameter("tid", trainerId)
                    .setParameter("start", start)
                    .setParameter("end", end)
                    .uniqueResult();

            if (covering == null || covering == 0) {
                tx.rollback();
                System.out.println("Trainer is not available in this time window.");
                return null;
            }

            // check trainer conflict
            if (trainerHasSessionConflict(session, trainerId, start, end)) {
                tx.rollback();
                System.out.println("Trainer has another session in this time window.");
                return null;
            }

            PTSession pt = new PTSession();
            pt.setMember(member);
            pt.setTrainer(trainer);
            pt.setRoom(null);   // room to be assigned by Admin (A1)
            pt.setAdmin(null);  // admin to be assigned by Admin (A1)
            pt.setStartTime(start);
            pt.setEndTime(end);
            pt.setStatus("PENDING");

            session.persist(pt);
            tx.commit();
            return pt;
        }
    }

    /**
     * M4 - Reschedule (member)
     *
     * Reschedule an existing session to a new time.
     *
     * Behaviour:
     *  - Validates trainer availability and trainer conflicts for the new window
     *  - Clears room/admin and sets status back to "PENDING"
     *    so that an admin must re-assign a room (A1) afterwards.
     */
    public PTSession rescheduleSession(long sessionId,
                                       LocalDateTime newStart,
                                       LocalDateTime newEnd) {

        if (!newEnd.isAfter(newStart)) {
            System.out.println("End time must be after start time.");
            return null;
        }

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            PTSession pt = session.get(PTSession.class, sessionId);
            if (pt == null) {
                tx.rollback();
                System.out.println("Session not found: " + sessionId);
                return null;
            }

            long trainerId = pt.getTrainer().getTrainerId();

            // check trainer availability coverage
            Long covering = session.createQuery(
                            "select count(a) " +
                                    "from TrainerAvailability a " +
                                    "where a.trainer.trainerId = :tid " +
                                    "and a.status = 'ACTIVE' " +
                                    "and a.startTime <= :start " +
                                    "and a.endTime >= :end",
                            Long.class)
                    .setParameter("tid", trainerId)
                    .setParameter("start", newStart)
                    .setParameter("end", newEnd)
                    .uniqueResult();

            if (covering == null || covering == 0) {
                tx.rollback();
                System.out.println("Trainer is not available in this time window.");
                return null;
            }

            // trainer conflicts, excluding this session
            if (trainerHasSessionConflictExcluding(session, trainerId, newStart, newEnd, sessionId)) {
                tx.rollback();
                System.out.println("Trainer has another session in this time window.");
                return null;
            }

            // update to new time, clear room/admin, set back to PENDING
            pt.setStartTime(newStart);
            pt.setEndTime(newEnd);
            pt.setRoom(null);
            pt.setAdmin(null);
            pt.setStatus("PENDING");

            session.merge(pt);
            tx.commit();
            return pt;
        }
    }

    /**
     * M4 - Cancel PT session (member)
     *
     * Member cancels their own session.
     * We enforce:
     *  - session exists
     *  - session belongs to the given member
     * Result:
     *  - status = 'CANCELLED'
     */
    public PTSession cancelSessionAsMember(long memberId, long sessionId) {

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            PTSession pt = session.get(PTSession.class, sessionId);
            if (pt == null) {
                tx.rollback();
                System.out.println("Session not found: " + sessionId);
                return null;
            }

            if (pt.getMember() == null || !pt.getMember().getMemberId().equals(memberId)) {
                tx.rollback();
                System.out.println("This session does not belong to member " + memberId);
                return null;
            }

            pt.setStatus("CANCELLED");
            session.merge(pt);

            tx.commit();
            return pt;
        }
    }

    // ---------- helper methods ----------

    private boolean trainerHasSessionConflict(Session session,
                                              long trainerId,
                                              LocalDateTime start,
                                              LocalDateTime end) {
        Long count = session.createQuery(
                        "select count(s) " +
                                "from PTSession s " +
                                "where s.status <> 'CANCELLED' " +
                                "and s.trainer.trainerId = :tid " +
                                "and :start < s.endTime " +
                                "and :end > s.startTime",
                        Long.class)
                .setParameter("tid", trainerId)
                .setParameter("start", start)
                .setParameter("end", end)
                .uniqueResult();

        return count != null && count > 0;
    }

    private boolean trainerHasSessionConflictExcluding(Session session,
                                                       long trainerId,
                                                       LocalDateTime start,
                                                       LocalDateTime end,
                                                       long excludedSessionId) {
        Long count = session.createQuery(
                        "select count(s) " +
                                "from PTSession s " +
                                "where s.status <> 'CANCELLED' " +
                                "and s.trainer.trainerId = :tid " +
                                "and s.sessionId <> :sid " +
                                "and :start < s.endTime " +
                                "and :end > s.startTime",
                        Long.class)
                .setParameter("tid", trainerId)
                .setParameter("sid", excludedSessionId)
                .setParameter("start", start)
                .setParameter("end", end)
                .uniqueResult();

        return count != null && count > 0;
    }
}

