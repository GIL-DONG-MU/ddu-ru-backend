# ADR-006: 홈 API 섹션 단위 로딩 구조

- 상태: Proposed
- 날짜: 2026-06-20
- 결정자: [GitHub @shinheekim](https://github.com/shinheekim)

> TODO: 백엔드/프론트 합의 후 상태를 Accepted로 변경하고, 실제 API PR 링크를 추가합니다.

## 맥락

현재 홈 화면은 `GET /api/v1/home` 한 번으로 필요한 데이터를 모두 내려받는 구조입니다. 이 응답에는 사용자 상태, 예정 여행, 인기 여행지, 메이트 추천, 슈퍼호스트, 같은 여행지 여행, 또래 여행이 함께 포함됩니다.

이 방식은 첫 구현이 단순하지만, 홈에 들어가는 데이터가 늘어날수록 다음 문제가 생깁니다.

- 한 섹션 조회가 실패하면 홈 전체 조회가 실패할 수 있습니다.
- 섹션마다 갱신 주기와 조회 비용이 다릅니다.
- 사용자별 추천, 모집글 상태, 참여 인원, pending task count처럼 실시간성이 있는 값이 정적인 값과 같은 응답에 묶입니다.
- 프론트가 섹션별 로딩, 빈 상태, 실패 상태를 독립적으로 보여주기 어렵습니다.

예를 들어 메이트 추천 조회가 일시적으로 실패해도 인기 여행지나 슈퍼호스트 섹션은 보여줄 수 있어야 합니다. 하지만 모든 데이터를 하나의 홈 API에서 조립하면, 서버와 클라이언트 모두 부분 실패를 다루기 어려워집니다.

## 결정

홈 API를 "홈 전체 데이터 조회 API"가 아니라 "홈 초기 구성 API"로 축소합니다.

`GET /api/v1/home`은 홈을 그리기 위해 가장 먼저 필요한 최소 정보만 반환합니다.

- 현재 사용자 상태
- 노출 가능한 홈 섹션 목록
- 섹션별 호출 가능 여부
- 필요한 경우 섹션별 endpoint 정보

실제 섹션 데이터는 섹션별 API에서 독립적으로 조회합니다.

```http
GET /api/v1/home
GET /api/v1/home/upcoming-trip
GET /api/v1/home/mate-recommendations
GET /api/v1/home/same-destination-trips
GET /api/v1/home/same-age-trips
GET /api/v1/destinations/popular
GET /api/v1/posts/super-hosts
```

초기 응답 예시는 다음과 같습니다. 실제 필드명과 endpoint는 구현 PR에서 확정합니다.

```json
{
  "viewerStatus": "MEMBER_SURVEY_COMPLETED",
  "sections": [
    {
      "key": "UPCOMING_TRIP",
      "enabled": true,
      "endpoint": "/api/v1/home/upcoming-trip"
    },
    {
      "key": "POPULAR_DESTINATIONS",
      "enabled": true,
      "endpoint": "/api/v1/destinations/popular"
    },
    {
      "key": "MATE_RECOMMENDATIONS",
      "enabled": true,
      "endpoint": "/api/v1/home/mate-recommendations"
    },
    {
      "key": "SUPER_HOSTS",
      "enabled": true,
      "endpoint": "/api/v1/posts/super-hosts"
    },
    {
      "key": "SAME_DESTINATION_TRIPS",
      "enabled": true,
      "endpoint": "/api/v1/home/same-destination-trips"
    },
    {
      "key": "SAME_AGE_TRIPS",
      "enabled": true,
      "endpoint": "/api/v1/home/same-age-trips"
    }
  ]
}
```

프론트는 홈 진입 시 `GET /api/v1/home`을 먼저 호출하고, 응답에 포함된 섹션을 기준으로 각 섹션 API를 병렬 호출합니다.

```text
Home Screen
  -> GET /api/v1/home
      -> viewerStatus 확인
      -> 노출 가능한 section 확인
  -> section API 병렬 호출
      -> upcomingTrip
      -> popularDestinations
      -> mateRecommendations
      -> superHosts
      -> sameDestinationTrips
      -> sameAgeTrips
```

## 프론트 영향

프론트 변경의 핵심은 "홈 전체 로딩 상태 하나"에서 "섹션별 로딩 상태"로 바꾸는 것입니다.

기존 구조는 대략 다음과 같습니다.

```text
home loading
  -> success: 전체 홈 렌더링
  -> failure: 전체 홈 실패 화면
```

변경 후 구조는 다음과 같습니다.

```text
home shell loading
  -> success: 홈 기본 구조 렌더링

section loading
  -> upcomingTrip success/failure/empty
  -> popularDestinations success/failure/empty
  -> mateRecommendations success/failure/empty
  -> superHosts success/failure/empty
  -> sameDestinationTrips success/failure/empty
  -> sameAgeTrips success/failure/empty
```

이렇게 바꾸면 특정 섹션이 실패해도 다른 섹션은 정상적으로 보여줄 수 있습니다.

예를 들어 추천 섹션만 실패한 경우:

- 홈 화면 자체는 정상 진입합니다.
- 인기 여행지, 슈퍼호스트, 예정 여행은 그대로 보여줍니다.
- 메이트 추천 섹션만 재시도 버튼, 빈 상태, 또는 fallback UI를 노출합니다.

사용자 상태별 섹션 노출도 명확해집니다.

| viewerStatus | 홈 초기 응답 | 섹션 호출 |
|---|---|---|
| `GUEST` | 비회원에게 노출 가능한 섹션만 반환 | 회원 전용 섹션 호출하지 않음 |
| `MEMBER_SURVEY_REQUIRED` | 설문 전 회원용 섹션 반환 | 추천 섹션은 비활성 또는 호출하지 않음 |
| `MEMBER_SURVEY_COMPLETED` | 개인화 섹션 포함 | 추천, 또래 여행 등 개인화 섹션 호출 |

## 전환 순서

클라이언트 영향을 줄이기 위해 두 단계로 진행합니다.

1. 홈 API 구조 분리
   - `GET /api/v1/home`의 책임을 홈 초기 구성 정보 중심으로 축소합니다.
   - 섹션별 API 계약을 정의합니다.
   - Swagger 문서와 테스트를 먼저 맞춥니다.

2. 섹션별 실제 조회 로직 고도화
   - 예정 여행, 추천, 같은 여행지/또래 여행을 실제 데이터 기반으로 조회합니다.
   - 인기 여행지처럼 집계성 데이터는 캐시와 갱신 시각을 검토합니다.
   - 섹션별 성능과 인덱스 필요 여부를 확인합니다.

## 대안

| 대안 | 기각/보류 이유 |
|---|---|
| 기존처럼 `GET /api/v1/home`에서 모든 데이터를 계속 반환 | 특정 섹션 실패가 홈 전체 실패로 이어질 수 있고, 섹션별 갱신 주기와 로딩 상태를 분리하기 어려움 |
| `GET /api/v1/home`은 유지하고 내부에서 섹션별 실패를 감싸서 내려줌 | 프론트 호출 수는 줄지만 홈 API가 계속 여러 도메인의 orchestration 책임을 갖게 됨 |
| 모든 섹션을 완전히 독립 API로만 제공하고 초기 홈 API 제거 | 사용자 상태에 따른 홈 구성 판단이 프론트에 흩어질 수 있음 |

## 결과

좋은 점:

- 홈 화면의 부분 실패 대응이 쉬워집니다.
- 섹션별 skeleton, empty, retry UI를 자연스럽게 만들 수 있습니다.
- 조회 비용이 큰 추천/집계 API를 다른 섹션과 분리해서 운영할 수 있습니다.
- 백엔드는 섹션 단위로 쿼리, 캐시, 성능 개선을 진행할 수 있습니다.

감수할 점:

- 프론트에서 API 호출 수가 늘어납니다.
- 프론트 상태 관리가 홈 전체 단일 상태에서 섹션별 상태로 바뀝니다.
- 초기 전환 시 기존 `GET /api/v1/home` 응답에 의존하던 화면 코드를 조정해야 합니다.

이 트레이드오프는 홈 화면 안정성을 위해 감수할 만합니다.
홈은 여러 성격의 데이터를 모아 보여주는 화면이므로, 하나의 API 성공 여부에 전체 화면을 묶는 것보다 섹션 단위로 점진적으로 렌더링하는 편이 사용자 경험과 장애 격리에 유리합니다.

## 관련 문서

- [#282 [TASK] 홈 API 섹션 단위 분리](https://github.com/ddu-ru/ddu-ru-backend/issues/282)
- [#283 [REFACTOR] 홈 API 섹션 단위 응답 구조 분리](https://github.com/ddu-ru/ddu-ru-backend/issues/283)
- [#284 [FEAT] 홈 섹션 실제 조회 로직 및 캐시 고도화](https://github.com/ddu-ru/ddu-ru-backend/issues/284)
- [#285 [TASK] 홈 API 섹션 분리 ADR 문서화](https://github.com/ddu-ru/ddu-ru-backend/issues/285)
