# 005. 알림 구현 문서

> 알림 저장, FCM 푸시, 읽음 처리, 여행 임박 스케줄러의 구현 기준을 정리합니다.
> 제품 정책은 [알림 흐름](../002-policy/flows/005-notification-flow.md)을 참고합니다.

---

## 1. 현재 구현 범위

| 영역 | 구현 |
|---|---|
| 인앱 알림 저장 | `notifications` 테이블, `NotificationEventListener`, `NotificationPersistService` |
| 알림 목록 조회 | `GET /api/v1/notifications`, 커서 기반 조회 |
| 안 읽은 수 조회 | `GET /api/v1/notifications/unread-count` |
| 읽음 처리 | 단건 읽음, 전체 읽음 |
| 수신 설정 | `users.notification_enabled` 전체 on/off |
| FCM 토큰 | `user_fcm_tokens`, 사용자+디바이스 타입 기준 upsert |
| FCM 푸시 | `FcmPushService` 비동기 발송 |
| 여행 임박 알림 | `TripUpcomingScheduler`, `TripUpcomingNotificationService` |

채팅 메시지는 인앱 알림으로 저장하지 않고 `ChatPushNotificationService`에서 FCM 푸시만 발송합니다.

---

## 2. 이벤트 기반 알림

`NotificationEventListener`는 도메인 이벤트를 `AFTER_COMMIT`에서 처리합니다.
원래 도메인 트랜잭션이 롤백되면 알림도 발송하지 않습니다.

| 이벤트 | 알림 타입 | 리소스 타입 | 리소스 ID |
|---|---|---|---|
| `MatchAppliedEvent` | `MATCH_APPLIED` | `MATCH` | `participationId` |
| `MatchApprovedEvent` | `MATCH_APPROVED` | `JOURNEY` | `journeyId` |
| `JourneyNoticeCreatedEvent` | `JOURNEY_NOTICE` | `JOURNEY_POST` | `journeyPostId` |
| `ScheduleCreatedEvent` | `SCHEDULE_CREATED` | `SCHEDULE` | `scheduleId` |
| `ScheduleUpdatedEvent` | `SCHEDULE_UPDATED` | `SCHEDULE` | `scheduleId` |
| `ScheduleCanceledEvent` | `SCHEDULE_CANCELED` | `SCHEDULE` | `scheduleId` |
| `PostUpdatedEvent` | `POST_UPDATED` | `POST` | `postId` |

수신자 결정:

- 매칭 신청: 모집글 작성자 1명
- 매칭 승인: 신청자 1명
- 여정 공지/일정: 여정 ACTIVE 멤버 중 행위자 제외
- 모집글 수정: 해당 모집글을 좋아요한 사용자

---

## 3. FCM 발송

FCM 데이터 payload는 아래 키를 사용합니다.

```json
{
  "resourceType": "JOURNEY",
  "resourceId": "1"
}
```

`resourceType` 값은 `ResourceType.payloadValue`를 사용합니다.
현재 값은 `JOURNEY`, `JOURNEY_POST`, `SCHEDULE`, `MATCH`, `POST`, `CHAT_ROOM`입니다.

사용자가 `notification_enabled=false`이면 인앱 알림은 저장하지만 FCM 푸시는 보내지 않습니다.
만료되었거나 영구적으로 유효하지 않은 FCM 토큰은 발송 실패 응답을 기준으로 삭제합니다.

---

## 4. 여행 임박 알림

`TripUpcomingScheduler`는 여행 시작 3일 전인 여정을 찾아 `TRIP_UPCOMING` 알림을 생성합니다.
같은 날 같은 여정/수신자에게 이미 발송된 알림은 재발송하지 않습니다.

중복 판정은 `NotificationType.TRIP_UPCOMING`, `ResourceType.JOURNEY`, `journeyId`, 당일 시작 시각을 기준으로 합니다.

---

## 5. 테스트 기준

- 도메인 이벤트별 알림 타입, 본문, 리소스 타입, 리소스 ID가 맞는지 검증합니다.
- 행위자 제외와 수신자 0명일 때 저장 생략을 검증합니다.
- FCM 수신 설정이 꺼진 사용자는 푸시 대상에서 제외되는지 검증합니다.
- 여행 임박 알림은 같은 날 중복 생성되지 않는지 검증합니다.
