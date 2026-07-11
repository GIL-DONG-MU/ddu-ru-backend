# 006. 채팅 구현 문서

> 채팅방, 메시지 조회, 읽음 처리, 실시간 목록 갱신, FCM 푸시의 구현 기준을 정리합니다.
> API 계약과 클라이언트 호출 순서는 [채팅 API 클라이언트 구현 가이드](../004-api/001-chat-api-client-guide.md)를 참고합니다.

---

## 1. 현재 구현 범위

| 영역 | 구현 |
|---|---|
| 1:1 채팅방 | `PrivateChatRoomService`, 게시글 기준 방 생성/재사용 |
| 그룹 채팅방 | `GroupChatRoomService`, 여정 기준 방 생성/초대 |
| 메시지 전송 | `ChatMessageSendService`, STOMP `/app/chat/rooms/{roomId}/messages` |
| 메시지 조회 | `ChatMessageQueryService`, `GET /api/v1/chat-rooms/{chatRoomId}/messages` |
| 읽음 처리 | `ChatReadService`, `PATCH /api/v1/chat-rooms/{chatRoomId}/read` |
| 채팅방 목록 | `ChatRoomListService`, `GET /api/v1/chat-rooms` |
| 목록 실시간 갱신 | `ChatRoomListRealtimePublisher`, `/user/queue/chat-room-list` |
| 채팅 메시지 푸시 | `ChatPushNotificationService`, FCM 전용 발송 |

---

## 2. 메시지 조회

메시지 조회는 읽기 전용 트랜잭션에서 처리합니다.

- `ChatRoom`을 조회하고 `DELETED` 상태를 차단합니다.
- 현재 사용자의 `ChatRoomMember`를 확인합니다.
- 그룹방은 `JourneyMember ACTIVE` 여부도 확인합니다.
- `visibleFrom = chatRoomMember.createdAt` 이후 메시지만 조회합니다.
- `beforeMessageId`가 있으면 해당 메시지보다 오래된 메시지만 조회합니다.
- `size + 1`개를 조회해 `hasNext`와 `nextCursor`를 계산합니다.
- 응답 직전에는 오래된 메시지부터 최신 메시지 순서로 정렬합니다.
- `TEXT`, `IMAGE` 메시지는 unread count를 계산하고, `SYSTEM` 메시지는 `unreadCount = null`로 둡니다.

주요 클래스:

- `ChatMessageRetrieveRequest`
- `ChatMessagesResponse`
- `ChatRoomInfoResponse`
- `ChatMessagePageResponse`
- `ChatMessageItemResponse`
- `ChatMessageQueryService`
- `ChatMessageRepository.findVisibleMessages(...)`

DB 인덱스:

- `idx_chat_messages_room_id_id` — `(room_id, id)` 메시지 커서 조회용

---

## 3. 읽음 처리

읽음 처리는 쓰기 트랜잭션에서 처리합니다.

- 채팅방과 현재 사용자의 `ChatRoomMember`를 확인합니다.
- 그룹방은 `JourneyMember ACTIVE` 여부도 확인합니다.
- 요청한 `lastReadMessageId`가 해당 방 메시지인지 검증합니다.
- 현재 사용자의 참여 시점 이전 메시지는 읽음 처리할 수 없습니다.
- 기존 읽음 위치보다 과거 또는 같은 메시지를 요청하면 `updated = false`로 멱등 처리합니다.
- 더 최신 메시지를 요청한 경우 `ChatRoomMember.readUpTo(...)`로 읽음 위치를 갱신합니다.
- 읽음 위치가 갱신된 경우에만 커밋 후 `READ` 이벤트를 발행합니다.

READ 이벤트 destination:

```text
/topic/chat/rooms/{chatRoomId}
```

READ 이벤트 payload:

```json
{
  "eventType": "READ",
  "roomId": 1,
  "readerUserId": 10,
  "lastReadMessageId": 123,
  "readAt": "2026-05-09T12:30:00"
}
```

주요 클래스:

- `ChatReadRequest`
- `ChatReadResponse`
- `ChatReadEventPayload`
- `ChatReadService`
- `ChatRoomMember.readUpTo(...)`
- `ChatMessageRepository.findByIdAndRoomId(...)`

---

## 4. 메시지 전송자 자동 읽음

사용자가 메시지를 보내면 sender의 `last_read_message_id`를 새 메시지로 갱신합니다.
sender 자동 읽음 처리로 별도 `READ` 이벤트는 발행하지 않습니다.

---

## 5. 테스트 기준

- 메시지 최초 조회와 과거 메시지 조회의 정렬, cursor, `hasNext`를 검증합니다.
- 1:1/그룹 `roomInfo` 필드를 검증합니다.
- `TEXT`, `IMAGE`, `SYSTEM` 타입별 응답 필드를 검증합니다.
- 그룹 신규 승인자와 재참여자는 참여 시점 이후 메시지만 볼 수 있어야 합니다.
- 읽음 처리 성공, no-op, 다른 방 메시지, 참여 시점 이전 메시지, 비멤버 접근을 검증합니다.
- 1:1/그룹 unread count 계산에서 sender, 읽은 멤버, 신규 참여자, 나간 멤버 제외 규칙을 검증합니다.
- `CLOSED` 방은 조회/읽음 가능, 전송 불가 정책을 회귀 테스트로 유지합니다.
