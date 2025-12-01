package models;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * HealthMetric entity
 * -----------------------------
 * Maps to table: health_metric
 * PK: metric_id (auto-generated)
 * FK: member_id (-> member.member_id)
 * Columns: recorded_date, height, weight, heart_rate, body_fat_pct
 */
@Entity
@Table(name = "health_metric")
public class HealthMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "metric_id")
    private Long metricId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "recorded_date", nullable = false)
    private LocalDate recordedDate;

    @Column(name = "height")
    private Double height;

    @Column(name = "weight")
    private Double weight;

    @Column(name = "heart_rate")
    private Integer heartRate;

    @Column(name = "body_fat_pct")
    private Double bodyFatPct;

    // ---- Constructors ----

    public HealthMetric() {
        // required by JPA
    }

    public HealthMetric(Member member,
                        LocalDate recordedDate,
                        Double height,
                        Double weight,
                        Integer heartRate,
                        Double bodyFatPct) {
        this.member = member;
        this.recordedDate = recordedDate;
        this.height = height;
        this.weight = weight;
        this.heartRate = heartRate;
        this.bodyFatPct = bodyFatPct;
    }

    // ---- Getters & Setters ----

    public Long getMetricId() {
        return metricId;
    }

    public void setMetricId(Long metricId) {
        this.metricId = metricId;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public LocalDate getRecordedDate() {
        return recordedDate;
    }

    public void setRecordedDate(LocalDate recordedDate) {
        this.recordedDate = recordedDate;
    }

    public Double getHeight() {
        return height;
    }

    public void setHeight(Double height) {
        this.height = height;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public Integer getHeartRate() {
        return heartRate;
    }

    public void setHeartRate(Integer heartRate) {
        this.heartRate = heartRate;
    }

    public Double getBodyFatPct() {
        return bodyFatPct;
    }

    public void setBodyFatPct(Double bodyFatPct) {
        this.bodyFatPct = bodyFatPct;
    }

    // ---- Utility ----

    @Override
    public String toString() {
        return "HealthMetric{" +
                "metricId=" + metricId +
                ", memberId=" + (member != null ? member.getMemberId() : null) +
                ", recordedDate=" + recordedDate +
                ", height=" + height +
                ", weight=" + weight +
                ", heartRate=" + heartRate +
                ", bodyFatPct=" + bodyFatPct +
                '}';
    }
}
