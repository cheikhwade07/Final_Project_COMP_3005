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

            // 1) Check that there is an ACTIVE availability covering [start, end]
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

            // 2) Check trainer does not already have a session in this window
            if (trainerHasSessionConflict(session, trainerId, start, end)) {
                tx.rollback();
                System.out.println("Trainer has another session in this time window.");
                return null;
            }

            // 3) Find ONE concrete availability slot and mark it as BOOKED
            TrainerAvailability slot = session.createQuery(
                            "from TrainerAvailability a " +
                                    "where a.trainer.trainerId = :tid " +
                                    "and a.status = 'ACTIVE' " +
                                    "and a.startTime <= :start " +
                                    "and a.endTime >= :end " +
                                    "order by a.startTime",
                            TrainerAvailability.class)
                    .setParameter("tid", trainerId)
                    .setParameter("start", start)
                    .setParameter("end", end)
                    .setMaxResults(1)
                    .uniqueResult();

            if (slot == null) {
                tx.rollback();
                System.out.println("No matching availability slot found to book.");
                return null;
            }

            // 4) Create the PT session
            PTSession pt = new PTSession();
            pt.setMember(member);
            pt.setTrainer(trainer);
            pt.setRoom(null);   // room to be assigned later by Admin (A1)
            pt.setAdmin(null);
            pt.setStartTime(start);
            pt.setEndTime(end);
            pt.setStatus("PENDING");

            // 5) Consume the slot so it no longer appears as ACTIVE
            slot.setStatus("BOOKED");
            session.merge(slot);

            session.persist(pt);
            tx.commit();
            return pt;
        }
    }

    /**
     * M4 - Reschedule (member)
     *
     * Reschedule an existing session to a new time, using a concrete availability slot.
     *
     * Behaviour:
     *  - Validates that the chosen availability belongs to the same trainer and covers [newStart, newEnd]
     *  - Validates trainer conflicts for the new window (excluding this session)
     *  - Frees the old BOOKED slot (if any)
     *  - Marks the chosen slot as BOOKED
     *  - Clears room/admin and sets status back to "PENDING"
     */
    public PTSession rescheduleSession(long sessionId,
                                       long availabilityId,
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

            if (pt.getTrainer() == null) {
                tx.rollback();
                System.out.println("Session has no trainer assigned; cannot reschedule.");
                return null;
            }

            long trainerId = pt.getTrainer().getTrainerId();
            LocalDateTime oldStart = pt.getStartTime();
            LocalDateTime oldEnd   = pt.getEndTime();

            // Load the chosen availability slot
            TrainerAvailability newSlot = session.get(TrainerAvailability.class, availabilityId);
            if (newSlot == null ||
                    newSlot.getTrainer() == null ||
                    newSlot.getTrainer().getTrainerId() != trainerId) {
                tx.rollback();
                System.out.println("Invalid availability selection for this trainer.");
                return null;
            }

            // Check newSlot is ACTIVE and covers the new window
            if (!"ACTIVE".equalsIgnoreCase(newSlot.getStatus()) ||
                    newSlot.getStartTime().isAfter(newStart) ||
                    newSlot.getEndTime().isBefore(newEnd)) {
                tx.rollback();
                System.out.println("Selected availability does not cover the requested time window.");
                return null;
            }

            // trainer conflicts, excluding this session
            if (trainerHasSessionConflictExcluding(session, trainerId, newStart, newEnd, sessionId)) {
                tx.rollback();
                System.out.println("Trainer has another session in this time window.");
                return null;
            }

            // Free old BOOKED slot (if any) that covered oldStart-oldEnd
            TrainerAvailability oldSlot = session.createQuery(
                            "from TrainerAvailability a " +
                                    "where a.trainer.trainerId = :tid " +
                                    "and a.status = 'BOOKED' " +
                                    "and a.startTime <= :oldStart " +
                                    "and a.endTime >= :oldEnd",
                            TrainerAvailability.class)
                    .setParameter("tid", trainerId)
                    .setParameter("oldStart", oldStart)
                    .setParameter("oldEnd", oldEnd)
                    .setMaxResults(1)
                    .uniqueResult();

            if (oldSlot != null) {
                oldSlot.setStatus("ACTIVE");
                session.merge(oldSlot);
            }

            // update to new time, clear room/admin, set back to PENDING
            pt.setStartTime(newStart);
            pt.setEndTime(newEnd);
            pt.setRoom(null);
            pt.setAdmin(null);
            pt.setStatus("PENDING");

            // mark chosen slot as BOOKED
            newSlot.setStatus("BOOKED");
            session.merge(newSlot);

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
     *  - any matching BOOKED availability slot is freed back to ACTIVE
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

            // Free BOOKED slot, if any
            if (pt.getTrainer() != null) {
                TrainerAvailability slot = session.createQuery(
                                "from TrainerAvailability a " +
                                        "where a.trainer.trainerId = :tid " +
                                        "and a.status = 'BOOKED' " +
                                        "and a.startTime <= :start " +
                                        "and a.endTime >= :end",
                                TrainerAvailability.class)
                        .setParameter("tid", pt.getTrainer().getTrainerId())
                        .setParameter("start", pt.getStartTime())
                        .setParameter("end", pt.getEndTime())
                        .setMaxResults(1)
                        .uniqueResult();

                if (slot != null) {
                    slot.setStatus("ACTIVE");
                    session.merge(slot);
                }
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
