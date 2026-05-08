-- ============================================================
-- 채팅방 컨텍스트 분리
-- 1) 1:1 채팅방은 공개 모집글(post)을 기준으로 유지
-- 2) 그룹 채팅방은 참여 후 워크스페이스(journey)를 기준으로 이전
-- ============================================================

ALTER TABLE chat_rooms
    ADD COLUMN journey_id BIGINT NULL;

UPDATE chat_rooms cr
JOIN journeys j ON j.post_id = cr.post_id
SET cr.journey_id = j.id
WHERE cr.room_type = 'GROUP'
  AND cr.journey_id IS NULL;

ALTER TABLE chat_rooms
    MODIFY COLUMN post_id BIGINT NULL;

UPDATE chat_rooms
SET post_id = NULL
WHERE room_type = 'GROUP';

ALTER TABLE chat_rooms
    ADD CONSTRAINT fk_chat_rooms_journey
        FOREIGN KEY (journey_id) REFERENCES journeys (id) ON DELETE CASCADE;

CREATE UNIQUE INDEX uk_chat_rooms_journey_id
    ON chat_rooms (journey_id);

ALTER TABLE chat_rooms
    ADD CONSTRAINT chk_chat_rooms_context CHECK (
        (room_type = 'PRIVATE' AND post_id IS NOT NULL AND journey_id IS NULL)
        OR
        (room_type = 'GROUP' AND post_id IS NULL AND journey_id IS NOT NULL)
    );
