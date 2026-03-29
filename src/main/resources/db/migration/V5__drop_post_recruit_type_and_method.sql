-- posts: 모집 공개/기간 방식 컬럼 제거 (RecruitType, RecruitMethod 삭제)
ALTER TABLE posts DROP COLUMN recruit_type;
ALTER TABLE posts DROP COLUMN recruit_method;

-- 선호 연령: enum 컬렉션 제거 후 min/max 만 나이 구간 저장
DROP TABLE IF EXISTS post_preferred_ages;

ALTER TABLE posts ADD COLUMN min_age INT NULL;
ALTER TABLE posts ADD COLUMN max_age INT NULL;

ALTER TABLE posts ADD COLUMN is_age_any BOOLEAN NOT NULL DEFAULT FALSE;
UPDATE posts SET is_age_any = TRUE WHERE min_age IS NULL AND max_age IS NULL;

-- 단일 썸네일 URL (JSON 배열 photo_urls 제거)
ALTER TABLE posts ADD COLUMN photo_url TEXT NULL;
ALTER TABLE posts DROP COLUMN photo_urls;

-- 모집 정원·현재 인원: 호스트 1명을 포함한 총원 기준으로 통일
-- 이전 의미: recruit_capacity = 최대 '게스트' 수, recruit_count = 승인된 게스트 수(호스트 미포함)
UPDATE posts SET recruit_capacity = recruit_capacity + 1;
UPDATE posts SET recruit_count = recruit_count + 1;
