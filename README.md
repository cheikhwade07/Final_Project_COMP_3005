# Health & Fitness Club Management System

COMP 3005 – Fall 2025  
Instructor: Abdelghny Orogat
Seydi Cheikh Wade : 101323727
Solo project (1 student) – using **Java + Hibernate (ORM)** + **PostgreSQL**.

---

## 1. Project Overview

This project implements a **Health & Fitness Club Management System** with three roles:

- **Member** – manages profile, fitness goals, and health metrics; schedules PT sessions.
- **Trainer** – defines availability and views their PT schedule.
- **Admin** – manages room bookings for PT sessions and equipment maintenance.

All data is stored in a **PostgreSQL** database and accessed through a **Hibernate ORM** layer.  
The application is a **CLI (console) app** that demonstrates all required operations, plus one **view**, one **trigger**, and one **index** on the database side.

---

## 2. Technologies

- **Language:** Java (JDK 17+)
- **ORM:** Hibernate 6
- **Database:** PostgreSQL
- **Build tool:** Maven
- **Client:** Console (CLI)

Main packages:

- `models` – Entity classes (Member, Trainer, Admin, Room, Equipment, PTSession, FitnessGoal, HealthMetric, TrainerAvailability, Manage, etc.)
- `app` – Application entry point and console UI (`Main`, `ConsoleApp`, `DataSeeder`)
- `app.service` – Service layer (`MemberService`, `TrainerService`, `PTSessionService`, `AdminService`, `DatabaseResetService`, `HibernateUtil`)

---

## 3. Requirements Coverage (Operations by Role)

### 3.1 Member Operations (at least 4 required)

Implemented member operations:

1. **User Registration (M1)**
    - Menu: `Main → 1) Register Member`
    - Creates a `Member` with unique email, password, personal info.
    - Email uniqueness enforced at both **DB level** and in `MemberService.registerMember`.

2. **Profile Management (M2)**
    - Menu: `Member Menu → 1) Update profile (M2)`
    - Update full name, email, gender, date of birth, password.
    - Email format validation and uniqueness check before update.

3. **Fitness Goals – Create, View, Update (M2)**
    - Menu:
        - `Member Menu → 2) Add fitness goal (M2)`
        - `Member Menu → 8) View fitness goals (M2)`
        - `Member Menu → 9) Update fitness goal (M2)`
    - Goals are stored in `FitnessGoal` as a **weak entity** with composite PK `(member_id, goal_seq)`.
    - Behaviours:
        - Add new goals with type, target value, start date, target date, status.
        - View all goals for the member.
        - Update goal status (e.g., ACTIVE, COMPLETED) and/or target value.

4. **Health History – Log Metrics (M3)**
    - Menu: `Member Menu → 3) Log health metric (M3)`
    - Logs **multiple entries** (never overwrites): weight, height, heart rate, body fat %, date.
    - Stored in `HealthMetric` with `recordedDate`, ordered by date for history.

5. **View Recent Metrics (Dashboard-like) (M3)**
    - Menu: `Member Menu → 4) View recent metrics`
    - Shows the latest N health metric entries (N chosen by user).
    - This can be used in the video as a lightweight **“dashboard”** demonstration.

6. **PT Session Scheduling (M4)**
    - Menu:
        - `Member Menu → 5) Request PT session (M4)`
        - `Member Menu → 6) Reschedule PT session (M4)`
        - `Member Menu → 7) Cancel PT session (M4)`
    - Uses `PTSessionService`:
        - Request a new session with a trainer, validating **trainer availability** and **room/overlap constraints** in the service.
        - Reschedule an existing session (status changes accordingly).
        - Cancel sessions (status updated to `CANCELLED`, but history kept).

> In the video, you can explicitly say which requirement each menu option corresponds to (M1–M4).

---

### 3.2 Trainer Operations (at least 2 required)

Implemented trainer operations:

1. **Set Availability (T1)**
    - Menu: `Trainer Menu → 1) Add availability (T1)`
    - Trainer enters start and end as `YYYY-MM-DDTHH:MM`.
    - `TrainerService.addAvailability` checks for overlapping slots for that trainer and rejects invalid ones.

2. **Schedule View (T2)**
    - Menu: `Trainer Menu → 2) View schedule (T2)`
    - Shows all upcoming PT sessions for the logged-in trainer in a **simple table-like layout**:
        - Session id, member name, room id, start/end time, status.
    - This directly supports the “Schedule View” requirement.

---

### 3.3 Admin Operations (at least 2 required)

Implemented admin operations:

1. **Room Booking for PT Sessions (A1)**
    - Menu: `Admin Menu → 1) Assign room to PT session (A1)`
    - Admin sees all `PENDING` or `RESCHEDULED` PT sessions, then chooses:
        - Session id
        - Room id
    - `AdminService.assignRoomToSession`:
        - Verifies admin actually **manages the target room** (via `Manage` table).
        - Checks for **room double-booking** during the session interval.
        - If valid, assigns the room and marks the session as **CONFIRMED**.

2. **Equipment Maintenance (A2)**
    - Menu: `Admin Menu → 2) Update equipment status (A2)`
    - Lists all equipment with id, name, room id, status in a compact “table” style.
    - Admin selects equipment id and sets status (e.g. `OK`, `OUT_OF_SERVICE`, `UNDER_MAINTENANCE`).
    - Uses `AdminService.updateEquipmentStatus` and the **DB trigger** (see below) to keep room flags in sync.

---

## 4. Database Objects: View, Trigger, Index

These are created directly in PostgreSQL (outside Hibernate) in a small DDL script (e.g. `sql/extra_objects.sql`).

### 4.1 View – `vw_trainer_schedule`

**Purpose:**  
A read-only summary view that joins `pt_session`, `trainer`, `member`, and `room` so we can quickly see a trainer’s schedule with human-friendly names.

Typical columns:

- `trainer_id`, `trainer_name`
- `session_id`
- `member_name`
- `room_id`, `room_type`
- `start_time`, `end_time`
- `status`

**How you can use it in the video:**

- In pgAdmin: `SELECT * FROM vw_trainer_schedule ORDER BY start_time;`
- Show that it returns the same kind of data that the trainer sees in the CLI schedule.
- Explain that Hibernate could query this view using a native query, but the core schedule logic is already covered by the ORM.

---

### 4.2 Trigger – `trg_equipment_room_status`

**Table:** `equipment` (AFTER UPDATE OF status)  
**Purpose:**  
Automatically synchronize the **room’s status** based on the equipment inside it.

Typical behaviour:

- If **any equipment** in a room is `OUT_OF_SERVICE` or `UNDER_MAINTENANCE`, the trigger can mark the room as something like `RESTRICTED` (or leave logic as: “room flagged accordingly”).
- When all equipment in that room is back to `OK`, the room status can revert to `AVAILABLE`.

**Why this is useful:**

- Admin only updates **equipment.status** in the CLI.
- The trigger **automates business logic** so room state remains consistent, without requiring extra code in Java.

**How to show it in the video:**

1. Query room + equipment before change.
2. Use the CLI: Admin → Update equipment status to `OUT_OF_SERVICE`.
3. Refresh room row in pgAdmin to show that room status changed automatically thanks to the trigger.

---

### 4.3 Index – `idx_ptsession_trainer_start`

**Table:** `pt_session`  
**Columns:** `(trainer_id, start_time)`

**Purpose:**

- Accelerates queries of the form:  
  `WHERE trainer_id = ? AND start_time BETWEEN ? AND ?`
- This matches the schedule queries used in `TrainerService.getScheduleForTrainer` and also any queries / view that show sessions by trainer in time order.

**How to show it in the video:**

- In pgAdmin, expand **Indexes** under `pt_session` and show `idx_ptsession_trainer_start`.
- Briefly explain why indexing `(trainer_id, start_time)` is better than full table scans when the data grows.

---

## 5. Data Seeding & Reset

### 5.1 Base Seed (DataSeeder)

`app.DataSeeder` runs once at application startup:

- If there are **no admins**, it inserts:
    - 2 Admins (`Alice Admin`, `Bob Admin`)
    - 2 Trainers (`Tom Trainer`, `Tina Trainer`)
    - 2 Rooms
    - 3 Equipment items distributed across rooms
    - `Manage` relationships so each room is managed by exactly one admin (enforced by a unique constraint on `room_id` in `manage`).

This gives you a **stable initial state** that you can always come back to for testing and for the video.

### 5.2 Reset to Base Seed (DatabaseResetService + Console)

From the **main menu**:

- Option `9) Reset database to base seed`

This will:

1. **Truncate** application tables (while respecting FK constraints).
2. Rerun the `DataSeeder` to repopulate base admins/trainers/rooms/equipment/manage links.

This is ideal for the demo: if you break things, you can reset and show operations from a clean baseline.

---

## 6. How to Run the Project

### 6.1 PostgreSQL Setup

1. Create the database and user (example):

   ```sql
   CREATE DATABASE health_and_fitness_club;
   CREATE USER health_and_fitness_user WITH PASSWORD 'your_password';
   GRANT ALL PRIVILEGES ON DATABASE health_and_fitness_club TO health_and_fitness_user;
   ```
2. In pgAdmin, connect to health_and_fitness_club.

3. (Optional but recommended) Run your extra DDL script for view, trigger, index, e.g.
   -- examples, actual file is part of the submission
   -- CREATE VIEW vw_trainer_schedule AS ...
   -- CREATE OR REPLACE FUNCTION fn_update_room_status_from_equipment() RETURNS trigger AS ...
   -- CREATE TRIGGER trg_equipment_room_status AFTER UPDATE OF status ON equipment ...
   -- CREATE INDEX idx_ptsession_trainer_start ON pt_session(trainer_id, start_time);
4. Ensure the database name, username, and password match the entries in HibernateUtil / hibernate.cfg.xml:
   <property name="hibernate.connection.url">
   jdbc:postgresql://localhost:5432/health_and_fitness_club
   </property>
   <property name="hibernate.connection.username">health_and_fitness_user</property>
   <property name="hibernate.connection.password">your_password</property>

