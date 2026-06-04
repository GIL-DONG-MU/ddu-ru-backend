# 003. 나의 여정 구현 현황

나의 여정 기능의 개발 진행 상태, 도메인 구조, 설계 포인트를 정리한 문서다.
기획/제품 흐름은 [나의 여정 제품 흐름](./flows/001-my-journey-product-flow.md)을 참고한다.

---

## 1. 도메인 구분

백엔드 관점에서 나의 여정은 아래 테이블 구조로 표현된다.

| 테이블 | 역할 |
|--------|------|
| `posts` | 여행/모집글 루트 |
| `participations` | 신청 과정 관리 (`PENDING / CONTACTING / APPROVED / REJECTED`) |
| `journey_members` | 실제 협업 멤버 관리 (`HOST / MEMBER`, `ACTIVE / REMOVED / LEFT`) |
| `journey_posts` | 여행 게시판 게시글 |
| `journey_post_comments` | 게시글 댓글 |
| `journey_schedules` | 여행 일정 아이템 |

나의 여정 관련 권한과 목록 조회는 `journey_members`를 중심으로 판단한다.

---

## 2. 개발 순서 및 진행 상태

### 1단계. 정책 확정 ✅ 완료

- 호스트 전용 권한과 공통 협업 권한 경계 확정
- `participations`와 `journey_members`의 역할 경계 확정

### 2단계. 공통 상세 응답 ✅ 완료

상세 화면 렌더링에 필요한 핵심값 확정

- `isOwner` — 내가 호스트인지
- `myParticipationStatus` — 내 참여 상태
- `canEditPost` — 모집글 원본 수정 가능 여부
- `groupRoomId` — 그룹 채팅방 ID

### 3단계. 메인/상세 진입 ✅ 완료

- 나의 여정 메인 목록 (`journey_members` 기준 조회)
- 나의 여정 상세 조회

### 4단계. 협업 도메인 순차 개발 🔄 진행 중

- 여행 게시판 → ✅ 완료
- 댓글 → ✅ 완료
- 공지 지정/해제 → ✅ 완료
- 일정 → ✅ 완료
- 할 일 → 🔜 구현예정
- 여행 정보 → 🔜 구현예정
- 투표 → 🔜 구현예정

### 5단계. 호스트 전용 관리 기능 🔄 진행 중

- 게시글 공지 설정/고정 → ✅ 완료
- 참여자 내보내기 → ✅ 완료
- 투표 확정 → 🔜 구현예정
- 여행 종료 → 🔜 구현예정

---

## 3. API 구현 현황

### 나의 여정 (워크스페이스 진입)

| 기능 | 메서드 | 엔드포인트 | 상태 |
|------|--------|-----------|------|
| 메인 목록 조회 | `GET` | `/api/v1/users/me/journeys` | ✅ 구현 |
| 상세 조회 | `GET` | `/api/v1/journeys/{journeyId}` | ✅ 구현 |
| 기본 정보 수정 (제목·대표 사진) | `PATCH` | `/api/v1/journeys/{journeyId}` | ✅ 구현 |
| 참여자 내보내기 | `DELETE` | `/api/v1/journeys/{journeyId}/members/{memberUserId}` | ✅ 구현 |
| 여행 종료 | - | - | 🔜 구현예정 |

### 여행 게시판

| 기능 | 메서드 | 엔드포인트 | 상태 |
|------|--------|-----------|------|
| 게시글 목록 조회 (커서 기반, 공지 우선) | `GET` | `/api/v1/journeys/{journeyId}/posts` | ✅ 구현 |
| 게시글 단건 조회 | `GET` | `/api/v1/journeys/{journeyId}/posts/{journeyPostId}` | ✅ 구현 |
| 게시글 작성 | `POST` | `/api/v1/journeys/{journeyId}/posts` | ✅ 구현 |
| 게시글 수정 | `PATCH` | `/api/v1/journeys/{journeyId}/posts/{journeyPostId}` | ✅ 구현 |
| 게시글 삭제 | `DELETE` | `/api/v1/journeys/{journeyId}/posts/{journeyPostId}` | ✅ 구현 |
| 공지 지정/해제 (호스트 전용, 최대 3개) | `PATCH` | `/api/v1/journeys/{journeyId}/posts/{journeyPostId}/notice` | ✅ 구현 |

### 댓글

| 기능 | 메서드 | 엔드포인트 | 상태 |
|------|--------|-----------|------|
| 댓글 목록 조회 (전체) | `GET` | `/api/v1/journeys/{journeyId}/posts/{journeyPostId}/comments` | ✅ 구현 |
| 댓글 작성 | `POST` | `/api/v1/journeys/{journeyId}/posts/{journeyPostId}/comments` | ✅ 구현 |
| 댓글 수정 | `PATCH` | `/api/v1/journeys/{journeyId}/posts/{journeyPostId}/comments/{commentId}` | ✅ 구현 |
| 댓글 삭제 (작성자 또는 호스트) | `DELETE` | `/api/v1/journeys/{journeyId}/posts/{journeyPostId}/comments/{commentId}` | ✅ 구현 |

### 일정

| 기능 | 메서드 | 엔드포인트 | 상태 |
|------|--------|-----------|------|
| 일정 목록 조회 (Day 그룹) | `GET` | `/api/v1/journeys/{journeyId}/schedules` | ✅ 구현 |
| 일정 생성 | `POST` | `/api/v1/journeys/{journeyId}/schedules` | ✅ 구현 |
| 일정 수정 | `PATCH` | `/api/v1/journeys/{journeyId}/schedules/{scheduleId}` | ✅ 구현 |
| 일정 삭제 | `DELETE` | `/api/v1/journeys/{journeyId}/schedules/{scheduleId}` | ✅ 구현 |

### 할 일

| 기능 | 메서드 | 엔드포인트 | 상태 |
|------|--------|-----------|------|
| 할 일 목록 조회 | `GET` | `/api/v1/journeys/{journeyId}/todos` | 🔜 구현예정 |
| 할 일 추가 | `POST` | `/api/v1/journeys/{journeyId}/todos` | 🔜 구현예정 |
| 할 일 상태 변경/수정 | `PATCH` | `/api/v1/journeys/{journeyId}/todos/{todoId}` | 🔜 구현예정 |
| 할 일 삭제 | `DELETE` | `/api/v1/journeys/{journeyId}/todos/{todoId}` | 🔜 구현예정 |

### 여행 정보

| 기능 | 메서드 | 엔드포인트 | 상태 |
|------|--------|-----------|------|
| 여행 정보 조회 | `GET` | `/api/v1/journeys/{journeyId}/info` | 🔜 구현예정 |
| 여행 정보 추가/수정 | `PATCH` | `/api/v1/journeys/{journeyId}/info` | 🔜 구현예정 |

### 투표

| 기능 | 메서드 | 엔드포인트 | 상태 |
|------|--------|-----------|------|
| 투표 목록 조회 | `GET` | `/api/v1/journeys/{journeyId}/polls` | 🔜 구현예정 |
| 투표 생성 | `POST` | `/api/v1/journeys/{journeyId}/polls` | 🔜 구현예정 |
| 투표 참여 | `POST` | `/api/v1/journeys/{journeyId}/polls/{pollId}/votes` | 🔜 구현예정 |
| 투표 확정 (호스트 전용) | `PATCH` | `/api/v1/journeys/{journeyId}/polls/{pollId}/finalize` | 🔜 구현예정 |

---

## 4. 도메인별 구현 상태

| 도메인 | 엔티티 | DB 마이그레이션 | 서비스 | 상태 |
|--------|--------|----------------|--------|------|
| 여행 워크스페이스 | `Journey` | V14 | `JourneyQueryService`, `JourneyService` | ✅ 구현 |
| 여행 멤버십 | `JourneyMember` | V13 | `JourneyService` | ✅ 구현 |
| 여행 게시판 | `JourneyPost` | V18 | `JourneyPostService` | ✅ 구현 |
| 댓글 | `JourneyPostComment` | V19 | `JourneyPostCommentService` | ✅ 구현 |
| 일정 | `JourneySchedule` | V20 | `JourneyScheduleService` | ✅ 구현 |
| 할 일 | - | - | - | 🔜 구현예정 |
| 여행 정보 | - | - | - | 🔜 구현예정 |
| 투표 | - | - | - | 🔜 구현예정 |

---

## 5. 주요 설계 포인트

- **접근 제어**: 모든 나의 여정 API는 `journey_members.status = ACTIVE` 여부를 확인해 비멤버 접근을 차단한다.
- **공지 최대 3개 제한**: 공지 지정 시 비관적 락(`SELECT FOR UPDATE`)으로 직렬화해 동시성을 제어한다.
- **커서 기반 페이지네이션**: 게시글 목록은 공지 우선 → 최신순 정렬을 DB `ORDER BY`로 보장하며, look-ahead 방식으로 `hasNext`를 판단한다.
- **댓글 전체 로드**: 댓글 목록은 게시글 상세 진입 시 전체를 한 번에 반환한다. 여정 멤버는 소규모 그룹으로 댓글 수가 구조적으로 제한되므로 페이지네이션 없이 전체 조회한다.
- **댓글 수 N+1 방지**: 게시글 목록 조회 시 `GROUP BY` 벌크 쿼리로 댓글 수를 한 번에 집계한다.
- **소프트 삭제**: 게시글(`JourneyPost`), 댓글(`JourneyPostComment`), 일정(`JourneySchedule`) 모두 `isDeleted / deletedAt / deletedBy` 패턴을 사용한다.
- **댓글 삭제 권한**: 댓글은 작성자 본인 또는 호스트가 삭제할 수 있다. 수정은 작성자 본인만 가능하다.
- **content 검증 위치**: 댓글 content의 유효성(빈 값, 300자 초과)은 `JourneyPostComment` 도메인 내부에서 검증한다. 서비스는 trim만 수행한다.
- **호스트 표시**: 게시글·댓글 응답에서 작성자가 호스트인지 여부를 `isHost` 필드로 함께 내려준다.
- **그룹 채팅 연동**: 나의 여정 상세 응답에 `groupRoomId`를 포함해 프론트가 바로 채팅방으로 이동할 수 있게 한다.
- **일정 상대 일차(dayOffset)**: 일정은 절대 날짜 대신 여행 시작일 기준 상대 일차(`dayOffset: 0, 1, 2...`)로 저장한다. 여행 날짜가 변경되어도 일정 데이터가 그대로 보존된다. 응답에서 실제 날짜는 `startDate.plusDays(dayOffset)`으로 계산해 내려준다.
- **일정 Day 그룹핑**: 일정 목록은 `dayOffset` 기준으로 그룹핑해 Day 단위로 반환한다. 일정이 없는 날도 빈 Day 구조를 유지한다. `day` 번호는 `dayOffset + 1`이며 클라이언트는 보내지 않는다.
- **일정 시간 제약**: 시작 시간(`startTime`)과 종료 시간(`endTime`) 모두 선택값이다. 종료 시간은 시작 시간이 있을 때만 허용하며, 이 제약은 엔티티 생성/수정 시 도메인 내부에서 검증한다.
- **일정 수정·삭제 권한**: 일정은 작성자 여부와 관계없이 `ACTIVE` 멤버 전체가 수정·삭제할 수 있다. 협업 일정 관리 특성상 소유권 제한을 두지 않는다.
