-- ChatRoom은 생성 즉시 사용 가능한 방으로 취급한다.
-- 기존 PENDING 방은 ACTIVE로 승격하고, 신규 기본값에서도 PENDING을 제거한다.
UPDATE chat_rooms
SET status = 'ACTIVE'
WHERE status = 'PENDING';

ALTER TABLE chat_rooms
    MODIFY COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
        COMMENT 'ACTIVE, CLOSED, DELETED';
