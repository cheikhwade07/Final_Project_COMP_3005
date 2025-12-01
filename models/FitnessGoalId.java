package models;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

/**
 * Composite primary key for FitnessGoal
 * ------------------------------------
 * PK columns: member_id, goal_seq
 */
@Embeddable
public class FitnessGoalId implements Serializable {

    @Column(name = "member_id")
    private Long memberId;

    @Column(name = "goal_seq")
    private Integer goalSeq;

    public FitnessGoalId() {
        // required by JPA
    }

    public FitnessGoalId(Long memberId, Integer goalSeq) {
        this.memberId = memberId;
        this.goalSeq = goalSeq;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public Integer getGoalSeq() {
        return goalSeq;
    }

    public void setGoalSeq(Integer goalSeq) {
        this.goalSeq = goalSeq;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FitnessGoalId that)) return false;
        return Objects.equals(memberId, that.memberId) &&
                Objects.equals(goalSeq, that.goalSeq);
    }

    @Override
    public int hashCode() {
        return Objects.hash(memberId, goalSeq);
    }
}
