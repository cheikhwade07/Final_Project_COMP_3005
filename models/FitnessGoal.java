package models;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * FitnessGoal entity (weak entity of Member)
 * -----------------------------------------
 * Maps to table: fitness_goal
 * PK: (member_id, goal_seq) via FitnessGoalId
 * FK: member_id -> member.member_id
 * Columns: goal_type, target_value, start_date, target_date, status
 */
@Entity
@Table(name = "fitness_goal")
public class FitnessGoal {

    @EmbeddedId
    private FitnessGoalId id;

    @MapsId("memberId") // maps id.memberId to this relation's member_id
    @ManyToOne(optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "goal_type", nullable = false)
    private String goalType;       // e.g. 'WEIGHT', 'BODY_FAT', 'ENDURANCE'

    @Column(name = "target_value", nullable = false)
    private Double targetValue;    // target number (e.g. 70.0 kg)

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "target_date")
    private LocalDate targetDate;

    @Column(name = "status", nullable = false)
    private String status;         // e.g. 'ACTIVE', 'COMPLETED', 'CANCELLED'

    // ---- Constructors ----

    public FitnessGoal() {
        // required by JPA
    }

    public FitnessGoal(Member member,
                       Integer goalSeq,
                       String goalType,
                       Double targetValue,
                       LocalDate startDate,
                       LocalDate targetDate,
                       String status) {
        this.member = member;
        this.id = new FitnessGoalId(member.getMemberId(), goalSeq);
        this.goalType = goalType;
        this.targetValue = targetValue;
        this.startDate = startDate;
        this.targetDate = targetDate;
        this.status = status;
    }

    // ---- Getters & Setters ----

    public FitnessGoalId getId() {
        return id;
    }

    public void setId(FitnessGoalId id) {
        this.id = id;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public String getGoalType() {
        return goalType;
    }

    public void setGoalType(String goalType) {
        this.goalType = goalType;
    }

    public Double getTargetValue() {
        return targetValue;
    }

    public void setTargetValue(Double targetValue) {
        this.targetValue = targetValue;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getTargetDate() {
        return targetDate;
    }

    public void setTargetDate(LocalDate targetDate) {
        this.targetDate = targetDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // ---- Utility ----

    public Long getMemberId() {
        return (id != null ? id.getMemberId() : null);
    }

    public Integer getGoalSeq() {
        return (id != null ? id.getGoalSeq() : null);
    }

    @Override
    public String toString() {
        return "FitnessGoal{" +
                "memberId=" + getMemberId() +
                ", goalSeq=" + getGoalSeq() +
                ", goalType='" + goalType + '\'' +
                ", targetValue=" + targetValue +
                ", status='" + status + '\'' +
                '}';
    }
}
