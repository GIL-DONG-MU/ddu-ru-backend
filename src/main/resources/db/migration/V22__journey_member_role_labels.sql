-- ============================================================
-- 멤버 다중 역할 지원
-- 1) journey_member_role_labels: 멤버당 N개 역할(최대 5개)을 저장
-- 2) journey_members의 단일 역할 컬럼(role_type, custom_role_label) 제거
-- ============================================================

CREATE TABLE journey_member_role_labels (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    journey_member_id BIGINT       NOT NULL,
    role_type         VARCHAR(20)  NOT NULL COMMENT 'TREASURER, SCHEDULE, PHOTO, NAVIGATION, RESERVATION, CUSTOM',
    custom_role_label VARCHAR(10)  NULL     COMMENT 'role_type = CUSTOM일 때만 사용',
    created_at        DATETIME(6),
    modified_at       DATETIME(6),

    INDEX idx_role_labels_member (journey_member_id),
    CONSTRAINT fk_role_labels_member
        FOREIGN KEY (journey_member_id) REFERENCES journey_members (id)
);

ALTER TABLE journey_members
    DROP COLUMN IF EXISTS role_type,
    DROP COLUMN IF EXISTS custom_role_label;
