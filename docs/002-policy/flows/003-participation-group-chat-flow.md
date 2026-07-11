# 003. Participation / Group Chat Final Design

## 1. 목적

이 문서는 게시글 동행 신청, 1:1 연락, 그룹 채팅방 초대, 승인/거절/취소/퇴장 흐름을 하나의 기준으로 고정하기 위한 설계 문서다.

핵심 목표는 아래 3가지다.

- `participation`과 `groupchatroom`의 책임을 분리하되 흐름은 일관되게 유지한다.
- 방장 화면의 상태값을 단순하게 유지한다.
- 정원 판단과 실제 그룹방 참여 인원을 분리해서 관리한다.

## 2. 확정 정책

- 게시글 신청 상태는 `PENDING`, `CONTACTING`, `APPROVED`, `REJECTED`만 사용한다.
- 게시글 신청 취소 시 `Participation` row를 삭제한다.
- 승인 이후의 실제 여행 접근 권한은 `journey_members`로 판단한다.
- 방장이 승인된 사용자를 내보내면 `journey_members.status = REMOVED`로 변경하고 `Participation` row는 승인 이력으로 유지한다.
- 내보내진 사용자는 그룹 채팅방의 `ChatRoomMember` row를 삭제한다.
- 신청이 `REJECTED` 되면 row는 유지되며 재신청할 수 없다.
- 모집 가능 여부는 `Post.recruitCount < recruitCapacity` 기준으로 판단한다.
- 나의 여정 접근 권한은 `journey_members.status = ACTIVE` 기준으로 판단한다.
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

참여 신청과 승인 이력을 보관한다.

상태:

- `PENDING`: 신청 완료, 아직 방장 액션 없음
- `CONTACTING`: 방장이 1:1 채팅으로 연락 시작
- `APPROVED`: 수락 완료, 그룹방 자동 초대 완료
- `REJECTED`: 거절됨, 재신청 불가

row 삭제 이벤트:

- 신청자 취소

승인 이후 내보내기 여부는 `participations.status`가 아니라 `journey_members.status`로 표현한다.

### 4.3 Group Chat Room

- 나의 여정(`journey`)당 그룹방 1개를 가진다.
- 공개 모집글(`post`)이 아니라 참여 후 워크스페이스(`journey`)의 채팅방으로 본다.
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

허용 범위:

- `PENDING`

`APPROVED` 이후에는 단순 신청 취소가 아니라 그룹방 나가기 흐름으로 처리한다.

### 5.7 그룹방 자진 이탈

아직 별도 API와 정책을 확정하지 않은 흐름이다.

정책 후보:

1. 승인된 사용자가 나의 여정을 직접 나간다.
2. 해당 사용자의 `journey_members.status`를 `LEFT`로 변경한다.
3. 해당 사용자의 그룹방 멤버 row를 삭제한다.
4. `Participation` row는 승인 이력으로 유지할지 별도 확정한다.
5. `Post.recruitCount`를 1 감소시켜 승인 정원 1칸이 비게 한다.

### 5.8 방장이 승인자 내보내기

1. 방장이 승인된 사용자를 나의 여정에서 내보낸다.
2. 해당 사용자의 `journey_members.status`를 `REMOVED`로 변경한다.
3. 해당 사용자의 그룹방 멤버 row를 삭제한다.
4. 해당 사용자의 `Participation` row는 `APPROVED` 승인 이력으로 유지한다.
5. `Post.recruitCount`를 1 감소시켜 승인 정원 1칸이 비게 한다.
6. 내 신청 내역 응답에서는 `journey_members.status = REMOVED`를 기준으로 `REMOVED_BY_HOST`를 계산해서 내려준다.

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
| `APPROVED` | 신청자 그룹방 나가기 | 정책 확정 필요 | `journey_members.status = LEFT` 사용 후보 |
| `APPROVED` | 방장 내보내기 | `APPROVED` 유지 | `journey_members.status = REMOVED` + 그룹방 멤버 삭제 |

## 7. 정원 규칙

모집 가능 여부는 게시글 작성자를 포함한 승인 인원 수와 모집 정원을 기준으로 판단한다.
그룹 채팅방 입장 가능 여부는 실제 채팅방 참여 인원 수를 기준으로 한 번 더 판단한다.

이 둘은 별도 값이며, 승인 처리와 채팅방 입장이 서로 다른 시점에 일어날 수 있기 때문에 각각 검증한다.

## 8. 구현 참고

상태 변경과 채팅방 연결의 세부 처리 방식은 [참여 신청/그룹 채팅 구현 문서](../../005-implementation/004-participation.md)를 참고한다.
