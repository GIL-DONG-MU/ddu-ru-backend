-- ============================================================
-- 참여신청 흐름 정비
-- 1) participations 테이블을 Flyway 기준으로 생성/보정
-- 2) CONTACTING 상태와 contacted_at 컬럼 추가
-- 3) posts.recruit_count 의미를 '호스트 포함 현재 인원 수'로 정렬
-- 4) FULL 상태를 제거하고 OPEN/CLOSED만 사용
-- ============================================================

CREATE TABLE IF NOT EXISTS participations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING, CONTACTING, APPROVED, REJECTED',
    message VARCHAR(500) NULL,
    contacted_at DATETIME(6) NULL,
    approved_at DATETIME(6) NULL,
    rejected_at DATETIME(6) NULL,
    created_at DATETIME(6) NULL,
    modified_at DATETIME(6) NULL,
    CONSTRAINT uk_participations_post_user UNIQUE (post_id, user_id),
    CONSTRAINT fk_participations_post
        FOREIGN KEY (post_id) REFERENCES posts (id) ON DELETE CASCADE,
    CONSTRAINT fk_participations_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

# ALTER TABLE participations
#     MODIFY COLUMN status VARCHAR(20) NOT NULL DEFAULT 'PENDING'
#         COMMENT 'PENDING, CONTACTING, APPROVED, REJECTED';

# ALTER TABLE participations
#     ADD COLUMN contacted_at DATETIME(6) NULL AFTER message;

UPDATE posts
SET status = 'OPEN'
WHERE status = 'FULL';

ALTER TABLE posts
    MODIFY COLUMN status ENUM('OPEN', 'CLOSED') NOT NULL DEFAULT 'OPEN';

# UPDATE posts p
# SET recruit_count = (
#     SELECT COUNT(*) + 1
#     FROM participations pa
#     WHERE pa.post_id = p.id
#       AND pa.status = 'APPROVED'
# );
