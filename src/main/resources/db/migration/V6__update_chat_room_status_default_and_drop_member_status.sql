-- 그룹 채팅방 기본 상태를 현재 코드와 맞춰 PENDING 으로 변경한다.
ALTER TABLE chat_rooms
    MODIFY COLUMN status VARCHAR(20) NOT NULL DEFAULT 'PENDING'
        COMMENT 'PENDING, ACTIVE, CLOSED, DELETED';

-- 참여자는 row 존재 = 현재 멤버. 나감 시 row 삭제로 통일한다.
ALTER TABLE chat_room_members DROP COLUMN member_status;
