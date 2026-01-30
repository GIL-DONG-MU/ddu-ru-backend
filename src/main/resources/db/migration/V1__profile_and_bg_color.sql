-- ============================================================
-- Profile 관련 스키마 마이그레이션 (운영 DB 적용용)
-- - bg_colors 테이블 생성 및 시딩
-- - profiles: bg_color_id 추가 + FK, avatar_id FK, 컬럼명 변경
--
-- 사용법: 운영 DB 접속 후 전체 실행.
-- 이미 컬럼/테이블이 있으면 해당 문만 에러 → 그 문 건너뛰고 다음 실행.
-- ============================================================

-- 1. 배경색 테이블 생성
CREATE TABLE IF NOT EXISTS bg_colors (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    hex_code VARCHAR(7) NOT NULL,
    display_order INT NOT NULL,
    created_at DATETIME(6) NULL,
    modified_at DATETIME(6) NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. 배경색 초기 데이터 (24개) — FK 추가 전에 데이터 필요
INSERT INTO bg_colors (hex_code, display_order, created_at, modified_at) VALUES
('#FFB3BA', 1, NOW(), NOW()),
('#FFDFBA', 2, NOW(), NOW()),
('#E1FFBD', 3, NOW(), NOW()),
('#BAE1FF', 4, NOW(), NOW()),
('#97C8FF', 5, NOW(), NOW()),
('#C9B3FF', 6, NOW(), NOW()),
('#FFB3E6', 7, NOW(), NOW()),
('#A6A6A6', 8, NOW(), NOW()),
('#FF8B95', 9, NOW(), NOW()),
('#FFBE73', 10, NOW(), NOW()),
('#D0FF98', 11, NOW(), NOW()),
('#76C4FF', 12, NOW(), NOW()),
('#5EA9FF', 13, NOW(), NOW()),
('#AD8CFF', 14, NOW(), NOW()),
('#E19BFF', 15, NOW(), NOW()),
('#888888', 16, NOW(), NOW()),
('#FF7A7A', 17, NOW(), NOW()),
('#FFC559', 18, NOW(), NOW()),
('#BFFF70', 19, NOW(), NOW()),
('#5CB8FF', 20, NOW(), NOW()),
('#469DFF', 21, NOW(), NOW()),
('#9E77FF', 22, NOW(), NOW()),
('#DB86FF', 23, NOW(), NOW()),
('#646464', 24, NOW(), NOW());

-- 3. profiles: bg_color_id 컬럼 추가 (bg_colors.id와 타입 맞춤)
ALTER TABLE profiles ADD COLUMN bg_color_id BIGINT NULL;

-- 4. profiles: bg_color_id FK 추가 (bg_colors.id 참조)
ALTER TABLE profiles ADD CONSTRAINT fk_profiles_bg_color_id
    FOREIGN KEY (bg_color_id) REFERENCES bg_colors(id) ON DELETE SET NULL;

-- 5. profiles: avatar_id 컬럼 추가 (이미 있으면 에러 → 해당 라인만 제외하고 실행)
ALTER TABLE profiles ADD COLUMN avatar_id BIGINT NULL;

-- 6. profiles: avatar_id FK 추가 (avatar_profiles.id 참조)
ALTER TABLE profiles ADD CONSTRAINT fk_profiles_avatar_id
    FOREIGN KEY (avatar_id) REFERENCES avatar_profiles(id) ON DELETE SET NULL;

-- 7. profiles: profile_image → uploadedImageUrl 컬럼명 변경
ALTER TABLE profiles CHANGE COLUMN profile_image uploadedImageUrl VARCHAR(500) NULL;

-- 8. profiles: self_introduction → bio 컬럼명 변경
ALTER TABLE profiles CHANGE COLUMN self_introduction bio VARCHAR(60) NULL;
