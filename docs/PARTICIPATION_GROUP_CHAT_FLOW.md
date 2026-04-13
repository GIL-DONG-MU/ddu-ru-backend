# Participation / Group Chat Final Design

## 1. 목적

이 문서는 게시글 동행 신청, 1:1 연락, 그룹 채팅방 초대, 승인/거절/취소/퇴장 흐름을 하나의 기준으로 고정하기 위한 설계 문서다.

핵심 목표는 아래 3가지다.

- `participation`과 `groupchatroom`의 책임을 분리하되 흐름은 일관되게 유지한다.
- 방장 화면의 상태값을 단순하게 유지한다.
- 정원 판단과 실제 그룹방 참여 인원을 분리해서 관리한다.

## 2. 확정 정책

- 게시글 신청 상태는 `PENDING`, `CONTACTING`, `APPROVED`, `REJECTED`만 사용한다.
- 게시글 신청 취소 시 `Participation` row를 삭제한다.
- 승인 후 사용자가 그룹방을 나가면 `Participation` row도 삭제한다.
- 방장이 승인된 사용자를 그룹방에서 내보내면 `Participation` row도 삭제한다.
- 신청이 `REJECTED` 되면 row는 유지되며 재신청할 수 없다.
- 모집 가능 여부는 `Post.recruitCount < recruitCapacity` 기준으로 판단한다.
- 그룹방 현재 인원은 `ChatRoomMember` 기준으로 계산한다.
- 게시글이 마감되거나 삭제되어도 그룹 채팅방은 삭제하지 않는다.
- 정원이 가득 차면 신규 신청, 연락 시작, 승인이 모두 불가하다.

## 3. 용어

- 신청 상태: 방장 신청자 목록에서 보이는 상태
- 정원: 방장을 포함한 현재 수락 인원 수
- 그룹방 인원: 실제 그룹 채팅방에 남아 있는 현재 참여자 수
- 1:1 채팅: 방장과 신청자 사이의 개별 연락용 채팅방

## 4. 도메인 모델

### 4.1 Post

- 모집 상태를 관리한다.
- 정원 값 `recruitCapacity`를 가진다.
- 모집 가능 여부는 `recruitCount`와 `recruitCapacity`로 판단한다.

권장 상태:

- `OPEN`
- `CLOSED`

### 4.2 Participation

현재 유효한 신청만 보관한다.

상태:

- `PENDING`: 신청 완료, 아직 방장 액션 없음
- `CONTACTING`: 방장이 1:1 채팅으로 연락 시작
- `APPROVED`: 수락 완료, 그룹방 자동 초대 완료
- `REJECTED`: 거절됨, 재신청 불가

row 삭제 이벤트:

- 신청자 취소
- 승인 후 신청자의 그룹방 자진 이탈
- 승인 후 방장의 그룹방 내보내기

### 4.3 Group Chat Room

- 게시글당 그룹방 1개를 가진다.
- 게시글 삭제/마감과 분리된 생명주기를 가진다.
- 현재 그룹방 인원은 `ChatRoomMember` row로만 판단한다.

## 5. 최종 사용자 흐름

### 5.1 신청

1. 사용자가 게시글을 본다.
2. 동행 신청을 한다.
3. `Participation` row가 생성되고 상태는 `PENDING`이다.

### 5.2 방장 검토

방장은 신청자를 보고 아래 액션을 할 수 있다.

- `거절하기`
- `채팅하기`
- `수락하기`

아무 액션도 하지 않으면 신청은 `PENDING` 상태로 남는다.

### 5.3 거절

1. 방장이 `거절하기`를 누른다.
2. 상태가 `PENDING` 또는 `CONTACTING`에서 `REJECTED`로 바뀐다.
3. 거절 목록으로 이동한다.
4. 해당 사용자는 같은 게시글에 재신청할 수 없다.

### 5.4 채팅하기

1. 방장이 `채팅하기`를 누른다.
2. 방장과 신청자 사이의 1:1 채팅방을 생성하거나 기존 방을 재사용한다.
3. 상태가 `PENDING`이면 `CONTACTING`으로 바뀐다.
4. 이미 `CONTACTING`이면 상태를 유지하고 기존 채팅방을 재사용한다.
5. `APPROVED`, `REJECTED` 상태에서는 호출할 수 없다.

### 5.5 수락

1. 방장이 수락을 결정한다.
2. 상태가 `PENDING` 또는 `CONTACTING`에서 `APPROVED`로 바뀐다.
3. 그룹 채팅방에 자동 초대된다. 이미 그룹방 멤버면 기존 방 정보를 반환한다.
4. 수락함 목록으로 이동한다.

수락 가능 조건:

- 게시글 상태가 `OPEN`
- 현재 `recruitCount < recruitCapacity`

### 5.6 신청 취소

1. 신청자가 신청을 취소한다.
2. `Participation` row를 삭제한다.
3. 방장 목록에서는 더 이상 보이지 않는다.

권장 허용 범위:

- `PENDING`
- `CONTACTING`

`APPROVED` 이후에는 단순 신청 취소가 아니라 그룹방 나가기 흐름으로 처리한다.

### 5.7 그룹방 자진 이탈

1. 승인된 사용자가 그룹방을 나간다.
2. 해당 사용자의 그룹방 멤버 row를 삭제한다.
3. 해당 사용자의 `Participation` row도 삭제한다.
4. 결과적으로 승인 정원 1칸이 비게 된다.

### 5.8 방장이 승인자 내보내기

1. 방장이 승인된 사용자를 그룹방에서 내보낸다.
2. 해당 사용자의 그룹방 멤버 row를 삭제한다.
3. 해당 사용자의 `Participation` row도 삭제한다.
4. 결과적으로 승인 정원 1칸이 비게 된다.

### 5.9 정원 가득 참

- 정원이 가득 차면 신규 신청 생성이 불가하다.
- 이 상태에서는 `채팅하기`와 `수락하기`도 모두 불가하다.
- 승인자가 나가거나 모집 정원을 늘려 `recruitCount < recruitCapacity`가 되면, `OPEN` 게시글은 별도 상태 변경 없이 다시 신청/처리가 가능하다.

### 5.10 게시글 마감/삭제

- 신규 신청은 불가하다.
- 신규 연락 시작도 불가하다.
- 신규 승인도 불가하다.
- 기존 그룹 채팅방은 유지된다.
- 기존 1:1 채팅방도 유지된다.

## 6. 상태 전이표

| 현재 상태 | 액션 | 다음 상태 | 비고 |
| --- | --- | --- | --- |
| 없음 | 신청 | `PENDING` | 새 row 생성 |
| `PENDING` | 채팅하기 | `CONTACTING` | 1:1 채팅방 생성/재사용 |
| `CONTACTING` | 채팅하기 | `CONTACTING` | 기존 1:1 채팅방 재사용 |
| `PENDING` | 거절하기 | `REJECTED` | row 유지 |
| `PENDING` | 수락 | `APPROVED` | 그룹방 자동 초대 |
| `CONTACTING` | 수락 | `APPROVED` | 그룹방 자동 초대 |
| `CONTACTING` | 거절하기 | `REJECTED` | row 유지 |
| `PENDING` | 신청자 취소 | 삭제 | row 삭제 |
| `CONTACTING` | 신청자 취소 | 삭제 | row 삭제 |
| `APPROVED` | 신청자 그룹방 나가기 | 삭제 | row 삭제 + 그룹방 멤버 삭제 |
| `APPROVED` | 방장 내보내기 | 삭제 | row 삭제 + 그룹방 멤버 삭제 |

## 7. 정원 규칙

모집 가능 여부 계산 기준:

- `Post.recruitCount < recruitCapacity`
- `recruitCount`는 게시글 작성자 1명을 포함한 현재 승인 인원 수로 관리한다.

그룹방 현재 인원 계산 기준:

- `ChatRoomMember` count
- 그룹 채팅 멤버 초대 시 `ChatRoomMember` count 기준으로 한 번 더 정원을 검증한다.

이 둘은 별도 값이다.

현재 구현에서는 승인 시 `Post.approveParticipation()`이 `recruitCount`를 증가시킨다.

## 8. DB 설계

### 8.1 posts

권장 컬럼:

- `id`
- `user_id`
- `recruit_capacity`
- `status` (`OPEN`, `CLOSED`)
- `is_deleted`
- `deleted_at`
- `deleted_by`

권장 사항:

- `FULL` 상태는 사용하지 않는다.
- 모집 가득 참 여부는 파생값으로 계산한다.

### 8.2 participations

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
- `updated_at`

권장 제약:

- `UNIQUE(post_id, user_id)`

이 제약을 유지하는 이유:

- `REJECTED` row가 남아 있으면 재신청을 막을 수 있다.
- 신청 취소/자진 이탈/방장 내보내기는 row 삭제이므로 다시 신청 가능하다.

권장 인덱스:

- `(post_id, status, created_at desc)`
- `(post_id, user_id)`

### 8.3 chat_rooms

권장 규칙:

- 그룹방은 게시글당 1개
- 1:1 채팅방은 게시글 단위 + 사용자 쌍 기준 재사용
- 그룹방 정원은 `recruit_capacity`와 동일하게 둔다. `recruit_capacity` 자체가 이미 방장을 포함한 총원이다.

### 8.4 chat_room_members

권장 규칙:

- 현재 실제 방 참여자만 저장한다.
- 나가기/내보내기 시 row 삭제

## 9. 서비스 책임

### 9.1 ParticipationApplicantService

책임:

- 신청 생성
- 신청 중복 검증
- 자기 게시글 신청 금지 검증
- 게시글 모집 가능 여부 검증
- 내가 보낸 신청 내역 조회(채팅방 ID 포함)
- 대기 중 신청 취소

### 9.2 ParticipationCommandService

책임:

- 받은 신청 목록 조회
- 1:1 채팅 시작과 상태 전환
- 승인과 그룹방 초대 오케스트레이션

### 9.3 PrivateChatRoomService / GroupChatRoomService

책임:

- 1:1 채팅방 생성/재사용
- 그룹방 멤버 초대
- 그룹방 정원 검증
- 게시글 생성 시 그룹방 생성

### 9.4 현재 오케스트레이션 원칙

현재 구현에서 같은 유스케이스로 묶여 처리되는 케이스는 아래와 같다.

- 연락 시작: 1:1 채팅방 생성/재사용 + 필요 시 `CONTACTING` 전환
- 승인: 그룹방 초대 + `Participation APPROVED` + `recruitCount` 증가

후속 설계 메모:

- 그룹방 자진 이탈 시 그룹방 멤버 삭제 + `Participation` 삭제
- 그룹방 내보내기 시 그룹방 멤버 삭제 + `Participation` 삭제

## 10. 예외 규칙

- `REJECTED` 상태면 재신청 불가
- 그룹방을 나가거나 내보내진 경우는 row 삭제이므로 재신청 가능
- 게시글 `CLOSED` 상태면 신청 생성, 연락 시작, 승인 불가
- 정원이 가득 찬 경우 신청 생성, 연락 시작, 승인 불가
- `APPROVED`, `REJECTED` 상태에서는 `채팅하기`, `수락하기` 불가
- `approve`는 `PENDING`, `CONTACTING` 상태에서만 가능
- 이미 `REJECTED`인 신청은 추가 거절 불필요
