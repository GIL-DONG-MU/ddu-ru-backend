# 005. 데이터 검증 및 문자열 정제 전략

> 이 문서를 보면 각 계층이 어떤 검증·정제 책임을 갖는지, 그리고 왜 그 책임을 그 계층에 두는지 파악할 수 있습니다.

---

## 한 줄 요약

DTO가 문자열을 정제하고, Domain이 비즈니스 규칙을 최종 보장하며, Service는 흐름만 조율한다.

---

## 1. 계층별 역할과 데이터 흐름

```
Client 요청
    │
    ▼
[DTO / Compact Constructor]
  · 문자열 앞뒤 공백 제거 (trim / strip)
  · 선택 문자열: 공백 → 빈 문자열로 유지 (null 변환은 하지 않음)
  · @NotBlank — 빈 값 진입 차단 (400 Bad Request)
    │
    ▼
[Service]
  · 유스케이스 흐름만 담당 (조회 → 도메인 메서드 호출 → 저장)
  · 복잡도가 낮은 비즈니스 검증은 Domain 정적 메서드에 위임
  · 태그처럼 도메인 외부 규칙(TagValidator)은 서비스에서 처리
    │
    ▼
[Domain / Entity]
  · 길이, 범위, 날짜 논리 등 비즈니스 규칙을 최종 보장
  · 팩토리 메서드(createXxx) 또는 상태 변경 메서드 진입 시 검증
  · 규칙 위반 시 팀 커스텀 예외를 throw
    │
    ▼
[DB]
  · @Column(nullable = false) 등 DB 제약 — 마지막 안전망
```

---

## 2. DTO 계층 — 문자열 정제

### 역할

외부 입력을 정제해 이후 계층이 데이터를 100% 신뢰할 수 있도록 만든다.  
검증(validation)은 최소화하고, 정규화(normalization)에 집중한다.

### 규칙

| 필드 종류 | 처리 방식 |
|----------|----------|
| 필수 문자열 | `trim()` 후 `@NotBlank`로 빈 값 차단 |
| 선택 문자열 | `trim()` (빈 문자열은 그대로 — null 변환 안 함) |
| 리스트 요소 | 각 요소 `strip()` + 비어있는 요소 제거 |
| 숫자 / 날짜 / Enum | 정제 불필요, `@NotNull` / `@Min` / `@Max` 등 형식 검증만 |

> **선택 문자열을 null 변환하지 않는 이유**  
> `null`은 "변경 없음(patch 없음)"을 의미하는 경우가 많다. 예를 들어 `photoUrl`이 `null`이면 기존 값 유지, `""`이면 여행지 기본 이미지로 대체로 처리된다. `trimToNull`로 의미를 왜곡하지 않는다.

### 예시

```java
public record PostCreateRequest(
        @NotBlank(message = "제목은 필수입니다")
        String title,

        @NotBlank(message = "내용은 필수입니다")
        String content,

        String photoUrl,   // 선택

        List<String> tags  // 각 요소 정제
) {
    public PostCreateRequest {
        if (title != null)   title   = title.trim();
        if (content != null) content = content.trim();
        if (photoUrl != null) photoUrl = photoUrl.trim();
        if (tags != null) {
            tags = tags.stream()
                    .filter(t -> t != null && !t.strip().isEmpty())
                    .map(String::strip)
                    .toList();
        }
    }
}
```

### DTO에 두지 않는 것

- `@Size(min, max)` — 길이 제한은 비즈니스 규칙이므로 Domain이 담당한다.
- 날짜 범위 검증 (`startDate < endDate`) — 두 필드 간의 관계이므로 Domain이 담당한다.
- 선호 연령 범위 검증 (`isAgeAny` ↔ `minAge/maxAge` 상관관계) — 동일한 이유.

---

## 3. Domain 계층 — 비즈니스 규칙 보장

### 역할

어떤 경로(API, 내부 로직, 테스트 코드)로 Entity가 생성·변경되더라도 비즈니스 규칙이 항상 지켜지도록 한다.

### 규칙

- 팩토리 메서드(`createPost`, `createParticipation` 등)에서 입력값 검증을 수행한다.
- 상태 변경 메서드(`updatePost`, `approve` 등)에서도 직접 검증한다.
- 규칙 위반 시 팀 커스텀 예외(`BusinessException` 하위)를 throw한다.
- 정적 검증 메서드는 Service가 "effective 값"을 계산해야 하는 경우 공개(`public static`)로 노출한다.

### 예시

```java
// Post.java

private static final int TITLE_MIN = 5;
private static final int TITLE_MAX = 40;
private static final int CONTENT_MIN = 20;
private static final int CONTENT_MAX = 1000;
private static final int MIN_PREFERRED_AGE = 20;
private static final int MAX_PREFERRED_AGE = 100;

public static Post createPost(...) {
    validateDateRange(startDate, endDate);
    validatePreferredAge(isAgeAny, minAge, maxAge);
    return Post.builder()
            .title(requireValidTitle(title))
            .content(requireValidContent(content))
            ...
            .build();
}

// update 시에도 동일한 메서드를 통해 값 설정
private void applyBasicChanges(...) {
    if (title != null)   this.title   = requireValidTitle(title);
    if (content != null) this.content = requireValidContent(content);
    ...
}

// Service가 호출할 수 있도록 public static으로 노출
public static void validateDateRange(LocalDate startDate, LocalDate endDate) {
    if (endDate.isBefore(startDate)) throw new InvalidPostDateException();
}

public static void validatePreferredAge(boolean isAgeAny, Integer minAge, Integer maxAge) { ... }

private static String requireValidTitle(String title) {
    if (title.length() < TITLE_MIN || title.length() > TITLE_MAX)
        throw new InvalidPostTitleException();
    return title;
}
```

---

## 4. Service 계층 — 순수한 오케스트레이터

### 역할

"무엇을 어떤 순서로 호출할 것인가"만 담당한다.  
문자열 조작이나 비즈니스 규칙 검증 로직을 직접 구현하지 않는다.

### 규칙

- 검증 로직을 private 메서드로 감싸지 않는다. 도메인에 위임한다.
- update처럼 "effective 값(변경 후 실제 적용될 값)"을 계산한 뒤 Domain 정적 메서드에 위임하는 것은 허용한다.
- 도메인 모델이 알기 어려운 외부 규칙(예: `TagValidator`)은 서비스에서 직접 처리한다.

### 예시

```java
// create — DB 조회 전에 검증을 먼저 끝낸다 (fast-fail)
// 이유: userRepository.getByIdOrThrow() 같은 DB 조회는 검증 실패 시 낭비가 됨
//      도메인 정적 메서드를 서비스에서 먼저 호출해 불필요한 쿼리를 차단
public PostCreateResponse create(Long userId, PostCreateRequest request) {
    TagValidator.validateOrThrow(request.tags());         // 외부 규칙, 서비스에 위치
    Post.validateDateRange(request.startDate(), request.endDate());   // fast-fail
    Post.validatePreferredAge(request.isAgeAny(), request.minAge(), request.maxAge()); // fast-fail

    User user = userRepository.getByIdOrThrow(userId);            // 검증 통과 후 DB 조회
    Destination destination = destinationRepository.getByIdOrThrow(request.destinationId());
    Post post = Post.createPost(user, destination, ...);  // 도메인 내부에서도 동일 검증 재수행
    ...
}

// update — effective 값 계산 후 도메인 정적 메서드에 위임
public void update(Long postId, Long userId, PostUpdateRequest request) {
    Post post = getOwnedPost(postId, userId);
    Post.validateDateRange(
            getStartDateOrCurrent(post, request),   // effective startDate
            getEndDateOrCurrent(post, request)      // effective endDate
    );
    if (hasAgePatch(request)) {
        Post.validatePreferredAge(Boolean.TRUE.equals(request.isAgeAny()), ...);
    }
    if (request.tags() != null) {
        TagValidator.validateOrThrow(request.tags());
    }
    ...
}
```

---

## 5. DB 계층 — 마지막 안전망

애플리케이션 검증이 완벽해도 DB 제약은 제거하지 않는다.

```java
@Column(nullable = false, length = 100)
private String title;
```

- 직접 DB 조작, 마이그레이션 스크립트, 미래의 배치 작업 등 애플리케이션을 우회하는 경로가 항상 존재한다.
- DB 제약은 데이터 정합성의 마지막 방어선으로 유지한다.

---

## 6. 기존 방식에서 변경한 이유

### 기존 문제점

| 문제 | 위치 | 내용 |
|------|------|------|
| 문자열 trim 없음 | DTO | `" 제목 "` 같은 입력이 그대로 도메인까지 전달됨 |
| 비즈니스 검증이 서비스에 | Service | `validateDateRange()`, `validatePreferredAge()` 등 private 메서드들이 서비스 내부에 산재 |
| Entity 생성 시 무검증 | Domain | `Post.createPost()`가 title/content 길이, 날짜 범위를 검증하지 않음 |
| 계층별 책임 불일치 | 전체 | Profile은 도메인에서 nickname 검증, Post는 서비스에서 날짜 검증 — 일관성 없음 |
| 이중 정규화 | Service | `JsonConverter.normalizeTagList()`가 서비스에서 한 번, `convertTagListToJson()` 내부에서 한 번 더 strip 수행 |

### 변경 이후

| 기존 | 변경 후 |
|------|---------|
| 서비스에 `validateCreateRequest()` 등 private 메서드 | 삭제 — 도메인에 위임 |
| DTO에 `@Size(min=5, max=40)` | 삭제 — Domain의 `requireValidTitle()`이 보장 |
| `JsonConverter.normalizeTagList()` 서비스 호출 | 삭제 — DTO compact constructor가 처리 |
| `Post.createPost()` — 검증 없이 그냥 빌더 호출 | `validateDateRange`, `validatePreferredAge`, `requireValidTitle/Content` 추가 |
| `resolvePhotoUrl()` 내부 중복 `trim()` | 삭제 — DTO가 이미 처리 |

---

## 7. 안티패턴

### 서비스에서 직접 검증 로직 구현

```java
// 잘못된 예
@Service
public class PostService {
    private void validateDateRange(LocalDate start, LocalDate end) {
        if (end.isBefore(start)) throw new InvalidPostDateException();
    }
}

// 올바른 예
Post.validateDateRange(start, end);  // 도메인 정적 메서드에 위임
```

### DTO에서 비즈니스 규칙 검증

```java
// 잘못된 예 — DTO가 비즈니스 규칙(길이 제한)을 직접 보유
@Size(min = 5, max = 40, message = "제목은 5자 이상 40자 이하여야 합니다")
String title;

// 올바른 예 — DTO는 빈 값만 차단, 길이는 도메인이 처리
@NotBlank(message = "제목은 필수입니다")
String title;
```

### 도메인 생성 경로에서 검증 생략

```java
// 잘못된 예 — 서비스가 검증한다고 믿고 엔티티에서 생략
public static Post createPost(...) {
    return Post.builder().title(title)...build();  // title이 1글자여도 통과
}

// 올바른 예 — 어떤 경로로 생성되어도 항상 유효
public static Post createPost(...) {
    return Post.builder().title(requireValidTitle(title))...build();
}
```

---

## 8. 새 도메인에 적용할 때 체크리스트

- [ ] DTO compact constructor에서 문자열 필드 trim 처리
- [ ] 필수 문자열에 `@NotBlank` 추가 (길이 등 비즈니스 규칙은 제외)
- [ ] 팩토리 메서드에서 비즈니스 규칙(길이, 범위, 날짜 등) 검증
- [ ] 상태 변경 메서드에서도 동일하게 검증 (update 경로)
- [ ] 규칙 위반 시 `BusinessException` 하위 커스텀 예외 throw
- [ ] `ErrorCode`에 해당 에러 코드 추가
- [ ] DB `@Column` 제약 유지
