package models;

import jakarta.persistence.*;

/**
 * Admin entity
 * -----------------------------
 * Maps to table: admin
 * PK: admin_id (auto-generated)
 * Columns: full_name, password_hash, email, status
 */
@Entity
@Table(name = "admin")
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "admin_id")
    private Long adminId;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "status", nullable = false)
    private String status;   // e.g. 'ACTIVE', 'INACTIVE'

    // ---- Constructors ----

    public Admin() {
        // required by JPA
    }

    public Admin(String fullName,
                 String passwordHash,
                 String email,
                 String status) {
        this.fullName = fullName;
        this.passwordHash = passwordHash;
        this.email = email;
        this.status = status;
    }

    // ---- Getters & Setters ----

    public Long getAdminId() {
        return adminId;
    }

    public void setAdminId(Long adminId) {
        this.adminId = adminId;
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
        return "Admin{" +
                "adminId=" + adminId +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
