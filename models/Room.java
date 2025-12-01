package models;

import jakarta.persistence.*;

/**
 * Room entity
 * -----------------------------
 * Maps to table: room
 * PK: room_id (auto-generated)
 * Columns: room_type, capacity, status
 */
@Entity
@Table(name = "room")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id")
    private Long roomId;

    @Column(name = "room_type", nullable = false)
    private String roomType;    // e.g. 'CARDIO', 'WEIGHTS', 'STUDIO'

    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @Column(name = "status", nullable = false)
    private String status;      // e.g. 'AVAILABLE', 'MAINTENANCE', 'CLOSED'

    // ---- Constructors ----

    public Room() {
        // required by JPA
    }

    public Room(String roomType, Integer capacity, String status) {
        this.roomType = roomType;
        this.capacity = capacity;
        this.status = status;
    }

    // ---- Getters & Setters ----

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
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
        return "Room{" +
                "roomId=" + roomId +
                ", roomType='" + roomType + '\'' +
                ", capacity=" + capacity +
                ", status='" + status + '\'' +
                '}';
    }
}
