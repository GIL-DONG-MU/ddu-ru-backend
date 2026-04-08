-- 설문 v2: 주요 4축(각 3문항) + 기록 스타일 + 활동 태그
-- 기존 `surveys` 행이 있으면 NOT NULL 컬럼 추가가 실패할 수 있어, 배포 전 데이터 정리(또는 TRUNCATE)가 필요할 수 있습니다.

DROP TABLE IF EXISTS survey_interests;

ALTER TABLE surveys
    DROP COLUMN q1_transport,
    DROP COLUMN q2_waiting,
    DROP COLUMN q3_stay,
    DROP COLUMN q4_wakeup,
    DROP COLUMN q5_expense,
    DROP COLUMN q6_spend,
    DROP COLUMN q8_planning,
    DROP COLUMN q9_menu,
    DROP COLUMN q10_companion,
    DROP COLUMN q11_photo;

ALTER TABLE surveys
    ADD COLUMN rhythm_q1 VARCHAR(64) NOT NULL,
    ADD COLUMN rhythm_q2 VARCHAR(64) NOT NULL,
    ADD COLUMN rhythm_q3 VARCHAR(64) NOT NULL,
    ADD COLUMN consumption_q1 VARCHAR(64) NOT NULL,
    ADD COLUMN consumption_q2 VARCHAR(64) NOT NULL,
    ADD COLUMN consumption_q3 VARCHAR(64) NOT NULL,
    ADD COLUMN energy_q1 VARCHAR(64) NOT NULL,
    ADD COLUMN energy_q2 VARCHAR(64) NOT NULL,
    ADD COLUMN energy_q3 VARCHAR(64) NOT NULL,
    ADD COLUMN decision_q1 VARCHAR(64) NOT NULL,
    ADD COLUMN decision_q2 VARCHAR(64) NOT NULL,
    ADD COLUMN decision_q3 VARCHAR(64) NOT NULL,
    ADD COLUMN record_style VARCHAR(64) NOT NULL;

CREATE TABLE survey_activity_tags (
    survey_id BIGINT NOT NULL,
    tag VARCHAR(64) NOT NULL,
    CONSTRAINT fk_survey_activity_tags_survey FOREIGN KEY (survey_id) REFERENCES surveys (id) ON DELETE CASCADE
);

-- travel_tendencies: 성향 점수 컬럼명을 도메인 용어와 일치
ALTER TABLE travel_tendencies
    CHANGE COLUMN r rhythm_score DECIMAL(3, 1) NOT NULL,
    CHANGE COLUMN p energy_score DECIMAL(3, 1) NOT NULL,
    CHANGE COLUMN w consumption_score DECIMAL(3, 1) NOT NULL,
    CHANGE COLUMN s decision_score DECIMAL(3, 1) NOT NULL;

-- avatar_type이 ENUM이면 TTUR_DASOM 등 신코드로 UPDATE할 수 없으므로, 성향 리네임 UPDATE보다 먼저 VARCHAR로 전환
ALTER TABLE travel_tendencies
    MODIFY COLUMN avatar_type VARCHAR(64) NOT NULL;

-- AvatarType 16종: display_name·body 정리 + 기존 personality/strength/tip 제거

ALTER TABLE avatar_profiles
    MODIFY COLUMN avatar_type VARCHAR(64) NOT NULL;

ALTER TABLE avatar_profiles
    ADD COLUMN display_name VARCHAR(64) NULL AFTER avatar_type,
    ADD COLUMN body TEXT NULL AFTER description;

UPDATE avatar_profiles SET display_name = avatar_type WHERE display_name IS NULL;

UPDATE avatar_profiles
SET body = CONCAT_WS('\n\n', personality, strength, tip)
WHERE body IS NULL;

ALTER TABLE avatar_profiles
    DROP COLUMN personality,
    DROP COLUMN strength,
    DROP COLUMN tip;

ALTER TABLE avatar_profiles
    MODIFY COLUMN display_name VARCHAR(64) NOT NULL,
    MODIFY COLUMN body TEXT NOT NULL;

-- 레거시 avatar_type 문자열 → 캐릭터명 정렬 코드 (TTUR_SWEET 재사용 전에 다솜 행을 TTUR_DASOM으로 이동)
UPDATE avatar_profiles SET avatar_type = 'TTUR_DASOM' WHERE avatar_type = 'TTUR_SWEET';
UPDATE travel_tendencies SET avatar_type = 'TTUR_DASOM' WHERE avatar_type = 'TTUR_SWEET';
UPDATE avatar_profiles SET avatar_type = 'TTUR_POGUN' WHERE avatar_type = 'TTUR_POGUNI';
UPDATE travel_tendencies SET avatar_type = 'TTUR_POGUN' WHERE avatar_type = 'TTUR_POGUNI';
UPDATE avatar_profiles SET avatar_type = 'TTUR_POSEUL' WHERE avatar_type = 'TTUR_MOOD';
UPDATE travel_tendencies SET avatar_type = 'TTUR_POSEUL' WHERE avatar_type = 'TTUR_MOOD';
UPDATE avatar_profiles SET avatar_type = 'TTUR_TTORANG' WHERE avatar_type = 'TTUR_MALLANGI';
UPDATE travel_tendencies SET avatar_type = 'TTUR_TTORANG' WHERE avatar_type = 'TTUR_MALLANGI';
UPDATE avatar_profiles SET avatar_type = 'TTUR_MUDI' WHERE avatar_type = 'TTUR_POPO';
UPDATE travel_tendencies SET avatar_type = 'TTUR_MUDI' WHERE avatar_type = 'TTUR_POPO';
UPDATE avatar_profiles SET avatar_type = 'TTUR_SODAM' WHERE avatar_type = 'TTUR_SPARKLE';
UPDATE travel_tendencies SET avatar_type = 'TTUR_SODAM' WHERE avatar_type = 'TTUR_SPARKLE';
UPDATE avatar_profiles SET avatar_type = 'TTUR_SWEET' WHERE avatar_type = 'TTUR_GLIMMING';
UPDATE travel_tendencies SET avatar_type = 'TTUR_SWEET' WHERE avatar_type = 'TTUR_GLIMMING';
UPDATE avatar_profiles SET avatar_type = 'TTUR_BANJJAK' WHERE avatar_type = 'TTUR_PADO';
UPDATE travel_tendencies SET avatar_type = 'TTUR_BANJJAK' WHERE avatar_type = 'TTUR_PADO';
UPDATE avatar_profiles SET avatar_type = 'TTUR_SPARK' WHERE avatar_type = 'TTUR_DASHI';
UPDATE travel_tendencies SET avatar_type = 'TTUR_SPARK' WHERE avatar_type = 'TTUR_DASHI';
UPDATE avatar_profiles SET avatar_type = 'TTUR_LUNA' WHERE avatar_type = 'TTUR_FLARE';
UPDATE travel_tendencies SET avatar_type = 'TTUR_LUNA' WHERE avatar_type = 'TTUR_FLARE';
UPDATE avatar_profiles SET avatar_type = 'TTUR_GLIM' WHERE avatar_type = 'TTUR_BOUNCY';
UPDATE travel_tendencies SET avatar_type = 'TTUR_GLIM' WHERE avatar_type = 'TTUR_BOUNCY';
UPDATE avatar_profiles SET avatar_type = 'TTUR_HARAM' WHERE avatar_type = 'TTUR_PEPPI';
UPDATE travel_tendencies SET avatar_type = 'TTUR_HARAM' WHERE avatar_type = 'TTUR_PEPPI';
UPDATE avatar_profiles SET avatar_type = 'TTUR_MALLANG' WHERE avatar_type = 'TTUR_JETTI';
UPDATE travel_tendencies SET avatar_type = 'TTUR_MALLANG' WHERE avatar_type = 'TTUR_JETTI';
UPDATE avatar_profiles SET avatar_type = 'TTUR_BONGBONG' WHERE avatar_type = 'TTUR_BLAZE';
UPDATE travel_tendencies SET avatar_type = 'TTUR_BONGBONG' WHERE avatar_type = 'TTUR_BLAZE';
UPDATE avatar_profiles SET avatar_type = 'TTUR_BEOMI' WHERE avatar_type = 'TTUR_VIVID';
UPDATE travel_tendencies SET avatar_type = 'TTUR_BEOMI' WHERE avatar_type = 'TTUR_VIVID';
UPDATE avatar_profiles SET avatar_type = 'TTUR_BANGUL' WHERE avatar_type = 'TTUR_SURGE';
UPDATE travel_tendencies SET avatar_type = 'TTUR_BANGUL' WHERE avatar_type = 'TTUR_SURGE';

-- 기존 8행(레거시 row): 코드 치환 직후 avatar_type 기준으로 최종 캐릭터 카피 반영 — body는 앱 본문(문단 + 빈 줄 반복), image_url은 TTUR_* 슬러그(ttur-{소문자}.png)
UPDATE avatar_profiles SET display_name = '포근', description = '계획 없이 떠나는 여행은 왠지 불안해요!', tags = '["꼼꼼함","여유로움","안정감"]', body = CONCAT('즉흥적인 변수보다 미리 짜둔 계획 안에서 안정감을 느끼는 타입이에요.', '\n\n', '바쁘게 몰아치는 여행보다 여유로운 흐름을 선호하지만, 자연스럽게 그 흐름을 만들어가는 건 바로 본인이에요.', '\n\n', '예산도 꼼꼼히 따지면서 알차고 안정적인 여행을 설계하는 타입입니다.'), image_url = 'https://dduru.s3.ap-northeast-2.amazonaws.com/avatar/ttur-pogun.png', modified_at = NOW() WHERE avatar_type = 'TTUR_POGUN';
UPDATE avatar_profiles SET display_name = '포슬', description = '모두가 편안해야 내가 편해요!', tags = '["따뜻함","배려심","조용함"]', body = CONCAT('무리하지 않고 계획 안에서 편안하게 흘러가는 여행을 좋아하는 타입이에요.', '\n\n', '앞장서기보다는 주변 사람들과 자연스럽게 맞춰가며 분위기를 부드럽게 만들어줘요.', '\n\n', '가성비를 챙기면서도 조용하고 안정적인 여행을 즐기는 타입입니다.'), image_url = 'https://dduru.s3.ap-northeast-2.amazonaws.com/avatar/ttur-poseul.png', modified_at = NOW() WHERE avatar_type = 'TTUR_POSEUL';
UPDATE avatar_profiles SET display_name = '또랑', description = '동선이 곧 행복이에요, 낭비는 NO!', tags = '["야무짐","전략적","부지런함"]', body = CONCAT('동선부터 예산까지 꼼꼼하게 짜고 부지런하게 실행하는 전략형 타입이에요.', '\n\n', '하루를 알차게 채우는 걸 좋아하고, 자연스럽게 일정을 정리하고 이끌어가는 역할을 맡게 돼요.', '\n\n', '여행을 효율적으로 설계하고 실행하는 타입입니다.'), image_url = 'https://dduru.s3.ap-northeast-2.amazonaws.com/avatar/ttur-ttorang.png', modified_at = NOW() WHERE avatar_type = 'TTUR_TTORANG';
UPDATE avatar_profiles SET display_name = '다솜', description = '알차게 움직이되, 함께가 먼저예요!', tags = '["세심함","조화로움","성실함"]', body = CONCAT('계획적으로 움직이면서도 함께하는 사람들의 페이스에 맞춰주는 타입이에요.', '\n\n', '예산을 꼼꼼히 챙기면서 부지런히 움직이지만, 주도하기보다는 조화를 중요하게 생각해요.', '\n\n', '알차면서도 부드러운 여행을 만들어가는 타입입니다.'), image_url = 'https://dduru.s3.ap-northeast-2.amazonaws.com/avatar/ttur-dasom.png', modified_at = NOW() WHERE avatar_type = 'TTUR_DASOM';
UPDATE avatar_profiles SET display_name = '무디', description = '분위기 좋은 곳엔 돈 아끼지 않아요!', tags = '["감성적","안목","여유로움"]', body = CONCAT('감성적인 장소와 특별한 분위기를 중요하게 여기는 타입이에요.', '\n\n', '여유롭게 즐기되, 여행의 흐름은 직접 만들어가고 싶어하는 편이에요.', '\n\n', '돈이 좀 들더라도 기억에 남는 경험을 선택하는 타입입니다.'), image_url = 'https://dduru.s3.ap-northeast-2.amazonaws.com/avatar/ttur-mudi.png', modified_at = NOW() WHERE avatar_type = 'TTUR_MUDI';
UPDATE avatar_profiles SET display_name = '소담', description = '좋은 순간은 천천히 스며드는 거예요!', tags = '["따뜻함","낭만","세심함"]', body = CONCAT('여행의 분위기와 감성을 소중히 여기며 천천히 스며드는 타입이에요.', '\n\n', '앞장서기보다는 그 순간의 분위기에 자연스럽게 녹아드는 걸 좋아해요.', '\n\n', '특별한 경험에는 기꺼이 투자하는 타입입니다.'), image_url = 'https://dduru.s3.ap-northeast-2.amazonaws.com/avatar/ttur-sodam.png', modified_at = NOW() WHERE avatar_type = 'TTUR_SODAM';
UPDATE avatar_profiles SET display_name = '스윗', description = '좋은 건 무조건 다 경험해봐야죠!', tags = '["열정적","도전정신","적극적"]', body = CONCAT('좋은 경험을 절대 놓치지 않으려 부지런히 움직이는 적극적인 타입이에요.', '\n\n', '계획을 바탕으로 다양한 활동을 직접 이끌어가는 걸 좋아하고, 경험을 위해서라면 지출도 아끼지 않아요.', '\n\n', '여행을 꽉 채워 이끌어가는 타입입니다.'), image_url = 'https://dduru.s3.ap-northeast-2.amazonaws.com/avatar/ttur-sweet.png', modified_at = NOW() WHERE avatar_type = 'TTUR_SWEET';
UPDATE avatar_profiles SET display_name = '반짝', description = '함께라면 어떤 경험도 빛나요!', tags = '["밝음","배려심","활발함"]', body = CONCAT('하루를 알차게 채우며 활동적으로 움직이는 걸 좋아하는 타입이에요.', '\n\n', '직접 나서기보다는 함께하는 사람들과의 조화를 중요하게 생각하며 경험에 적극적으로 투자해요.', '\n\n', '활기차면서도 배려심 있는 타입입니다.'), image_url = 'https://dduru.s3.ap-northeast-2.amazonaws.com/avatar/ttur-banjjak.png', modified_at = NOW() WHERE avatar_type = 'TTUR_BANJJAK';

-- 신규 8종 행 추가(기존 DB에 없던 타입)
INSERT INTO avatar_profiles
    (avatar_type, display_name, description, body, image_url, tags, created_at, modified_at)
VALUES
    ('TTUR_SPARK', '스파크', '계획이요? 현장에서 만들어가면 되죠!', CONCAT('계획보다 현장의 느낌을 중요하게 여기고 새로운 경험을 찾아 즉흥적으로 움직이는 탐험형 타입이에요.', '\n\n', '예상치 못한 상황에서도 흔들리지 않고 여행을 신나게 이끌어가요.', '\n\n', '자유롭게 앞장서며 여행을 개척하는 타입입니다.'), 'https://dduru.s3.ap-northeast-2.amazonaws.com/avatar/ttur-spark.png', '["탐험가","추진력","자유로움"]', NOW(), NOW()),
    ('TTUR_LUNA', '루나', '새로운 건 일단 해보고 생각해요!', CONCAT('정해진 틀 없이 자유롭게 움직이며 새로운 경험을 적극적으로 받아들이는 타입이에요.', '\n\n', '앞장서기보다는 즉흥적인 흐름 속에서 자연스럽게 어울리는 걸 좋아해요.', '\n\n', '어디서든 새로운 경험에 열려있는 타입입니다.'), 'https://dduru.s3.ap-northeast-2.amazonaws.com/avatar/ttur-luna.png', '["개방적","도전정신","자유로움"]', NOW(), NOW()),
    ('TTUR_GLIM', '글림', '감성 있는 순간은 내가 만들어요!', CONCAT('감성적인 순간을 즉흥적으로 포착하며 여행의 분위기를 직접 만들어가는 타입이에요.', '\n\n', '서두르지 않고 천천히 움직이지만, 여행의 흐름은 본인이 이끌고 싶어해요.', '\n\n', '분위기 있는 여행을 즉흥적으로 설계하는 타입입니다.'), 'https://dduru.s3.ap-northeast-2.amazonaws.com/avatar/ttur-glim.png', '["감성적","창의적","낭만"]', NOW(), NOW()),
    ('TTUR_HARAM', '하람', '흘러가는 대로, 느끼는 대로예요!', CONCAT('흘러가는 순간을 천천히 만끽하며 자연스럽게 주변과 어울리는 타입이에요.', '\n\n', '계획보다는 그 순간의 감성과 분위기에 몸을 맡기는 걸 좋아해요.', '\n\n', '즉흥적인 여행의 감성을 가장 잘 즐기는 타입입니다.'), 'https://dduru.s3.ap-northeast-2.amazonaws.com/avatar/ttur-haram.png', '["자유로움","감성적","여유로움"]', NOW(), NOW()),
    ('TTUR_MALLANG', '말랑', '부담 없이 가요, 근데 가성비는 챙겨요!', CONCAT('빡빡한 계획 없이 느긋하게 즐기면서도 가성비는 놓치지 않는 타입이에요.', '\n\n', '분위기에 따라 자연스럽게 방향을 제시하며 부담 없는 여행을 만들어가요.', '\n\n', '여유롭지만 흐름을 슬쩍 잡아주는 타입입니다.'), 'https://dduru.s3.ap-northeast-2.amazonaws.com/avatar/ttur-mallang.png', '["유연함","편안함","가성비"]', NOW(), NOW()),
    ('TTUR_BONGBONG', '봉봉', '어디든 좋아요, 같이 가면 그게 최고!', CONCAT('가볍고 편안하게 즉흥적으로 움직이며 예산도 슬기롭게 챙기는 타입이에요.', '\n\n', '주도하기보다는 흐름에 자연스럽게 녹아들며 주변 사람들과 편하게 어울려요.', '\n\n', '어디서든 부담 없이 함께할 수 있는 타입입니다.'), 'https://dduru.s3.ap-northeast-2.amazonaws.com/avatar/ttur-bongbong.png', '["친화력","따뜻함","편안함"]', NOW(), NOW()),
    ('TTUR_BEOMI', '범이', '일단 출발! 방법은 가면서 찾아요!', CONCAT('상황에 맞게 빠르게 판단하고 가성비 있게 부지런히 움직이는 타입이에요.', '\n\n', '계획보다는 현장에서 직접 결론을 내리고 이끌어가는 걸 좋아해요.', '\n\n', '에너지 넘치게 여행을 주도하는 타입입니다.'), 'https://dduru.s3.ap-northeast-2.amazonaws.com/avatar/ttur-beomi.png', '["추진력","순발력","활발함"]', NOW(), NOW()),
    ('TTUR_BANGUL', '방울', '에너지 넘치게, 근데 예산은 지켜요!', CONCAT('에너지 넘치게 즉흥적으로 움직이면서도 예산은 현명하게 챙기는 타입이에요.', '\n\n', '직접 이끌기보다는 빠르게 적응하며 어떤 상황에서도 분위기에 잘 녹아들어요.', '\n\n', '활발하게 움직이며 자연스럽게 어울리는 타입입니다.'), 'https://dduru.s3.ap-northeast-2.amazonaws.com/avatar/ttur-bangul.png', '["에너지","적응력","가성비"]', NOW(), NOW());

-- SurveyQuestionSeeder의 v2 문항/옵션 시드를 SQL로 이관
-- 레거시/기존 데이터를 정리한 뒤 v2(14문항)로 재시드

DELETE FROM survey_question_options;
DELETE FROM survey_questions;

INSERT INTO survey_questions
    (question_id, display_order, type, required, min_select, max_select, text, image_url, created_at, modified_at)
VALUES
    ('rhythm_q1', 1, 'SINGLE', 1, 1, 1, '목적지로 향하는 길, 당신의 발걸음은 어디를 향하나요?', 'survey/rhythm_q1.png', NOW(), NOW()),
    ('rhythm_q2', 2, 'SINGLE', 1, 1, 1, '여행 전, 짐 싸는 날!', 'survey/rhythm_q2.png', NOW(), NOW()),
    ('rhythm_q3', 3, 'SINGLE', 1, 1, 1, '여행 일정 짜는 스타일은?', 'survey/rhythm_q3.png', NOW(), NOW()),
    ('activity_tags', 4, 'MULTI', 1, 1, 3, '여행 가서 주로 뭐 하고 싶어?\n(최대 3개)', 'survey/activity_tags.png', NOW(), NOW()),
    ('consumption_q1', 5, 'SINGLE', 1, 1, 1, '여행 예산이 살짝 오버됐다', 'survey/consumption_q1.png', NOW(), NOW()),
    ('consumption_q2', 6, 'SINGLE', 1, 1, 1, '이동할 때는 어떻게 이동할까?', 'survey/consumption_q2.png', NOW(), NOW()),
    ('consumption_q3', 7, 'SINGLE', 1, 1, 1, '여행 중 돈을 써야 할 선택지가 생겼다', 'survey/consumption_q3.png', NOW(), NOW()),
    ('energy_q1', 8, 'SINGLE', 1, 1, 1, '여행지에서의 하루', 'survey/energy_q1.png', NOW(), NOW()),
    ('energy_q2', 9, 'SINGLE', 1, 1, 1, '조식이 제공되는 호텔을 예약했다.', 'survey/energy_q2.png', NOW(), NOW()),
    ('energy_q3', 10, 'SINGLE', 1, 1, 1, '하루 나에게 개인 자유 시간이 주어졌다.', 'survey/energy_q3.png', NOW(), NOW()),
    ('decision_q1', 11, 'SINGLE', 1, 1, 1, '여행 준비할 때 역할을 나눈다면?', 'survey/decision_q1.png', NOW(), NOW()),
    ('decision_q2', 12, 'SINGLE', 1, 1, 1, '의견을 모으거나 정해야 하는 상황에서 나는?', 'survey/decision_q2.png', NOW(), NOW()),
    ('decision_q3', 13, 'SINGLE', 1, 1, 1, '단톡방에서 다들 눈치 보는 중…', 'survey/decision_q3.png', NOW(), NOW()),
    ('record_style', 14, 'SINGLE', 1, 1, 1, '여행 중 사람/풍경/음식 등 사진이 예쁘게 나올 것 같은 순간을 만났다', 'survey/record_style.png', NOW(), NOW());

INSERT INTO survey_question_options
    (question_id, display_order, code, icon, text, created_at, modified_at)
VALUES
    ('rhythm_q1', 1, 1, '✨', '최단 거리가 최고! 정해진 루트대로 다음 일정으로', NOW(), NOW()),
    ('rhythm_q1', 2, 2, '🌈', '어? 저기 예쁜데? 가는 길에 궁금한 곳이 생기면 일단 들렀다 가자!', NOW(), NOW()),

    ('rhythm_q2', 1, 1, '📋', '캐리어는 3일 전부터 오픈… 미리미리 완벽 준비', NOW(), NOW()),
    ('rhythm_q2', 2, 2, '🔥', '여행 전날 밤? 그때 몰아서 싸지 뭐~', NOW(), NOW()),

    ('rhythm_q3', 1, 1, '📅', '동선이랑 시간은 어느 정도 정해둬야 마음이 편해~', NOW(), NOW()),
    ('rhythm_q3', 2, 2, '📍', '대충 가고 싶은 곳만 정해두고, 나머진 현장 느낌대로!', NOW(), NOW()),

    ('activity_tags', 1, 1, '🏛️', '관광', NOW(), NOW()),
    ('activity_tags', 2, 2, '⚽️', '관람', NOW(), NOW()),
    ('activity_tags', 3, 3, '🌳', '자연 탐방', NOW(), NOW()),
    ('activity_tags', 4, 4, '🍖', '맛집 탐방', NOW(), NOW()),
    ('activity_tags', 5, 5, '🛍️', '쇼핑', NOW(), NOW()),
    ('activity_tags', 6, 6, '🏖️', '휴양', NOW(), NOW()),
    ('activity_tags', 7, 7, '🏄🏻‍♂️', '액티비티', NOW(), NOW()),
    ('activity_tags', 8, 8, '🎡', '놀이공원', NOW(), NOW()),
    ('activity_tags', 9, 9, '💃', '페스티벌', NOW(), NOW()),

    ('consumption_q1', 1, 1, '📊', '잠깐… 지출 조정 타임. 예산에 맞춰야지', NOW(), NOW()),
    ('consumption_q1', 2, 2, '💳', '여행인데 이 정도 FLEX는 괜찮지~', NOW(), NOW()),

    ('consumption_q2', 1, 1, '🚇', '여행은 교통도 체험이지~ 가성비 이동!', NOW(), NOW()),
    ('consumption_q2', 2, 2, '🚕', '시간 아끼는 게 곧 돈이야. 바로 택시!', NOW(), NOW()),

    ('consumption_q3', 1, 1, '📊', '굳이 돈 안 써도 즐길 방법 많지! 가성비로 간다', NOW(), NOW()),
    ('consumption_q3', 2, 2, '✨', '여행 추억은 이런 데서 생기지. 경험에 투자!', NOW(), NOW()),

    ('energy_q1', 1, 1, '🐌', '카페-산책 … 여유롭게 즐기자 ~', NOW(), NOW()),
    ('energy_q1', 2, 2, '🌅', '하루에 최대한 많이 다보자! 지금 아니면 언제 오겠어?', NOW(), NOW()),

    ('energy_q2', 1, 1, '🙅🏻‍♀️', '오픈런은 무리… 느지막이 일어나서 근처 브런치 카페나 갈까?', NOW(), NOW()),
    ('energy_q2', 2, 2, '🏃', '1등으로 입장! 조식 빨리 먹고 남들보다 먼저 관광지로 출발~', NOW(), NOW()),

    ('energy_q3', 1, 1, '🌿', '아무것도 안 하는 날도 필요해', NOW(), NOW()),
    ('energy_q3', 2, 2, '📍', '근처 명소 검색해서 바로 채우기!', NOW(), NOW()),

    ('decision_q1', 1, 1, '🙂', '나는 맡겨주는 역할 하는 게 편해', NOW(), NOW()),
    ('decision_q1', 2, 2, '📋', '자연스럽게 총괄 또는 정리하는 편!', NOW(), NOW()),

    ('decision_q2', 1, 1, '👍', '난 다 좋아~ 따라갈게!', NOW(), NOW()),
    ('decision_q2', 2, 2, '🗣️', '내가 먼저 "이렇게 해볼까?" 하고 제안한다.', NOW(), NOW()),

    ('decision_q3', 1, 1, '😅', '나도 일단 말 안 하고 흐름 타기', NOW(), NOW()),
    ('decision_q3', 2, 2, '🚀', '답답해! 내가 정리해서 결론 낸다', NOW(), NOW()),

    ('record_style', 1, 1, '👀', '카메라는 잠시 넣어두고… 눈에 먼저 저장', NOW(), NOW()),
    ('record_style', 2, 2, '📸', '잠깐만!! 이건 인생샷 각이다. 바로 찍어야지', NOW(), NOW());
