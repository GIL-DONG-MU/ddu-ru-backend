-- ============================================================
-- 게시글 다중 이미지 지원 (최대 4장)
-- 1) journey_post_images: 게시글당 N개 이미지를 순서(sort_order)와 함께 저장
-- 2) journey_posts의 단일 image_url 컬럼 제거
-- ============================================================

CREATE TABLE journey_post_images (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    journey_post_id BIGINT       NOT NULL,
    image_url       TEXT         NOT NULL,
    sort_order      INT          NOT NULL DEFAULT 0,
    created_at      DATETIME(6),
    modified_at     DATETIME(6),

    INDEX idx_journey_post_images_post (journey_post_id),
    CONSTRAINT fk_journey_post_images_post
        FOREIGN KEY (journey_post_id) REFERENCES journey_posts (id) ON DELETE CASCADE
);

ALTER TABLE journey_posts
    DROP COLUMN IF EXISTS image_url;
