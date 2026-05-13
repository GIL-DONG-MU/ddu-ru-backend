CREATE TABLE journey_posts
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    journey_id     BIGINT       NOT NULL,
    author_user_id BIGINT       NOT NULL,
    content        VARCHAR(300) NOT NULL,
    image_url      TEXT         NULL,
    is_notice      BOOLEAN      NOT NULL DEFAULT FALSE,
    is_deleted     BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_at     DATETIME(6)  NULL,
    deleted_by     BIGINT       NULL,
    created_at     DATETIME(6)  NULL,
    modified_at    DATETIME(6)  NULL,
    CONSTRAINT fk_journey_posts_journey FOREIGN KEY (journey_id) REFERENCES journeys (id) ON DELETE CASCADE,
    CONSTRAINT fk_journey_posts_author FOREIGN KEY (author_user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_journey_posts_journey_deleted_notice_created
    ON journey_posts (journey_id, is_deleted, is_notice, created_at, id);
