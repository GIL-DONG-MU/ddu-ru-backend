-- 참여자는 row 존재 = 현재 멤버. 나감 시 row 삭제로 통일.
ALTER TABLE chat_room_members DROP COLUMN member_status;
