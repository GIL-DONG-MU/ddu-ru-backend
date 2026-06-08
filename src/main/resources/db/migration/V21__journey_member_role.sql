-- ============================================================
-- 나의 여정 멤버 역할 라벨 추가
-- 1) role_type: 기본 역할(TREASURER, SCHEDULE 등) 또는 CUSTOM
-- 2) custom_role_label: role_type이 CUSTOM일 때만 사용
-- ============================================================

ALTER TABLE journey_members
    ADD COLUMN role_type         VARCHAR(20) NULL COMMENT 'TREASURER, SCHEDULE, PHOTO, NAVIGATION, RESERVATION, CUSTOM',
    ADD COLUMN custom_role_label VARCHAR(20) NULL COMMENT 'role_type = CUSTOM일 때만 사용';
