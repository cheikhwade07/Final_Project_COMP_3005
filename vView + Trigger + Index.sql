-- ===== VIEW: trainer_schedule_view =====
-- Shows non-cancelled PT sessions with trainer, member, and room info.

CREATE OR REPLACE VIEW trainer_schedule_view AS
SELECT
    s.session_id,
    s.trainer_id,
    t.full_name AS trainer_name,
    s.member_id,
    m.full_name AS member_name,
    s.room_id,
    r.room_type,
    s.start_time,
    s.end_time,
    s.status
FROM pt_session AS s
JOIN trainer AS t
    ON s.trainer_id = t.trainer_id
JOIN member AS m
    ON s.member_id = m.member_id
LEFT JOIN room AS r
    ON s.room_id = r.room_id
WHERE s.status <> 'CANCELLED';
-- ===== TRIGGER FUNCTION: update_room_status_from_equipment =====

CREATE OR REPLACE FUNCTION update_room_status_from_equipment()
RETURNS trigger AS
$$
DECLARE
    broken_count integer;
BEGIN
    -- Only act when the status actually changes (or on insert)
    IF TG_OP = 'UPDATE' AND NEW.status = OLD.status THEN
        RETURN NEW;
    END IF;

    -- Count equipment in the same room that are not OK
    SELECT COUNT(*)
    INTO broken_count
    FROM equipment
    WHERE room_id = NEW.room_id
      AND status <> 'OK';

    IF broken_count > 0 THEN
        -- At least one problematic equipment -> room in MAINTENANCE
        UPDATE room
        SET status = 'MAINTENANCE'
        WHERE room_id = NEW.room_id;
    ELSE
        -- All equipment OK -> room available
        UPDATE room
        SET status = 'AVAILABLE'
        WHERE room_id = NEW.room_id;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
-- ===== TRIGGER: trg_equipment_room_status =====
-- Fires after INSERT or status UPDATE on equipment.

DROP TRIGGER IF EXISTS trg_equipment_room_status ON equipment;

CREATE TRIGGER trg_equipment_room_status
AFTER INSERT OR UPDATE OF status
ON equipment
FOR EACH ROW
EXECUTE FUNCTION update_room_status_from_equipment();
-- ===== INDEX: idx_pt_session_trainer_start_time =====
-- Helps queries that filter by trainer_id and order/filter by start_time.

CREATE INDEX IF NOT EXISTS idx_pt_session_trainer_start_time
ON pt_session (trainer_id, start_time);

