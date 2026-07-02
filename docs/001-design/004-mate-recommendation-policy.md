# 004. 여행방 추천 정책 및 도메인 설계

> 이 문서를 보면 홈 여행방 추천의 대상, 제외 조건, 점수 계산, 일일 제공 정책, 저장 모델, 후속 구현 범위를 파악할 수 있습니다.

---

## 한 줄 요약

홈 추천은 사용자가 아니라 여행방을 하루 최대 3개 추천하는 기능이며, 같은 KST 일자에는 이미 생성된 추천 묶음을 재사용합니다.

---

## 1. 배경

295번 이슈는 자동매칭 구현 전에 정책과 저장 모델을 확정하는 작업입니다. 실제 후보 조회, 점수 계산, 추천 결과 생성, 홈 응답 연결은 후속 이슈에서 구현합니다.

| 이슈 | 범위 |
|---|---|
| #295 | 정책 문서화, 저장 테이블 마이그레이션 초안 |
| #298 | 후보 조회, 제외 조건, 점수 계산 구현 |
| #297 | 일일 추천 묶음 생성, idempotent 처리, 홈 응답 연결 |

이 기능에서 "매칭"은 즉시 사용자를 연결하는 자동 승인 기능이 아닙니다. 홈에서 여행방을 추천하고, 사용자가 신청하면 방장이 수락/거절하며, 수락 후 기존 참여 신청과 채팅 흐름으로 이어집니다.

---

## 2. 현재 상태

- 홈 추천 엔드포인트는 `/api/v1/home/mate-recommendations`입니다.
- 홈 추천 DTO는 `isAvailable`, `remainingFreeCount`, `recommendations`를 이미 가지고 있습니다.
- 설문 완료 여부는 `UserOnboarding.surveyStatus == COMPLETED`로 판단합니다.
- 성향 4축 점수는 `TravelTendency`의 `rhythmScore`, `energyScore`, `consumptionScore`, `decisionScore`에 0~10 점수로 저장됩니다.
- 모집글은 `Post`가 `Destination`, 일정, 모집 인원, 상태, 동행 방식(`FULL`, `PARTIAL`, `MEAL`), 선호 성별, 선호 나이 조건을 가집니다.
- 관심 여행지, 가능한 여행 기간, 추천 패스 이력은 새 테이블로 저장합니다.
- 차단 도메인은 아직 없으므로 차단 관계 제외는 후속 차단 기능이 생기면 후보 제외 조건에 추가합니다.

---

## 3. 추천 정책

### 3.1 추천 가능 조건

추천을 받을 수 있는 사용자는 아래 조건을 모두 만족해야 합니다.

- 로그인 사용자입니다.
- 온보딩 설문을 완료했습니다.
- 신청자 본인의 `TravelTendency`가 존재합니다.

설문 미완료 또는 성향 점수 없음은 실패가 아니라 추천 불가 상태입니다. 홈 응답은 후속 #297에서 `isAvailable=false`, `remainingFreeCount=0`, `recommendations=[]` 형태로 연결합니다.

### 3.2 항상 적용하는 후보 제외 조건

추천 후보 여행방은 아래 조건을 모두 만족해야 합니다.

- `Post.isDeleted=false`입니다.
- `Post.status=OPEN`입니다.
- `Post.endDate >= today(KST)`입니다.
- `Post.recruitCount < Post.recruitCapacity`입니다.
- 신청자가 해당 게시글 작성자가 아닙니다.
- 신청자가 게시글의 선호 성별과 선호 나이 조건을 만족합니다.
- 신청자가 해당 게시글에 이미 참여 신청하지 않았습니다.
- 신청자가 해당 게시글을 이미 패스하지 않았습니다.
- 신청자가 해당 게시글을 신고한 적이 없습니다. 신고 상태와 무관하게 제외합니다.
- 게시글 작성자(호스트)의 `TravelTendency`가 존재합니다.

선호 성별과 나이는 추천 저장 테이블에 복제하지 않고 후보 조회 시 `posts`와 신청자 `profiles`를 기준으로 필터링합니다.

| 조건 | 통과 기준 |
|---|---|
| 성별 무관 | `post.preferredGender = U`이면 통과 |
| 성별 지정 | `post.preferredGender`가 `M` 또는 `F`이면 `applicant.profile.gender`와 같아야 통과 |
| 나이 무관 | `post.isAgeAny = true`이면 통과 |
| 나이 지정 | `post.isAgeAny = false`이면 신청자의 KST 기준 만 나이가 `post.minAge`~`post.maxAge` 범위에 있어야 통과 |

신청자의 `Profile.gender` 또는 `Profile.birthday`가 필요한 조건인데 값이 없으면 해당 게시글은 후보에서 제외합니다. 예를 들어 성별 지정 게시글에서 신청자 성별이 없거나, 나이 지정 게시글에서 생년월일이 없으면 추천하지 않습니다.

참여 신청 이력은 `participations`를 source of truth로 사용합니다. 추천 후보 조회에서는 신청 상태와 무관하게 신청자와 게시글의 참여 신청 row가 하나라도 있으면 제외합니다.

```sql
NOT EXISTS (
  SELECT 1
  FROM participations p
  WHERE p.post_id = post.id
    AND p.user_id = :userId
)
```

현재 참여 신청 상태는 `PENDING`, `CONTACTING`, `APPROVED`, `REJECTED`입니다. 네 상태 모두 추천 후보에서 제외합니다. `REJECTED`도 이미 거절된 방을 다시 노출하지 않기 위해 제외하며, 현재 도메인에 없는 `EXPIRED`, `CANCELED` 같은 상태가 추가되면 재추천 허용 여부를 별도 정책으로 정합니다.

### 3.3 사용자가 설정한 경우에만 적용하는 필터

관심 여행지와 가능한 여행 날짜는 필수 입력이 아닙니다. 설정되어 있을 때만 하드 필터로 적용합니다.

| 사용자 설정 | 필터 |
|---|---|
| 여행지 O / 날짜 O | 여행지 필터 + 날짜 필터 |
| 여행지 O / 날짜 X | 여행지 필터만 |
| 여행지 X / 날짜 O | 날짜 필터만 |
| 여행지 X / 날짜 X | 둘 다 미적용 |

여행지 필터는 아래 기준을 사용합니다.

- `CITY`: `destination_id`가 정확히 일치해야 합니다.
- `COUNTRY`: `country_code`가 일치하면 해당 국가의 모든 도시를 허용합니다.
- 여러 여행지를 설정한 경우 하나라도 일치하면 통과합니다.

날짜 필터는 사용자의 가능한 기간 중 하나라도 여행방 일정과 조건에 맞으면 통과합니다.

| 동행 방식 | 날짜 통과 기준 |
|---|---|
| `FULL` | 사용자 가능 기간이 여행방의 `startDate`~`endDate` 전체를 포함 |
| `PARTIAL` | 겹치는 날짜가 2일 이상 |
| `MEAL` | 겹치는 날짜가 1일 이상 |

날짜 겹침 일수는 양 끝 날짜를 모두 포함해 계산합니다. 예를 들어 2026-07-01~2026-07-02와 2026-07-02~2026-07-03은 1일 겹칩니다.

### 3.4 점수 계산

MVP 점수는 신청자 성향과 게시글 호스트 성향만 비교합니다. 여행방에 여러 멤버가 있어도 평균 성향은 계산하지 않습니다.

축별 유사도는 아래와 같이 계산합니다.

```text
axisSimilarity = 1 - (abs(applicantScore - hostScore) / 10)
```

최종 적합도는 아래 가중합을 0~100 정수로 반올림합니다.

```text
matchPercentage =
  round((
    rhythmSimilarity * 0.30 +
    energySimilarity * 0.30 +
    consumptionSimilarity * 0.30 +
    decisionSimilarity * 0.10
  ) * 100)
```

활동 태그, 기록 스타일, 사진 성향은 점수에 넣지 않습니다. 추천 카드나 상세 화면에서 참고 정보로만 사용할 수 있습니다.

### 3.5 정렬 기준

후보는 아래 순서로 정렬하고 상위 3개를 추천 묶음으로 저장합니다.

1. `matchPercentage DESC`
2. `Post.startDate ASC`
3. `Post.id DESC`

추천 이유(`matchReasons`)와 확인 필요 요소(`cautionPoints`)는 #298에서 점수 차이와 축별 특성을 기반으로 생성합니다. 저장 테이블에는 JSON 배열로 보관합니다.

---

## 4. 일일 추천 묶음과 재실행 정책

### 4.1 제공량과 일자 기준

- 하루 추천 제공량은 총 3개입니다.
- 초기화 기준은 매일 00:00 KST입니다.
- 추천 일자는 `TimeProvider.today()`가 반환하는 KST 날짜를 사용합니다.

### 4.2 캐싱과 idempotent 처리

같은 사용자와 같은 추천 일자에 `COMPLETED` 또는 `EMPTY` 상태의 `mate_recommendation_batches`가 있으면 새 후보를 계산하지 않고 기존 추천 묶음을 반환합니다.

- 홈 재진입, 앱 재실행, 추천 카드 상세 조회는 제공량을 차감하지 않습니다.
- 추천 신청, 추천 패스도 당일 묶음을 새로 채우지 않습니다.
- 후보가 0~2개만 생성되어도 그날의 묶음은 확정된 것으로 봅니다.
- 같은 날 추가 생성은 하지 않으며 `remainingFreeCount=0`으로 응답합니다.

동시 실행은 `(user_id, recommendation_date)` unique key로 방어합니다. #297 구현에서는 unique 충돌 시 이미 생성된 묶음을 다시 조회하고, `CREATED` 상태면 진행 중으로 보고 중복 생성을 시도하지 않습니다.

### 4.3 상태와 재시도

추천 결과 생성 중 후보가 없으면 실패가 아니라 빈 추천 묶음입니다. 시스템 오류로 묶음 생성 자체가 실패한 경우에만 재시도 대상입니다.

| 상태 | 의미 |
|---|---|
| `CREATED` | 추천 묶음 행이 생성되었고 결과 생성이 진행 중임 |
| `COMPLETED` | 추천 묶음과 추천 결과가 정상 생성됨 |
| `EMPTY` | 추천 가능하지만 조건에 맞는 후보가 없음 |
| `FAILED` | 시스템 오류로 생성 실패, 수동 또는 자동 재시도 가능 |

재시도는 `FAILED` 상태만 대상으로 합니다. `COMPLETED`와 `EMPTY`는 같은 날 재실행해도 새 후보를 만들지 않습니다. `CREATED`가 장시간 남아 있으면 생성 중 장애 또는 중단으로 보고 운영 기준에 따라 `FAILED`로 전환한 뒤 재시도합니다.

---

## 5. 저장 모델

`V27__mate_recommendations.sql`에서 아래 테이블을 추가합니다.

| 테이블 | 책임 |
|---|---|
| `user_recommendation_destination_preferences` | 사용자 관심 여행지. 국가 전체 또는 도시 단위 선택을 저장 |
| `user_recommendation_available_dates` | 사용자 가능한 여행 기간을 여러 개 저장 |
| `mate_recommendation_batches` | 사용자별 KST 일자 추천 묶음 |
| `mate_recommendations` | 묶음 안의 추천 여행방, 추천 순위, 점수, 이유 |
| `mate_recommendation_passes` | 사용자가 패스한 추천 여행방 |

`user_recommendation_destination_preferences`는 중복 문자열 키를 저장하지 않습니다. `COUNTRY`는 `(user_id, preference_type, country_code)`, `CITY`는 `(user_id, preference_type, destination_id)` unique key로 중복을 막습니다.

`user_recommendation_available_dates`의 unique key는 동일한 기간 중복만 막습니다. 겹치는 기간까지 금지할지는 #298 또는 별도 설정 API 구현 시 애플리케이션에서 `new.start <= end_date AND new.end >= start_date` 조건으로 검증합니다.

`mate_recommendations.id`가 홈 추천 응답의 `recommendationId`가 됩니다. 같은 게시글은 다른 날짜에 다시 추천될 수 있지만, 같은 일자 묶음 안에서는 중복될 수 없습니다. 추천 결과는 당시 노출된 스냅샷으로 유지하고 `APPLIED` 여부는 `participations`, `PASSED` 여부는 `mate_recommendation_passes`를 조인해서 판단합니다.

`mate_recommendations`는 `user_id`를 직접 저장하지 않습니다. 추천 결과의 사용자는 `mate_recommendations.batch_id`를 통해 `mate_recommendation_batches.user_id`로 판단합니다. 홈 조회도 `user_id + recommendation_date`로 batch를 먼저 찾고, 해당 batch의 recommendations를 `recommendation_rank` 순서로 조회합니다.

`mate_recommendation_passes`는 패스 여부의 source of truth만 담당합니다. 추천 결과와 직접 연결하는 `recommendation_id`는 두지 않고, 같은 사용자가 같은 게시글을 패스했는지는 `(user_id, post_id)`로 판단합니다.

추천 묶음의 실제 추천 개수는 `mate_recommendations` row 수로 계산합니다. 하루 최대 3개 정책은 `mate_recommendations.recommendation_rank`의 1~3 CHECK와 `(batch_id, recommendation_rank)` unique key로 제한하고, 별도 count 캐시 컬럼은 두지 않습니다.

---

## 6. 테스트 방향

#298에서는 아래 케이스를 검증합니다.

- 설문 미완료 또는 신청자 성향 없음은 추천 불가입니다.
- 후보가 없으면 빈 결과를 반환합니다.
- 여행지/날짜 미설정 시 해당 필터를 적용하지 않습니다.
- 도시와 국가 전체 여행지 필터가 각각 의도대로 동작합니다.
- `FULL`, `PARTIAL`, `MEAL` 동행 방식별 날짜 겹침 기준을 적용합니다.
- 게시글 선호 성별이 `U`이면 신청자 성별과 무관하게 통과하고, `M`/`F`이면 신청자 성별과 일치할 때만 통과합니다.
- 게시글 나이 조건이 무관이면 신청자 생년월일과 무관하게 통과하고, 나이 범위가 있으면 신청자 만 나이가 범위 안에 있을 때만 통과합니다.
- 성별 또는 생년월일이 필요한 조건인데 신청자 프로필 값이 없으면 후보에서 제외합니다.
- 자기 게시글, 참여 신청 row가 있는 게시글, 패스한 게시글, 신고한 게시글을 제외합니다.
- 참여 신청 상태가 `PENDING`, `CONTACTING`, `APPROVED`, `REJECTED` 중 무엇이든 추천 후보에서 제외합니다.
- 점수 계산과 동점 정렬 기준을 검증합니다.

#297에서는 아래 케이스를 검증합니다.

- 같은 날 홈 추천 재호출은 기존 묶음을 재사용합니다.
- 다음 날에는 새 묶음을 생성합니다.
- 동시 생성 시 unique key 충돌을 기존 묶음 재조회로 처리합니다.
- 후보가 0~2개여도 같은 날 추가 생성하지 않습니다.
- 추천 개수는 batch 캐시 컬럼이 아니라 `mate_recommendations` row count로 계산합니다.
- 신청/패스 상태는 추천 결과 row를 갱신하지 않고 참여 신청/패스 이력에서 계산합니다.
- `FAILED` 상태만 재시도 대상으로 처리합니다.

---

## 관련 문서

- [홈 API 섹션 기반 로딩 ADR](../006-adr/006-home-api-section-based-loading.md)
- [도메인 명세](./001-domain.md)
- [데이터 명세](./002-data.md)
