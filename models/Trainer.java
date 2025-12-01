package models;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Trainer entity
 * -----------------------------
 * Maps to table: trainer
 * PK: trainer_id (auto-generated)
 * Columns: full_name, password_hash, hire_date, email, status
 */
@Entity
@Table(name = "trainer")
public class Trainer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trainer_id")
    private Long trainerId;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "hire_date")
    private LocalDate hireDate;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "status", nullable = false)
    private String status;   // e.g. 'ACTIVE', 'INACTIVE'

    // ---- Constructors ----

    public Trainer() {
        // required by JPA
    }

    public Trainer(String fullName,
                   String passwordHash,
                   LocalDate hireDate,
                   String email,
                   String status) {
        this.fullName = fullName;
        this.passwordHash = passwordHash;
        this.hireDate = hireDate;
        this.email = email;
        this.status = status;
    }

    // ---- Getters & Setters ----

    public Long getTrainerId() {
        return trainerId;
    }

    public void setTrainerId(Long trainerId) {
        this.trainerId = trainerId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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
        return "Trainer{" +
                "trainerId=" + trainerId +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
