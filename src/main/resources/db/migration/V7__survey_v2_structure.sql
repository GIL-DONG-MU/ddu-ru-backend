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

-- AvatarType 16종 확장에 따른 신규 8종 프로필 시드
-- 기존 데이터가 있는 환경에서도 중복 없이 반영되도록 INSERT IGNORE 사용

-- 과거 환경에서 avatar_type 컬럼이 ENUM(8종)으로 생성된 경우를 대비해 VARCHAR로 보정
ALTER TABLE avatar_profiles
    MODIFY COLUMN avatar_type VARCHAR(64) NOT NULL;

INSERT IGNORE INTO avatar_profiles
    (avatar_type, description, image_url, personality, strength, tip, tags, created_at, modified_at)
VALUES
    (
        'TTUR_DASHI',
        '여유를 지키면서도 움직일 때는 빠르게 몰입하는 알뜰 액션러',
        'https://dduru.s3.ap-northeast-2.amazonaws.com/avatar/ttur-dashi.png',
        '차분한 기본 리듬 위에 순간 집중력이 높아, 반나절 코스를 효율적으로 완주하는 타입이에요.',
        '시간 낭비를 줄이고 핵심 경험을 빠르게 챙기는 실행력이 뛰어나요.',
        '쉬는 날과 움직이는 날을 번갈아 배치하면 만족도와 체력을 같이 챙길 수 있어요.',
        '["속도조절","효율동선","가성비액티비티"]',
        NOW(), NOW()
    ),
    (
        'TTUR_FLARE',
        '감성과 속도를 함께 챙기는 독립형 하이라이트 수집가',
        'https://dduru.s3.ap-northeast-2.amazonaws.com/avatar/ttur-flare.png',
        '전체 일정은 안정적으로 잡되, 마음에 드는 순간에는 과감히 에너지를 쓰는 타입이에요.',
        '짧은 시간에도 임팩트 있는 장소를 골라내는 눈이 좋아요.',
        '플렉스는 하루 한 타임으로 정하면 예산 압박 없이 오래 즐길 수 있어요.',
        '["감성스팟","하이라이트집중","선택적플렉스"]',
        NOW(), NOW()
    ),
    (
        'TTUR_BOUNCY',
        '따뜻한 텐션으로 팀 분위기를 띄우는 사교형 밸런서',
        'https://dduru.s3.ap-northeast-2.amazonaws.com/avatar/ttur-bouncy.png',
        '사람들과 함께 있을 때 활력이 올라가고, 서로 편한 흐름을 만드는 데 강한 타입이에요.',
        '활동과 휴식의 균형을 자연스럽게 맞춰 팀 피로도를 낮춰줘요.',
        '모두를 챙기다 지치지 않게 하루 30분 개인 회복 시간을 먼저 확보해 보세요.',
        '["팀케어","활동휴식밸런스","다정한리드"]',
        NOW(), NOW()
    ),
    (
        'TTUR_PEPPI',
        '함께하는 순간의 퀄리티에 투자하는 소셜 힐링 메이커',
        'https://dduru.s3.ap-northeast-2.amazonaws.com/avatar/ttur-peppi.png',
        '동행의 기분과 분위기를 섬세하게 살피며, 좋은 한 끼와 좋은 공간을 소중히 여기는 타입이에요.',
        '단체 만족도가 높은 코스를 큐레이션하고 관계 갈등을 부드럽게 완충해요.',
        '핵심 일정 2개만 고정하고 나머지는 유연하게 두면 모두가 편해져요.',
        '["관계중심","분위기투자","힐링큐레이션"]',
        NOW(), NOW()
    ),
    (
        'TTUR_JETTI',
        '빠르고 영리하게 움직이는 실속형 솔로 탐험가',
        'https://dduru.s3.ap-northeast-2.amazonaws.com/avatar/ttur-jetti.png',
        '새로운 장소를 빠르게 훑으면서도 비용 대비 효용을 놓치지 않는 타입이에요.',
        '교통, 동선, 체험 우선순위를 빠르게 계산해 실행하는 능력이 좋아요.',
        '모험 밀도가 높을수록 하루 한 번은 느린 코스를 넣어 번아웃을 예방하세요.',
        '["스마트탐험","빠른판단","실속중심"]',
        NOW(), NOW()
    ),
    (
        'TTUR_BLAZE',
        '도전과 속도에 진심인 하이텐션 독립 모험러',
        'https://dduru.s3.ap-northeast-2.amazonaws.com/avatar/ttur-blaze.png',
        '즉흥 상황에서도 주저하지 않고 강한 경험을 찾아 나서는 추진형 타입이에요.',
        '변수 많은 일정에서 빠르게 대안을 찾고 페이스를 끌어올리는 데 강해요.',
        '연속 강행군 대신 회복 구간을 미리 넣으면 여행 후반 만족도가 크게 올라가요.',
        '["고에너지","즉흥대응","강한경험추구"]',
        NOW(), NOW()
    ),
    (
        'TTUR_VIVID',
        '함께 움직이며 선명한 추억을 만드는 알뜰 사교 모험가',
        'https://dduru.s3.ap-northeast-2.amazonaws.com/avatar/ttur-vivid.png',
        '사람들과 역동적으로 움직이는 걸 좋아하고, 지출은 현실적으로 관리하는 타입이에요.',
        '단체 동선 최적화와 현지 추천 스팟 실행력이 높아 팀 시너지를 만들어요.',
        '식사 한 끼만 고정하고 나머지는 자유로 두면 만족도와 유연성이 동시에 올라가요.',
        '["그룹시너지","현지스팟실행","합리적소비"]',
        NOW(), NOW()
    ),
    (
        'TTUR_SURGE',
        '여행의 분위기를 끌어올리는 풀파워 소셜 어드벤처 리더',
        'https://dduru.s3.ap-northeast-2.amazonaws.com/avatar/ttur-surge.png',
        '새로운 사람, 새로운 경험, 새로운 일정 변화를 모두 즐기는 에너지 중심 타입이에요.',
        '액티비티 추진과 현장 리드가 뛰어나 단체 여행의 몰입도를 높여줘요.',
        '하루 예산 상한과 필수 휴식 1회만 지켜도 만족도는 유지하고 피로는 크게 줄일 수 있어요.',
        '["풀스로틀","소셜리더","모험지향"]',
        NOW(), NOW()
    );

ALTER TABLE travel_tendencies
    MODIFY COLUMN avatar_type VARCHAR(64) NOT NULL;

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
