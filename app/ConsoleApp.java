package app;

import app.service.AdminService;
import app.service.DatabaseResetService;
import app.service.HibernateUtil;
import app.service.MemberService;
import app.service.PTSessionService;
import app.service.TrainerService;
import models.*;

import org.hibernate.Session;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ConsoleApp {

    private final Scanner scanner = new Scanner(System.in);

    private final MemberService memberService = new MemberService();
    private final PTSessionService ptSessionService = new PTSessionService();
    private final TrainerService trainerService = new TrainerService();
    private final AdminService adminService = new AdminService();

    private final DatabaseResetService resetService;

    public ConsoleApp(DatabaseResetService resetService) {
        this.resetService = resetService;
    }

    // ===================== COMMON INPUT HELPERS =====================

    private String readRequired(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            if (!line.isEmpty()) {
                return line;
            }
            System.out.println("This field is required. Please enter a value.");
        }
    }

    private boolean isValidEmail(String email) {
        return email != null &&
                email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    }

    private String readValidEmail(String prompt) {
        while (true) {
            String email = readRequired(prompt);
            if (isValidEmail(email)) {
                return email;
            }
            System.out.println("Please enter a valid email address (example: user@example.com).");
        }
    }

    private double readRequiredDoubleWithPercent(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                System.out.println("Please enter a numeric value.");
                continue;
            }
            if (input.endsWith("%")) {
                input = input.substring(0, input.length() - 1).trim();
            }
            try {
                return Double.parseDouble(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Use a format like 75 or 75.0 (optionally with %).");
            }
        }
    }

    private Double readOptionalDoubleWithPercent(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return null;
            }
            if (input.endsWith("%")) {
                input = input.substring(0, input.length() - 1).trim();
            }
            try {
                return Double.parseDouble(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid number; please try again or leave empty to skip.");
            }
        }
    }

    private int readRequiredInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid integer.");
            }
        }
    }

    private long readRequiredLong(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Long.parseLong(input);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid numeric id.");
            }
        }
    }

    private LocalDate readOptionalDate(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return null;
            }
            try {
                return LocalDate.parse(input);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Use YYYY-MM-DD or leave empty to skip.");
            }
        }
    }

    private LocalDateTime readRequiredDateTime(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return LocalDateTime.parse(input);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date-time format. Use YYYY-MM-DDTHH:MM.");
            }
        }
    }

    // ========================== MAIN LOOP ==========================

    public void run() {
        boolean running = true;
        while (running) {
            System.out.println();
            System.out.println("=== Main Menu ===");
            System.out.println("1) Register Member");
            System.out.println("2) Member Login");
            System.out.println("3) Trainer Login");
            System.out.println("4) Admin Login");
            System.out.println("9) Reset database to base seed");
            System.out.println("0) Exit");
            System.out.print("Choice: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> handleMemberRegistration();
                case "2" -> handleMemberLogin();
                case "3" -> handleTrainerLogin();
                case "4" -> handleAdminLogin();
                case "9" -> {
                    System.out.println("--- Resetting database to base seed ---");
                    resetService.resetToBaseSeed();
                    System.out.println("Database reset complete.");
                }
                case "0" -> running = false;
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    // =========================== MEMBER FLOW ===========================

    private void handleMemberRegistration() {
        System.out.println("--- Member Registration (M1) ---");
        String fullName = readRequired("Full name: ");
        String email = readValidEmail("Email: ");
        String password = readRequired("Password: ");

        LocalDate dob = readOptionalDate("Date of birth (YYYY-MM-DD) or empty: ");

        System.out.print("Gender (optional): ");
        String gender = scanner.nextLine();

        Member m = memberService.registerMember(fullName, email, password, dob, gender);
        if (m != null) {
            System.out.println("Registered member with id: " + m.getMemberId());
        } else {
            System.out.println("Registration failed (email may already be in use).");
        }
    }

    private Member findMemberByCredentials(String email, String password) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "from Member m where m.email = :email and m.passwordHash = :pw",
                            Member.class)
                    .setParameter("email", email)
                    .setParameter("pw", password)
                    .uniqueResult();
        }
    }

    private void handleMemberLogin() {
        System.out.println("--- Member Login ---");
        String email = readValidEmail("Email: ");
        String password = readRequired("Password: ");

        Member member = findMemberByCredentials(email, password);
        if (member == null) {
            System.out.println("Invalid credentials or member not found.");
            return;
        }

        System.out.println("Welcome, " + member.getFullName() +
                " (id=" + member.getMemberId() + ")");
        memberMenu(member);
    }

    private void memberMenu(Member member) {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println();
            System.out.println("=== Member Menu ===");
            System.out.println("1) Update profile (M2)");
            System.out.println("2) Add fitness goal (M2)");
            System.out.println("3) Log health metric (M3)");
            System.out.println("4) View health logs (M3)");
            System.out.println("5) Request PT session (M4)");
            System.out.println("6) Reschedule PT session (M4)");
            System.out.println("7) Cancel PT session (M4)");
            System.out.println("8) View fitness goals (M2)");
            System.out.println("9) Update/delete fitness goal (M2)");
            System.out.println("10) View PT sessions (M4)");
            System.out.println("0) Logout");
            System.out.print("Choice: ");


            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> handleUpdateProfile(member);
                case "2" -> handleAddFitnessGoal(member);
                case "3" -> handleLogHealthMetric(member);
                case "4" -> handleViewHealthLogs(member);
                case "5" -> handleRequestPTSession(member);
                case "6" -> handleRescheduleSession(member);
                case "7" -> handleCancelSession(member);
                case "8" -> handleViewFitnessGoals(member);
                case "9" -> handleUpdateFitnessGoal(member);
                case "10" -> handleViewPTSessions(member);
                case "0" -> loggedIn = false;
                default -> System.out.println("Invalid choice.");
            }

        }
    }

    private void handleUpdateProfile(Member member) {
        System.out.println("--- Update Profile (M2) ---");
        System.out.print("New full name (leave empty to keep): ");
        String newName = scanner.nextLine();
        if (newName.isEmpty()) newName = null;

        System.out.print("New email (leave empty to keep): ");
        String newEmail = scanner.nextLine();
        if (newEmail.isEmpty()) {
            newEmail = null;
        } else if (!isValidEmail(newEmail)) {
            System.out.println("Invalid email format; ignoring email change.");
            newEmail = null;
        }

        System.out.print("New gender (leave empty to keep): ");
        String newGender = scanner.nextLine();
        if (newGender.isEmpty()) newGender = null;

        LocalDate newDob = readOptionalDate("New date of birth (YYYY-MM-DD, empty to keep): ");

        System.out.print("New password (leave empty to keep): ");
        String newPw = scanner.nextLine();
        if (newPw.isEmpty()) newPw = null;

        Member updated = memberService.updateProfile(
                member.getMemberId(),
                newName,
                newEmail,
                newGender,
                newDob,
                newPw
        );

        if (updated != null) {
            System.out.println("Profile updated.");
        } else {
            System.out.println("Profile update failed (possibly email already in use).");
        }
    }

    private void handleAddFitnessGoal(Member member) {
        System.out.println("--- Add Fitness Goal (M2) ---");
        System.out.print("Goal type (e.g. WEIGHT_LOSS): ");
        String goalType = scanner.nextLine();

        double target = readRequiredDoubleWithPercent("Target value (e.g. 75.0 or 75%): ");

        LocalDate start = readOptionalDate("Start date (YYYY-MM-DD, empty for today): ");
        if (start == null) {
            start = LocalDate.now();
        }

        LocalDate targetDate = readOptionalDate("Target date (YYYY-MM-DD, empty to skip): ");

        FitnessGoal goal = memberService.addFitnessGoal(
                member.getMemberId(),
                goalType,
                target,
                start,
                targetDate,
                "ACTIVE"
        );
        if (goal != null) {
            System.out.println("Goal added for member " + member.getMemberId());
        } else {
            System.out.println("Failed to add goal.");
        }
    }

    private void handleLogHealthMetric(Member member) {
        System.out.println("--- Log Health Metric (M3) ---");

        Double weight = readOptionalDoubleWithPercent("Weight (kg, empty if unknown): ");
        Double height = readOptionalDoubleWithPercent("Height (m, empty if unknown): ");

        Integer hr = null;
        while (true) {
            System.out.print("Heart rate (bpm, empty if unknown): ");
            String hrStr = scanner.nextLine().trim();
            if (hrStr.isEmpty()) {
                break;
            }
            try {
                hr = Integer.valueOf(hrStr);
                break;
            } catch (NumberFormatException e) {
                System.out.println("Invalid integer; please try again or leave empty.");
            }
        }

        Double bf = readOptionalDoubleWithPercent("Body fat (%) (empty if unknown): ");

        LocalDate date = readOptionalDate("Recorded date (YYYY-MM-DD, empty for today): ");
        if (date == null) {
            date = LocalDate.now();
        }

        HealthMetric metric = memberService.logHealthMetric(
                member.getMemberId(),
                weight,
                height,
                hr,
                bf,
                date
        );

        if (metric != null) {
            System.out.println("Metric logged with id: " + metric.getMetricId());
        } else {
            System.out.println("Failed to log metric.");
        }
    }

    private void handleViewHealthLogs(Member member) {
        System.out.println("--- Health Logs (M3) ---");

        List<HealthMetric> logs = memberService.getMetricsForMember(member.getMemberId());
        if (logs.isEmpty()) {
            System.out.println("No health logs found.");
            return;
        }

        System.out.println("+---------------------------------------------------------------------+");
        System.out.println("| ID   | Date       | Weight | Height | HeartRate | BodyFat%         |");
        System.out.println("+---------------------------------------------------------------------+");
        for (HealthMetric m : logs) {
            String weightStr = (m.getWeight() != null) ? String.format("%.1f", m.getWeight()) : "-";
            String heightStr = (m.getHeight() != null) ? String.format("%.2f", m.getHeight()) : "-";
            String hrStr     = (m.getHeartRate() != null) ? m.getHeartRate().toString() : "-";
            String bfStr     = (m.getBodyFatPct() != null) ? String.format("%.1f", m.getBodyFatPct()) : "-";

            System.out.printf(
                    "| %-4d | %-10s | %-6s | %-6s | %-9s | %-16s |%n",
                    m.getMetricId(),
                    m.getRecordedDate(),
                    weightStr,
                    heightStr,
                    hrStr,
                    bfStr
            );
        }
        System.out.println("+---------------------------------------------------------------------+");
    }


    private void handleViewFitnessGoals(Member member) {
        System.out.println("--- Your Fitness Goals (M2) ---");
        printFitnessGoalsTable(member);
    }


    private void handleUpdateFitnessGoal(Member member) {
        System.out.println("--- Update Fitness Goal (M2) ---");

        // 1) Show current goals and bail out if none
        java.util.List<FitnessGoal> goals = printFitnessGoalsTable(member);
        if (goals.isEmpty()) {
            // Nothing to update
            return;
        }

        // 2) Ask which goal to act on
        int seq = readRequiredInt("Enter goal sequence: ");

        // 2.5) Ask if we want to delete instead of update
        System.out.print("Do you want to DELETE this goal? (y/N): ");
        String delAnswer = scanner.nextLine().trim();
        if (delAnswer.equalsIgnoreCase("y") || delAnswer.equalsIgnoreCase("yes")) {
            boolean deleted = memberService.deleteFitnessGoal(member.getMemberId(), seq);
            if (deleted) {
                System.out.println("Goal deleted successfully.");
            } else {
                System.out.println("Goal deletion failed (goal may not exist).");
            }
            return;
        }

        // 3) Otherwise, proceed with update
        System.out.print("New status (e.g. ACTIVE, COMPLETED) (empty or '-' to keep): ");
        String statusInput = scanner.nextLine().trim();
        String newStatus = null;
        if (!statusInput.isEmpty() && !statusInput.equals("-")) {
            newStatus = statusInput;
        }

        Double newTarget = readOptionalDoubleWithPercent("New target value (empty to keep): ");

        // 4) If user gave nothing, skip update
        if (newStatus == null && newTarget == null) {
            System.out.println("No changes provided. Goal left unchanged.");
            return;
        }

        // 5) Call service
        FitnessGoal updated = memberService.updateFitnessGoal(
                member.getMemberId(),
                seq,
                newStatus,
                newTarget
        );

        if (updated != null) {
            System.out.println("Goal updated successfully.");
        } else {
            System.out.println("Goal update failed (goal may not exist).");
        }
    }



    private void handleRequestPTSession(Member member) {
        System.out.println("--- Request PT Session (M4) ---");

        // Show trainer availabilities as a table
        List<TrainerAvailability> slots = trainerService.getAllActiveAvailabilities();
        if (slots.isEmpty()) {
            System.out.println("No trainer availabilities at the moment.");
            return;
        }

        System.out.println("+--------------------------------------------------------------------------------+");
        System.out.println("| ID  | Trainer           | Start               | End                 | Status   |");
        System.out.println("+--------------------------------------------------------------------------------+");
        for (TrainerAvailability a : slots) {
            System.out.printf("| %-3d | %-17s | %-19s | %-19s | %-8s |%n",
                    a.getAvailabilityId(),
                    a.getTrainer().getFullName(),
                    a.getStartTime(),
                    a.getEndTime(),
                    a.getStatus());
        }
        System.out.println("+--------------------------------------------------------------------------------+");

        long availabilityId = readRequiredLong("Enter Availability ID to book: ");

        TrainerAvailability chosen;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            chosen = session.get(TrainerAvailability.class, availabilityId);
        }

        if (chosen == null || !"ACTIVE".equalsIgnoreCase(chosen.getStatus())) {
            System.out.println("Invalid or inactive availability slot.");
            return;
        }

        PTSession s = ptSessionService.requestSession(
                member.getMemberId(),
                chosen.getTrainer().getTrainerId(),
                chosen.getStartTime(),
                chosen.getEndTime()
        );

        if (s != null) {
            System.out.println("Requested session with " + chosen.getTrainer().getFullName() +
                    " on " + s.getStartTime() +
                    " (status=" + s.getStatus() + ")");
        } else {
            System.out.println("Failed to request session.");
        }
    }

    private void handleViewPTSessions(Member member) {
        System.out.println("--- Your PT Sessions (M4) ---");
        listMemberSessions(member);
    }

    private void handleRescheduleSession(Member member) {
        System.out.println("--- Reschedule PT Session (M4) ---");
        listMemberSessions(member);

        long sid = readRequiredLong("Enter session id to reschedule: ");

        // Load the session to find its trainer and verify ownership
        PTSession sessionEntity;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            sessionEntity = session.get(PTSession.class, sid);
        }

        if (sessionEntity == null) {
            System.out.println("Session not found.");
            return;
        }
        if (sessionEntity.getMember() == null ||
                sessionEntity.getMember().getMemberId() != member.getMemberId()) {
            System.out.println("You can only reschedule your own sessions.");
            return;
        }
        if (sessionEntity.getTrainer() == null) {
            System.out.println("Session has no trainer assigned yet; cannot reschedule using trainer availability.");
            return;
        }

        long trainerId = sessionEntity.getTrainer().getTrainerId();

        // Show only ACTIVE availability slots for this trainer
        List<TrainerAvailability> allSlots = trainerService.getAllActiveAvailabilities();
        List<TrainerAvailability> trainerSlots = new ArrayList<>();
        for (TrainerAvailability a : allSlots) {
            if (a.getTrainer() != null && a.getTrainer().getTrainerId() == trainerId) {
                trainerSlots.add(a);
            }
        }

        if (trainerSlots.isEmpty()) {
            System.out.println("This trainer has no ACTIVE availability slots to reschedule into.");
            return;
        }

        System.out.println("Available slots for trainer " + sessionEntity.getTrainer().getFullName() + ":");
        System.out.println("+--------------------------------------------------------------------------------+");
        System.out.println("| ID  | Trainer           | Start               | End                 | Status   |");
        System.out.println("+--------------------------------------------------------------------------------+");
        for (TrainerAvailability a : trainerSlots) {
            System.out.printf("| %-3d | %-17s | %-19s | %-19s | %-8s |%n",
                    a.getAvailabilityId(),
                    a.getTrainer().getFullName(),
                    a.getStartTime(),
                    a.getEndTime(),
                    a.getStatus());
        }
        System.out.println("+--------------------------------------------------------------------------------+");

        long availabilityId = readRequiredLong("Enter Availability ID to move session to: ");

        TrainerAvailability chosen;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            chosen = session.get(TrainerAvailability.class, availabilityId);
        }

        if (chosen == null ||
                chosen.getTrainer() == null ||
                chosen.getTrainer().getTrainerId() != trainerId ||
                !"ACTIVE".equalsIgnoreCase(chosen.getStatus())) {
            System.out.println("Invalid availability selection.");
            return;
        }

        PTSession s = ptSessionService.rescheduleSession(
                sid,
                availabilityId,
                chosen.getStartTime(),
                chosen.getEndTime()
        );

        if (s != null) {
            System.out.println("Session rescheduled to " + s.getStartTime() +
                    ", now status=" + s.getStatus());
        } else {
            System.out.println("Reschedule failed.");
        }
    }


    private void handleCancelSession(Member member) {
        System.out.println("--- Cancel PT Session (M4) ---");
        listMemberSessions(member);

        long sid = readRequiredLong("Enter session id to cancel: ");

        PTSession s = ptSessionService.cancelSessionAsMember(
                member.getMemberId(),
                sid
        );
        if (s != null) {
            System.out.println("Session cancelled, status=" + s.getStatus());
        } else {
            System.out.println("Cancel failed.");
        }
    }

    private void listMemberSessions(Member member) {
        List<PTSession> sessions = memberService.getSessionsForMember(member.getMemberId());
        if (sessions.isEmpty()) {
            System.out.println("No sessions for this member.");
        } else {
            System.out.println("+-------------------------------------------------------------------------------+");
            System.out.println("| ID  | Trainer           | Start               | End                 | Status  |");
            System.out.println("+-------------------------------------------------------------------------------+");
            for (PTSession s : sessions) {
                String trainerName = (s.getTrainer() != null ? s.getTrainer().getFullName() : "-");
                System.out.printf(
                        "| %-3d | %-17s | %-19s | %-19s | %-7s |%n",
                        s.getSessionId(),
                        trainerName,
                        s.getStartTime(),
                        s.getEndTime(),
                        s.getStatus()
                );
            }
            System.out.println("+-------------------------------------------------------------------------------+");
        }
    }


    // ========================== TRAINER FLOW ==========================

    private Trainer findTrainerByCredentials(String email, String password) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "from Trainer t where t.email = :email and t.passwordHash = :pw",
                            Trainer.class)
                    .setParameter("email", email)
                    .setParameter("pw", password)
                    .uniqueResult();
        }
    }

    private void handleTrainerLogin() {
        System.out.println("--- Trainer Login ---");
        String email = readValidEmail("Email: ");
        String password = readRequired("Password: ");

        Trainer trainer = findTrainerByCredentials(email, password);
        if (trainer == null) {
            System.out.println("Invalid credentials or trainer not found.");
            return;
        }

        System.out.println("Welcome, " + trainer.getFullName() +
                " (id=" + trainer.getTrainerId() + ")");
        trainerMenu(trainer);
    }

    private void trainerMenu(Trainer trainer) {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println();
            System.out.println("=== Trainer Menu ===");
            System.out.println("1) Add availability (T1)");
            System.out.println("2) View schedule (T2)");
            System.out.println("0) Logout");
            System.out.print("Choice: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> handleAddAvailability(trainer);
                case "2" -> handleViewTrainerSchedule(trainer);
                case "0" -> loggedIn = false;
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private void handleAddAvailability(Trainer trainer) {
        System.out.println("--- Add Availability (T1) ---");
        LocalDateTime start = readRequiredDateTime("Start (YYYY-MM-DDTHH:MM): ");
        LocalDateTime end = readRequiredDateTime("End   (YYYY-MM-DDTHH:MM): ");

        TrainerAvailability a = trainerService.addAvailability(
                trainer.getTrainerId(),
                start,
                end,
                "ACTIVE"
        );
        if (a != null) {
            System.out.println("Availability added with id: " + a.getAvailabilityId());
        } else {
            System.out.println("Failed to add availability.");
        }
    }

    private void handleViewTrainerSchedule(Trainer trainer) {
        System.out.println("--- Trainer Schedule (T2) ---");

        List<PTSession> sessions =
                trainerService.getScheduleForTrainer(trainer.getTrainerId());

        if (sessions.isEmpty()) {
            System.out.println("You have no scheduled sessions.");
            return;
        }

        System.out.println("+--------------------------------------------------------------------------------------+");
        System.out.println("| ID  | Member              | Room | Start              | End                | Status  |");
        System.out.println("+--------------------------------------------------------------------------------------+");
        for (PTSession s : sessions) {
            String memberName = (s.getMember() != null ? s.getMember().getFullName() : "-");
            String roomStr = (s.getRoom() != null ? String.valueOf(s.getRoom().getRoomId()) : "-");

            System.out.printf(
                    "| %-3d | %-18s | %-4s | %-18s | %-18s | %-7s |%n",
                    s.getSessionId(),
                    memberName,
                    roomStr,
                    s.getStartTime(),
                    s.getEndTime(),
                    s.getStatus()
            );
        }
        System.out.println("+--------------------------------------------------------------------------------------+");
    }


    // ============================ ADMIN FLOW ============================

    private Admin findAdminByCredentials(String email, String password) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "from Admin a where a.email = :email and a.passwordHash = :pw",
                            Admin.class)
                    .setParameter("email", email)
                    .setParameter("pw", password)
                    .uniqueResult();
        }
    }

    private void handleAdminLogin() {
        System.out.println("--- Admin Login ---");
        String email = readValidEmail("Email: ");
        String password = readRequired("Password: ");

        Admin admin = findAdminByCredentials(email, password);
        if (admin == null) {
            System.out.println("Invalid credentials or admin not found.");
            return;
        }

        System.out.println("Welcome, " + admin.getFullName() +
                " (id=" + admin.getAdminId() + ")");
        adminMenu(admin);
    }

    private void adminMenu(Admin admin) {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println();
            System.out.println("=== Admin Menu ===");
            System.out.println("1) Assign room to PT session (A1)");
            System.out.println("2) Update equipment status (A2)");
            System.out.println("0) Logout");
            System.out.print("Choice: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> handleAssignRoomToSession(admin);
                case "2" -> handleUpdateEquipmentStatus(admin);
                case "0" -> loggedIn = false;
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private void handleAssignRoomToSession(Admin admin) {
        System.out.println("--- Assign Room to PT Session (A1) ---");

        // 1) Show all PENDING / RESCHEDULED sessions in a table
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<PTSession> sessions = session.createQuery(
                            "from PTSession s " +
                                    "where s.status in ('PENDING', 'RESCHEDULED') " +
                                    "order by s.startTime",
                            PTSession.class)
                    .getResultList();

            if (sessions.isEmpty()) {
                System.out.println("No pending or rescheduled sessions.");
                return;
            }

            System.out.println("+----------------------------------------------------------------------------------------------+");
            System.out.println("| ID  | Member              | Trainer             | Start              | End                | Status  |");
            System.out.println("+----------------------------------------------------------------------------------------------+");
            for (PTSession s : sessions) {
                String memberName  = (s.getMember()  != null ? s.getMember().getFullName()  : "-");
                String trainerName = (s.getTrainer() != null ? s.getTrainer().getFullName() : "-");

                System.out.printf(
                        "| %-3d | %-18s | %-18s | %-18s | %-18s | %-7s |%n",
                        s.getSessionId(),
                        memberName,
                        trainerName,
                        s.getStartTime(),
                        s.getEndTime(),
                        s.getStatus()
                );
            }
            System.out.println("+----------------------------------------------------------------------------------------------+");
        }

        long sid = readRequiredLong("Session id to assign room to: ");

        // 2) Show only rooms managed by this admin
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Room> rooms = session.createQuery(
                            "select m.room " +
                                    "from Manage m " +
                                    "where m.admin.adminId = :aid " +
                                    "order by m.room.roomId",
                            Room.class)
                    .setParameter("aid", admin.getAdminId())
                    .getResultList();

            if (rooms.isEmpty()) {
                System.out.println("You do not manage any rooms.");
                return;
            }

            System.out.println("Rooms you manage:");
            System.out.println("+-------------------------------------------------+");
            System.out.println("| RoomID | Type       | Status                    |");
            System.out.println("+-------------------------------------------------+");
            for (Room r : rooms) {
                System.out.printf(
                        "| %-6d | %-10s | %-24s |%n",
                        r.getRoomId(),
                        r.getRoomType(),
                        r.getStatus()
                );
            }
            System.out.println("+-------------------------------------------------+");
        }

        long rid = readRequiredLong("Room id: ");

        PTSession s = adminService.assignRoomToSession(
                admin.getAdminId(),
                sid,
                rid
        );

        if (s != null) {
            System.out.println("Room assigned and session validated. Status=" + s.getStatus());
        } else {
            System.out.println("Room assignment failed.");
        }
    }

    private void handleUpdateEquipmentStatus(Admin admin) {
        System.out.println("--- Update Equipment Status (A2) ---");

        List<Equipment> eqs;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            eqs = session.createQuery(
                            "select e " +
                                    "from Equipment e " +
                                    "where e.room.roomId in (" +
                                    "   select m.room.roomId " +
                                    "   from Manage m " +
                                    "   where m.admin.adminId = :aid" +
                                    ") " +
                                    "order by e.room.roomId, e.equipmentId",
                            Equipment.class)
                    .setParameter("aid", admin.getAdminId())
                    .getResultList();
        }

        if (eqs.isEmpty()) {
            System.out.println("You do not manage any rooms with equipment (or no equipment exists).");
            return;
        }

        // Show only equipment in rooms this admin manages
        printEquipmentTable(eqs);

        long eid = readRequiredLong("Equipment id to update: ");
        System.out.print("New status (e.g. OK, OUT_OF_SERVICE, UNDER_MAINTENANCE): ");
        String newStatus = scanner.nextLine();

        Equipment e = adminService.updateEquipmentStatus(
                admin.getAdminId(),
                eid,
                newStatus
        );

        if (e != null) {
            System.out.println("Equipment updated:");
            printEquipmentTable(List.of(e));  // show updated row
        } else {
            System.out.println("Failed to update equipment (maybe this equipment is not in your rooms).");
        }
    }


    // Helper: fetch and print all fitness goals for this member.
    private java.util.List<FitnessGoal> printFitnessGoalsTable(Member member) {
        java.util.List<FitnessGoal> goals = memberService.getFitnessGoals(member.getMemberId());
        if (goals.isEmpty()) {
            System.out.println("No goals found.");
            return goals;
        }

        System.out.println("+--------------------------------------------------------------------------+");
        System.out.println("| Seq | Type           | Target | Start Date | Target Date | Status       |");
        System.out.println("+--------------------------------------------------------------------------+");
        for (FitnessGoal g : goals) {
            System.out.printf("| %-3d | %-14s | %-6.1f | %-10s | %-11s | %-11s |%n",
                    g.getId().getGoalSeq(),
                    g.getGoalType(),
                    g.getTargetValue(),
                    g.getStartDate(),
                    (g.getTargetDate() != null ? g.getTargetDate() : "-"),
                    g.getStatus());
        }
        System.out.println("+--------------------------------------------------------------------------+");

        return goals;
    }

    // Pretty-print a table of equipment with room info for A2
    private void printEquipmentTable(List<Equipment> eqs) {
        if (eqs == null || eqs.isEmpty()) {
            System.out.println("No equipment in system.");
            return;
        }

        System.out.println("+-------------------------------------------------------------------------------------------+");
        System.out.println("| EqID | Name               | RoomID | RoomType    | Category     | Status                 |");
        System.out.println("+-------------------------------------------------------------------------------------------+");

        for (Equipment e : eqs) {
            String roomId   = (e.getRoom() != null ? String.valueOf(e.getRoom().getRoomId()) : "-");
            String roomType = (e.getRoom() != null ? e.getRoom().getRoomType() : "-");

            System.out.printf(
                    "| %-4d | %-18s | %-6s | %-11s | %-12s | %-21s |%n",
                    e.getEquipmentId(),
                    e.getName(),
                    roomId,
                    roomType,
                    e.getCategory(),
                    e.getStatus()
            );
        }

        System.out.println("+-------------------------------------------------------------------------------------------+");
    }

}
