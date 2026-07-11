# 004. 참여 신청 / 그룹 채팅 구현 문서

> 참여 신청 상태 전이, 락 순서, 저장 모델, 서비스 책임을 정리합니다.
> 제품 흐름은 [참여 신청 흐름](../001-policy/flows/002-participation-flow.md)과 [동행 신청/그룹 채팅 흐름](../001-policy/flows/003-participation-group-chat-flow.md)을 참고합니다.

---

## 1. 락과 동시성

기존 참여 신청 row를 변경하는 흐름에서는 데드락 방지를 위해 동일 트랜잭션 내에서 아래 순서로 락을 잡습니다.

```text
Participation FOR UPDATE -> Post FOR UPDATE
```

신청 생성은 아직 `Participation` row가 없으므로 예외적으로 `Post FOR UPDATE`를 먼저 잡고, 검증 후 `Participation` row를 생성합니다.

`participations(post_id, user_id)` UNIQUE 제약으로 DB 수준에서 중복 신청을 막습니다.
동시성 충돌(`DataIntegrityViolationException`) 발생 시 중복 신청 예외로 변환합니다.

---

## 2. 주요 처리 순서

### 동행 신청

1. `Post`를 `FOR UPDATE`로 잠급니다.
2. 신청 가능 조건을 검증합니다.
3. `Participation` row를 `PENDING` 상태로 생성합니다.
4. 동시성 충돌 시 중복 신청 예외를 반환합니다.

### 연락 시작

1. `Participation -> Post` 순서로 락을 획득합니다.
2. 1:1 채팅방을 생성하거나 기존 방을 재사용합니다.
3. 신청 상태를 `CONTACTING`으로 변경합니다.

### 수락

1. `Participation -> Post` 순서로 락을 획득합니다.
2. `Post.approveParticipation()`을 호출합니다.
3. 내부에서 `participation.approve()`로 상태를 `APPROVED`로 전환하고 `recruitCount`를 증가시킵니다.
4. `JourneyMember`를 생성하거나 활성화합니다.
5. 그룹 채팅방에 자동 초대합니다.

### 거절

- `Participation.status = REJECTED`로 변경합니다.
- `participations` row는 유지해 재신청을 막습니다.

---

## 3. 응답 상태 계산

신청자 화면에 내려가는 상태는 DB 상태를 그대로 쓰지 않고 파생 계산합니다.

```text
participations.status == APPROVED
    AND journey_members.status == REMOVED
    -> REMOVED_BY_HOST

그 외
    -> participations.status 그대로 사용
```

호스트(`isOwner == true`) 또는 비로그인(`currentUserId == null`)이면 `NONE`을 반환합니다.

### 내 신청 내역 조회

신청한 모든 모집글의 신청 상태와 채팅방 이동 정보를 함께 반환합니다.

| 신청 상태 | 응답 필드 |
| --- | --- |
| `PENDING` | 채팅방 ID 없음, `cancellable=true` |
| `CONTACTING` | `privateRoomId` |
| `APPROVED` + `journey_members.status = ACTIVE` | `groupRoomId` |
| `APPROVED` + `journey_members.status = REMOVED` | 채팅방 ID 없음, 상태는 `REMOVED_BY_HOST`로 계산 |
| `REJECTED` | 채팅방 ID 없음 |

---

## 4. 저장 모델

### posts

권장 컬럼:

- `id`
- `user_id`
- `recruit_capacity`
- `status`
- `is_deleted`
- `deleted_at`
- `deleted_by`

권장 사항:

- `FULL` 상태는 사용하지 않습니다.
- 모집 가득 참 여부는 파생값으로 계산합니다.

### participations

권장 컬럼:

- `id`
- `post_id`
- `user_id`
- `status`
- `message`
- `contacted_at`
- `approved_at`
- `rejected_at`
- `created_at`
- `modified_at`

권장 제약:

- `UNIQUE(post_id, user_id)`

이 제약을 유지하는 이유:

- `REJECTED` row가 남아 있으면 재신청을 막을 수 있습니다.
- 신청 취소는 row 삭제이므로 다시 신청할 수 있습니다.
- 방장 내보내기는 승인 이력을 유지하고 `journey_members.status = REMOVED`로 현재 멤버십만 차단합니다.

권장 인덱스:

- `(post_id, status, created_at desc)`
- `(post_id, user_id)`

### chat_rooms

권장 규칙:

- 1:1 채팅방은 `post_id`를 가집니다.
- 그룹방은 `journey_id`를 가집니다.
- `PRIVATE`: `post_id IS NOT NULL`, `journey_id IS NULL`
- `GROUP`: `post_id IS NULL`, `journey_id IS NOT NULL`
- 그룹방은 나의 여정당 1개이므로 `UNIQUE(journey_id)`를 둡니다.
- 1:1 채팅방은 게시글 단위 + 사용자 쌍 기준으로 재사용합니다.
- 그룹방 정원은 `recruit_capacity`와 동일하게 둡니다.

### chat_room_members

권장 규칙:

- 현재 실제 방 참여자만 저장합니다.
- 나가기/내보내기 시 row를 삭제합니다.

---

## 5. 서비스 책임

### ParticipationApplicantService

- 신청 생성
- 신청 중복 검증
- 자기 게시글 신청 금지 검증
- 게시글 모집 가능 여부 검증
- 내가 보낸 신청 내역 조회
- 대기 중 신청 취소

### ParticipationCommandService

- 받은 신청 목록 조회
- 1:1 채팅 시작과 상태 전환
- 승인과 그룹방 초대 오케스트레이션

### PrivateChatRoomService / GroupChatRoomService

- 1:1 채팅방 생성/재사용
- 그룹방 멤버 초대
- 그룹방 정원 검증
- 나의 여정 생성 시 그룹방 생성

---

## 6. 예외 규칙

- `REJECTED` 상태면 재신청할 수 없습니다.
- 방장에게 내보내진 경우는 승인 이력을 유지하되 나의 여정 접근과 그룹방 접근이 차단됩니다.
- 게시글 `CLOSED` 상태면 신청 생성, 연락 시작, 승인할 수 없습니다.
- 정원이 가득 찬 경우 신청 생성, 연락 시작, 승인할 수 없습니다.
- `APPROVED`, `REJECTED` 상태에서는 `채팅하기`, `수락하기`를 호출할 수 없습니다.
- `approve`는 `PENDING`, `CONTACTING` 상태에서만 가능합니다.
- 이미 `REJECTED`인 신청은 추가 거절이 필요 없습니다.
