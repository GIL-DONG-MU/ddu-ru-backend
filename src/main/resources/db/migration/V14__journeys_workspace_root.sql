-- ============================================================
-- 나의 여정 루트 분리
-- 1) posts(공개 모집글)와 journeys(협업 워크스페이스)를 분리
-- 2) journey_members 가 post 가 아니라 journey 를 기준으로 멤버십을 관리
-- ============================================================

CREATE TABLE IF NOT EXISTS journeys (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    photo_url TEXT NULL,
    created_at DATETIME(6) NULL,
    modified_at DATETIME(6) NULL,
    CONSTRAINT uk_journeys_post UNIQUE (post_id),
    CONSTRAINT fk_journeys_post
        FOREIGN KEY (post_id) REFERENCES posts (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO journeys (post_id, title, photo_url, created_at, modified_at)
SELECT p.id,
       p.title,
       p.photo_url,
       p.created_at,
       p.modified_at
FROM posts p
WHERE NOT EXISTS (
    SELECT 1
    FROM journeys j
    WHERE j.post_id = p.id
);

ALTER TABLE journey_members
    ADD COLUMN journey_id BIGINT NULL;

UPDATE journey_members jm
SET jm.journey_id = (
    SELECT j.id
    FROM journeys j
    WHERE j.post_id = jm.post_id
)
WHERE jm.journey_id IS NULL;

ALTER TABLE journey_members
    MODIFY COLUMN journey_id BIGINT NOT NULL;

ALTER TABLE journey_members
    DROP FOREIGN KEY fk_journey_members_post;

ALTER TABLE journey_members
    DROP INDEX uk_journey_members_post_user;

ALTER TABLE journey_members
    DROP INDEX idx_journey_members_post_status;

ALTER TABLE journey_members
    ADD CONSTRAINT uk_journey_members_journey_user UNIQUE (journey_id, user_id);

ALTER TABLE journey_members
    ADD CONSTRAINT fk_journey_members_journey
        FOREIGN KEY (journey_id) REFERENCES journeys (id) ON DELETE CASCADE;

CREATE INDEX idx_journey_members_journey_status
    ON journey_members (journey_id, status);

ALTER TABLE journey_members
    DROP COLUMN post_id;
