# 003. Chat Message Retrieve API Design

## 1. API 개요

채팅방 메시지 목록을 조회하는 API다.

채팅방은 1:1 채팅방과 그룹 채팅방이 있지만, 메시지 조회는 같은 화면 구조와 같은 페이징 정책을 사용하므로 통합 API로 제공한다.

- 1:1 채팅방: 모집글(`Post`)을 보고 작성자와 상대 사용자가 동행 여부를 조율하는 채팅방
- 그룹 채팅방: 신청 승인 후 정식 여행 동행 멤버가 참여하는 `Journey` 기준 채팅방
- 메시지 타입: `TEXT`, `IMAGE`, `SYSTEM`
- 최초 진입: 최신 메시지 20개 조회
- 추가 조회: 위로 스크롤 시 `beforeMessageId` 기반 커서 조회
- 읽음 수: 응답 필드는 제공하되, 읽음 처리 구현 전까지 `null`
- 날짜 구분선: 서버 응답에 포함하지 않고 클라이언트가 `createdAt` 기준으로 계산
- 사진 메시지 UI 규격: Figma 기준 `220 x 180`, radius `16`, cover crop

시스템 메시지는 서버에서 `displayText`까지 만들어 내려준다. 클라이언트는 시스템 메시지 문구를 조립하지 않고 그대로 노출한다.

## 2. API 명세

### Endpoint

```http
GET /api/v1/chat-rooms/{chatRoomId}/messages
```

현재 채팅 REST API 규모에서는 `ChatController`에 메시지 조회 엔드포인트를 함께 두고, 비즈니스 로직은 `ChatMessageQueryService`로 분리한다.

### Path Variable

| 이름 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `chatRoomId` | `Long` | Y | 조회할 채팅방 ID |

### Query Parameter

| 이름 | 타입 | 필수 | 기본값 | 설명 |
| --- | --- | --- | --- | --- |
| `beforeMessageId` | `Long` | N | `null` | 해당 메시지보다 오래된 메시지를 조회한다. 없으면 최신 메시지부터 조회한다. |
| `size` | `Integer` | N | `20` | 조회할 메시지 개수. 최대 `50`을 권장한다. |

### Response

```json
{
  "status": 200,
  "data": {
    "roomInfo": {
      "chatRoomId": 1,
      "roomType": "GROUP",
      "isActive": true,
      "postTitle": null,
      "opponentNickname": null,
      "journeyTitle": "제주 애월 2박 3일",
      "memberCount": 3
    },
    "page": {
      "size": 20,
      "hasNext": true,
      "nextCursor": 120
    },
    "messages": [
      {
        "messageId": 121,
        "messageType": "TEXT",
        "sender": {
          "userId": 10,
          "nickname": "username",
          "isHost": false
        },
        "isMine": false,
        "content": "시간 다르면 다르게 뜨도록",
        "images": [],
        "systemMessage": null,
        "unreadCount": null,
        "createdAt": "2026-05-09T09:41:00"
      }
    ]
  }
}
```

응답 정렬은 클라이언트 렌더링 편의를 위해 오래된 메시지에서 최신 메시지 순서로 반환한다.

## 3. 요청/응답 예시

### 최초 조회

```http
GET /api/v1/chat-rooms/1/messages?size=20
Authorization: Bearer {accessToken}
```

### 과거 메시지 조회

```http
GET /api/v1/chat-rooms/1/messages?beforeMessageId=121&size=20
Authorization: Bearer {accessToken}
```

### 1:1 채팅방 응답 예시

```json
{
  "status": 200,
  "data": {
    "roomInfo": {
      "chatRoomId": 10,
      "roomType": "PRIVATE",
      "isActive": true,
      "postTitle": "코끼리 아저씨",
      "opponentNickname": "username",
      "journeyTitle": null,
      "memberCount": null
    },
    "page": {
      "size": 20,
      "hasNext": false,
      "nextCursor": null
    },
    "messages": [
      {
        "messageId": 501,
        "messageType": "TEXT",
        "sender": {
          "userId": 2,
          "nickname": "username",
          "isHost": false
        },
        "isMine": false,
        "content": "head는 이렇게",
        "images": [],
        "systemMessage": null,
        "unreadCount": null,
        "createdAt": "2026-05-09T09:41:00"
      },
      {
        "messageId": 502,
        "messageType": "IMAGE",
        "sender": {
          "userId": 1,
          "nickname": "host",
          "isHost": true
        },
        "isMine": true,
        "content": null,
        "images": [
          {
            "imageUrl": "https://cdn.example.com/chats/image.jpg"
          }
        ],
        "systemMessage": null,
        "unreadCount": null,
        "createdAt": "2026-05-09T09:42:00"
      }
    ]
  }
}
```

사진 메시지의 API 응답은 MVP에서는 이미지 URL만 내려준다. 화면 렌더링 규격은 Figma 노드 기준으로 클라이언트가 적용한다.

- Figma: [Chat node 4828:51049](https://www.figma.com/design/1QO9ghu1skndgCLLoy88ni/Ddu-ru?node-id=4828-51049&m=dev)
- 이미지 메시지 영역: `220 x 180`
- border radius: `16`
- content mode: `object-cover` / center crop
- 한 메시지당 이미지는 우선 1장만 지원
- 이미지 아래 텍스트 말풍선이 이어질 경우, Figma 기준 이미지와 다음 말풍선 간격은 `4px`

### 그룹 채팅방 응답 예시

```json
{
  "status": 200,
  "data": {
    "roomInfo": {
      "chatRoomId": 20,
      "roomType": "GROUP",
      "isActive": false,
      "postTitle": null,
      "opponentNickname": null,
      "journeyTitle": "제주 애월 2박 3일",
      "memberCount": 3
    },
    "page": {
      "size": 20,
      "hasNext": true,
      "nextCursor": 441
    },
    "messages": [
      {
        "messageId": 441,
        "messageType": "SYSTEM",
        "sender": null,
        "isMine": false,
        "content": null,
        "images": [],
        "systemMessage": {
          "type": "USER_INVITED",
          "displayText": "username 님이 그룹 채팅방에 참여했습니다.",
          "actorUserId": 1,
          "inviteeUserId": 2,
          "userId": null,
          "targetUserId": null
        },
        "unreadCount": null,
        "createdAt": "2026-05-09T09:40:00"
      }
    ]
  }
}
```

현재 코드에는 `ChatSystemMessageType.USER_INVITED`가 존재한다. 제품 문구는 초대보다 참여에 가깝기 때문에, enum은 유지하더라도 조회 응답의 `displayText`는 "참여했습니다"로 내려준다. enum 이름까지 바꾸려면 기존 저장 데이터와 직렬화 호환성을 함께 고려해야 한다.

## 4. 처리 흐름

1. 인증된 사용자 ID를 `@CurrentUser`로 받는다.
2. `chatRoomId`로 `ChatRoom`을 조회한다.
3. 방이 없거나 `DELETED` 상태면 `CHAT_ROOM_NOT_FOUND`를 반환한다.
4. 현재 사용자의 `ChatRoomMember`를 조회한다.
5. 멤버 row가 없으면 `CHAT_ACCESS_DENIED`를 반환한다.
6. 그룹 채팅방이면 `JourneyMember`가 `ACTIVE` 상태인지 추가 확인한다.
7. 메시지 조회 시작 시점을 결정한다.
   - 기본 기준: `ChatRoomMember.createdAt`
   - 그룹 채팅방 신규 승인자는 승인 후 생성된 멤버 row의 `createdAt` 이후 메시지만 조회 가능하다.
   - 그룹 채팅방을 나갔다가 재참여하면 새 멤버 row의 `createdAt` 이후 메시지만 조회 가능하다.
8. `beforeMessageId`가 있으면 해당 ID보다 작은 메시지만 조회한다.
9. DB에서는 `id DESC`로 `size + 1`개를 조회한다.
10. `size + 1`개가 조회되면 `hasNext = true`로 판단하고 마지막 1개는 응답에서 제외한다.
11. 응답 메시지는 `id ASC`로 뒤집어 반환한다.
12. 메시지 sender, 이미지 첨부, 시스템 메시지 `displayText`를 조립한다.
13. `roomInfo`, `page`, `messages`를 `ApiResult.ok(data)`로 반환한다.

`CLOSED` 방은 조회 가능해야 하므로, 메시지 조회용 멤버 검증에서는 `room.status = ACTIVE` 조건이 붙은 기존 `existsByChatRoom_IdAndUser_Id` 메서드를 재사용하지 않는다. 해당 메서드는 메시지 전송 검증에 더 적합하다.

## 5. 검증 조건

### Query 검증

- `size`가 없으면 `20`
- `size < 1`이면 `INVALID_INPUT_VALUE`
- `size > 50`이면 `INVALID_INPUT_VALUE` 또는 서버에서 `50`으로 보정
- `beforeMessageId <= 0`이면 `INVALID_INPUT_VALUE`

권장 방식은 잘못된 입력을 조용히 보정하지 않고 `400`을 반환하는 것이다.

### 권한 검증

- 1:1 채팅방
  - 현재 사용자가 해당 `ChatRoomMember`에 존재해야 한다.
  - 1:1 방은 `Post` 컨텍스트를 가진다.
  - `roomInfo.opponentNickname`은 현재 사용자가 아닌 다른 멤버의 닉네임이다.
- 그룹 채팅방
  - 현재 사용자가 해당 `ChatRoomMember`에 존재해야 한다.
  - 현재 사용자가 해당 `Journey`의 `ACTIVE` 멤버여야 한다.
  - 승인 이전 메시지는 조회할 수 없다.
  - 재참여 이전 메시지는 조회할 수 없다.

### 메시지 타입 검증

- `TEXT`
  - `content`에 텍스트를 내려준다.
  - `images`는 빈 배열이다.
- `IMAGE`
  - 현재는 메시지당 이미지 1장만 지원한다.
  - 현재 DB 구조에서는 `ChatMessage.content`에 이미지 URL이 저장된다.
  - 조회 응답에서는 `content = null`, `images[0].imageUrl = content`로 매핑한다.
  - 이미지 실제 원본 크기는 응답하지 않는다.
  - 클라이언트는 Figma 기준 `220 x 180`, radius `16`, cover crop으로 렌더링한다.
- `SYSTEM`
  - `sender = null`
  - `content = null`
  - `systemMessage.type`과 `systemMessage.displayText`를 내려준다.
  - 사용자가 직접 전송할 수 없다.

## 6. 예외 처리

기존 프로젝트의 에러 포맷을 따른다.

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
| `400` | `INVALID_INPUT_VALUE` | `size`, `beforeMessageId`가 유효하지 않음 |
| `401` | `UNAUTHORIZED` | 인증되지 않은 사용자 |
| `403` | `CHAT_ACCESS_DENIED` | 채팅방 멤버가 아니거나, 그룹 여정 멤버가 아님 |
| `404` | `CHAT_ROOM_NOT_FOUND` | 채팅방이 없거나 `DELETED` 상태 |
| `404` | `CHAT_MESSAGE_NOT_FOUND` | 커서 메시지 존재 여부를 엄격히 검증하는 경우 |
| `500` | `JSON_CONVERT_ERROR` | 시스템 메시지 JSON 역직렬화 실패 |

`beforeMessageId`가 현재 채팅방에 속하지 않는 경우는 두 가지 정책 중 하나를 선택할 수 있다.

- 권장: `CHAT_MESSAGE_NOT_FOUND` 반환
- 단순 구현: 해당 ID보다 작은 메시지를 조회하되, 다른 방 메시지 ID여도 숫자 커서로만 취급

채팅 데이터 노출 가능성을 줄이려면 커서 메시지가 현재 방에 속하는지 검증하는 방식을 권장한다.

## 7. DB 및 도메인 설계

### 기존 테이블 활용

현재 주요 테이블:

- `chat_rooms`
- `chat_room_members`
- `chat_messages`
- `journeys`
- `journey_members`
- `posts`
- `users`
- `profiles`

### ChatRoom

```text
id
post_id nullable
journey_id nullable
room_type: PRIVATE | GROUP
status: ACTIVE | CLOSED | DELETED
created_at
modified_at
```

정책:

- `PRIVATE`: `post_id` 필수, `journey_id` 없음
- `GROUP`: `journey_id` 필수, `post_id` 없음
- `ACTIVE`: 조회 가능, 전송 가능
- `CLOSED`: 조회 가능, 전송 불가
- `DELETED`: 조회 불가

### ChatRoomMember

```text
id
room_id
user_id
role: HOST | GUEST
last_read_message_id nullable
created_at
modified_at
```

현재 설계는 "row 존재 = 현재 채팅방 멤버" 정책이다.

- 나가기/내보내기 시 row 삭제
- 그룹방 재참여 시 row 재생성
- 재생성된 row의 `created_at`을 메시지 조회 시작 시점으로 사용

1:1 채팅방은 나간 뒤 다시 대화를 시작할 때 새 방을 생성하는 정책이 현재 구조와 가장 단순하게 맞는다. 기존 방에 한 명만 남아 있는 상태에서 같은 방을 재사용하려면 과거 참여 이력 또는 별도 상태 컬럼이 필요하다. 이번 메시지 조회 API에서는 "현재 멤버인 방만 조회 가능"으로 제한한다.

### ChatMessage

```text
id
room_id
sender_id nullable
message_type: TEXT | IMAGE | SYSTEM
content
created_at
modified_at
```

정책:

- `TEXT`: `content`는 메시지 본문
- `IMAGE`: `content`는 이미지 URL
- `SYSTEM`: `content`는 `ChatSystemMessagePayload` JSON

이미지 메시지의 표시 규격은 DB에 저장하지 않는다. 현재는 업로드된 S3 URL만 저장하고, 채팅 화면에서 고정 규격으로 렌더링한다.

추후 원본 비율 유지, 썸네일 분리, 이미지 메타데이터 기반 placeholder가 필요해지면 별도 첨부 테이블을 추가한다.

```text
chat_message_attachments
id
message_id
url
thumbnail_url nullable
width nullable
height nullable
mime_type nullable
size_bytes nullable
created_at
```

### 시스템 메시지 타입

채팅 타임라인에 노출하는 시스템 메시지는 아래만 사용한다.

| type | 의미 | displayText 예시 |
| --- | --- | --- |
| `USER_INVITED` | 그룹 채팅방 참여 | `username 님이 그룹 채팅방에 참여했습니다.` |
| `USER_LEFT` | 멤버 퇴장 | `username 님이 채팅방을 나갔습니다.` |
| `USER_KICKED` | 멤버 내보내기 | `username 님이 채팅방에서 내보내졌습니다.` |
| `ROOM_CLOSED` | 방 닫힘 | `채팅방이 닫혔습니다.` |

알림성 문구인 "새 메시지가 도착했습니다", "읽지 않은 메시지가 있습니다"는 `ChatMessage.SYSTEM`으로 저장하지 않고 알림 도메인에서 관리한다.

### 권장 인덱스

현재 `idx_chat_messages_room_id`, `idx_chat_messages_room_created`가 있다. 커서 조회는 `room_id + id` 기준으로 수행하는 편이 단순하므로 아래 인덱스를 추가한다.

```sql
CREATE INDEX idx_chat_messages_room_id_id
    ON chat_messages (room_id, id);
```

## 8. 트랜잭션 설계

메시지 조회 API는 읽기 전용 트랜잭션으로 처리한다.

```java
@Transactional(readOnly = true)
public ChatMessagesResponse retrieveMessages(Long userId, Long roomId, ChatMessageRetrieveRequest request)
```

조회 API에서는 아래 작업을 하지 않는다.

- `last_read_message_id` 갱신
- 메시지 전송
- 시스템 메시지 생성

읽음 위치 갱신은 별도 읽음 처리 API에서 수행한다.

읽음 처리 API:

```http
PATCH /api/v1/chat-rooms/{chatRoomId}/read
```

## 9. 보안 고려사항

- `chatRoomId`는 추측 가능한 숫자 ID이므로 반드시 서버에서 멤버십을 검증한다.
- 그룹 채팅방은 `ChatRoomMember`뿐 아니라 `JourneyMember ACTIVE` 여부도 검증한다.
- 나간 사용자는 `ChatRoomMember` row가 없으므로 조회할 수 없다.
- `CLOSED` 방은 기존 멤버만 조회 가능하고 메시지 전송은 불가하다.
- `DELETED` 방은 존재하지 않는 방처럼 처리한다.
- 시스템 메시지 JSON 역직렬화 실패 시 원본 JSON을 응답하지 않는다.
- 이미지 메시지는 S3 업로드 경로 검증을 전송 시점에 끝내고, 조회 시에는 저장된 URL만 내려준다.

## 10. 성능 고려사항

- offset 페이징은 사용하지 않는다.
- `beforeMessageId` 커서 기반으로 `room_id`, `id` 조건을 사용한다.
- `size + 1`개를 조회해 `hasNext`를 계산한다.
- 메시지 sender 프로필 조회에서 N+1이 발생하지 않도록 fetch join 또는 batch 조회를 사용한다.
- 시스템 메시지 `displayText` 생성을 위해 필요한 userId들을 모아 한 번에 조회한다.
- `memberCount`는 그룹방에서 `journey_members.status = ACTIVE` 기준 count를 사용한다.
- 1:1 `opponentNickname`은 현재 방 멤버 2명을 한 번에 조회해서 계산한다.

권장 조회 쿼리 개념:

```sql
SELECT *
FROM chat_messages
WHERE room_id = :roomId
  AND id < COALESCE(:beforeMessageId, 9223372036854775807)
  AND created_at >= :visibleFrom
ORDER BY id DESC
LIMIT :sizePlusOne;
```

응답 직전 애플리케이션에서 `id ASC`로 정렬한다.

## 11. 테스트 케이스

### 성공 케이스

- 최초 조회 시 최신 20개를 오래된 순으로 반환한다.
- `beforeMessageId`가 있으면 해당 메시지보다 오래된 메시지만 반환한다.
- 메시지가 `size`보다 많으면 `hasNext = true`, `nextCursor`를 반환한다.
- 메시지가 `size` 이하이면 `hasNext = false`, `nextCursor = null`을 반환한다.
- 1:1 방 응답에 `postTitle`, `opponentNickname`, `roomType`, `isActive`가 포함된다.
- 그룹 방 응답에 `journeyTitle`, `memberCount`, `roomType`, `isActive`가 포함된다.
- `TEXT` 메시지는 `content`에 본문을 내려준다.
- `IMAGE` 메시지는 `images[0].imageUrl`에 이미지 URL을 내려준다.
- `SYSTEM` 메시지는 `systemMessage.displayText`를 내려준다.
- `CLOSED` 방은 기존 멤버가 메시지를 조회할 수 있다.
- 그룹 신규 승인자는 참여 시점 이후 메시지만 조회할 수 있다.
- 그룹 재참여자는 재참여 시점 이후 메시지만 조회할 수 있다.

### 실패 케이스

- 인증되지 않은 사용자는 `UNAUTHORIZED`를 반환한다.
- 존재하지 않는 채팅방은 `CHAT_ROOM_NOT_FOUND`를 반환한다.
- `DELETED` 채팅방은 `CHAT_ROOM_NOT_FOUND`를 반환한다.
- 채팅방 멤버가 아닌 사용자는 `CHAT_ACCESS_DENIED`를 반환한다.
- 그룹 채팅방 멤버 row가 있어도 `JourneyMember`가 `ACTIVE`가 아니면 `CHAT_ACCESS_DENIED`를 반환한다.
- `size < 1`이면 `INVALID_INPUT_VALUE`를 반환한다.
- `size > 50`이면 `INVALID_INPUT_VALUE`를 반환한다.
- `beforeMessageId <= 0`이면 `INVALID_INPUT_VALUE`를 반환한다.
- 커서 메시지 검증 정책을 사용할 경우, 다른 방의 `beforeMessageId`는 `CHAT_MESSAGE_NOT_FOUND`를 반환한다.

## 12. 구현 참고

구현 클래스, 저장소 메서드, 테스트 기준은 [채팅 구현 문서](../005-implementation/006-chat.md)를 참고한다.
