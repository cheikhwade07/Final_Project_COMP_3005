package models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * PTSession entity
 * ---------------------------------
 * Maps to table: pt_session
 * PK: session_id (auto-generated)
 * FKs:
 *   member_id -> member.member_id
 *   trainer_id -> trainer.trainer_id
 *   room_id   -> room.room_id
 *   admin_id  -> admin.admin_id
 *   status example: status examples: "PENDING", "VALIDATED", "CANCELLED", "COMPLETED";
 * Columns: start_time, end_time, status
 */
@Entity
@Table(name = "pt_session")
public class PTSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "session_id")
    private Long sessionId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(optional = false)
    @JoinColumn(name = "trainer_id", nullable = false)
    private Trainer trainer;

    @ManyToOne(optional = true)
    @JoinColumn(name = "room_id", nullable = true)
    private Room room;


    @ManyToOne(optional = true)
    @JoinColumn(name = "admin_id", nullable = true)
    private Admin admin;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "status", nullable = false)
    private String status; // e.g. 'BOOKED', 'CANCELLED', 'COMPLETED'

    // ---- Constructors ----

    public PTSession() {
        // required by JPA
    }

    public PTSession(Member member,
                     Trainer trainer,
                     Room room,
                     Admin admin,
                     LocalDateTime startTime,
                     LocalDateTime endTime,
                     String status) {
        this.member = member;
        this.trainer = trainer;
        this.room = room;
        this.admin = admin;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
    }

    // ---- Getters & Setters ----

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public Trainer getTrainer() {
        return trainer;
    }

    public void setTrainer(Trainer trainer) {
        this.trainer = trainer;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public Admin getAdmin() {
        return admin;
    }

    public void setAdmin(Admin admin) {
        this.admin = admin;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // ---- Utility ----

    @Override
    public String toString() {
        return "PTSession{" +
                "sessionId=" + sessionId +
                ", memberId=" + (member != null ? member.getMemberId() : null) +
                ", trainerId=" + (trainer != null ? trainer.getTrainerId() : null) +
                ", roomId=" + (room != null ? room.getRoomId() : null) +
                ", adminId=" + (admin != null ? admin.getAdminId() : null) +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", status='" + status + '\'' +
                '}';
    }
}
