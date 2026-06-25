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
  ├── notificationPersistService.save() / saveAll()  ← REQUIRES_NEW 별도 트랜잭션
  ├── fcmPushService.sendToUser() / sendToUsers()    ← 비동기 FCM 발송
  └── 예외 발생 시 log.error()만 남기고 무시
```

### FCM 푸시 발송 흐름

```
fcmPushService.sendToUser(userId, title, body)  [fcmExecutor 스레드풀, @Async]
  │
  ├── Firebase 미초기화 → return (graceful skip)
  ├── user_fcm_tokens 에서 토큰 조회 → 없으면 return
  ├── 500개 단위 청크 분할
  └── FirebaseMessaging.sendEachForMulticast()
        ├── 성공 → 완료
        └── UNREGISTERED / INVALID_ARGUMENT → 해당 토큰 즉시 삭제
```

FCM 발송은 인앱 알림 저장과 독립적이다. DB 저장 후 비동기로 실행되므로 FCM 실패가 알림 저장에 영향을 주지 않는다.

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

> 행위자 본인은 항상 수신자에서 제외된다.

### 알림 문구(body)

| 타입 | body |
|---|---|
| `MATCH_APPLIED` | `{닉네임} 님이 매칭을 신청했습니다.` |
| `MATCH_APPROVED` | `{닉네임} 님과 매칭이 성사되었습니다.` |
| `JOURNEY_NOTICE` | `[{여행지/방 이름}] 새 공지가 등록되었습니다.` |
| `SCHEDULE_CREATED` | `[{일정 이름}] 일정이 추가되었습니다.` |
| `SCHEDULE_UPDATED` | `[{일정 이름}] 일정이 변경되었습니다.` |
| `SCHEDULE_CANCELED` | `[{일정 이름}] 일정이 취소되었습니다.` |
| `POST_UPDATED` | `관심 있는 모집글에 변경이 있습니다.` |

> `MATCH_APPLIED`의 닉네임은 신청자, `MATCH_APPROVED`의 닉네임은 승인자(모집글 작성자)이다.
> 닉네임은 이벤트 발행 시점에 `ProfileRepository`로 조회해 이벤트에 포함한다.

### resourceType / resourceId (탭 시 이동 대상)

| 타입 | resourceType | resourceId |
|---|---|---|
| `MATCH_APPLIED` | `MATCH` | `participationId` |
| `MATCH_APPROVED` | `JOURNEY` | `journeyId` (승인된 여정으로 이동) |
| `JOURNEY_NOTICE` | `JOURNEY_POST` | `journeyPostId` |
| `SCHEDULE_*` | `SCHEDULE` | `scheduleId` |
| `POST_UPDATED` | `JOURNEY_POST` | `postId` (모집글 상세로 이동) |

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

## 7. 현재 범위 제외 항목 (3차)

- 알림 on/off 설정
- `SCHEDULE_UPCOMING` — 시간 기반 스케줄러 + 푸시 연동 필요
- 채팅 새 메시지 알림 — 디바운스/그룹핑 + 푸시 연동 필요
- 시스템 공지/이벤트 (어드민 발송)
- 90일 경과 알림 자동 삭제 배치
