# 003. 공통 기반 사용 가이드

> 이 문서를 보면 공통 응답, 예외 처리, JWT, 인증 사용자 주입, Swagger 문서화 패턴을 파악할 수 있습니다.

---

## 한 줄 요약

성공 응답은 `ApiResult`, 실패 응답은 `ErrorResponse`, 비즈니스 예외는 중앙 `ErrorCode` + 개별 `BusinessException` 하위 클래스로 처리합니다.

---

## 1. 성공 응답

위치: `common/dto/ApiResult.java`

```json
{
  "status": 200,
  "data": {}
}
```

사용 기준:

| 상황 | 메서드 |
|---|---|
| 200 OK | `ApiResult.ok(data)` |
| 201 Created | `ApiResult.created(data)` |
| 204 No Content | `ApiResult.noContent()` |
| 커스텀 status | `ApiResult.of(status, data)` |

---

## 2. 실패 응답

위치: `common/exception/ErrorResponse.java`

```json
{
  "status": 404,
  "data": {
    "errorCode": "POST_NOT_FOUND",
    "field": null,
    "message": "게시글을 찾을 수 없습니다."
  }
}
```

`GlobalExceptionHandler`가 아래 예외를 처리합니다.

- `BusinessException`
- `MethodArgumentNotValidException`, `BindException`
- `ConstraintViolationException`
- `IllegalArgumentException`
- JSON 역직렬화 실패
- endpoint not found
- method/content-type 오류
- 기타 예상하지 못한 예외

---

## 3. 비즈니스 예외

현재 구조:

```text
ErrorCode (중앙 enum)
  -> BusinessException
      -> PostNotFoundException
      -> InvalidTokenException
      -> ChatRoomNotFoundException
      -> ...
```

새 예외 추가 절차:

1. `common/exception/ErrorCode.java`에 enum 값을 추가합니다.
2. 해당 도메인의 `exception` 패키지에 개별 예외 클래스를 추가합니다.
3. 서비스/도메인에서 `throw new XxxException()`으로 던집니다.
4. 컨트롤러에는 try-catch를 두지 않습니다.

---

## 4. 인증/JWT

주요 클래스:

| 클래스 | 역할 |
|---|---|
| `SecurityConfig` | 인증/인가, CORS, stateless session, admin 권한 설정 |
| `JwtAuthenticationFilter` | Authorization Bearer token 추출, SecurityContext 설정 |
| `JwtTokenProvider` | access/refresh/verification token 생성·검증 |
| `JwtAuthenticationEntryPoint` | 인증 실패 응답 |
| `JwtAccessDeniedHandler` | 인가 실패 응답 |

정책:

- Access token type claim은 `access`.
- Refresh token type claim은 `refresh`.
- Verification token type claim은 `verification`.
- Token expiration 설정은 `Duration`으로 관리합니다. 운영 secret은 `30m`, `7d`, `PT1H` 같은 명시적 duration 형식을 사용합니다.
- Refresh token 재발급은 JWT 형식/타입 검증, userId 추출, Redis 저장값 비교 순서로 처리합니다.
- Admin API 요청은 DB에서 사용자 role을 다시 조회합니다.
- Refresh token은 Redis 저장값과 요청값이 일치해야 재발급됩니다.
- CORS와 WebSocket origin은 운영에서 `CORS_ALLOWED_ORIGIN_PATTERNS`, `WEBSOCKET_ALLOWED_ORIGIN_PATTERNS` allowlist로 제한합니다.
- Actuator는 인증 없이 `/actuator/health`, `/actuator/health/readiness`, `/actuator/health/liveness`만 허용합니다.

---

## 5. 현재 사용자 주입

공통 annotation/resolver:

| 구성요소 | 용도 |
|---|---|
| `@CurrentUser` | 인증 필수 사용자 ID 주입 |
| `@OptionalCurrentUser` | 비로그인 허용 API에서 사용자 ID optional 주입 |
| `CurrentUserArgumentResolver` | SecurityContext 기반 userId 해석 |
| `OptionalCurrentUserArgumentResolver` | optional userId 해석 |

Controller에서는 `Long userId`를 직접 받는 패턴을 사용합니다.

---

## 6. Swagger 문서화

주요 패턴:

- Controller는 `*ApiDocs` 인터페이스를 구현합니다.
- 공통 에러 응답은 `@ApiErrorResponses`로 문서화합니다.
- Swagger 그룹은 `public`, `admin`으로 나뉩니다.
- Swagger UI 경로는 `/swagger-ui.html`입니다.

---

## 7. Common 패키지

| 패키지 | 용도 |
|---|---|
| `common/config` | Security, Swagger, Redis, S3, WebSocket, Scheduler, Time 설정 |
| `common/dto` | 공통 성공 응답 |
| `common/entity` | `BaseTimeEntity` |
| `common/exception` | `ErrorCode`, `BusinessException`, `ErrorResponse`, handler |
| `common/jwt` | JWT 생성/검증/filter/handler |
| `common/resolver` | 현재 사용자 argument resolver |
| `common/websocket` | STOMP 인증/에러 처리 |
| `common/validation` | 이미지 URL, null element 등 공통 검증 |
| `common/logging` | MDC logging, service performance logging |
| `common/time` | 한국 시간, time provider |

---

## 체크리스트

- [ ] 성공 응답은 `ApiResult`인가?
- [ ] 실패 응답은 `BusinessException` 또는 validation handler로 처리되는가?
- [ ] 새 ErrorCode가 클라이언트가 구분하기 쉬운 이름인가?
- [ ] 인증 필요 API는 `@CurrentUser` 또는 SecurityContext 기반 주입을 사용하는가?
- [ ] Swagger `*ApiDocs`와 `@ApiErrorResponses`를 갱신했는가?
