# 006. 날짜/시간 사용 기준

> 현재 날짜/시간이 비즈니스 판단에 영향을 주는 코드는 `TimeProvider`를 사용합니다.

---

## 한 줄 요약

서비스 로직에서 `LocalDate.now()`, `LocalDateTime.now()`를 직접 호출하지 않고, `TimeProvider.today()` 또는 `TimeProvider.now()`를 주입받아 사용합니다.

---

## 1. 기준 시간대

서버의 기준 시간대는 KST입니다.

- `KoreaTime.ZONE_ID`: `Asia/Seoul`
- `TimeConfig`: KST `Clock` Bean 등록
- `TimeProvider`: KST `Clock` 기반 `today()`, `now()`, `zoneId()` 제공
- Bean Validation의 시간 기준도 같은 `Clock`을 사용

---

## 2. 언제 TimeProvider를 쓰나?

아래처럼 현재 날짜/시간이 정책 판단에 들어가면 반드시 `TimeProvider`를 사용합니다.

- 모집 마감일, 여행 시작/종료일 기준 검증
- 추천 가능 날짜, 오늘 추천 묶음 조회/생성
- 알림 생성 시각, 여행 임박 알림 기준일
- 채팅 읽음 시각, 사용자에게 노출되는 현재 시각 기반 계산
- 배치/스케줄러에서 오늘 날짜를 기준으로 대상을 고르는 로직

예:

```java
LocalDate today = timeProvider.today();
LocalDateTime now = timeProvider.now();
```

---

## 3. 직접 now() 호출을 피하는 이유

`LocalDate.now()`나 `LocalDateTime.now()`를 직접 호출하면 아래 문제가 생깁니다.

- 테스트에서 현재 시간을 고정하기 어렵습니다.
- 개발자 로컬, CI, 운영 서버의 timezone 차이에 영향을 받을 수 있습니다.
- 자정 근처 테스트가 불안정해질 수 있습니다.
- 같은 요청 안에서도 여러 곳에서 호출한 현재 시간이 미묘하게 달라질 수 있습니다.

---

## 4. 계층별 원칙

| 계층 | 기준 |
|---|---|
| Service / Scheduler | `TimeProvider`를 주입받아 현재 날짜/시간을 계산 |
| Domain Entity | 가능하면 service에서 계산한 `now`를 메서드 인자로 전달 |
| Repository | 현재 시간 계산을 새로 하지 않고 service에서 받은 값을 조건으로 사용 |
| DTO / Controller | 현재 시간 기준 정책 판단을 두지 않음 |
| Test | `Clock.fixed(...)` 기반 `TimeProvider` 또는 mock `TimeProvider` 사용 |

도메인 메서드 안에서 상태 변경 시각이 필요하면 아래처럼 호출자가 시간을 전달하는 형태를 우선합니다.

```java
participation.approve(timeProvider.now());
```

---

## 5. 예외

- JPA 감사 필드(`createdAt`, `modifiedAt`)는 `BaseTimeEntity`와 JPA auditing에 맡깁니다.
- DB가 생성하는 timestamp, 외부 provider가 내려주는 만료 시각처럼 source of truth가 서버 애플리케이션 시간이 아닌 경우에는 해당 값을 사용합니다.
- 테스트 fixture에서 단순 미래 날짜를 만들기 위한 `LocalDate.now().plusDays(...)`는 허용할 수 있지만, 시간 경계가 중요한 테스트는 고정 `Clock`을 사용합니다.

---

## 6. 기존 코드 정리 기준

기존 코드에 직접 `now()` 호출이 남아 있을 수 있습니다. 새로 작성하거나 수정하는 비즈니스 로직에서는 `TimeProvider`를 기준으로 맞추고, 기존 직접 호출 코드를 건드릴 때 함께 교체합니다.
