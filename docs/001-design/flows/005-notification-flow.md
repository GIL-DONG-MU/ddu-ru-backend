# 005. Notification Flow

> 알림 서비스는 도메인 이벤트를 수신해 수신자에게 인앱 알림을 생성·저장하고,
> FCM 푸시 알림을 비동기로 발송하며, 사용자가 알림함에서 조회·읽음 처리하는 흐름이다.

이 문서는 `알림` 기능을 처음 보는 사람이 아래를 한 번에 이해할 수 있도록 정리한 제품 흐름 문서다.

- 알림이 어떤 경로로 생성되는지 (도메인 이벤트 → 리스너)
- FCM 푸시 발송 흐름 및 토큰 관리
- 알림 타입별 수신자 결정 규칙
- 조회 API의 커서 페이지네이션 방식
- 예외 처리 및 격리 정책

---

## 1. 핵심 컨셉

### 알림 생성은 API가 아닌 이벤트 기반이다

직접 알림을 생성하는 엔드포인트는 없다. 각 도메인 서비스가 동작을 완료한 뒤
`ApplicationEventPublisher`로 이벤트를 발행하면, `NotificationEventListener`가
수신해 수신자를 결정하고 `notifications` 테이블에 저장한다.

### 이벤트에 필요한 정보를 모두 싣는다

리스너에서 DB를 재조회하지 않도록, 이벤트 객체에 알림 문구 생성·수신자 결정에
필요한 정보를 함께 실어 보낸다.

예) `MatchAppliedEvent` → 이벤트 발행 시점에 신청자 닉네임을 조회해 포함
→ 리스너는 재조회 없이 body를 조립한다.

### 알림 저장 실패는 본문 트랜잭션에 영향을 주지 않는다

`@TransactionalEventListener(AFTER_COMMIT)` + 리스너 내부 `try-catch`로 격리한다.
본문 트랜잭션이 커밋된 후에만 리스너가 실행되므로, 알림 저장이 실패해도
매칭/일정 처리 결과는 그대로 유지된다.

---

## 2. 알림 생성 흐름

```
도메인 서비스 (write transaction)
  │
  ├── 비즈니스 로직 완료
  ├── eventPublisher.publishEvent(XxxEvent)  ← 이벤트 발행
  └── commit

                          ↓ AFTER_COMMIT

NotificationEventListener
  │
  ├── 수신자 결정
  ├── 알림 문구(body) 조립
  ├── notificationPersistService.save() / saveAll()  ← REQUIRES_NEW 별도 트랜잭션 (전체 수신자)
  ├── notificationEnabled = true 유저만 필터링       ← FCM 발송 대상 결정
  ├── fcmPushService.sendToUser() / sendToUsers()    ← 비동기 FCM 발송 (필터링된 수신자)
  └── 예외 발생 시 log.error()만 남기고 무시
```

### FCM 토큰 라이프사이클 (클라이언트 계약)

서버는 FCM 토큰을 직접 발급하거나 갱신할 수 없다. 토큰은 앱의 Firebase SDK가 관리하며, 클라이언트가 아래 시점에 서버 API를 호출해야 한다.

| 시점 | 호출 API | 비고 |
|---|---|---|
| 로그인 성공 후 | `POST /api/v1/fcm/token` | 앱 실행 시 SDK에서 현재 토큰 조회 후 등록 |
| Firebase token refresh 콜백 발생 시 | `POST /api/v1/fcm/token` | 토큰 갱신 누락 시 이전 토큰으로 푸시 유실 |
| 로그아웃 시 | `DELETE /api/v1/fcm/token` | 로그아웃 상태에서 푸시 수신 방지 |

> token refresh 이후 등록을 누락하면 서버에 이전 토큰만 남아 FCM 발송이 실패한다.
> Firebase는 주기적으로 또는 앱 재설치 시 토큰을 갱신하므로 refresh 콜백 처리가 필수다.

**토큰 등록 시 소유권 이전**

같은 토큰이 이미 다른 유저에게 등록된 경우(기기 공유, 로그아웃 누락 등), 등록 요청 시 기존 토큰을 먼저 삭제한 뒤 현재 유저로 재등록한다. 이를 통해 이전 유저에게 푸시가 잘못 발송되는 문제를 방지한다.

---

### FCM 푸시 발송 흐름

```
fcmPushService.sendToUser(userId, title, body, data)  [fcmExecutor 스레드풀, @Async]
  │
  ├── Firebase 미초기화 → return (graceful skip)
  ├── user_fcm_tokens 에서 토큰 조회 → 없으면 return
  ├── 500개 단위 청크 분할
  └── FirebaseMessaging.sendEachForMulticast()
        ├── notification payload: title, body
        ├── data payload: resourceType, resourceId  ← 앱 딥링크용
        ├── 성공 → 완료
        └── UNREGISTERED / INVALID_ARGUMENT → 해당 토큰 즉시 삭제
```

FCM 발송은 인앱 알림 저장과 독립적이다. DB 저장 후 비동기로 실행되므로 FCM 실패가 알림 저장에 영향을 주지 않는다.

> **알림 설정과 FCM 발송의 관계**
> 인앱 알림(`notifications` 테이블)은 `notificationEnabled` 여부와 무관하게 항상 저장된다.
> FCM 푸시만 `notificationEnabled = false` 유저를 발송 대상에서 제외한다.
> 카카오톡 알림 끄기와 동일한 동작 — 앱 알림함에서는 확인 가능하고, 핸드폰 푸시만 차단.

### 이벤트 발행 지점 목록

| 도메인 서비스 | 메서드 | 발행 이벤트 |
|---|---|---|
| `ParticipationApplicantService` | `participate()` | `MatchAppliedEvent` |
| `ParticipationCommandService` | `approveParticipation()` | `MatchApprovedEvent` |
| `JourneyPostService` | `updatePostNotice()` | `JourneyNoticeCreatedEvent` |
| `JourneyScheduleService` | `createSchedule()` | `ScheduleCreatedEvent` |
| `JourneyScheduleService` | `updateSchedule()` | `ScheduleUpdatedEvent` |
| `JourneyScheduleService` | `deleteSchedule()` | `ScheduleCanceledEvent` |
| `PostService` | `update()` | `PostUpdatedEvent` |

> `TRIP_UPCOMING`은 이벤트 기반이 아닌 스케줄러 기반으로 발송한다. 아래 7절 참고.
> 채팅 메시지 푸시는 `ChatMessageCreatedEvent`를 `ChatMessagePushEventListener`가 수신한다. 인앱 저장 없이 FCM만 발송한다. 아래 9절 참고.

---

## 3. 알림 타입별 규칙

### 수신자 결정

| 타입 | 수신자 | 결정 방식 |
|---|---|---|
| `MATCH_APPLIED` | 모집글 작성자 1명 | 이벤트에 `recipientUserId` 포함 |
| `MATCH_APPROVED` | 신청자 1명 | 이벤트에 `applicantUserId` 포함 |
| `JOURNEY_NOTICE` | 여정 ACTIVE 멤버 전원 (행위자 제외) | `JourneyMemberRepository`로 조회 |
| `SCHEDULE_CREATED / UPDATED / CANCELED` | 여정 ACTIVE 멤버 전원 (행위자 제외) | `JourneyMemberRepository`로 조회 |
| `POST_UPDATED` | 해당 모집글을 찜한 유저 전원 | `PostLikeRepository`로 조회 |
| `TRIP_UPCOMING` | 여정 ACTIVE 멤버 전원 | 스케줄러에서 `JourneyMemberRepository`로 조회 |

> 행위자 본인은 항상 수신자에서 제외된다. (`TRIP_UPCOMING` 제외)

### 알림 문구(body)

| 타입 | body |
|---|---|
| `MATCH_APPLIED` | `{닉네임} 님이 매칭을 신청했습니다.` |
| `MATCH_APPROVED` | `{닉네임} 님과 매칭이 성사되었습니다.` |
| `JOURNEY_NOTICE` | `{여행지/방 이름} 에 공지가 등록되었습니다.` |
| `SCHEDULE_CREATED` | `{일정 이름} 일정이 추가되었습니다.` |
| `SCHEDULE_UPDATED` | `{일정 이름} 일정이 변경되었습니다.` |
| `SCHEDULE_CANCELED` | `{일정 이름} 일정이 취소되었습니다.` |
| `POST_UPDATED` | `관심 있는 모집글에 변경이 있습니다.` |
| `TRIP_UPCOMING` | `곧 여행이 시작됩니다. 준비물은 다 챙기셨나요?` |

> `MATCH_APPLIED`의 닉네임은 신청자, `MATCH_APPROVED`의 닉네임은 승인자(모집글 작성자)이다.
> 닉네임은 이벤트 발행 시점에 `ProfileRepository`로 조회해 이벤트에 포함한다.

### resourceType / resourceId (탭 시 이동 대상)

FCM data payload에 `resourceType`, `resourceId`를 실어 보낸다. 앱은 이 값을 읽어 알림 탭 시 해당 화면으로 딥링크 처리한다. 인앱 알림함 API도 동일한 필드를 응답에 포함하므로, 앱 내 알림함 탭 시에도 동일하게 동작한다.

| 알림 종류 | resourceType | resourceId | 이동 화면 |
|---|---|---|---|
| `MATCH_APPLIED` | `MATCH` | `participationId` | 신청 목록 화면 |
| `MATCH_APPROVED` | `JOURNEY` | `journeyId` | 여정 상세 화면 |
| `JOURNEY_NOTICE` | `JOURNEY_POST` | `journeyPostId` | 공지 상세 화면 |
| `SCHEDULE_*` | `SCHEDULE` | `scheduleId` | 일정 상세 화면 |
| `POST_UPDATED` | `JOURNEY_POST` | `postId` | 모집글 상세 화면 |
| `TRIP_UPCOMING` | `JOURNEY` | `journeyId` | 여정 상세 화면 |
| 채팅 메시지 | `CHAT_ROOM` | `roomId` | 채팅방 화면 |

> 채팅 메시지 알림은 `notifications` 테이블에 저장되지 않으므로 인앱 알림함에는 표시되지 않는다. FCM data payload로만 딥링크를 전달한다.

---

## 4. JOURNEY_NOTICE 중복 발송 방지

**서비스 레벨**에서만 처리한다. `updatePostNotice()`에서 `nextNotice && !journeyPost.isNotice()`인 경우에만 이벤트를 발행한다 (비공지 → 공지 전환 시점에만).

```
journeyPost.isNotice() == false → true 전환 시에만 이벤트 발행
    ↓
NotificationEventListener → 수신자 조회 → 알림 저장
```

공지 해제 후 재지정 시에는 이벤트가 다시 발행되어 알림이 재발송된다. 이는 의도된 동작이다.

---

## 5. 조회 API 흐름

### 알림 목록 (커서 페이지네이션)

`GET /api/v1/notifications?cursor={lastId}&size={n}`

```
NotificationController
  └── NotificationQueryService.getNotifications(userId, cursor, size)
        ├── size 클램프: null → 20, 초과 → 50
        ├── NotificationRepository.findByRecipientIdWithCursor(userId, cursor, size+1)
        │     WHERE recipient_user_id = ? AND id < cursor (cursor 없으면 전체)
        │     ORDER BY id DESC
        └── NotificationListResponse.of(fetched, requestedSize)
              ├── size+1 개 fetch → hasNext 결정
              ├── hasNext=true: nextCursor = 마지막 항목 id
              └── hasNext=false: nextCursor = null (필드 생략 아님)
```

### 읽음 처리

```
PATCH /{notificationId}/read
  └── NotificationService.markAsRead(userId, notificationId)
        ├── 알림 존재 확인 → NotificationNotFoundException (404)
        ├── recipient.id == userId 검증 → NotificationAccessDeniedException (403)
        └── isRead == false일 때만 markAsRead(now) 호출 (멱등)

PATCH /read-all
  └── NotificationService.markAllAsRead(userId)
        └── UPDATE notifications SET is_read=1, read_at=? WHERE recipient_user_id=? AND is_read=0
```

---

## 6. 예외 처리

| 상황 | 에러 코드 | HTTP |
|---|---|---|
| 존재하지 않는 알림 ID | `NOTIFICATION_NOT_FOUND` | 404 |
| 타인의 알림 읽음 처리 시도 | `NOTIFICATION_ACCESS_DENIED` | 403 |
| 이미 읽은 알림 재요청 | 에러 없음 (200 멱등) | 200 |
| 수신자 0명 (행위자 제외 후) | 알림 미생성, 정상 처리 | — |
| 알림 저장 실패 | 로그만 남기고 무시 (본문 트랜잭션 영향 없음) | — |

---

## 7. 여행 임박 알림 (TRIP_UPCOMING)

이벤트 기반이 아닌 **스케줄러 기반**으로 동작한다.

```
TripUpcomingScheduler (@Scheduled — 매일 오전 9시)
  └── TripUpcomingNotificationService.notifyUpcomingTrips()
        ├── startDate = 오늘 + 3일인 여정의 ACTIVE 멤버 전체 조회
        ├── 여정별 그룹핑
        ├── 오늘 이미 발송된 여정은 스킵 (중복 발송 방지)
        ├── Notification 인앱 저장 (전체 멤버)
        └── FCM 발송 (notificationEnabled = true 멤버만)
```

**중복 발송 방지**: `notifications` 테이블에 해당 `journeyId`로 `TRIP_UPCOMING` 알림이 오늘 이미 생성돼 있으면 스킵한다.

---

## 8. 알림 수신 설정 (on/off)

`PATCH /api/v1/notifications/settings` — 알림 수신 전체 on/off 설정

```json
{ "enabled": false }
```

- `users.notification_enabled` 컬럼으로 관리 (기본값 `true`)
- off 시 FCM 푸시만 차단, 인앱 알림은 계속 저장됨
- 1:1 알림(`MATCH_APPLIED`, `MATCH_APPROVED`): `existsByIdAndNotificationEnabled`로 발송 전 확인
- 그룹 알림: `findEnabledUserIds(recipientIds)`로 발송 대상 필터링 후 FCM 호출

---

## 9. 채팅 메시지 FCM 푸시

채팅 메시지는 `notifications` 테이블에 저장하지 않는다. **FCM 푸시만 발송**한다.
채팅 메시지 자체가 `chat_messages` 테이블에 저장되므로 인앱 저장은 중복이다.

### 발송 흐름

```
ChatMessageSendService (메시지 저장 트랜잭션)
  └── ChatMessageCreatedEvent 발행

                    ↓ AFTER_COMMIT

ChatMessagePushEventListener
  ├── [1] messageType == SYSTEM → return         (입장/퇴장 시스템 메시지 제외)
  ├── [2] 수신자 = 채팅방 멤버 전체 - 발신자
  │         수신자 없으면 → return
  ├── [3] Redis presence 조회 → 접속 중인 유저 제외
  │         (WebSocket으로 실시간 메시지 수신 중이므로 푸시 불필요)
  │         전원 접속 중이면 → return
  ├── [4] SET chat:push:lastsent:{roomId} NX EX 30
  │         실패 (쿨다운 중) → return
  ├── [5] findEnabledUserIds() → notificationEnabled = false 제외
  │         없으면 → return
  └── [6] FCM 발송
```

### Leading Edge Debounce (30초 쿨다운)

같은 채팅방에서 연속 메시지가 올 때 첫 메시지에만 즉시 푸시하고, 이후 30초간 억제한다.

```
t=0초   메시지 1 → SET NX 성공 → 푸시 발송 ✅  (키 TTL 30초 시작)
t=10초  메시지 2 → SET NX 실패 (키 있음)  → 스킵 ❌
t=32초  메시지 3 → SET NX 성공 (키 만료됨) → 푸시 발송 ✅
```

30초 타이머는 첫 메시지 기준으로 고정된다. 이후 메시지가 와도 리셋되지 않는다.

### 접속 상태 추적 (Redis)

```
chat:online:{roomId}                      → Set<userId>        (TTL 2h, 좀비 세션 자동 정리)
chat:session:{sessionId}                  → {userId}:{roomId}  (TTL 2h, DISCONNECT 시 roomId 역추적용)
chat:subscription:{sessionId}:{subId}     → {roomId}           (TTL 2h, UNSUBSCRIBE 시 roomId 역추적용)
```

> STOMP UNSUBSCRIBE 프레임은 `destination` 없이 `subscriptionId`만 전달하므로,
> SUBSCRIBE 시 subscription 키를 저장해두고 UNSUBSCRIBE 시 roomId를 역추적한다.

| STOMP 이벤트 | 처리 |
|---|---|
| SUBSCRIBE `/topic/chat/rooms/{roomId}` | `SADD chat:online:{roomId} {userId}` + subscription 키 저장 |
| UNSUBSCRIBE | subscription 키 조회 → `SREM chat:online:{roomId} {userId}` → subscription 키 삭제 |
| SessionDisconnectEvent (앱 종료/네트워크 끊김) | session 키 조회 → `SREM` → session 키 삭제 |

### 알림 포맷

| 채팅방 타입 | title | body |
|---|---|---|
| GROUP (동행 여행방) | 여정 제목 | `{닉네임}: {메시지 내용}` (30자 초과 시 `...`) |
| GROUP IMAGE | 여정 제목 | `{닉네임}: 사진을 보냈습니다.` |
| PRIVATE (1:1) | 발신자 닉네임 | `{메시지 내용}` (닉네임 미포함, title에 이미 표시) |
| PRIVATE IMAGE | 발신자 닉네임 | `사진을 보냈습니다.` |

> SYSTEM 메시지(입장·퇴장 등)는 푸시 대상이 아니다.

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

앱은 `data.resourceType == "CHAT_ROOM"` 확인 후 `data.resourceId`(roomId)로 해당 채팅방 화면으로 이동한다.

### collapseKey

`collapseKey = "chat:{roomId}"` 를 Android/APNS 모두에 적용한다.
디바이스가 오프라인 상태일 때 같은 채팅방 알림이 여러 개 FCM 큐에 쌓이면, 기기가 온라인 복귀 시 최신 1개만 수신한다.

---

## 10. 현재 범위 제외 항목

- `SCHEDULE_UPCOMING` — 시간 기반 스케줄러 + 푸시 연동 필요
- 시스템 공지/이벤트 (어드민 발송)
- 90일 경과 알림 자동 삭제 배치
