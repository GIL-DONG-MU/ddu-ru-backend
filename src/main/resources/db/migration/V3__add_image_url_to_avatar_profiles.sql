-- ============================================================
-- avatar_profiles 테이블에 아바타 이미지 URL 컬럼 추가
-- ============================================================

ALTER TABLE avatar_profiles
    ADD COLUMN image_url VARCHAR(500) NULL;
