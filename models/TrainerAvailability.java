package models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * TrainerAvailability entity
 * -----------------------------
 * Maps to table: trainer_availability
 * PK: availability_id (auto-generated)
 * FK: trainer_id -> trainer.trainer_id
 * Columns: start_time, end_time, status
 */
@Entity
@Table(name = "trainer_availability")
public class TrainerAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "availability_id")
    private Long availabilityId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "trainer_id", nullable = false)
    private Trainer trainer;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "status", nullable = false)
    private String status;   // e.g. 'AVAILABLE', 'BLOCKED'

    // ---- Constructors ----

    public TrainerAvailability() {
        // required by JPA
    }

    public TrainerAvailability(Trainer trainer,
                               LocalDateTime startTime,
                               LocalDateTime endTime,
                               String status) {
        this.trainer = trainer;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
    }

    // ---- Getters & Setters ----

    public Long getAvailabilityId() {
        return availabilityId;
    }

    public void setAvailabilityId(Long availabilityId) {
        this.availabilityId = availabilityId;
    }

    public Trainer getTrainer() {
        return trainer;
    }

    public void setTrainer(Trainer trainer) {
        this.trainer = trainer;
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
        return "TrainerAvailability{" +
                "availabilityId=" + availabilityId +
                ", trainerId=" + (trainer != null ? trainer.getTrainerId() : null) +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", status='" + status + '\'' +
                '}';
    }
}
