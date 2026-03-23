-- ============================================================
-- 채팅 도메인 테이블 (chat_rooms, chat_messages, chat_room_members)
-- 엔티티: ChatRoom, ChatMessage, ChatRoomMember
-- 운영 DB 적용 시 posts, users 테이블이 존재해야 합니다.
-- ============================================================

-- 1. 채팅방
CREATE TABLE IF NOT EXISTS chat_rooms (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    room_type VARCHAR(20) NOT NULL COMMENT 'PRIVATE, GROUP',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE, CLOSED',
    max_capacity INT NOT NULL,
    created_at DATETIME(6) NULL,
    modified_at DATETIME(6) NULL,
    CONSTRAINT fk_chat_rooms_post
        FOREIGN KEY (post_id) REFERENCES posts (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_chat_rooms_post_id ON chat_rooms (post_id);

-- 2. 채팅 메시지 (chat_room_members.last_read_message_id 보다 먼저 생성)
CREATE TABLE IF NOT EXISTS chat_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_id BIGINT NOT NULL,
    sender_id BIGINT NULL COMMENT '시스템 메시지 등 NULL 가능',
    message_type VARCHAR(20) NOT NULL COMMENT 'TEXT, IMAGE, SYSTEM',
    content TEXT NOT NULL,
    created_at DATETIME(6) NULL,
    modified_at DATETIME(6) NULL,
    CONSTRAINT fk_chat_messages_room
        FOREIGN KEY (room_id) REFERENCES chat_rooms (id) ON DELETE CASCADE,
    CONSTRAINT fk_chat_messages_sender
        FOREIGN KEY (sender_id) REFERENCES users (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_chat_messages_room_id ON chat_messages (room_id);
CREATE INDEX idx_chat_messages_room_created ON chat_messages (room_id, created_at);

-- 3. 채팅방 멤버
CREATE TABLE IF NOT EXISTS chat_room_members (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL COMMENT 'HOST, GUEST',
    member_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE, INVITED, LEFT, KICKED',
    last_read_message_id BIGINT NULL,
    created_at DATETIME(6) NULL,
    modified_at DATETIME(6) NULL,
    CONSTRAINT uk_chat_room_members_room_user UNIQUE (room_id, user_id),
    CONSTRAINT fk_chat_room_members_room
        FOREIGN KEY (room_id) REFERENCES chat_rooms (id) ON DELETE CASCADE,
    CONSTRAINT fk_chat_room_members_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_chat_room_members_last_read
        FOREIGN KEY (last_read_message_id) REFERENCES chat_messages (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_chat_room_members_room_id ON chat_room_members (room_id);
CREATE INDEX idx_chat_room_members_user_id ON chat_room_members (user_id);
