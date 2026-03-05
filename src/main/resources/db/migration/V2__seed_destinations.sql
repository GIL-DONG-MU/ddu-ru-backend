-- ============================================================
-- 여행지 더미 데이터 (여러 국가/도시 기본 데이터)
-- 검색 및 인기 여행지 선택 UI용
-- ============================================================

CREATE TABLE IF NOT EXISTS destinations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    country_code VARCHAR(2) NOT NULL,
    country_name VARCHAR(100) NOT NULL,
    city VARCHAR(100) NOT NULL,
    image VARCHAR(500) NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 🇰🇷 대한민국
INSERT INTO destinations (country_code, country_name, city, image)
SELECT 'KR', '대한민국', '서울', NULL FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM destinations WHERE country_code = 'KR' AND city = '서울');

INSERT INTO destinations (country_code, country_name, city, image)
SELECT 'KR', '대한민국', '부산', NULL FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM destinations WHERE country_code = 'KR' AND city = '부산');

INSERT INTO destinations (country_code, country_name, city, image)
SELECT 'KR', '대한민국', '제주도', NULL FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM destinations WHERE country_code = 'KR' AND city = '제주도');

INSERT INTO destinations (country_code, country_name, city, image)
SELECT 'KR', '대한민국', '제주시', NULL FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM destinations WHERE country_code = 'KR' AND city = '제주시');

INSERT INTO destinations (country_code, country_name, city, image)
SELECT 'KR', '대한민국', '서귀포', NULL FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM destinations WHERE country_code = 'KR' AND city = '서귀포');

INSERT INTO destinations (country_code, country_name, city, image)
SELECT 'KR', '대한민국', '인천', NULL FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM destinations WHERE country_code = 'KR' AND city = '인천');

INSERT INTO destinations (country_code, country_name, city, image)
SELECT 'KR', '대한민국', '강릉', NULL FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM destinations WHERE country_code = 'KR' AND city = '강릉');

INSERT INTO destinations (country_code, country_name, city, image)
SELECT 'KR', '대한민국', '속초', NULL FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM destinations WHERE country_code = 'KR' AND city = '속초');

INSERT INTO destinations (country_code, country_name, city, image)
SELECT 'KR', '대한민국', '여수', NULL FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM destinations WHERE country_code = 'KR' AND city = '여수');

INSERT INTO destinations (country_code, country_name, city, image)
SELECT 'KR', '대한민국', '통영', NULL FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM destinations WHERE country_code = 'KR' AND city = '통영');

INSERT INTO destinations (country_code, country_name, city, image)
SELECT 'KR', '대한민국', '경주', NULL FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM destinations WHERE country_code = 'KR' AND city = '경주');

INSERT INTO destinations (country_code, country_name, city, image)
SELECT 'KR', '대한민국', '전주', NULL FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM destinations WHERE country_code = 'KR' AND city = '전주');

INSERT INTO destinations (country_code, country_name, city, image)
SELECT 'KR', '대한민국', '안동', NULL FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM destinations WHERE country_code = 'KR' AND city = '안동');

INSERT INTO destinations (country_code, country_name, city, image)
SELECT 'KR', '대한민국', '수원', NULL FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM destinations WHERE country_code = 'KR' AND city = '수원');

INSERT INTO destinations (country_code, country_name, city, image)
SELECT 'KR', '대한민국', '춘천', NULL FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM destinations WHERE country_code = 'KR' AND city = '춘천');

INSERT INTO destinations (country_code, country_name, city, image)
SELECT 'KR', '대한민국', '가평', NULL FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM destinations WHERE country_code = 'KR' AND city = '가평');

INSERT INTO destinations (country_code, country_name, city, image)
SELECT 'KR', '대한민국', '포항', NULL FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM destinations WHERE country_code = 'KR' AND city = '포항');

INSERT INTO destinations (country_code, country_name, city, image)
SELECT 'KR', '대한민국', '대구', NULL FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM destinations WHERE country_code = 'KR' AND city = '대구');

INSERT INTO destinations (country_code, country_name, city, image)
SELECT 'KR', '대한민국', '광주', NULL FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM destinations WHERE country_code = 'KR' AND city = '광주');

INSERT INTO destinations (country_code, country_name, city, image)
SELECT 'KR', '대한민국', '울산', NULL FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM destinations WHERE country_code = 'KR' AND city = '울산');

INSERT INTO destinations (country_code, country_name, city, image)
SELECT 'KR', '대한민국', '대전', NULL FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM destinations WHERE country_code = 'KR' AND city = '대전');

INSERT INTO destinations (country_code, country_name, city, image)
SELECT 'KR', '대한민국', '평창', NULL FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM destinations WHERE country_code = 'KR' AND city = '평창');

INSERT INTO destinations (country_code, country_name, city, image)
SELECT 'KR', '대한민국', '남해', NULL FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM destinations WHERE country_code = 'KR' AND city = '남해');

-- 🇯🇵 일본
INSERT INTO destinations (country_code, country_name, city, image)
SELECT 'JP', '일본', '도쿄', NULL FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM destinations WHERE country_code = 'JP' AND city = '도쿄');

INSERT INTO destinations (country_code, country_name, city, image)
SELECT 'JP', '일본', '오사카', NULL FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM destinations WHERE country_code = 'JP' AND city = '오사카');

INSERT INTO destinations (country_code, country_name, city, image)
SELECT 'JP', '일본', '교토', NULL FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM destinations WHERE country_code = 'JP' AND city = '교토');

INSERT INTO destinations (country_code, country_name, city, image)
SELECT 'JP', '일본', '후쿠오카', NULL FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM destinations WHERE country_code = 'JP' AND city = '후쿠오카');

INSERT INTO destinations (country_code, country_name, city, image)
SELECT 'JP', '일본', '삿포로', NULL FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM destinations WHERE country_code = 'JP' AND city = '삿포로');

-- 🇹🇭 태국
INSERT INTO destinations (country_code, country_name, city, image)
SELECT 'TH', '태국', '방콕', NULL FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM destinations WHERE country_code = 'TH' AND city = '방콕');

INSERT INTO destinations (country_code, country_name, city, image)
SELECT 'TH', '태국', '푸켓', NULL FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM destinations WHERE country_code = 'TH' AND city = '푸켓');

INSERT INTO destinations (country_code, country_name, city, image)
SELECT 'TH', '태국', '치앙마이', NULL FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM destinations WHERE country_code = 'TH' AND city = '치앙마이');

-- 🇻🇳 베트남
INSERT INTO destinations (country_code, country_name, city, image)
SELECT 'VN', '베트남', '다낭', NULL FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM destinations WHERE country_code = 'VN' AND city = '다낭');

INSERT INTO destinations (country_code, country_name, city, image)
SELECT 'VN', '베트남', '하노이', NULL FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM destinations WHERE country_code = 'VN' AND city = '하노이');

INSERT INTO destinations (country_code, country_name, city, image)
SELECT 'VN', '베트남', '호치민', NULL FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM destinations WHERE country_code = 'VN' AND city = '호치민');

INSERT INTO destinations (country_code, country_name, city, image)
SELECT 'VN', '베트남', '나트랑', NULL FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM destinations WHERE country_code = 'VN' AND city = '나트랑');

-- 🇸🇬 싱가포르
INSERT INTO destinations (country_code, country_name, city, image)
SELECT 'SG', '싱가포르', '싱가포르', NULL FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM destinations WHERE country_code = 'SG' AND city = '싱가포르');

-- 🇹🇼 대만
INSERT INTO destinations (country_code, country_name, city, image)
SELECT 'TW', '대만', '타이베이', NULL FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM destinations WHERE country_code = 'TW' AND city = '타이베이');

-- 🇮🇩 인도네시아
INSERT INTO destinations (country_code, country_name, city, image)
SELECT 'ID', '인도네시아', '덴파사르', NULL FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM destinations WHERE country_code = 'ID' AND city = '덴파사르');

-- 🇺🇸 미국
INSERT INTO destinations (country_code, country_name, city, image)
SELECT 'US', '미국', '뉴욕', NULL FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM destinations WHERE country_code = 'US' AND city = '뉴욕');

INSERT INTO destinations (country_code, country_name, city, image)
SELECT 'US', '미국', '로스앤젤레스', NULL FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM destinations WHERE country_code = 'US' AND city = '로스앤젤레스');

INSERT INTO destinations (country_code, country_name, city, image)
SELECT 'US', '미국', '라스베이거스', NULL FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM destinations WHERE country_code = 'US' AND city = '라스베이거스');

INSERT INTO destinations (country_code, country_name, city, image)
SELECT 'US', '미국', '샌프란시스코', NULL FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM destinations WHERE country_code = 'US' AND city = '샌프란시스코');

INSERT INTO destinations (country_code, country_name, city, image)
SELECT 'US', '미국', '호놀룰루', NULL FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM destinations WHERE country_code = 'US' AND city = '호놀룰루');

-- 🇫🇷 프랑스
INSERT INTO destinations (country_code, country_name, city, image)
SELECT 'FR', '프랑스', '파리', NULL FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM destinations WHERE country_code = 'FR' AND city = '파리');

-- 🇮🇹 이탈리아
INSERT INTO destinations (country_code, country_name, city, image)
SELECT 'IT', '이탈리아', '로마', NULL FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM destinations WHERE country_code = 'IT' AND city = '로마');

INSERT INTO destinations (country_code, country_name, city, image)
SELECT 'IT', '이탈리아', '피렌체', NULL FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM destinations WHERE country_code = 'IT' AND city = '피렌체');

INSERT INTO destinations (country_code, country_name, city, image)
SELECT 'IT', '이탈리아', '베네치아', NULL FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM destinations WHERE country_code = 'IT' AND city = '베네치아');

-- 🇪🇸 스페인
INSERT INTO destinations (country_code, country_name, city, image)
SELECT 'ES', '스페인', '바르셀로나', NULL FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM destinations WHERE country_code = 'ES' AND city = '바르셀로나');

INSERT INTO destinations (country_code, country_name, city, image)
SELECT 'ES', '스페인', '마드리드', NULL FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM destinations WHERE country_code = 'ES' AND city = '마드리드');

-- 🇬🇧 영국
INSERT INTO destinations (country_code, country_name, city, image)
SELECT 'GB', '영국', '런던', NULL FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM destinations WHERE country_code = 'GB' AND city = '런던');

-- 🇨🇭 스위스
INSERT INTO destinations (country_code, country_name, city, image)
SELECT 'CH', '스위스', '취리히', NULL FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM destinations WHERE country_code = 'CH' AND city = '취리히');

INSERT INTO destinations (country_code, country_name, city, image)
SELECT 'CH', '스위스', '인터라켄', NULL FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM destinations WHERE country_code = 'CH' AND city = '인터라켄');

-- 🇦🇺 호주
INSERT INTO destinations (country_code, country_name, city, image)
SELECT 'AU', '호주', '시드니', NULL FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM destinations WHERE country_code = 'AU' AND city = '시드니');

INSERT INTO destinations (country_code, country_name, city, image)
SELECT 'AU', '호주', '멜버른', NULL FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM destinations WHERE country_code = 'AU' AND city = '멜버른');

-- 🇦🇪 아랍에미리트
INSERT INTO destinations (country_code, country_name, city, image)
SELECT 'AE', '아랍에미리트', '두바이', NULL FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM destinations WHERE country_code = 'AE' AND city = '두바이');
