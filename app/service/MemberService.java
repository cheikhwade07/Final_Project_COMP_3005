package app.service;

import models.FitnessGoal;
import models.FitnessGoalId;
import models.HealthMetric;
import models.Member;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.LocalDate;
import java.util.List;

public class MemberService {

    /**
     * M1 - User Registration
     * Creates a new member with a unique email.
     * joinDate is set to today, status is set to "ACTIVE".
     */
    public Member registerMember(String fullName,
                                 String email,
                                 String plainPassword,
                                 LocalDate dateOfBirth,
                                 String gender) {

        String passwordHash = plainPassword; // can later be replaced by real hashing

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            // enforce unique email at the application level (in addition to DB constraint)
            List<Member> existing = session.createQuery(
                            "from Member m where m.email = :email", Member.class)
                    .setParameter("email", email)
                    .getResultList();

            if (!existing.isEmpty()) {
                tx.rollback();
                System.out.println("Registration failed: email already in use.");
                return null;
            }

            Member member = new Member();
            member.setFullName(fullName);
            member.setEmail(email);
            member.setPasswordHash(passwordHash);
            member.setDateOfBirth(dateOfBirth);
            member.setGender(gender);
            member.setJoinDate(LocalDate.now());
            member.setStatus("ACTIVE");

            session.persist(member);
            tx.commit();

            return member;
        }
    }

    /**
     * M2 - Profile Management (update personal details).
     * Only non-null parameters are updated.
     */
    public Member updateProfile(long memberId,
                                String newFullName,
                                String newEmail,
                                String newGender,
                                LocalDate newDateOfBirth,
                                String newPasswordPlain) {

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            Member member = session.get(Member.class, memberId);
            if (member == null) {
                tx.rollback();
                System.out.println("Member not found: " + memberId);
                return null;
            }

            if (newEmail != null && !newEmail.equals(member.getEmail())) {
                // check email uniqueness
                Long count = session.createQuery(
                                "select count(m) from Member m " +
                                        "where m.email = :email and m.memberId <> :id",
                                Long.class)
                        .setParameter("email", newEmail)
                        .setParameter("id", memberId)
                        .uniqueResult();
                if (count != null && count > 0) {
                    tx.rollback();
                    System.out.println("Update failed: email already in use.");
                    return null;
                }
                member.setEmail(newEmail);
            }

            if (newFullName != null) {
                member.setFullName(newFullName);
            }
            if (newGender != null) {
                member.setGender(newGender);
            }
            if (newDateOfBirth != null) {
                member.setDateOfBirth(newDateOfBirth);
            }
            if (newPasswordPlain != null) {
                member.setPasswordHash(newPasswordPlain);
            }

            session.merge(member);
            tx.commit();
            return member;
        }
    }

    /**
     * M2 - Profile Management (add a new fitness goal).
     * FitnessGoal is a weak entity identified by (member_id, goal_seq).
     * This method chooses the next goal_seq for this member.
     */
    public FitnessGoal addFitnessGoal(long memberId,
                                      String goalType,
                                      double targetValue,
                                      LocalDate startDate,
                                      LocalDate targetDate,
                                      String status) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            Member member = session.get(Member.class, memberId);
            if (member == null) {
                tx.rollback();
                System.out.println("Member not found: " + memberId);
                return null;
            }

            // compute next goal_seq
            Integer maxSeq = session.createQuery(
                            "select max(g.id.goalSeq) from FitnessGoal g " +
                                    "where g.id.memberId = :mid",
                            Integer.class)
                    .setParameter("mid", memberId)
                    .uniqueResult();
            int nextSeq = (maxSeq == null ? 1 : maxSeq + 1);

            FitnessGoalId id = new FitnessGoalId(memberId, nextSeq);
            FitnessGoal goal = new FitnessGoal();
            goal.setId(id);
            goal.setGoalType(goalType);
            goal.setTargetValue(targetValue);
            goal.setStartDate(startDate);
            goal.setTargetDate(targetDate);
            goal.setStatus(status != null ? status : "ACTIVE");
            goal.setMember(member);

            session.persist(goal);
            tx.commit();
            return goal;
        }
    }

    /**
     * Optional helper for M2: update an existing goal's status or target value.
     */
    public FitnessGoal updateFitnessGoal(long memberId,
                                         int goalSeq,
                                         String newStatus,
                                         Double newTargetValue) {

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            FitnessGoalId id = new FitnessGoalId(memberId, goalSeq);
            FitnessGoal goal = session.get(FitnessGoal.class, id);
            if (goal == null) {
                tx.rollback();
                System.out.println("Goal not found for member " + memberId + " seq " + goalSeq);
                return null;
            }

            if (newStatus != null) {
                goal.setStatus(newStatus);
            }
            if (newTargetValue != null) {
                goal.setTargetValue(newTargetValue);
            }

            session.merge(goal);
            tx.commit();
            return goal;
        }
    }

    /**
     * M3 - Health History: log a new HealthMetric row.
     * Does not overwrite previous entries.
     */
    public HealthMetric logHealthMetric(long memberId,
                                        Double weight,
                                        Double height,
                                        Integer heartRate,
                                        Double bodyFatPct,
                                        LocalDate recordedDate) {

        if (recordedDate == null) {
            recordedDate = LocalDate.now();
        }

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            Member member = session.get(Member.class, memberId);
            if (member == null) {
                tx.rollback();
                System.out.println("Member not found: " + memberId);
                return null;
            }

            HealthMetric metric = new HealthMetric();
            metric.setMember(member);
            metric.setRecordedDate(recordedDate);
            metric.setWeight(weight);
            metric.setHeight(height);
            metric.setHeartRate(heartRate);
            metric.setBodyFatPct(bodyFatPct);

            session.persist(metric);
            tx.commit();
            return metric;
        }
    }

    /**
     * Helper: fetch recent health metrics for a member (for the dashboard or tests).
     */
    public List<HealthMetric> getMetricsForMember(long memberId, int limit) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "from HealthMetric m " +
                                    "where m.member.memberId = :mid " +
                                    "order by m.recordedDate desc",
                            HealthMetric.class)
                    .setParameter("mid", memberId)
                    .setMaxResults(limit)
                    .getResultList();
        }
    }

    /**
     * NEW (M2) – View all fitness goals for a member.
     */
    public List<FitnessGoal> getFitnessGoals(long memberId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "from FitnessGoal g " +
                                    "where g.member.memberId = :mid " +
                                    "order by g.id.goalSeq",
                            FitnessGoal.class)
                    .setParameter("mid", memberId)
                    .getResultList();
        }
    }
}
