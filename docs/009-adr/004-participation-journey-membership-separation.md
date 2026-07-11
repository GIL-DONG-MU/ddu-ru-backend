# ADR-004: 참여 신청과 나의 여정 멤버십 분리

- 상태: Proposed
- 날짜: TODO
- 결정자: TODO

> TODO: `journey_members` 분리를 결정한 제품/기술 논의, 기존 `participations`만으로 부족했던 사례를 추가합니다.

## 맥락

모집글 단계에서는 `participations`가 신청 상태를 관리합니다. 승인 이후에는 나의 여정 워크스페이스, 여정 게시판, 그룹 채팅처럼 실제 멤버 권한을 기준으로 하는 기능이 필요합니다.

## 결정

신청 이력은 `participations`에 유지하고, 승인 이후 실제 협업 멤버십은 `journey_members`로 분리합니다.

- `participations`: `PENDING`, `CONTACTING`, `APPROVED`, `REJECTED`
- `journey_members`: `HOST`, `MEMBER` role과 `ACTIVE`, `REMOVED`, `LEFT` status
- `journeys`: 공개 모집글 `posts`와 1:1 연결되는 워크스페이스 root

## 대안

| 대안 | 기각/보류 이유 |
|---|---|
| `participations`만으로 멤버십 판단 | 호스트 row가 없고, 승인 이후 제거/나가기/재참여 표현이 불명확 |
| `posts.user_id`와 승인 참여자 query 조합 | 매번 권한 계산이 복잡해지고 그룹 채팅/워크스페이스 확장에 불리 |
| 별도 journey 없이 post를 root로 유지 | 공개 모집글과 승인 후 협업 공간의 생명주기 분리가 어려움 |

## 결과

- 나의 여정 접근 권한을 `journey_members.status = ACTIVE`로 단순하게 판단할 수 있습니다.
- 호스트와 멤버를 같은 테이블에서 조회할 수 있습니다.
- 신청 이력과 실제 멤버십이 분리되므로 상태 동기화 테스트가 중요합니다.

## 관련 문서

- [도메인 명세](../003-design/001-domain.md)
- [나의 여정 제품 흐름](../002-policy/flows/004-my-journey-product-flow.md)
- [동행 신청/그룹 채팅 흐름](../002-policy/flows/003-participation-group-chat-flow.md)
