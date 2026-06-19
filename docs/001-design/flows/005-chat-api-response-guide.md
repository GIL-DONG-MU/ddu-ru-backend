# 005. Chat API Response Guide

> Swagger 응답 스키마만으로 판단하기 어려운 채팅 API 응답 데이터의 의미와 프론트엔드 사용 기준을 정리한다.

---

## 한 줄 요약

채팅 화면은 `roomType`과 `messageType`을 먼저 판단한 뒤, 타입별 표시 필드와 null 규칙에 맞춰 렌더링한다.

---

## 1. 목적

채팅 API는 목록, 메시지 조회, 읽음 처리, 실시간 이벤트가 함께 동작한다. Swagger는 필드 타입은 보여주지만 아래 기준은 충분히 드러내기 어렵다.

- `PRIVATE`와 `GROUP` 채팅방에서 같은 필드가 서로 다른 의미를 가지는 경우
- 메시지 타입별로 사용해야 하는 필드가 달라지는 경우
- `null`이 정상 상태인지, 데이터 누락인지 구분해야 하는 경우
- REST 조회 결과와 WebSocket 이벤트를 화면 상태에 적용하는 기준

이 문서는 현재 백엔드 구현을 기준으로 API 소비자가 화면 표시와 상태 갱신에 사용할 수 있는 기준을 제공한다.

## 2. 공통 enum

### 2.1 ChatRoomType

| 값 | 의미 | 주요 사용 화면 |
|---|---|---|
| `PRIVATE` | 게시글 기준 1:1 채팅방 | 신청자-방장 개별 연락 |
| `GROUP` | 여정 기준 그룹 채팅방 | 승인 이후 참여자 그룹 대화 |

### 2.2 ChatRoomStatus

| 값 | 의미 | 프론트 사용 기준 |
|---|---|---|
| `ACTIVE` | 사용 가능한 채팅방 | 일반 채팅 화면으로 진입 가능 |
| `CLOSED` | 닫힌 채팅방 | 메시지 입력 비활성화 후보 |
| `DELETED` | 삭제된 채팅방 | 일반 조회 대상에서 제외되며 조회 시 not found 처리 |

### 2.3 ChatMessageType

| 값 | 의미 | 사용 필드 |
|---|---|---|
| `TEXT` | 일반 텍스트 메시지 | `content`, `sender`, `unreadCount` |
| `IMAGE` | 이미지 메시지 | `images`, `sender`, `unreadCount` |
| `SYSTEM` | 서버가 생성한 시스템 메시지 | `systemMessage` |

클라이언트가 STOMP로 보낼 수 있는 타입은 `TEXT`, `IMAGE`다. `SYSTEM`은 서버에서만 생성한다.

### 2.4 ChatRoomListType

| 값 | 의미 |
|---|---|
| `ALL` | 모든 채팅방 |
| `PRIVATE` | 1:1 채팅방만 |
| `GROUP` | 그룹 채팅방만 |

`GET /api/v1/chat-rooms`에서 `roomType`을 생략하면 `ALL`로 처리된다.

## 3. REST API 응답 기준

### 3.1 1:1 채팅방 생성/조회

`POST /api/v1/posts/{postId}/chats/private`

| 필드 | 의미 | 사용 기준 |
|---|---|---|
| `roomId` | 생성되었거나 기존에 있던 1:1 채팅방 ID | 채팅방 상세 진입에 사용 |
| `isCreated` | 이번 요청에서 새로 생성되었는지 여부 | `true`면 HTTP 201, `false`면 HTTP 200 응답 |

같은 게시글과 사용자 조합에 이미 채팅방이 있으면 기존 방을 재사용한다.

### 3.2 채팅방 목록

`GET /api/v1/chat-rooms`

요청 파라미터:

| 필드 | 의미 | 기본값/제약 |
|---|---|---|
| `roomType` | 목록 필터 | 생략 시 `ALL` |
| `size` | 페이지 크기 | 생략 시 20, 최소 1, 최대 50 |
| `cursor` | 다음 페이지 커서 | 이전 응답의 `page.nextCursor`를 그대로 전달 |

응답 최상위:

| 필드 | 의미 | 사용 기준 |
|---|---|---|
| `selectedRoomType` | 실제 적용된 목록 필터 | 탭/필터 UI의 현재 선택값 |
| `chatRooms` | 채팅방 목록 | `activityAt` 기준 정렬 결과 |
| `page.hasNext` | 다음 페이지 존재 여부 | 추가 로딩 가능 여부 |
| `page.nextCursor` | 다음 페이지 조회 커서 | `hasNext=false`면 `null` |

`chatRooms[]`:

| 필드 | 의미 | 사용 기준 |
|---|---|---|
| `chatRoomId` | 채팅방 ID | 상세 화면 진입, 이벤트 병합 key |
| `roomType` | 채팅방 타입 | 타입별 필드 선택 기준 |
| `status` | 채팅방 상태 | 입력 가능 여부 판단 후보 |
| `displayName` | 목록 표시명 | `PRIVATE`는 상대 닉네임, `GROUP`은 여정 제목 |
| `postTitle` | 1:1 채팅방의 게시글 제목 | `GROUP`에서는 `null` |
| `thumbnailUrl` | 목록 썸네일 | `PRIVATE`는 상대 프로필, `GROUP`은 여정/게시글 이미지 |
| `postId` | 연결 게시글 ID | `PRIVATE` 컨텍스트 이동에 사용 |
| `journeyId` | 연결 여정 ID | `GROUP` 컨텍스트 이동에 사용 |
| `participantCount` | 현재 채팅방 멤버 수 | 목록 인원 표시 |
| `lastMessage` | 마지막으로 표시 가능한 메시지 | 메시지가 없으면 `null` |
| `unreadCount` | 현재 사용자가 아직 읽지 않은 메시지 수 | 뱃지 표시 |
| `lastReadMessageId` | 현재 사용자의 마지막 읽음 메시지 ID | 없으면 `null` |
| `activityAt` | 목록 정렬 기준 시각 | 최근 활동 순 정렬/갱신 기준 |
| `createdAt` | 채팅방 생성 시각 | 보조 정보 |

`lastMessage`:

| 필드 | 의미 | 사용 기준 |
|---|---|---|
| `messageId` | 마지막 메시지 ID | 상세 이동 후 위치 계산 후보 |
| `messageType` | 마지막 메시지 타입 | 미리보기 문구 선택 기준 |
| `content` | 마지막 메시지 원문 | `TEXT`는 텍스트, `IMAGE`는 이미지 URL, `SYSTEM`은 시스템 payload 원문 |
| `senderId` | 발신자 ID | 시스템 메시지는 `null` 가능 |
| `senderNickname` | 발신자 표시명 | 시스템 메시지는 `null` 가능 |
| `createdAt` | 메시지 생성 시각 | 목록 시간 표시 |

`lastMessage.messageType=SYSTEM`인 경우 `content`를 그대로 사용자에게 노출하지 않는다. 목록 미리보기는 클라이언트에서 고정 문구를 사용하거나 상세 메시지 조회의 `systemMessage.displayText` 기준으로 처리한다.

### 3.3 채팅방 메시지 목록

`GET /api/v1/chat-rooms/{chatRoomId}/messages`

요청 파라미터:

| 필드 | 의미 | 기본값/제약 |
|---|---|---|
| `beforeMessageId` | 이 메시지보다 과거 메시지를 조회하는 커서 | 첫 페이지에서는 생략 |
| `size` | 페이지 크기 | 생략 시 20, 최소 1, 최대 50 |

응답 최상위:

| 필드 | 의미 | 사용 기준 |
|---|---|---|
| `roomInfo` | 채팅방 헤더 정보 | `roomType`별로 표시 필드 선택 |
| `page.size` | 요청에 적용된 페이지 크기 | 디버깅/상태 보관용 |
| `page.hasNext` | 더 과거 메시지 존재 여부 | 위로 스크롤 추가 로딩 |
| `page.nextCursor` | 다음 과거 페이지 커서 | 다음 요청의 `beforeMessageId`로 전달 |
| `messages` | 메시지 목록 | 오래된 순서로 반환 |

`roomInfo`:

| 필드 | `PRIVATE` | `GROUP` | 사용 기준 |
|---|---|---|---|
| `chatRoomId` | 채팅방 ID | 채팅방 ID | 현재 방 식별자 |
| `roomType` | `PRIVATE` | `GROUP` | 헤더 렌더링 분기 |
| `isActive` | 활성 여부 | 활성 여부 | 입력 UI 활성화 후보 |
| `postTitle` | 게시글 제목 | `null` | 1:1 헤더/컨텍스트 |
| `opponentNickname` | 상대 닉네임 | `null` | 1:1 헤더 |
| `journeyTitle` | `null` | 여정 제목 | 그룹 헤더 |
| `memberCount` | `null` | 활성 여정 멤버 수 | 그룹 인원 표시 |

`messages[]`:

| 필드 | 의미 | 사용 기준 |
|---|---|---|
| `messageId` | 메시지 ID | 정렬, 커서, 읽음 처리 기준 |
| `messageType` | 메시지 타입 | 말풍선 렌더링 분기 |
| `sender` | 발신자 정보 | `SYSTEM`에서는 `null` |
| `isMine` | 현재 사용자가 보낸 메시지인지 여부 | 좌/우 말풍선 배치 |
| `content` | 텍스트 메시지 본문 | `TEXT`에서 사용 |
| `images` | 이미지 메시지 목록 | `IMAGE`에서 사용 |
| `systemMessage` | 시스템 메시지 표시 정보 | `SYSTEM`에서 사용 |
| `unreadCount` | 해당 메시지를 아직 읽지 않은 멤버 수 | `TEXT`, `IMAGE`에서 사용 |
| `createdAt` | 메시지 생성 시각 | 말풍선 시간 표시 |

`sender`:

| 필드 | 의미 |
|---|---|
| `userId` | 발신자 ID |
| `nickname` | 프로필 닉네임이 있으면 닉네임, 없으면 사용자 이름 |
| `isHost` | 연결 게시글 작성자 여부 |

`systemMessage`:

| 필드 | 의미 | 사용 기준 |
|---|---|---|
| `type` | 시스템 메시지 타입 | 시스템 이벤트 구분 |
| `displayText` | 서버가 조립한 표시 문구 | 시스템 메시지 본문으로 사용 |
| `actorUserId` | 액션 수행자 ID | 초대/내보내기 주체 |
| `inviteeUserId` | 초대된 사용자 ID | `USER_INVITED`에서 사용 |
| `userId` | 나간 사용자 ID | `USER_LEFT`에서 사용 |
| `targetUserId` | 내보내진 사용자 ID | `USER_KICKED`에서 사용 |

시스템 메시지 타입:

| 값 | 표시 의미 |
|---|---|
| `USER_INVITED` | 사용자가 그룹 채팅방에 참여함 |
| `USER_LEFT` | 사용자가 채팅방을 나감 |
| `USER_KICKED` | 사용자가 채팅방에서 내보내짐 |
| `ROOM_CLOSED` | 채팅방이 닫힘 |

### 3.4 읽음 처리

`PATCH /api/v1/chat-rooms/{chatRoomId}/read`

요청:

| 필드 | 의미 | 제약 |
|---|---|---|
| `lastReadMessageId` | 이 메시지까지 읽음 처리할 메시지 ID | 필수, 양수 |

응답:

| 필드 | 의미 | 사용 기준 |
|---|---|---|
| `chatRoomId` | 읽음 처리된 채팅방 ID | 현재 방 검증 |
| `lastReadMessageId` | 서버에 반영된 마지막 읽음 메시지 ID | 로컬 읽음 위치 갱신 |
| `updated` | 실제 읽음 위치가 전진했는지 여부 | `false`면 기존 위치와 같거나 과거 메시지를 읽음 처리한 요청 |

읽음 위치가 전진한 경우에만 채팅방 토픽으로 `READ` 이벤트가 발행된다.

## 4. 메시지 렌더링 기준

`messageType`을 기준으로 아래 필드만 사용한다.

| `messageType` | 사용 필드 | 비사용/정상 null 필드 |
|---|---|---|
| `TEXT` | `sender`, `isMine`, `content`, `unreadCount`, `createdAt` | `images=[]`, `systemMessage=null` |
| `IMAGE` | `sender`, `isMine`, `images[0].imageUrl`, `unreadCount`, `createdAt` | `content=null`, `systemMessage=null` |
| `SYSTEM` | `systemMessage.displayText`, `createdAt` | `sender=null`, `content=null`, `images=[]`, `unreadCount=null` |

`unreadCount`는 메시지 발신자를 제외하고, 메시지 생성 이후 입장한 멤버를 제외하며, 해당 메시지 ID 이상을 읽은 멤버를 제외해서 계산된다.

## 5. null 규칙

| 상황 | 정상 null/빈 값 |
|---|---|
| 채팅방 목록에 아직 메시지가 없음 | `lastMessage=null` |
| `PRIVATE` 채팅방 목록 | `journeyId=null` 가능 |
| `GROUP` 채팅방 목록 | `postTitle=null` |
| 1:1 메시지 헤더 | `journeyTitle=null`, `memberCount=null` |
| 그룹 메시지 헤더 | `postTitle=null`, `opponentNickname=null` |
| 시스템 메시지 | `sender=null`, `content=null`, `images=[]`, `unreadCount=null` |
| 아직 읽은 메시지가 없음 | `lastReadMessageId=null` |

`displayName`과 `thumbnailUrl`은 화면 표시용으로 조립된 값이다. 프론트는 목록에서 별도 타입별 조합을 다시 만들기보다 이 값을 우선 사용한다.

## 6. WebSocket 이벤트

### 6.1 연결과 destination

| 구분 | 경로 | 설명 |
|---|---|---|
| WebSocket endpoint | `/ws/chat` | STOMP 연결 endpoint |
| 클라이언트 메시지 발행 | `/app/chat/rooms/{roomId}/messages` | 사용자 메시지 전송 |
| 채팅방 메시지/읽음 구독 | `/topic/chat/rooms/{roomId}` | 방 단위 브로드캐스트 |
| 채팅방 목록 이벤트 구독 | `/user/queue/chat-room-list` | 사용자별 목록 갱신 이벤트 |

### 6.2 메시지 전송 요청

클라이언트가 `/app/chat/rooms/{roomId}/messages`로 발행한다.

| 필드 | 의미 |
|---|---|
| `messageType` | `TEXT` 또는 `IMAGE` |
| `content` | 텍스트 본문 또는 이미지 URL |

### 6.3 채팅방 토픽 메시지 payload

`/topic/chat/rooms/{roomId}`에서 수신한다.

| 필드 | 의미 | 사용 기준 |
|---|---|---|
| `messageId` | 생성된 메시지 ID | 로컬 메시지 병합 key |
| `roomId` | 채팅방 ID | 현재 구독 방 검증 |
| `postTitle` | 연결 게시글 제목 | 컨텍스트 표시 후보 |
| `recruitCount` | 현재 승인 인원 수 | 모집 정보 표시 후보 |
| `recruitCapacity` | 모집 정원 | 모집 정보 표시 후보 |
| `messageType` | 메시지 타입 | `userMessage`/`systemMessage` 분기 |
| `sender` | 발신자 정보 | 시스템 메시지는 `null` |
| `userMessage` | 사용자 메시지 본문 | `TEXT`, `IMAGE`에서 사용 |
| `systemMessage` | 시스템 메시지 payload | `SYSTEM`에서 사용 |
| `createdAt` | 생성 시각 | 말풍선 시간 |

`userMessage`:

| 필드 | 사용 타입 |
|---|---|
| `text` | `TEXT` |
| `imageUrl` | `IMAGE` |

채팅방 토픽의 시스템 메시지는 REST 상세 조회의 `systemMessage.displayText`처럼 표시 문구를 포함하지 않는다. 클라이언트는 `systemMessage.type`과 관련 사용자 ID를 기준으로 문구를 만들거나, 이후 REST 조회 결과와 동일한 기준으로 보정한다.

### 6.4 읽음 이벤트

`/topic/chat/rooms/{roomId}`에서 수신한다.

| 필드 | 의미 | 사용 기준 |
|---|---|---|
| `eventType` | 항상 `READ` | 메시지 이벤트와 구분 |
| `roomId` | 채팅방 ID | 현재 방 검증 |
| `readerUserId` | 읽음 처리한 사용자 ID | 상대 읽음 상태 갱신 |
| `lastReadMessageId` | 해당 사용자가 읽은 마지막 메시지 ID | 메시지별 읽음 카운트 보정 |
| `readAt` | 읽음 처리 시각 | 보조 정보 |

### 6.5 채팅방 목록 이벤트

`/user/queue/chat-room-list`에서 수신한다.

| 필드 | 의미 | 사용 기준 |
|---|---|---|
| `eventType` | 목록 적용 방식 | `UPSERT` 또는 `REMOVE` |
| `reason` | 변경 원인 | UI 갱신 범위 판단 후보 |
| `chatRoom` | 변경 대상 채팅방 | 목록 item 병합 또는 제거 |
| `occurredAt` | 이벤트 발생 시각 | 로컬 이벤트 순서 판단 후보 |

`eventType`:

| 값 | 적용 기준 |
|---|---|
| `UPSERT` | `chatRoom.chatRoomId` 기준으로 기존 item을 교체하거나 없으면 삽입 |
| `REMOVE` | `chatRoom.chatRoomId` 기준으로 목록에서 제거 |

`reason`:

| 값 | 의미 |
|---|---|
| `MESSAGE_CREATED` | 새 메시지로 목록 item이 변경됨 |
| `READ_UPDATED` | 읽음 상태 변경으로 목록 item이 변경됨 |
| `ROOM_META_UPDATED` | 방 제목, 이미지 등 메타 정보가 변경됨 |
| `MEMBER_CHANGED` | 멤버 입장/퇴장/내보내기 등으로 목록 item이 변경됨 |

`UPSERT`의 `chatRoom`은 REST 목록의 `chatRooms[]`와 같은 의미의 완성된 item이다. `REMOVE`의 `chatRoom`은 제거에 필요한 `chatRoomId`, `roomType`만 포함한다.

## 관련 문서

- `docs/001-design/flows/002-participation-group-chat-flow.md`
- `docs/001-design/flows/003-post-product-flow.md`
- `docs/001-design/flows/004-participation-flow.md`
