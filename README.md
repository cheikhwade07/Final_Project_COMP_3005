# Health & Fitness Club Management System

COMP 3005 – Fall 2025  
Instructor: Abdelghny Orogat

Seydi Cheikh Wade :101323727 (SOLO)

Console-based gym management system using **Java + Hibernate** with three roles:

- **Member** – registration, profile, goals, health metrics, PT sessions (M1–M4)  
- **Trainer** – availability and schedule (T1–T2)  
- **Admin** – room assignment and equipment maintenance (A1–A2)

---

## 1. Prerequisites

- Java 17+  
- Maven  
- A database configured in `hibernate.cfg.xml`  
  - Set your JDBC URL, username, and password correctly

---

## 2. Build & Run
### 2.1 PostgreSQL Setup

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
   
5. Ensure the database name, username, and password match the entries in HibernateUtil / hibernate.cfg.xml:
   <property name="hibernate.connection.url">
   jdbc:postgresql://localhost:5432/health_and_fitness_club
   </property>
   <property name="hibernate.connection.username">health_and_fitness_user</property>
   <property name="hibernate.connection.password">your_password</property>

From the project root:

```bash
mvn clean compile
Run the main that creates a ConsoleApp and calls run() (via IDE or mvn exec:java if configured).
```
## 3.Seeding the Database
The project includes DatabaseResetService, wired in ConsoleApp:
The project includes DatabaseResetService, wired in ConsoleApp:
- If running for the first time The main App Populate the necessary DataBase with a Seed Class
- This will:

- Drop existing data (as implemented in DatabaseResetService)

- Insert base seed for:

    -Admins
    
    -Trainers
    
    -Rooms & Manage relationships
    
    -Equipment
## 4. Seeded Sample Accounts

From DataSeeder (adjust if you changed it):

Admins

    alice.admin@example.com   /   admin123
    bob.admin@example.com     /   admin123


Trainers

    tom.trainer@example.com   /   trainer123
    tina.trainer@example.com  /   trainer123
    tony.trainer@example.com  /   trainer123


No members are seeded: create one via registration (M1).

## 5. Member Flows (M1–M4)
### 5.1 Register & Login (M1)

    From main menu:
    
    1) Register Member
    Full name: Test Member
    Email: test.member@example.com
    Password: test123
    Date of birth (YYYY-MM-DD) or empty: 2000-01-01
    Gender (optional): M


Then:
    To log in:
    
    2) Member Login
    Email: test.member@example.com
    Password: test123

### 5.2 Profile & Fitness Goals (M2)

    From member Menu after Login:
    Update profile
    1) Update profile (M2)
    New full name (leave empty to keep): Test Member Updated
    New email (leave empty to keep): test.member.updated@example.com
    New gender (leave empty to keep): M
    New date of birth (YYYY-MM-DD, empty to keep): 2000-01-02
    New password (leave empty to keep): test456

Add a fitness goal:

    2) Add fitness goal (M2)
    Goal type (e.g. WEIGHT_LOSS): WEIGHT_LOSS
    Target value (e.g. 75.0 or 75%): 75
    Start date (YYYY-MM-DD, empty for today): 2025-12-01
    Target date (YYYY-MM-DD, empty to skip): 2026-03-01

View & update/delete goals:

    8) View fitness goals (M2)
    9) Update/delete fitness goal (M2)
    Enter goal sequence: 1
    Do you want to DELETE this goal? (y/N): N
    New status (e.g. ACTIVE, COMPLETED) (empty or '-' to keep): COMPLETED
    New target value (empty to keep): 70


To delete instead:

    Do you want to DELETE this goal? (y/N): y

### 5.3 Health Metrics (M3)
Log a metric:

    3) Log health metric (M3)
    Weight (kg, empty if unknown): 75
    Height (m, empty if unknown): 1.80
    Heart rate (bpm, empty if unknown): 70
    Body fat (%) (empty if unknown): 18%
    Recorded date (YYYY-MM-DD, empty for today): 2025-12-01

View health logs:

    4) View health logs (M3)


Outputs a table of metrics for that member.

### 5.4 PT Sessions (M4)
View your PT sessions:

    10) View PT sessions (M4)

Initially empty, then shows your booked sessions.

Request a PT session (schedule):


    5) Request PT session (M4)

The app shows ACTIVE trainer availabilities:
    
    +--------------------------------------------------------------------------------+
    | ID  | Trainer           | Start               | End                 | Status   |
    +--------------------------------------------------------------------------------+
    | 1   | Tom Trainer       | 2025-12-10T09:00    | 2025-12-10T10:00    | ACTIVE   |
    ...
    
    
Example:
    
    Enter Availability ID to book: 1


Backend:

- Checks member + trainer exist

- Ensures trainer has an ACTIVE availability covering that window

- Ensures no conflicting PTSession for that trainer

- Creates a PTSession with status = PENDING

- Marks that TrainerAvailability slot as BOOKED

Confirm:

    10) View PT sessions (M4)


    You should see the new PENDING session.

Reschedule a PT session:

    6) Reschedule PT session (M4)


Flow:

Lists your sessions.

Pick one:

    Enter session id to reschedule: 1


Shows ACTIVE availability for the same trainer:

    Available slots for trainer Tom Trainer:
    +--------------------------------------------------------------------------------+
    | ID  | Trainer           | Start               | End                 | Status   |
    +--------------------------------------------------------------------------------+
    | 2   | Tom Trainer       | 2025-12-11T09:00    | 2025-12-11T10:00    | ACTIVE   |
    ...


Choose:

    Enter Availability ID to move session to: 2


Backend:

- Validates availability + no trainer conflict (excluding this session)

- Updates session start/end

- Clears room/admin references

- Sets status back to PENDING

- Marks the chosen availability as BOOKED

Cancel a PT Session:

    7) Cancel PT session (M4)
    Enter session id to cancel: 1


Backend:

- Verifies session belongs to that member

- Sets status to CANCELLED

## 6. Trainer Flows (T1–T2)

Login as trainer (seed):

    3) Trainer Login
    Email: tom.trainer@example.com
    Password: trainer123

## 6.1 Add Availability (T1)

    1) Add availability (T1)
    
    Start (YYYY-MM-DDTHH:MM): 2025-12-15T09:00
    
    End   (YYYY-MM-DDTHH:MM): 2025-12-15T10:00


Creates an ACTIVE TrainerAvailability that members can book.

##6.2 View Schedule (T2)

    2) View schedule (T2)

Shows each PT session (member, room ID if assigned, start/end, status).

# 7. Admin Flows (A1–A2)

Login as admin (seed):

    4) Admin Login
    Email: alice.admin@example.com
    Password: admin123

## 7.1 Assign Room to PT Session (A1)
    1) Assign room to PT session (A1)

Flow:

Lists PENDING / RESCHEDULED sessions:

id=1 member=Test Member trainer=Tom Trainer start=... end=... status=PENDING


Pick session:

    Session id to assign room to: 1


Lists only rooms this admin manages (via Manage):

Rooms you manage:
  id=1 type=PT_ROOM status=AVAILABLE


Choose room:

    Room id: 1


Backend (AdminService.assignRoomToSession):

- Checks admin, session, room exist

- Verifies admin manages that room:

  ```sql

    select count(m)
    from Manage m
    where m.admin.adminId = :aid
       and m.room.roomId   = :rid
  ```

- Verifies no room double-booking on the same time window

- Links room + admin to the PTSession

- Updates session status = VALIDATED

If the admin does not manage the room, the method rolls back and prints an error.

## 7.2 Update Equipment Status (A2)

    2) Update equipment status (A2)


Flow:

Lists equipment only in rooms this admin manages:

| EqID | Name         | RoomID | RoomType | Category | Status          |
| 1    | Treadmill A  | 1      | PT_ROOM  | CARDIO   | OK              |


Update:

    Equipment id to update: 1
    New status (e.g. OK, OUT_OF_SERVICE, UNDER_MAINTENANCE): OUT_OF_SERVICE


Backend (AdminService.updateEquipmentStatus):

- Checks admin + equipment exist

- Verifies admin manages the room of that equipment

- Updates Equipment.status

- Prints updated row

# 8. Reset Between Test Runs

At any time, you can return to a clean state:

Main Menu -> 9) Reset database to base seed
BUT following the ReadMe You shouldn't meet any major issues.

