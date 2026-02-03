-- ============================================================
-- Profile 관련 스키마 마이그레이션 (운영 DB 적용용)
-- - bg_colors 테이블 생성 및 시딩
-- - profiles: 기존 profile_image, self_introduction 컬럼 삭제
--   (ddl-auto=update로 이미 새 컬럼들이 추가되었으므로 기존 컬럼만 삭제)
-- ============================================================

-- 1. 배경색 테이블 생성
CREATE TABLE IF NOT EXISTS bg_colors (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    hex_code VARCHAR(7) NOT NULL,
    display_order INT NOT NULL,
    created_at DATETIME(6) NULL,
    modified_at DATETIME(6) NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. 배경색 초기 데이터 (24개)
INSERT IGNORE INTO bg_colors (hex_code, display_order, created_at, modified_at) VALUES
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

-- 3. profiles: 기존 profile_image 컬럼 삭제 (uploaded_image_url이 이미 있으므로)
ALTER TABLE profiles DROP COLUMN profile_image;

-- 4. profiles: 기존 self_introduction 컬럼 삭제 (bio가 이미 있으므로)
ALTER TABLE profiles DROP COLUMN self_introduction;


