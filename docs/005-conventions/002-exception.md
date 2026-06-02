# 002. 예외 처리 전략

> 이 문서를 보면 현재 예외 구조와 새 비즈니스 예외를 추가하는 방법을 파악할 수 있습니다.

---

## 한 줄 요약

DDU-RU Backend는 중앙 `ErrorCode` enum으로 HTTP status/message를 관리하고, 각 도메인에서는 `BusinessException`을 상속한 개별 예외 클래스를 던집니다.

---

## 1. 현재 구조

```text
common/exception/
├── ErrorCode.java
├── BusinessException.java
├── ErrorResponse.java
├── ErrorData.java
└── GlobalExceptionHandler.java

post/exception/
└── PostNotFoundException.java

chat/exception/
└── ChatRoomNotFoundException.java
```

예외 클래스 예:

```java
public class PostNotFoundException extends BusinessException {
    public PostNotFoundException() {
        super(ErrorCode.POST_NOT_FOUND);
    }
}
```

---

## 2. 응답 형태

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

Validation 실패는 `INVALID_INPUT_VALUE`와 field 정보를 내려줍니다.

---

## 3. 새 예외 추가 절차

1. `ErrorCode`에 새 enum을 추가합니다.
2. 도메인 `exception` 패키지에 개별 예외 클래스를 추가합니다.
3. 서비스 또는 도메인 메서드에서 개별 예외를 던집니다.
4. Swagger `@ApiErrorResponses`에 필요한 에러를 문서화합니다.

예:

```java
// ErrorCode
POST_NOT_FOUND(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다.")

// Exception
public class PostNotFoundException extends BusinessException {
    public PostNotFoundException() {
        super(ErrorCode.POST_NOT_FOUND);
    }
}

// Service
Post post = postRepository.findById(postId)
        .orElseThrow(PostNotFoundException::new);
```

---

## 4. 예외를 나누는 기준

| 개별 예외로 만든다 | 공통 validation으로 둔다 |
|---|---|
| 특정 비즈니스 상태를 표현한다 | 단순 `@NotBlank`, `@Size` 실패 |
| 로그에서 별도 추적해야 한다 | 일반적인 잘못된 입력 |
| 클라이언트가 에러별 UI 분기를 한다 | 필드 형식 오류 |
| catch해서 다른 동작이 필요할 수 있다 | JSON 파싱 실패 |

---

## 5. 현재 한계

- `ErrorCode`가 중앙 enum 하나라 도메인이 늘수록 파일이 커집니다.
- ErrorCode 이름은 enum name만 내려가고 별도 숫자 코드 체계는 없습니다.
- 도메인별 ErrorCode 분리는 아직 적용되어 있지 않습니다.

> TODO: 도메인별 ErrorCode enum 분리 여부는 별도 ADR로 논의할 수 있습니다.
