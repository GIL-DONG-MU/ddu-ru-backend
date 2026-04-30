-- ============================================================
-- 여행 멤버십 분리
-- 1) 실제 협업 멤버를 journey_members 테이블로 관리
-- 2) host / approved member를 한 테이블에서 조회할 수 있게 정리
-- ============================================================

CREATE TABLE IF NOT EXISTS journey_members (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL COMMENT 'HOST, MEMBER',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE, REMOVED, LEFT',
    joined_at DATETIME(6) NOT NULL,
    removed_at DATETIME(6) NULL,
    created_at DATETIME(6) NULL,
    modified_at DATETIME(6) NULL,
    CONSTRAINT uk_journey_members_post_user UNIQUE (post_id, user_id),
    CONSTRAINT fk_journey_members_post
        FOREIGN KEY (post_id) REFERENCES posts (id) ON DELETE CASCADE,
    CONSTRAINT fk_journey_members_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_journey_members_user_status
    ON journey_members (user_id, status);

CREATE INDEX idx_journey_members_post_status
    ON journey_members (post_id, status);

INSERT INTO journey_members (post_id, user_id, role, status, joined_at, created_at, modified_at)
SELECT p.id,
       p.user_id,
       'HOST',
       'ACTIVE',
       COALESCE(p.created_at, NOW(6)),
       p.created_at,
       p.modified_at
FROM posts p
WHERE NOT EXISTS (
    SELECT 1
    FROM journey_members jm
    WHERE jm.post_id = p.id
      AND jm.user_id = p.user_id
);

INSERT INTO journey_members (post_id, user_id, role, status, joined_at, created_at, modified_at)
SELECT pa.post_id,
       pa.user_id,
       'MEMBER',
       'ACTIVE',
       COALESCE(pa.approved_at, pa.created_at, NOW(6)),
       pa.created_at,
       pa.modified_at
FROM participations pa
WHERE pa.status = 'APPROVED'
  AND NOT EXISTS (
      SELECT 1
      FROM journey_members jm
      WHERE jm.post_id = pa.post_id
        AND jm.user_id = pa.user_id
  );
