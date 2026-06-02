# 000. API 설계 문서 템플릿

> Swagger보다 상세한 API 설계가 필요할 때 사용하는 양식입니다.

---

## 사용 규칙

- `000-template.md`는 작성 양식이며 실제 API 문서가 아닙니다.
- 실제 API 문서는 `001-*`부터 시작합니다.
- 제목에도 번호를 붙입니다. 예: `# 001. Chat Message Retrieve API Design`
- 구현 전 설계 문서는 "권장/계획"과 "현재 구현"을 구분해서 씁니다.

---

## 템플릿

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
