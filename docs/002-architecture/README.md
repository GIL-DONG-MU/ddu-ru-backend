# 아키텍처 / 코드 구조 문서

실제 코드가 어떤 패키지 구조와 공통 기반 위에서 작성되는지 설명합니다.

| 문서 | 한 줄 요약 |
|---|---|
| [아키텍처 문서 템플릿](./000-template.md) | 코드 구조, 공통 모듈, 기술 구현 규칙 작성 양식 |
| [패키지 구조 가이드](./001-package-structure.md) | 도메인별 `controller/service/repository/domain/dto/exception` 구조와 코드 위치 |
| [JPA 엔티티 가이드](./002-jpa.md) | 엔티티 작성, 연관관계, soft delete, Querydsl 기준 |
| [공통 기반 사용 가이드](./003-common-foundation.md) | `ApiResult`, `ErrorResponse`, `ErrorCode`, JWT, Swagger 문서 패턴 |

> 이 문서는 Delicious_food_delivery의 카테고리 양식을 따르지만, 내용은 dduru backend의 실제 코드 구조를 기준으로 합니다.

## 파일명 / 제목 규칙

- 같은 폴더 안에서는 파일명과 문서 제목에 읽는 순서 숫자를 붙입니다.
- 예: `001-package-structure.md`, `# 001. 패키지 구조 가이드`
- `000-template.md`는 작성 양식입니다. 실제 내용 문서는 `001-*`부터 시작합니다.

## 템플릿

- [아키텍처 문서 템플릿](./000-template.md)
