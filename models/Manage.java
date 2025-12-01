package models;

import jakarta.persistence.*;

/**
 * Manage entity (Admin <-> Room)
 * ------------------------------
 * Maps to table: manage
 * PK: (admin_id, room_id) via ManageId (embedded id)
 * FKs:
 *   admin_id -> admin.admin_id
 *   room_id  -> room.room_id
 *
 * Business rule:
 *   Each room can be managed by at most one admin at a time.
 *   Enforced by UNIQUE(room_id).
 */
@Entity
@Table(
        name = "manage",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"room_id"})  // <= ONE admin per room
        }
)
public class Manage {

    @EmbeddedId
    private ManageId id = new ManageId();   // always non-null

    @ManyToOne(optional = false)
    @MapsId("adminId")
    @JoinColumn(name = "admin_id", nullable = false)
    private Admin admin;

    @ManyToOne(optional = false)
    @MapsId("roomId")
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    public Manage() {
        // default constructor for JPA
    }

    public Manage(Admin admin, Room room) {
        this.id = new ManageId();   // ensure not null
        setAdmin(admin);
        setRoom(room);
    }

    public ManageId getId() {
        return id;
    }

    public void setId(ManageId id) {
        this.id = (id != null) ? id : new ManageId();
    }

    public Admin getAdmin() {
        return admin;
    }

    public void setAdmin(Admin admin) {
        this.admin = admin;
        if (admin != null) {
            if (this.id == null) {
                this.id = new ManageId();
            }
            // adminId may be null for transient admins; Hibernate will
            // still override it via @MapsId when the admin is persisted.
            this.id.setAdminId(admin.getAdminId());
        }
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
        if (room != null) {
            if (this.id == null) {
                this.id = new ManageId();
            }
            this.id.setRoomId(room.getRoomId());
        }
    }
}
