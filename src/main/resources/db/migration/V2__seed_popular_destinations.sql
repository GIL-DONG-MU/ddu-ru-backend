-- ============================================================
-- 인기 여행지 더미 데이터 (인기 여행지 선택 UI용)
-- 제주도, 부산, 강릉, 후쿠오카, 오사카
-- ============================================================

CREATE TABLE IF NOT EXISTS destinations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    country_code VARCHAR(2) NOT NULL,
    country_name VARCHAR(100) NOT NULL,
    region VARCHAR(100) NULL,
    city VARCHAR(100) NOT NULL,
    image VARCHAR(500) NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO destinations (country_code, country_name, region, city, image)
SELECT 'KR', '대한민국', '제주', '제주도', NULL FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM destinations WHERE country_code = 'KR' AND city = '제주도');

INSERT INTO destinations (country_code, country_name, region, city, image)
SELECT 'KR', '대한민국', '부산', '부산', NULL FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM destinations WHERE country_code = 'KR' AND city = '부산');

INSERT INTO destinations (country_code, country_name, region, city, image)
SELECT 'KR', '대한민국', '강원', '강릉', NULL FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM destinations WHERE country_code = 'KR' AND city = '강릉');

INSERT INTO destinations (country_code, country_name, region, city, image)
SELECT 'JP', '일본', '후쿠오카', '후쿠오카', NULL FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM destinations WHERE country_code = 'JP' AND city = '후쿠오카');

INSERT INTO destinations (country_code, country_name, region, city, image)
SELECT 'JP', '일본', '오사카', '오사카', NULL FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM destinations WHERE country_code = 'JP' AND city = '오사카');
