# API 설계 문서

Swagger보다 상세한 설계 배경, 처리 흐름, 테스트 케이스가 필요한 API 문서를 둡니다.

| 문서 | 한 줄 요약 |
|---|---|
| [채팅 메시지 조회 API](./001-chat-message-retrieve.md) | 커서 기반 메시지 조회, roomInfo, unreadCount 설계 |
| [채팅 읽음 처리 API](./002-chat-read-receipt.md) | lastReadMessageId 갱신, READ 이벤트, unreadCount 연동 설계 |

## 작성 기준

- 구현 전 설계 문서는 "권장/계획"과 "현재 구현"을 구분해서 씁니다.
- 구현이 완료되면 DTO, 예외, 테스트 목록을 실제 코드 기준으로 갱신합니다.
- Swagger에 노출되는 계약과 다르면 이 문서를 우선 수정합니다.
- 같은 폴더 안에서는 파일명과 문서 제목에 읽는 순서 숫자를 붙입니다. 예: `001-chat-message-retrieve.md`, `# 001. Chat Message Retrieve API Design`

## API 설계 문서 양식

Swagger보다 상세한 API 설계가 필요할 때 사용합니다.

````markdown
# 001. API 이름 Design

## 1. API 개요

이 API가 필요한 이유와 핵심 정책을 적습니다.

## 2. API 명세

### Endpoint

```http
METHOD /api/v1/path
```

### Path Variable

| 이름 | 타입 | 필수 | 설명 |
|---|---|:---:|---|
| TODO | TODO | Y | TODO |

### Query Parameter

| 이름 | 타입 | 필수 | 기본값 | 설명 |
|---|---|:---:|---|---|
| TODO | TODO | N | TODO | TODO |

### Request Body

```json
{
  "TODO": "TODO"
}
```

### Response

```json
{
  "status": 200,
  "data": {}
}
```

## 3. 처리 흐름

1. TODO
2. TODO

## 4. 검증 조건

- TODO

## 5. 예외 처리

| HTTP Status | ErrorCode | 발생 조건 |
|---|---|---|
| 400 | `INVALID_INPUT_VALUE` | TODO |

## 6. DB / 도메인 영향

- TODO

## 7. 성능 / 보안 고려사항

- TODO

## 8. 테스트 케이스

### 성공

- TODO

### 실패

- TODO

## 9. 구현 체크리스트

- [ ] Controller / ApiDocs
- [ ] DTO
- [ ] Service
- [ ] Repository
- [ ] Tests
````
