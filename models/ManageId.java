package models;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ManageId implements Serializable {

    @Column(name = "admin_id")
    private Long adminId;

    @Column(name = "room_id")
    private Long roomId;

    public ManageId() {
    }

    public ManageId(Long adminId, Long roomId) {
        this.adminId = adminId;
        this.roomId = roomId;
    }

    public Long getAdminId() {
        return adminId;
    }

    public void setAdminId(Long adminId) {
        this.adminId = adminId;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ManageId that)) return false;
        return Objects.equals(adminId, that.adminId)
                && Objects.equals(roomId, that.roomId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(adminId, roomId);
    }
}

