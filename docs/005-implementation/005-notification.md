# 005. 알림 구현 문서

> 알림 이벤트 발행, FCM 발송, 조회 API, 채팅 메시지 푸시 구현 세부를 정리합니다.
> 제품 흐름은 [알림 흐름](../002-policy/flows/005-notification-flow.md)을 참고합니다.

---

## 1. 알림 생성 흐름

```text
도메인 서비스 (write transaction)
  |
  |-- 비즈니스 로직 완료
  |-- eventPublisher.publishEvent(XxxEvent)
  `-- commit

                          AFTER_COMMIT

NotificationEventListener
  |
  |-- 수신자 결정
  |-- 알림 문구(body) 조립
  |-- notificationPersistService.save() / saveAll()
  |-- notificationEnabled = true 유저만 필터링
  |-- fcmPushService.sendToUser() / sendToUsers()
  `-- 예외 발생 시 log.error()만 남기고 무시
```

알림 저장은 `REQUIRES_NEW` 별도 트랜잭션으로 처리합니다.

---

## 2. FCM 푸시 발송

```text
fcmPushService.sendToUser(userId, title, body, data)  [fcmExecutor 스레드풀, @Async]
  |
  |-- Firebase 미초기화 -> return
  |-- user_fcm_tokens 에서 토큰 조회 -> 없으면 return
  |-- 500개 단위 청크 분할
  `-- FirebaseMessaging.sendEachForMulticast()
        |-- notification payload: title, body
        |-- data payload: resourceType, resourceId
        |-- 성공 -> 완료
        `-- UNREGISTERED / INVALID_ARGUMENT -> 해당 토큰 즉시 삭제
```

인앱 알림 저장과 FCM 발송은 독립적입니다. FCM 실패는 알림 저장에 영향을 주지 않습니다.

### 이동 대상 payload

FCM data payload와 인앱 알림 응답에는 `resourceType`, `resourceId`를 포함합니다.

| 알림 종류 | resourceType | resourceId |
|---|---|---|
| `MATCH_APPLIED` | `MATCH` | `participationId` |
| `MATCH_APPROVED` | `JOURNEY` | `journeyId` |
| `JOURNEY_NOTICE` | `JOURNEY_POST` | `journeyPostId` |
| `SCHEDULE_*` | `SCHEDULE` | `scheduleId` |
| `POST_UPDATED` | `JOURNEY_POST` | `postId` |
| `TRIP_UPCOMING` | `JOURNEY` | `journeyId` |
| 채팅 메시지 | `CHAT_ROOM` | `roomId` |

---

## 3. FCM 토큰 라이프사이클

서버는 FCM 토큰을 직접 발급하거나 갱신할 수 없습니다. 토큰은 앱의 Firebase SDK가 관리하며, 클라이언트가 아래 시점에 서버 API를 호출해야 합니다.

| 시점 | 호출 API | 비고 |
|---|---|---|
| 로그인 성공 후 | `POST /api/v1/fcm/token` | 앱 실행 시 SDK에서 현재 토큰 조회 후 등록 |
| Firebase token refresh 콜백 발생 시 | `POST /api/v1/fcm/token` | 토큰 갱신 누락 시 이전 토큰으로 푸시 유실 |
| 로그아웃 시 | `DELETE /api/v1/fcm/token` | 로그아웃 상태에서 푸시 수신 방지 |

같은 토큰이 이미 다른 유저에게 등록된 경우, 기존 토큰을 먼저 삭제한 뒤 현재 유저로 재등록합니다.

---

## 4. 이벤트 발행 지점

| 도메인 서비스 | 메서드 | 발행 이벤트 |
|---|---|---|
| `ParticipationApplicantService` | `participate()` | `MatchAppliedEvent` |
| `ParticipationCommandService` | `approveParticipation()` | `MatchApprovedEvent` |
| `JourneyPostService` | `updatePostNotice()` | `JourneyNoticeCreatedEvent` |
| `JourneyScheduleService` | `createSchedule()` | `ScheduleCreatedEvent` |
| `JourneyScheduleService` | `updateSchedule()` | `ScheduleUpdatedEvent` |
| `JourneyScheduleService` | `deleteSchedule()` | `ScheduleCanceledEvent` |
| `PostService` | `update()` | `PostUpdatedEvent` |

`TRIP_UPCOMING`은 이벤트 기반이 아닌 스케줄러 기반으로 발송합니다.
채팅 메시지 푸시는 `ChatMessageCreatedEvent`를 `ChatMessagePushEventListener`가 수신하며, 인앱 저장 없이 FCM만 발송합니다.

---

## 5. 수신자 조회

| 타입 | 결정 방식 |
|---|---|
| `MATCH_APPLIED` | 이벤트에 `recipientUserId` 포함 |
| `MATCH_APPROVED` | 이벤트에 `applicantUserId` 포함 |
| `JOURNEY_NOTICE` | `JourneyMemberRepository`로 여정 ACTIVE 멤버 조회 |
| `SCHEDULE_CREATED / UPDATED / CANCELED` | `JourneyMemberRepository`로 여정 ACTIVE 멤버 조회 |
| `POST_UPDATED` | `PostLikeRepository`로 해당 모집글을 찜한 유저 조회 |
| `TRIP_UPCOMING` | 스케줄러에서 `JourneyMemberRepository`로 여정 ACTIVE 멤버 조회 |

행위자 본인은 수신자에서 제외합니다. 단, `TRIP_UPCOMING`은 행위자 개념이 없으므로 제외하지 않습니다.

---

## 6. 조회 API

### 알림 목록

```text
GET /api/v1/notifications?cursor={lastId}&size={n}

NotificationController
  `-- NotificationQueryService.getNotifications(userId, cursor, size)
        |-- size 클램프: null -> 20, 초과 -> 50
        |-- NotificationRepository.findByRecipientIdWithCursor(userId, cursor, size+1)
        |     WHERE recipient_user_id = ? AND id < cursor
        |     ORDER BY id DESC
        `-- NotificationListResponse.of(fetched, requestedSize)
              |-- size+1 개 fetch -> hasNext 결정
              |-- hasNext=true: nextCursor = 마지막 항목 id
              `-- hasNext=false: nextCursor = null
```

### 읽음 처리

```text
PATCH /{notificationId}/read
  `-- NotificationService.markAsRead(userId, notificationId)
        |-- 알림 존재 확인
        |-- recipient.id == userId 검증
        `-- isRead == false일 때만 markAsRead(now) 호출

PATCH /read-all
  `-- NotificationService.markAllAsRead(userId)
        `-- UPDATE notifications SET is_read=1, read_at=? WHERE recipient_user_id=? AND is_read=0
```

---

## 7. 여행 임박 알림

```text
TripUpcomingScheduler (@Scheduled, 매일 오전 9시)
  `-- TripUpcomingNotificationService.notifyUpcomingTrips()
        |-- startDate = 오늘 + 3일인 여정의 ACTIVE 멤버 전체 조회
        |-- 여정별 그룹핑
        |-- 오늘 이미 발송된 멤버 제외
        |-- Notification 인앱 저장
        `-- FCM 발송
```

중복 발송은 `notifications` 테이블에서 오늘 생성된 `TRIP_UPCOMING` 알림의 recipient_id를 조회해 방지합니다.

---

## 8. 알림 수신 설정

`PATCH /api/v1/notifications/settings` — 알림 수신 전체 on/off 설정

```json
{ "enabled": false }
```

- `users.notification_enabled` 컬럼으로 관리 (기본값 `true`)
- off 시 FCM 푸시만 차단, 인앱 알림은 계속 저장
- 1:1 알림(`MATCH_APPLIED`, `MATCH_APPROVED`): `existsByIdAndNotificationEnabled`로 발송 전 확인
- 그룹 알림: `findEnabledUserIds(recipientIds)`로 발송 대상 필터링 후 FCM 호출

---

## 9. 채팅 메시지 FCM 푸시

채팅 메시지는 `notifications` 테이블에 저장하지 않고 FCM 푸시만 발송합니다.

```text
ChatMessageSendService (메시지 저장 트랜잭션)
  `-- ChatMessageCreatedEvent 발행

                    AFTER_COMMIT

ChatMessagePushEventListener
  |-- messageType == SYSTEM -> return
  |-- 수신자 = 채팅방 멤버 전체 - 발신자
  |-- Redis presence 조회 -> 접속 중인 유저 제외
  |-- findEnabledUserIds() -> notificationEnabled = false 제외
  |-- SET chat:push:lastsent:{roomId} NX EX 30
  `-- FCM 발송
```

### Leading Edge Debounce

같은 채팅방에서 연속 메시지가 올 때 첫 메시지에만 즉시 푸시하고, 이후 30초간 억제합니다.

```text
t=0초   메시지 1 -> SET NX 성공 -> 푸시 발송
t=10초  메시지 2 -> SET NX 실패 -> 스킵
t=32초  메시지 3 -> SET NX 성공 -> 푸시 발송
```

30초 타이머는 첫 메시지 기준으로 고정됩니다. 이후 메시지가 와도 리셋되지 않습니다.

### 접속 상태 추적

```text
chat:online:{roomId}                  -> Set<userId>
chat:session:{sessionId}              -> {userId}:{roomId}
chat:subscription:{sessionId}:{subId} -> {roomId}
```

| STOMP 이벤트 | 처리 |
|---|---|
| SUBSCRIBE `/topic/chat/rooms/{roomId}` | `SADD chat:online:{roomId} {userId}` + subscription 키 저장 |
| UNSUBSCRIBE | subscription 키 조회 -> `SREM chat:online:{roomId} {userId}` -> subscription 키 삭제 |
| SessionDisconnectEvent | session 키 조회 -> `SREM` -> session 키 삭제 |

### 알림 포맷

| 채팅방 타입 | title | body |
|---|---|---|
| GROUP TEXT | 여정 제목 | `{닉네임}: {메시지 내용}` |
| GROUP IMAGE | 여정 제목 | `{닉네임}: 사진을 보냈습니다.` |
| PRIVATE TEXT | 발신자 닉네임 | `{메시지 내용}` |
| PRIVATE IMAGE | 발신자 닉네임 | `사진을 보냈습니다.` |

### FCM data payload

```json
{
  "notification": { "title": "도쿄 여행", "body": "홍길동: 오늘 저녁 어때?" },
  "data": {
    "resourceType": "CHAT_ROOM",
    "resourceId": "123"
  }
}
```

### collapseKey

`collapseKey = "chat:{roomId}"`를 Android/APNS 모두에 적용합니다.
디바이스가 오프라인 상태일 때 같은 채팅방 알림이 여러 개 FCM 큐에 쌓이면, 기기가 온라인 복귀 시 최신 1개만 수신합니다.
