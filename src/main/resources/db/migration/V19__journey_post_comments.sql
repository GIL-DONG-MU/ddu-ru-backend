CREATE TABLE journey_post_comments
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    journey_post_id BIGINT       NOT NULL,
    author_user_id  BIGINT       NOT NULL,
    content         VARCHAR(300) NOT NULL,
    is_deleted      BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_at      DATETIME(6)  NULL,
    deleted_by      BIGINT       NULL,
    created_at      DATETIME(6)  NULL,
    modified_at     DATETIME(6)  NULL,
    CONSTRAINT fk_journey_post_comments_post FOREIGN KEY (journey_post_id) REFERENCES journey_posts (id) ON DELETE CASCADE,
    CONSTRAINT fk_journey_post_comments_author FOREIGN KEY (author_user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_journey_post_comments_post_deleted_created
    ON journey_post_comments (journey_post_id, is_deleted, created_at, id);

CREATE INDEX idx_journey_post_comments_author
    ON journey_post_comments (author_user_id);
