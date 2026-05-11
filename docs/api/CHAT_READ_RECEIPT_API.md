# Chat Read Receipt API Design

## 1. API 개요

채팅방 메시지 읽음 위치를 갱신하고, 메시지별 `unreadCount`를 계산하기 위한 설계 문서다.

기본 정책:

- 읽음 위치는 `chat_room_members.last_read_message_id`로 관리한다.
- 읽음 처리는 "해당 메시지 ID 이하를 모두 읽음"으로 본다.
- 읽음 위치는 뒤로 가지 않는다.
- 채팅방 진입 시 클라이언트가 조회 응답의 마지막 `messageId`로 읽음 API를 호출한다.
- 채팅방을 보고 있는 중 새 메시지를 받으면 클라이언트가 해당 `messageId`로 읽음 API를 호출한다.
- 메시지를 보낸 사람은 본인이 보낸 메시지를 자동으로 읽은 것으로 처리한다.
- 읽음 위치가 실제로 앞으로 이동한 경우에만 WebSocket 읽음 이벤트를 발행한다.
- 채팅방 목록의 안 읽은 메시지 수는 이번 범위에서 제외한다.

## 2. API 명세

### Endpoint

```http
PATCH /api/v1/chat-rooms/{chatRoomId}/read
```

현재 채팅 REST API 규모에서는 기존 `ChatController`에 엔드포인트를 추가하고, 비즈니스 로직은 `ChatReadService`로 분리한다.

### Path Variable

| 이름 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `chatRoomId` | `Long` | Y | 읽음 처리할 채팅방 ID |

### Request Body

```json
{
  "lastReadMessageId": 123
}
```

| 이름 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `lastReadMessageId` | `Long` | Y | 사용자가 마지막으로 읽은 메시지 ID |

`lastReadMessageId`는 `TEXT`, `IMAGE`, `SYSTEM` 메시지 모두 허용한다. 이 값은 읽음 위치 커서이므로 메시지 타입과 무관하다.

### Response

```json
{
  "status": 200,
  "data": {
    "chatRoomId": 1,
    "lastReadMessageId": 123,
    "updated": true
  }
}
```

| 이름 | 타입 | 설명 |
| --- | --- | --- |
| `chatRoomId` | `Long` | 읽음 처리한 채팅방 ID |
| `lastReadMessageId` | `Long` | 요청 후 최종 반영된 읽음 메시지 ID |
| `updated` | `Boolean` | 읽음 위치가 실제로 앞으로 이동했는지 여부 |

기존 읽음 위치보다 과거 또는 같은 메시지를 요청하면 에러를 내지 않고 `updated = false`로 응답한다.

## 3. 요청/응답 예시

### 채팅방 진입 후 읽음 처리

1. 클라이언트가 메시지를 조회한다.
2. 응답 메시지 중 가장 마지막 메시지 ID가 `200`이다.
3. 렌더링 후 아래 API를 호출한다.

```http
PATCH /api/v1/chat-rooms/1/read
Authorization: Bearer {accessToken}
Content-Type: application/json
```

```json
{
  "lastReadMessageId": 200
}
```

```json
{
  "status": 200,
  "data": {
    "chatRoomId": 1,
    "lastReadMessageId": 200,
    "updated": true
  }
}
```

### No-op 응답

현재 사용자의 `last_read_message_id`가 이미 `200`인데 `150`을 요청한 경우:

```json
{
  "status": 200,
  "data": {
    "chatRoomId": 1,
    "lastReadMessageId": 200,
    "updated": false
  }
}
```

## 4. 처리 흐름

1. 인증된 사용자 ID를 `@CurrentUser`로 받는다.
2. `chatRoomId`로 `ChatRoom`을 조회한다.
3. 방이 없거나 `DELETED` 상태면 `CHAT_ROOM_NOT_FOUND`를 반환한다.
4. 현재 사용자의 `ChatRoomMember`를 조회한다.
5. 멤버 row가 없으면 `CHAT_ACCESS_DENIED`를 반환한다.
6. 그룹 채팅방이면 `JourneyMember`가 `ACTIVE` 상태인지 추가 확인한다.
7. `lastReadMessageId`에 해당하는 메시지를 조회한다.
8. 메시지가 해당 채팅방에 속하지 않으면 `CHAT_MESSAGE_NOT_FOUND`를 반환한다.
9. 메시지가 현재 사용자의 조회 가능 시점보다 이전이면 `CHAT_ACCESS_DENIED`를 반환한다.
10. 현재 `last_read_message_id`가 요청 메시지 ID 이상이면 갱신하지 않고 `updated = false`를 반환한다.
11. 요청 메시지가 더 최신이면 `chat_room_members.last_read_message_id`를 갱신한다.
12. 트랜잭션 커밋 후 기존 채팅방 topic으로 READ 이벤트를 발행한다.
13. `ApiResult.ok(response)`로 반환한다.

조회 가능 시점은 현재 `ChatRoomMember.createdAt` 기준이다. 그룹 채팅방 신규 참여자와 재참여자는 참여 이후 메시지만 읽음 처리할 수 있다.

## 5. 검증 조건

### Request 검증

- `lastReadMessageId`는 필수다.
- `lastReadMessageId`는 양수여야 한다.
- 요청 메시지는 해당 `chatRoomId`에 속해야 한다.
- 요청 메시지는 현재 사용자가 볼 수 있는 메시지여야 한다.

### 방 상태 검증

- `ACTIVE`: 읽음 처리 가능
- `CLOSED`: 읽음 처리 가능
- `DELETED`: 읽음 처리 불가, `CHAT_ROOM_NOT_FOUND`

`CLOSED` 방은 메시지 조회가 가능하므로 읽음 처리도 허용한다. 메시지 전송만 불가하다.

### 권한 검증

- 1:1 채팅방
  - 현재 사용자가 `ChatRoomMember`에 존재해야 한다.
- 그룹 채팅방
  - 현재 사용자가 `ChatRoomMember`에 존재해야 한다.
  - 현재 사용자가 해당 `Journey`의 `ACTIVE` 멤버여야 한다.
- 나간 사용자 또는 내보내진 사용자는 현재 멤버 row가 없으므로 읽음 처리할 수 없다.

### No-op 조건

아래 경우는 에러가 아니다.

- 현재 `last_read_message_id`와 같은 메시지를 요청
- 현재 `last_read_message_id`보다 과거 메시지를 요청

이 경우 읽음 위치는 변경하지 않고 `updated = false`로 응답한다. WebSocket READ 이벤트도 발행하지 않는다.

## 6. 예외 처리

기존 프로젝트의 `ErrorResponse` 포맷을 따른다.

```json
{
  "status": 403,
  "data": {
    "errorCode": "CHAT_ACCESS_DENIED",
    "field": null,
    "message": "채팅방 접근 권한이 없습니다."
  }
}
```

주요 에러:

| HTTP Status | ErrorCode | 발생 조건 |
| --- | --- | --- |
| `400` | `INVALID_INPUT_VALUE` | `lastReadMessageId`가 없거나 양수가 아님 |
| `401` | `UNAUTHORIZED` | 인증되지 않은 사용자 |
| `403` | `CHAT_ACCESS_DENIED` | 채팅방 멤버가 아니거나 볼 수 없는 메시지를 읽음 처리 요청 |
| `404` | `CHAT_ROOM_NOT_FOUND` | 채팅방이 없거나 `DELETED` 상태 |
| `404` | `CHAT_MESSAGE_NOT_FOUND` | 메시지가 없거나 해당 채팅방 메시지가 아님 |

`lastReadMessageId`가 현재 사용자의 `visibleFrom` 이전 메시지이면 `CHAT_ACCESS_DENIED`를 반환한다. 존재 여부보다 접근 가능 여부가 핵심이기 때문이다.

## 7. DB 및 도메인 설계

### ChatRoomMember

읽음 위치는 기존 컬럼을 사용한다.

```text
chat_room_members
id
room_id
user_id
role
last_read_message_id nullable
created_at
modified_at
```

권장 도메인 메서드:

```java
public boolean readUpTo(ChatMessage message) {
    if (lastReadMessage != null && lastReadMessage.getId() >= message.getId()) {
        return false;
    }
    this.lastReadMessage = message;
    return true;
}
```

`readUpTo`는 읽음 위치가 앞으로 이동했는지 여부를 반환한다.

### 메시지 전송자 자동 읽음 처리

메시지를 보낸 사람은 본인이 보낸 메시지를 자동으로 읽은 것으로 처리한다.

처리 위치:

- `ChatMessageSendService.saveUserMessageAndBroadcast`
- 메시지 저장 및 flush 후 sender의 `ChatRoomMember.lastReadMessage`를 방금 저장한 메시지로 갱신

정책:

- sender 자동 읽음 처리에는 별도 READ WebSocket 이벤트를 발행하지 않는다.
- 추후 채팅방 목록 unread count를 구현할 때 sender가 자기 메시지를 안 읽은 것으로 계산되는 문제를 방지한다.

### unreadCount 계산

`ChatMessageRetrieve API`의 메시지별 `unreadCount`는 읽음 처리 구현 후 아래 기준으로 계산한다.

대상 메시지:

- `TEXT`
- `IMAGE`

제외 메시지:

- `SYSTEM`: `unreadCount = null`

계산 대상 멤버:

- 현재 채팅방 멤버
- 메시지 sender 제외
- 메시지 생성 시점에 이미 참여 중인 멤버만 포함
- 나간 사람/내보내진 사람 제외

계산식:

```text
unreadCount =
  count(chat_room_members)
  where room_id = message.room_id
    and user_id != message.sender_id
    and created_at <= message.created_at
    and (
      last_read_message_id is null
      or last_read_message_id < message.id
    )
```

현재 설계는 "row 존재 = 현재 채팅방 멤버" 정책이므로, 나간 사람과 내보내진 사람은 자동으로 계산 대상에서 제외된다.

## 8. 트랜잭션 설계

읽음 처리 API는 쓰기 트랜잭션으로 처리한다.

```java
@Transactional
public ChatReadResponse read(Long userId, Long roomId, ChatReadRequest request)
```

트랜잭션 내부:

1. 채팅방 조회 및 상태 검증
2. 멤버십 검증
3. 메시지 조회 및 접근 가능 여부 검증
4. `last_read_message_id` 갱신

트랜잭션 커밋 후:

- `updated = true`인 경우에만 WebSocket READ 이벤트 발행
- 커밋 전 이벤트 발행 금지

기존 `ChatMessageSendService`가 메시지 브로드캐스트를 `afterCommit`으로 처리하고 있으므로, 읽음 이벤트도 같은 방식으로 맞춘다.

## 9. 보안 고려사항

- 숫자 ID 기반 API이므로 `chatRoomId`, `lastReadMessageId` 모두 서버에서 소유 관계를 검증한다.
- 다른 방의 메시지 ID로 읽음 처리할 수 없어야 한다.
- 현재 사용자가 볼 수 없는 과거 메시지로 읽음 처리할 수 없어야 한다.
- 그룹 채팅방은 `ChatRoomMember`뿐 아니라 `JourneyMember ACTIVE` 여부도 검증한다.
- `DELETED` 방은 존재하지 않는 것처럼 처리한다.
- `CLOSED` 방은 조회와 읽음 처리를 허용하되 전송은 금지한다.

## 10. 성능 고려사항

### 읽음 API

- 단일 `ChatRoomMember` row 업데이트만 수행한다.
- 같은 사용자가 같은 메시지로 여러 번 요청할 수 있으므로 no-op 경로가 가볍게 동작해야 한다.
- 읽음 위치 갱신은 id 비교로 판단한다.

### unreadCount 계산

메시지 목록 조회 시 각 메시지마다 멤버 수를 별도 쿼리로 계산하면 N+1 문제가 발생한다.

권장 방식:

- 조회된 메시지 목록의 최소/최대 ID를 기준으로 현재 멤버들의 `last_read_message_id`, `created_at`을 한 번에 조회한다.
- 애플리케이션 메모리에서 메시지별 `unreadCount`를 계산한다.
- 그룹 규모가 커지고 메시지 수가 많아지면 QueryDSL 집계 쿼리 또는 read model을 검토한다.

현재 예상 규모에서는 메시지 페이지 크기 최대 50개, 그룹 인원은 모집 정원 수준이므로 애플리케이션 계산이 단순하고 충분하다.

## 11. 테스트 케이스

### 읽음 API 성공 케이스

- 현재 멤버가 최신 메시지 ID로 읽음 처리하면 `last_read_message_id`가 갱신된다.
- 기존 읽음 위치보다 과거 메시지를 요청하면 `updated = false`를 반환한다.
- 기존 읽음 위치와 같은 메시지를 요청하면 `updated = false`를 반환한다.
- `SYSTEM` 메시지 ID로 읽음 처리를 요청해도 성공한다.
- `CLOSED` 방에서도 읽음 처리가 가능하다.
- `updated = true`인 경우 WebSocket READ 이벤트가 발행된다.
- `updated = false`인 경우 WebSocket READ 이벤트가 발행되지 않는다.

### 읽음 API 실패 케이스

- 인증되지 않은 사용자는 `UNAUTHORIZED`를 반환한다.
- 존재하지 않는 방은 `CHAT_ROOM_NOT_FOUND`를 반환한다.
- `DELETED` 방은 `CHAT_ROOM_NOT_FOUND`를 반환한다.
- 채팅방 멤버가 아닌 사용자는 `CHAT_ACCESS_DENIED`를 반환한다.
- 그룹방의 `JourneyMember`가 `ACTIVE`가 아니면 `CHAT_ACCESS_DENIED`를 반환한다.
- 다른 방 메시지 ID로 요청하면 `CHAT_MESSAGE_NOT_FOUND`를 반환한다.
- 현재 사용자의 참여 시점 이전 메시지로 요청하면 `CHAT_ACCESS_DENIED`를 반환한다.
- `lastReadMessageId`가 없거나 양수가 아니면 `INVALID_INPUT_VALUE`를 반환한다.

### unreadCount 계산 케이스

- 1:1에서 상대가 안 읽은 내 메시지는 `unreadCount = 1`이다.
- 1:1에서 상대가 읽은 내 메시지는 `unreadCount = 0`이다.
- 그룹 3명 중 sender를 제외한 2명 모두 안 읽으면 `unreadCount = 2`이다.
- 그룹 3명 중 sender를 제외한 2명 중 1명만 읽으면 `unreadCount = 1`이다.
- 그룹 신규 참여자는 참여 이전 메시지의 `unreadCount` 계산 대상이 아니다.
- 나간 멤버는 `unreadCount` 계산 대상이 아니다.
- `SYSTEM` 메시지는 `unreadCount = null`이다.

### 메시지 전송자 자동 읽음 케이스

- 사용자가 메시지를 보내면 sender의 `last_read_message_id`가 새 메시지로 갱신된다.
- sender 자동 읽음 처리로 별도 READ 이벤트는 발행되지 않는다.

## 12. 구현 체크리스트

### Controller / Docs

- `PATCH /api/v1/chat-rooms/{chatRoomId}/read` 추가
- `ChatApiDocs` 또는 별도 API docs에 Swagger 문서 추가
- `@ApiErrorResponses`에 `UNAUTHORIZED`, `INVALID_INPUT_VALUE`, `CHAT_ROOM_NOT_FOUND`, `CHAT_MESSAGE_NOT_FOUND`, `CHAT_ACCESS_DENIED` 반영

### DTO

- `ChatReadRequest`
  - `lastReadMessageId`
- `ChatReadResponse`
  - `chatRoomId`
  - `lastReadMessageId`
  - `updated`
- `ChatReadEventPayload`
  - `eventType`
  - `roomId`
  - `readerUserId`
  - `lastReadMessageId`
  - `readAt`

### Service

- `ChatReadService` 추가
- 채팅방 조회 및 `DELETED` 검증
- 현재 `ChatRoomMember` 조회
- 그룹방 `JourneyMember ACTIVE` 검증
- 요청 메시지 조회 및 room 검증
- `visibleFrom` 이전 메시지 접근 차단
- `last_read_message_id` 갱신
- `updated = true`일 때만 커밋 후 READ 이벤트 발행

### WebSocket

- 기존 destination 재사용

```text
/topic/chat/rooms/{chatRoomId}
```

- READ 이벤트 payload:

```json
{
  "eventType": "READ",
  "roomId": 1,
  "readerUserId": 10,
  "lastReadMessageId": 123,
  "readAt": "2026-05-09T12:30:00"
}
```

기존 메시지 브로드캐스트 payload에는 `eventType`이 없으므로, 클라이언트는 같은 topic에서 payload shape 또는 `eventType` 존재 여부로 READ 이벤트를 구분한다.

### Domain / Repository

- `ChatRoomMember.readUpTo(ChatMessage message)` 추가
- `ChatRoomMemberRepository`
  - `Optional<ChatRoomMember> findByRoomIdAndUserId(Long roomId, Long userId)`
  - 메시지 조회 unreadCount 계산용 room members 조회 메서드
- `ChatMessageRepository`
  - `Optional<ChatMessage> findByIdAndRoomId(Long messageId, Long roomId)`
- `JourneyMemberRepository`
  - 기존 `existsByJourneyIdAndUserIdAndStatus(...)` 활용

### Message Send 연동

- `ChatMessageSendService`에서 user message 저장 후 sender 자동 읽음 처리 추가
- sender 자동 읽음은 READ 이벤트를 발행하지 않음

### Message Retrieve 연동

- `ChatMessageRetrieve API`의 `unreadCount = null` 임시 정책 제거
- `TEXT`, `IMAGE` 메시지에 `unreadCount` 계산 반영
- `SYSTEM` 메시지는 `unreadCount = null` 유지

### Tests

- `ChatReadServiceTest`
- `ChatReadControllerTest` 또는 통합 테스트
- WebSocket READ 이벤트 발행 테스트
- sender 자동 읽음 처리 테스트
- 메시지 조회 `unreadCount` 계산 테스트

