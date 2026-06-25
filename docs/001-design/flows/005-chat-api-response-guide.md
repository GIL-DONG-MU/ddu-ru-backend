# 005. Chat API Client Implementation Guide

> 모바일 클라이언트가 채팅 목록, 채팅 상세, 읽음 처리, 실시간 이벤트를 구현할 때 어떤 API와 필드를 사용해야 하는지 정리한다.

---

## 한 줄 요약

채팅 화면은 REST로 최초 snapshot을 받고, WebSocket 이벤트로 변경분을 반영한다. 렌더링은 `roomType`과 `messageType`을 먼저 보고 분기한다.

---

## 1. 모바일 화면별 API 요약

| 화면/상황 | 사용할 API 또는 WS | 클라이언트 처리 기준 |
|---|---|---|
| 채팅 탭 최초 진입 | `GET /api/v1/chat-rooms?roomType=ALL` | 전체 채팅방 목록 snapshot을 만든다. |
| 채팅방 타입 필터 | `GET /api/v1/chat-rooms?roomType=PRIVATE`, `GROUP` | 디자인의 `전체`, `1:1 채팅`, `여행방 채팅` 드롭다운에 사용한다. |
| 채팅 목록 실시간 갱신 | `SUBSCRIBE /user/queue/chat-room-list` | `UPSERT`는 `chatRoomId` 기준 교체/삽입, `REMOVE`는 제거한다. |
| 요청함 탭 | `GET /api/v1/participations` | 내가 작성한 게시글에 들어온 신청 목록을 조회한다. |
| 요청함 상태 필터 | `GET /api/v1/participations?status=PENDING` 등 | 특정 신청 상태만 보고 싶을 때 사용한다. |
| 신청내역 탭 | `GET /api/v1/users/me/participations` | 내가 보낸 동행 신청 목록을 조회한다. |
| 1:1 채팅방 열기 | `POST /api/v1/posts/{postId}/chats/private` 또는 `POST /api/v1/participations/{participationId}/contact` | 게시글 상세에서 직접 열거나, 방장이 신청자에게 연락을 시작할 때 사용한다. |
| 채팅방 진입 | `GET /api/v1/chat-rooms/{chatRoomId}/messages` | 헤더 정보와 첫 메시지 페이지를 함께 만든다. |
| 과거 메시지 로딩 | `GET /api/v1/chat-rooms/{chatRoomId}/messages?beforeMessageId={nextCursor}` | 위로 스크롤할 때 `page.nextCursor`를 전달한다. |
| 텍스트 메시지 전송 | `SEND /app/chat/rooms/{roomId}/messages` | `messageType=TEXT`, `content=본문`으로 보낸다. |
| 이미지 메시지 전송 | `POST /api/v1/images/chats/presigned-url` 후 `SEND /app/chat/rooms/{roomId}/messages` | S3 업로드 후 `fileUrl`을 `IMAGE` 메시지의 `content`로 보낸다. |
| 메시지/읽음 이벤트 수신 | `SUBSCRIBE /topic/chat/rooms/{roomId}` | 메시지 payload와 `READ` 이벤트가 같은 topic으로 온다. |
| 읽음 처리 | `PATCH /api/v1/chat-rooms/{chatRoomId}/read` | 화면에 노출된 마지막 메시지 ID를 `lastReadMessageId`로 보낸다. |
| 방장이 멤버 내보내기 | `DELETE /api/v1/journeys/{journeyId}/members/{memberUserId}` | 여정 멤버십과 그룹 채팅 멤버십이 함께 제거된다. |

## 2. 채팅 목록 화면 구현 기준

### 2.1 최초 로딩

1. STOMP 연결을 준비한다.
2. `GET /api/v1/chat-rooms`로 현재 목록 snapshot을 조회한다.
3. `/user/queue/chat-room-list`를 구독한다.
4. 이후 수신하는 목록 이벤트를 `chatRoomId` 기준으로 반영한다.
5. 앱 foreground 복귀, WebSocket 재연결, 이벤트 누락이 의심될 때는 REST 목록을 다시 조회해 동기화한다.

### 2.2 목록 필터

| UI 필터 | 요청 값 | 의미 |
|---|---|---|
| 전체 | `roomType=ALL` 또는 생략 | 모든 채팅방 |
| 1:1 채팅 | `roomType=PRIVATE` | 게시글 기준 1:1 채팅방 |
| 여행방 채팅 | `roomType=GROUP` | 여정 기준 그룹 채팅방 |

### 2.3 목록 item 표시 필드

| UI 요소 | 우선 사용 필드 | 타입별 기준 |
|---|---|---|
| 썸네일 | `thumbnailUrl` | `PRIVATE`은 상대 프로필, `GROUP`은 여정 이미지 또는 게시글 이미지 |
| 제목 | `displayName` | `PRIVATE`은 상대 닉네임, `GROUP`은 여정 제목 |
| 부제 | `postTitle` 또는 클라이언트 조합 | `PRIVATE`은 게시글 제목, `GROUP`은 여정/인원 정보 표시 가능 |
| 인원 수 | `participantCount` | 그룹방 목록에서 인원 아이콘 옆에 표시 |
| 마지막 메시지 | `lastMessage` | 없으면 빈 상태 문구 또는 생략 |
| 안 읽은 수 | `unreadCount` | 0이면 뱃지를 숨긴다. |
| 정렬 기준 | `activityAt`, `chatRoomId` | `activityAt DESC`, `chatRoomId DESC` |

`displayName`과 `thumbnailUrl`은 서버가 화면 표시용으로 조립한 값이다. 클라이언트는 목록에서 상대 유저나 여정 정보를 다시 조합하기보다 이 값을 우선 사용한다.

### 2.4 마지막 메시지 미리보기

| `lastMessage.messageType` | 미리보기 기준 |
|---|---|
| `TEXT` | `lastMessage.content`를 표시한다. |
| `IMAGE` | "사진" 같은 고정 문구를 표시하거나 이미지 아이콘을 사용한다. `content`는 이미지 URL이다. |
| `SYSTEM` | `content`를 그대로 노출하지 않는다. 시스템 payload 원문일 수 있다. |
| `lastMessage=null` | 아직 메시지가 없는 상태다. |

## 3. 채팅 상세 화면 구현 기준

### 3.1 진입 시 호출

`GET /api/v1/chat-rooms/{chatRoomId}/messages`

응답은 헤더용 `roomInfo`, 페이지용 `page`, 메시지 배열 `messages`를 함께 내려준다. `messages`는 오래된 순서로 반환된다.

### 3.2 헤더 표시

| `roomInfo.roomType` | 제목 | 부제/보조 정보 |
|---|---|---|
| `PRIVATE` | `roomInfo.opponentNickname` | `roomInfo.postTitle` |
| `GROUP` | `roomInfo.journeyTitle` | `roomInfo.memberCount` |

`roomInfo.isActive=false`이면 메시지 입력 UI를 비활성화하는 후보로 사용한다.

### 3.3 메시지 렌더링

| `messageType` | 사용 필드 | 정상 null/빈 값 |
|---|---|---|
| `TEXT` | `sender`, `isMine`, `content`, `unreadCount`, `createdAt` | `images=[]`, `systemMessage=null` |
| `IMAGE` | `sender`, `isMine`, `images[0].imageUrl`, `unreadCount`, `createdAt` | `content=null`, `systemMessage=null` |
| `SYSTEM` | `systemMessage.displayText`, `createdAt` | `sender=null`, `content=null`, `images=[]`, `unreadCount=null` |

`isMine=true`이면 내 말풍선, `false`이면 상대 말풍선으로 배치한다. `SYSTEM` 메시지는 `isMine=false`이며 일반 말풍선이 아니라 시스템 안내 형태로 표시한다.

### 3.4 읽음 표시

메시지별 `unreadCount`는 해당 메시지를 아직 읽지 않은 멤버 수다.

- 1:1에서 상대가 안 읽은 내 메시지는 `unreadCount=1`이다.
- 상대가 읽었으면 `unreadCount=0`이다.
- 그룹에서는 발신자를 제외한 안 읽은 멤버 수가 내려간다.
- 메시지 생성 이후 입장한 멤버는 과거 메시지의 `unreadCount` 계산 대상이 아니다.
- `SYSTEM` 메시지는 `unreadCount=null`이다.

### 3.5 이전 메시지와 실시간 메시지 누적

채팅방에 들어가면 REST로 이전 메시지 기록을 먼저 조회하고, 이후 같은 방 topic의 WebSocket 이벤트를 받아 화면에 이어 붙인다.

1. `GET /api/v1/chat-rooms/{chatRoomId}/messages`로 `roomInfo`, `messages`, `page.nextCursor`를 가져온다.
2. 응답의 `messages`는 오래된 순서로 화면에 렌더링한다.
3. `/topic/chat/rooms/{roomId}`를 구독한다.
4. `TEXT`, `IMAGE`, `SYSTEM` 이벤트가 오면 현재 메시지 목록의 마지막에 추가한다.
5. 위로 스크롤해 과거 메시지가 더 필요하면 `beforeMessageId=page.nextCursor`로 다음 페이지를 조회해 목록 앞쪽에 붙인다.
6. 화면에 노출된 마지막 메시지까지 확인한 시점에 읽음 처리 API를 호출한다.

즉, 채팅방 진입 시점의 기록은 REST 응답으로 확인하고, 진입 이후 새로 쌓이는 메시지는 WebSocket payload로 바로 확인하는 구조다. WebSocket 연결이 끊겼거나 이벤트 누락이 의심되면 REST 메시지 조회를 다시 호출해 서버 snapshot과 맞춘다.

## 4. 읽음 처리 기준

### 4.1 언제 호출할지

채팅방 상세에서 사용자가 메시지를 실제로 확인한 시점에 아래 API를 호출한다.

`PATCH /api/v1/chat-rooms/{chatRoomId}/read`

```json
{
  "lastReadMessageId": 123
}
```

`lastReadMessageId`는 화면에 노출된 마지막 메시지 ID를 사용한다. `TEXT`, `IMAGE`, `SYSTEM` 모두 읽음 위치 커서로 사용할 수 있다.

### 4.2 응답 처리

| 필드 | 의미 | 클라이언트 처리 |
|---|---|---|
| `chatRoomId` | 읽음 처리된 채팅방 ID | 현재 방과 일치하는지 확인한다. |
| `lastReadMessageId` | 서버에 반영된 마지막 읽음 메시지 ID | 로컬 읽음 위치를 갱신한다. |
| `updated` | 읽음 위치가 실제로 전진했는지 여부 | `false`면 추가 UI 변경 없이 무시 가능하다. |

읽음 위치가 전진한 경우 서버는 `/topic/chat/rooms/{roomId}`로 `READ` 이벤트를 발행한다.

## 5. WebSocket 처리 기준

### 5.1 연결과 destination

| 구분 | 경로 |
|---|---|
| WebSocket endpoint | `/ws/chat` |
| 메시지 전송 | `/app/chat/rooms/{roomId}/messages` |
| 채팅방 메시지/읽음 구독 | `/topic/chat/rooms/{roomId}` |
| 채팅방 목록 이벤트 구독 | `/user/queue/chat-room-list` |

STOMP `CONNECT`에는 `Authorization: Bearer {accessToken}` 헤더가 필요하다.

### 5.2 메시지 전송 payload

```json
{
  "messageType": "TEXT",
  "content": "안녕하세요!"
}
```

| `messageType` | `content` |
|---|---|
| `TEXT` | 텍스트 본문 |
| `IMAGE` | 채팅 이미지 업로드 후 받은 `fileUrl` |

`SYSTEM`은 서버에서만 생성하므로 클라이언트가 전송하지 않는다.

### 5.3 채팅방 topic payload

`/topic/chat/rooms/{roomId}`로 사용자 메시지, 시스템 메시지, 읽음 이벤트가 모두 올 수 있다.

| 구분 기준 | 처리 |
|---|---|
| `eventType=READ` | 읽음 이벤트로 처리한다. |
| `messageType=TEXT` | `userMessage.text`를 일반 메시지로 추가한다. |
| `messageType=IMAGE` | `userMessage.imageUrl`을 이미지 메시지로 추가한다. |
| `messageType=SYSTEM` | `systemMessage.type` 기준으로 시스템 안내를 추가한다. |

실시간 `SYSTEM` 메시지 payload에는 REST 메시지 조회의 `systemMessage.displayText`가 없다. 즉시 표시하려면 클라이언트가 보유한 사용자 캐시와 `systemMessage.type`으로 문구를 만들고, 정확한 문구가 필요하면 REST 메시지 재조회로 보정한다.

### 5.4 읽음 이벤트 payload

| 필드 | 의미 |
|---|---|
| `eventType` | 항상 `READ` |
| `roomId` | 채팅방 ID |
| `readerUserId` | 읽음 처리한 사용자 ID |
| `lastReadMessageId` | 해당 사용자가 읽은 마지막 메시지 ID |
| `readAt` | 읽음 처리 시각 |

클라이언트는 `readerUserId`의 읽음 위치를 `lastReadMessageId`까지 갱신하고, 화면에 보이는 메시지들의 읽음 수를 보정한다.

### 5.5 채팅방 목록 이벤트 payload

| 필드 | 의미 |
|---|---|
| `eventType` | `UPSERT` 또는 `REMOVE` |
| `reason` | 변경 원인 |
| `chatRoom` | 변경 대상 채팅방 |
| `occurredAt` | 이벤트 발생 시각 |

`UPSERT` 처리:

1. 현재 필터와 `chatRoom.roomType`이 맞는지 확인한다.
2. 같은 `chatRoomId`가 있으면 item 전체를 교체한다.
3. 없으면 새 item으로 삽입한다.
4. `activityAt DESC`, `chatRoomId DESC`로 재정렬한다.

`REMOVE` 처리:

1. 같은 `chatRoomId` item을 목록에서 제거한다.

`reason`은 UI 갱신 범위를 좁히기 위한 보조 정보다.

| 값 | 의미 |
|---|---|
| `MESSAGE_CREATED` | 새 메시지로 마지막 메시지, 안 읽은 수, 정렬 기준이 변경됨 |
| `READ_UPDATED` | 내 읽음 위치 변경으로 안 읽은 수가 변경됨 |
| `ROOM_META_UPDATED` | 게시글, 여정, 프로필 변경으로 제목/썸네일이 변경됨 |
| `MEMBER_CHANGED` | 그룹 멤버 변경으로 인원 수 또는 목록 노출 여부가 변경됨 |

## 6. 타입별 null 규칙

| 상황 | 정상 null/빈 값 |
|---|---|
| 채팅방 목록에 아직 메시지가 없음 | `lastMessage=null` |
| `PRIVATE` 채팅방 목록 | `journeyId=null` 가능 |
| `GROUP` 채팅방 목록 | `postTitle=null` |
| 1:1 메시지 헤더 | `journeyTitle=null`, `memberCount=null` |
| 그룹 메시지 헤더 | `postTitle=null`, `opponentNickname=null` |
| `TEXT` 메시지 | `images=[]`, `systemMessage=null` |
| `IMAGE` 메시지 | `content=null`, `systemMessage=null` |
| `SYSTEM` 메시지 | `sender=null`, `content=null`, `images=[]`, `unreadCount=null` |
| 아직 읽은 메시지가 없음 | `lastReadMessageId=null` |

## 7. enum 빠른 참조

### 7.1 ChatRoomType

| 값 | 의미 |
|---|---|
| `PRIVATE` | 게시글 기준 1:1 채팅방 |
| `GROUP` | 여정 기준 그룹 채팅방 |

### 7.2 ChatMessageType

| 값 | 의미 | 클라이언트 사용 필드 |
|---|---|---|
| `TEXT` | 텍스트 메시지 | `content`, `userMessage.text` |
| `IMAGE` | 이미지 메시지 | `images[0].imageUrl`, `userMessage.imageUrl` |
| `SYSTEM` | 서버 생성 시스템 메시지 | `systemMessage` |

### 7.3 ChatRoomListType

| 값 | 의미 |
|---|---|
| `ALL` | 모든 채팅방 |
| `PRIVATE` | 1:1 채팅방만 |
| `GROUP` | 그룹 채팅방만 |

### 7.4 ChatRoomStatus

| 값 | 의미 |
|---|---|
| `ACTIVE` | 사용 가능한 채팅방 |
| `CLOSED` | 닫힌 채팅방 |
| `DELETED` | 삭제된 채팅방 |

## 8. 현재 API로 부족한 부분

아래 항목은 현재 구현된 채팅 API만으로는 디자인 요구를 완전히 채우기 어렵다. 다만 모두 같은 우선순위는 아니므로, 채팅 클라이언트 구현에 바로 필요한 항목과 기획/타 도메인 작업으로 분리할 항목을 구분한다.

### 8.1 채팅 클라이언트 구현에서 우선 확인할 항목

| 기능 | 현재 상태 | 필요 API 후보 |
|---|---|---|
| 채팅 탭 상단 뱃지 최초 조회 | 방별 `unreadCount`, 요청함 목록, 신청내역 목록은 각각 조회 가능하지만 화면용 요약 API는 없음 | `GET /api/v1/users/me/chat-tab-summary` |
| 채팅 탭 상단 뱃지 실시간 갱신 | 채팅방 목록 실시간 queue는 있으나 탭 뱃지 전용 이벤트는 없음 | `SUBSCRIBE /user/queue/chat-tab-summary` |
| 요청함/신청내역 목록 실시간 갱신 | 현재는 REST 목록 조회만 지원하고 participation 변경 이벤트를 클라이언트에 직접 push하지 않음 | 추후 participation 전용 개인 queue 또는 chat tab summary 이벤트와 함께 확장 |
| 전송 중/실패/재시도 매칭 | 서버 broadcast는 있지만 `clientMessageId`가 없음 | WS send/broadcast payload에 `clientMessageId` 추가 |

상단 뱃지는 채팅 도메인의 전체 안 읽은 수와 참여 신청 도메인의 요청함/신청내역 카운트를 함께 보여주는 화면 단위 요약 정보다. 실시간 갱신까지 필요하면 `GET /api/v1/chat-rooms`나 `/user/queue/chat-room-list`에 섞기보다 별도 summary API와 개인 queue를 둔다.

예시 응답:

```json
{
  "totalUnreadChatCount": 4,
  "receivedParticipationCount": 3,
  "myParticipationCount": 1
}
```

예시 실시간 이벤트:

```json
{
  "eventType": "SUMMARY_UPDATED",
  "totalUnreadChatCount": 4,
  "receivedParticipationCount": 3,
  "myParticipationCount": 1,
  "occurredAt": "2026-06-20T10:30:00"
}
```

`clientMessageId`는 필수 API는 아니지만, 전송 중/실패/재시도 UI를 정확히 구현하려면 필요하다. 현재는 서버 broadcast를 수신한 뒤 메시지를 확정하는 방식으로 구현할 수 있다.

요청함과 신청내역 목록은 현재 `GET /api/v1/participations`, `GET /api/v1/users/me/participations`로 snapshot을 다시 조회하는 방식만 지원한다. 신규 신청, 신청 취소, 연락 시작, 승인, 거절 같은 변경을 실시간으로 목록에 반영하려면 participation 변경 이벤트를 개인 queue로 발행하는 작업을 별도로 추가해야 한다.

### 8.2 기획 확정 후 판단할 항목

| 기능 | 현재 상태 | 판단 기준 |
|---|---|---|
| 채팅방 검색 | 검색 query가 없음 | 검색 버튼이 실제 검색 기능으로 확정되면 `GET /api/v1/chat-rooms/search?keyword=...` 같은 API가 필요하다. 단순 아이콘 또는 추후 기능이면 현재 채팅 목록 API만 사용한다. |

### 8.3 타 도메인 작업으로 분리할 항목

| 기능 | 현재 상태 | 담당/연계 기준 |
|---|---|---|
| 승인 후 사용자 자진 나가기 | `LEFT`, `USER_LEFT` 모델은 있으나 self leave API 없음 | 나의 여정 도메인에서 `DELETE /api/v1/journeys/{journeyId}/members/me` 같은 API로 구현하는 것이 자연스럽다. 구현 시 그룹 채팅 멤버십 삭제, `USER_LEFT` 시스템 메시지, 채팅방 목록 `REMOVE` 이벤트와 연계한다. |

## 9. 관련 문서

- `docs/003-api/001-chat-message-retrieve.md`
- `docs/003-api/002-chat-read-receipt.md`
- `docs/003-api/003-chat-room-list-realtime.md`
- `docs/001-design/flows/002-participation-group-chat-flow.md`
