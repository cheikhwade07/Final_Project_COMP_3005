package models;

import jakarta.persistence.*;

/**
 * Equipment entity
 * -----------------------------
 * Maps to table: equipment
 * PK: equipment_id (auto-generated)
 * Columns: name, category, status, room_id (FK -> room)
 */
@Entity
@Table(name = "equipment")
public class Equipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "equipment_id")
    private Long equipmentId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "name", nullable = false)
    private String name;        // e.g. 'Treadmill #3'

    @Column(name = "category", nullable = false)
    private String category;    // e.g. 'CARDIO', 'STRENGTH'

    @Column(name = "status", nullable = false)
    private String status;      // e.g. 'OK', 'BROKEN', 'UNDER_REPAIR'

    // ---- Constructors ----

    public Equipment() {
        // required by JPA
    }

    public Equipment(Room room,
                     String name,
                     String category,
                     String status) {
        this.room = room;
        this.name = name;
        this.category = category;
        this.status = status;
    }

    // ---- Getters & Setters ----

    public Long getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(Long equipmentId) {
        this.equipmentId = equipmentId;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
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
        return "Equipment{" +
                "equipmentId=" + equipmentId +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", status='" + status + '\'' +
                ", roomId=" + (room != null ? room.getRoomId() : null) +
                '}';
    }
}
