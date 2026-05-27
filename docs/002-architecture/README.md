# 아키텍처 / 코드 구조 문서

실제 코드가 어떤 패키지 구조와 공통 기반 위에서 작성되는지 설명합니다.

| 문서 | 한 줄 요약 |
|---|---|
| [패키지 구조 가이드](./001-package-structure.md) | 도메인별 `controller/service/repository/domain/dto/exception` 구조와 코드 위치 |
| [JPA 엔티티 가이드](./002-jpa.md) | 엔티티 작성, 연관관계, soft delete, Querydsl 기준 |
| [공통 기반 사용 가이드](./003-common-foundation.md) | `ApiResult`, `ErrorResponse`, `ErrorCode`, JWT, Swagger 문서 패턴 |

> 이 문서는 Delicious_food_delivery의 카테고리 양식을 따르지만, 내용은 dduru backend의 실제 코드 구조를 기준으로 합니다.

## 파일명 / 제목 규칙

- 같은 폴더 안에서는 파일명과 문서 제목에 읽는 순서 숫자를 붙입니다.
- 예: `001-package-structure.md`, `# 001. 패키지 구조 가이드`

## 아키텍처 문서 양식

코드 구조, 공통 모듈, 기술 구현 규칙을 설명할 때 사용합니다.

````markdown
# 001. 아키텍처 문서 제목

> 이 문서를 보면 어떤 코드 구조나 구현 규칙을 이해할 수 있는지 설명합니다.

---

## 한 줄 요약

팀이 따라야 할 핵심 구조나 규칙을 한 문장으로 씁니다.

---

## 1. 적용 범위

이 규칙이 적용되는 패키지, 레이어, 기능 범위를 적습니다.

## 2. 현재 구조

```text
TODO
```

## 3. 책임 분리

| 구성요소 | 책임 |
|---|---|
| TODO | TODO |

## 4. 작성 규칙

- TODO
- TODO

## 5. 예시

```java
// TODO
```

## 6. 체크리스트

- [ ] TODO
- [ ] TODO

## 관련 문서

- TODO
````
