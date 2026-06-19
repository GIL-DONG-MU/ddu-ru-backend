# 003. Chat Room List Realtime API Design

## 1. API 개요

채팅방 목록 화면은 최초 진입 시 REST API로 snapshot을 조회하고, 이후 변경분은 사용자 개인 WebSocket queue로 받는다.

기본 정책:

- 최초 목록은 `GET /api/v1/chat-rooms`로 조회한다.
- 실시간 변경은 `/user/queue/chat-room-list`를 구독한다.
- 서버는 사용자별로 완성된 채팅방 목록 item을 내려준다.
- 클라이언트는 `chatRoomId` 기준으로 item을 교체하거나 제거하고, `activityAt DESC`, `chatRoomId DESC`로 재정렬한다.
- `SYSTEM` 메시지는 채팅방 목록의 마지막 메시지, 안 읽은 메시지 수, 정렬 기준에 포함하지 않는다.
- WebSocket 재연결, 앱 foreground 복귀, 이벤트 누락 의심 시 REST 목록을 다시 조회해 동기화한다.

## 2. REST 목록 응답 변경

`GET /api/v1/chat-rooms`의 각 `chatRooms[]` item에 `activityAt`이 포함된다.

```json
{
  "chatRoomId": 10,
  "roomType": "PRIVATE",
  "status": "ACTIVE",
  "displayName": "guestNick",
  "postTitle": "제주 애월 2박 3일",
  "thumbnailUrl": "https://...",
  "postId": 100,
  "journeyId": null,
  "participantCount": 2,
  "lastMessage": {
    "messageId": 1234,
    "messageType": "TEXT",
    "content": "안녕하세요",
    "senderId": 20,
    "senderNickname": "guestNick",
    "createdAt": "2026-06-13T14:30:00"
  },
  "unreadCount": 3,
  "lastReadMessageId": 1200,
  "activityAt": "2026-06-13T14:30:00",
  "createdAt": "2026-06-01T10:00:00"
}
```

`activityAt`은 현재 사용자 기준 목록 정렬 시각이다. 사용자 입장 이후 최신 non-system 메시지 시각을 사용하고, 메시지가 없으면 현재 사용자의 채팅방 참여 시각을 사용한다.

## 3. WebSocket 명세

### 연결

기존 채팅 WebSocket endpoint를 사용한다.

```text
CONNECT /ws/chat
Authorization: Bearer {accessToken}
```

### 구독

```text
SUBSCRIBE /user/queue/chat-room-list
```

서버는 인증된 STOMP Principal의 userId 문자열을 기준으로 개인 queue에 발행한다.

```java
simpMessagingTemplate.convertAndSendToUser(
        String.valueOf(userId),
        "/queue/chat-room-list",
        payload
);
```

## 4. WebSocket 이벤트 payload

### UPSERT

목록에 채팅방 item을 삽입하거나 기존 item을 교체한다.

```json
{
  "eventType": "UPSERT",
  "reason": "MESSAGE_CREATED",
  "chatRoom": {
    "chatRoomId": 10,
    "roomType": "PRIVATE",
    "status": "ACTIVE",
    "displayName": "guestNick",
    "postTitle": "제주 애월 2박 3일",
    "thumbnailUrl": "https://...",
    "postId": 100,
    "journeyId": null,
    "participantCount": 2,
    "lastMessage": {
      "messageId": 1234,
      "messageType": "TEXT",
      "content": "안녕하세요",
      "senderId": 20,
      "senderNickname": "guestNick",
      "createdAt": "2026-06-13T14:30:00"
    },
    "unreadCount": 3,
    "lastReadMessageId": 1200,
    "activityAt": "2026-06-13T14:30:00",
    "createdAt": "2026-06-01T10:00:00"
  },
  "occurredAt": "2026-06-13T14:30:01"
}
```

### REMOVE

목록에서 채팅방 item을 제거한다. 제거 이벤트는 최소 식별 정보만 포함한다.

```json
{
  "eventType": "REMOVE",
  "reason": "MEMBER_CHANGED",
  "chatRoom": {
    "chatRoomId": 20,
    "roomType": "GROUP"
  },
  "occurredAt": "2026-06-13T14:30:01"
}
```

### reason

| 값 | 의미 |
| --- | --- |
| `MESSAGE_CREATED` | 새 사용자 메시지로 마지막 메시지, unread count, 정렬 기준이 바뀜 |
| `READ_UPDATED` | 내 읽음 위치 변경으로 unread count가 바뀜 |
| `ROOM_META_UPDATED` | 모집글, 여정, 프로필 수정으로 표시명, 제목, 썸네일이 바뀜 |
| `MEMBER_CHANGED` | 그룹 채팅방 멤버 변경으로 참여자 수 또는 목록 노출 여부가 바뀜 |

## 5. 클라이언트 처리 규칙

### UPSERT

1. 현재 필터의 `roomType`과 맞는지 확인한다.
2. 기존 목록에 같은 `chatRoomId`가 있으면 item 전체를 교체한다.
3. 없으면 새 item으로 삽입한다.
4. `activityAt DESC`, `chatRoomId DESC`로 재정렬한다.

### REMOVE

1. `chatRoom.chatRoomId`와 같은 item을 목록에서 제거한다.

### 정렬

서버는 이벤트마다 전체 목록을 다시 내려주지 않는다. 클라이언트는 `UPSERT` 처리 후 반드시 아래 기준으로 재정렬해야 한다.

```text
activityAt DESC, chatRoomId DESC
```

`SYSTEM` 메시지만 발생한 경우에는 `activityAt`이 바뀌지 않으므로 목록 순서가 유지될 수 있다. 단, 초대/내보내기 같은 멤버 변경은 `participantCount` 또는 `REMOVE` 이벤트로 반영된다.

## 6. 서버 처리 흐름

1. 채팅, 읽음, 게시글, 여정, 프로필 서비스가 도메인 변경 후 내부 이벤트를 발행한다.
2. `ChatRoomListRealtimeEventListener`가 `AFTER_COMMIT` 시점에 이벤트를 수신한다.
3. listener는 변경 영향 범위의 `roomId`, 대상 `userId`를 조회한다.
4. `ChatRoomListRealtimePublisher`가 대상 사용자별로 `ChatRoomListService.retrieveChatRoomItem(userId, roomId)`을 호출한다.
5. 사용자별 unread count, 표시명, 상대 프로필, 마지막 메시지, 정렬 기준을 재계산한다.
6. `/user/queue/chat-room-list`로 `UPSERT` 또는 `REMOVE`를 발행한다.

## 7. 이벤트 소스

| 내부 이벤트 | 발행 위치 | 목록 이벤트 |
| --- | --- | --- |
| `ChatMessageCreatedEvent` | 사용자 메시지 저장 성공 | 방 멤버 전체 `UPSERT(MESSAGE_CREATED)` |
| `ChatReadUpdatedEvent` | 읽음 위치가 실제 갱신됨 | 읽은 사용자 본인 `UPSERT(READ_UPDATED)` |
| `ChatMemberChangedEvent` | 그룹 초대/내보내기 | 남은 멤버 `UPSERT(MEMBER_CHANGED)`, 제거 사용자 `REMOVE(MEMBER_CHANGED)` |
| `PostUpdatedEvent` | 모집글 수정 성공 | 연결된 private/group 채팅방 멤버 `UPSERT(ROOM_META_UPDATED)` |
| `JourneyBasicInfoUpdatedEvent` | 여정 기본 정보 수정 성공 | 연결된 group 채팅방 멤버 `UPSERT(ROOM_META_UPDATED)` |
| `ProfileUpdatedEvent` | 프로필/아바타 수정 성공 | 해당 사용자가 상대방으로 보이는 private 채팅방 사용자 `UPSERT(ROOM_META_UPDATED)` |

## 8. 구현 클래스

- `ChatRoomListService`
  - REST 목록 조회와 실시간 이벤트용 단건 목록 item 조립을 담당한다.
- `ChatRoomListRealtimeEventListener`
  - 내부 도메인 이벤트를 after commit에 받아 목록 갱신 대상으로 변환한다.
- `ChatRoomListRealtimePublisher`
  - 사용자별 목록 item을 재계산해 개인 WebSocket queue로 발행한다.
- `ChatRoomListEventPayload`
  - 클라이언트가 수신하는 채팅방 목록 변경 payload다.

## 9. 테스트 범위

- 채팅방 목록 단건 item 조립
- 사용자별 WebSocket `UPSERT`/`REMOVE` payload 발행
- 메시지 전송, 읽음 처리, 멤버 변경, 게시글/여정/프로필 수정 이벤트 발행
- `activityAt` 포함 응답
- `SYSTEM` 메시지를 목록 마지막 메시지, unread count, 정렬 기준에서 제외하는 정책
