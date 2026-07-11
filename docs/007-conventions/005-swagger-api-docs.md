# 005. Swagger API 명세 작성 가이드

> 이 문서를 보면 Swagger UI에 노출되는 API 명세를 어떤 형식으로 작성해야 하는지 파악할 수 있습니다.

---

## 한 줄 요약

Swagger 명세는 Controller가 아니라 `*ApiDocs` 인터페이스에 작성하고, 성공 응답과 실제 발생 가능한 `ErrorCode`를 빠짐없이 문서화합니다.

---

## 1. 기본 위치

Controller는 구현만 담당하고 Swagger 어노테이션은 같은 패키지의 `{Domain}ApiDocs` 인터페이스에 둡니다.

```java
@RestController
public class PostController implements PostApiDocs {
}
```

```java
@Tag(name = "Posts", description = "여행 게시글 API")
public interface PostApiDocs {
}
```

작성 기준:

| 항목 | 규칙 |
|---|---|
| `@Tag` | 도메인 API 단위로 인터페이스에 작성 |
| `@Operation` | 모든 API 메서드에 `summary`, `description` 작성 |
| `@ApiResponse` | 성공 응답을 모든 API 메서드에 명시 |
| `@ApiErrorResponses` | 실패 응답이 있는 API 메서드에 실제 `ErrorCode` 작성 |
| `@Parameter` | path variable, 숨겨야 하는 인증 사용자 ID 등에 작성 |
| `@ParameterObject` | query parameter DTO에 사용 |
| DTO `@Schema` | 필드 의미, 예시, 허용 값이 코드만으로 명확하지 않을 때 작성 |

---

## 2. 성공 응답

성공 응답은 실제 Controller가 반환하는 `ApiResult<T>`와 HTTP status에 맞춰 문서화합니다.

| 상황 | 응답 문서 |
|---|---|
| 조회/일반 성공 | `@ApiResponse(responseCode = "200", description = "조회 성공")` |
| 생성 성공 | `@ApiResponse(responseCode = "201", description = "생성 성공")` |
| 본문 없는 성공 | `@ApiResponse(responseCode = "204", description = "삭제 성공", content = @Content())` |

204 응답은 본문이 없으므로 반드시 `content = @Content()`를 지정합니다. 지정하지 않으면 Swagger UI가 응답 본문이 있는 것처럼 보일 수 있습니다.

```java
@Operation(summary = "게시글 삭제", description = "게시글을 삭제합니다.")
@ApiResponse(responseCode = "204", description = "삭제 성공", content = @Content())
@ApiErrorResponses({
        ErrorCode.POST_NOT_FOUND,
        ErrorCode.POST_ACCESS_DENIED,
        ErrorCode.UNAUTHORIZED
})
ResponseEntity<ApiResult<Void>> deletePost(
        @Parameter(description = "게시글 ID") Long postId,
        @Parameter(hidden = true) Long userId
);
```

---

## 3. 실패 응답

비즈니스 실패 응답은 `@ApiErrorResponses`에 `ErrorCode`를 나열합니다. `ApiErrorResponseDocsCustomizer`가 HTTP status별 응답과 예시를 Swagger UI에 자동 추가합니다.

```java
@ApiErrorResponses({
        ErrorCode.INVALID_INPUT_VALUE,
        ErrorCode.POST_NOT_FOUND,
        ErrorCode.POST_ACCESS_DENIED
})
```

작성 기준:

| 상황 | 규칙 |
|---|---|
| request DTO에 validation이 있다 | `INVALID_INPUT_VALUE`를 포함 |
| 인증 필수 API다 | 인증 실패 응답이 필요한 경우 `UNAUTHORIZED`를 포함 |
| 권한 검증이 있다 | 도메인별 access denied `ErrorCode`를 포함 |
| 조회 대상이 없을 수 있다 | 도메인별 not found `ErrorCode`를 포함 |
| 같은 HTTP status의 에러가 여러 개다 | 모두 나열하면 examples로 묶여 표시됨 |
| 현재 코드에서 발생하지 않는다 | 문서화하지 않음 |

`@ApiErrorResponses`는 "클라이언트가 이 API에서 받을 수 있는 에러"만 적습니다. 다른 API에서 쓰는 공통 `ErrorCode`이거나 과거 구현에서만 발생하던 에러는 제외합니다.

---

## 4. 파라미터와 인증 사용자

API 경로에 드러나는 값은 `@Parameter(description = "...")`로 설명합니다.

```java
ResponseEntity<ApiResult<PostDetailResponse>> retrievePostDetail(
        @Parameter(description = "게시글 ID") Long postId,
        @Parameter(hidden = true) Long userId
);
```

Controller에서 `@CurrentUser`, `@OptionalCurrentUser`로 주입받는 사용자 ID는 클라이언트가 보내는 값이 아니므로 `@Parameter(hidden = true)`를 사용합니다.

Query parameter가 DTO로 묶여 있으면 `@ParameterObject`를 사용합니다.

```java
ResponseEntity<ApiResult<PostListResponse>> retrievePosts(
        @ParameterObject PostListRequest request
);
```

---

## 5. 보안과 그룹

Swagger UI는 `/swagger-ui.html`에서 확인합니다. 문서 그룹은 `public`, `admin`으로 나뉩니다.

| 그룹 | 기준 |
|---|---|
| `public` | 일반 사용자 API |
| `admin` | 관리자 API |

보안 표시 방식은 `SwaggerConfig`의 전역 OpenAPI 설정과 개별 `@SecurityRequirement`에 영향을 받습니다. 인증 필요 여부를 바꿀 때는 개별 `ApiDocs`만 보지 말고 `SwaggerConfig`와 실제 Spring Security 설정을 함께 확인합니다.

---

## 6. 체크리스트

- [ ] Controller가 `*ApiDocs` 인터페이스를 구현하는가?
- [ ] 모든 API 메서드에 `@Operation`이 있는가?
- [ ] 모든 API 메서드에 성공 `@ApiResponse`가 있는가?
- [ ] 204 응답에 `content = @Content()`가 있는가?
- [ ] request validation이 있으면 `INVALID_INPUT_VALUE`가 문서화되어 있는가?
- [ ] 실제 서비스/도메인에서 발생하는 비즈니스 `ErrorCode`가 모두 문서화되어 있는가?
- [ ] 현재 코드에서 발생하지 않는 `ErrorCode`를 문서화하지 않았는가?
- [ ] 인증 사용자 ID는 `@Parameter(hidden = true)`로 숨겼는가?
- [ ] Swagger UI에서 `public`/`admin` 그룹과 응답 예시를 확인했는가?

## 관련 문서

- [패키지 구조 가이드](../005-architecture/001-package-structure.md)
- [공통 기반 사용 가이드](../005-architecture/002-common-foundation.md)
- [예외 처리 전략](./003-exception.md)
