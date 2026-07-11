# 협업 / 컨벤션 문서

팀이 함께 지키는 GitHub, 코드 배치, 예외 처리 규칙을 정리합니다.

| 문서 | 한 줄 요약 |
|---|---|
| [팀 협업 컨벤션](./001-team.md) | Issue, branch, commit, PR 규칙 |
| [팀 FAQ](./002-faq.md) | DTO, ApiDocs, Service, Repository 등 자주 묻는 질문 |
| [예외 처리 전략](./003-exception.md) | 중앙 `ErrorCode` + 개별 예외 클래스 사용 기준 |
| [데이터 검증 및 문자열 정제 전략](./004-validation.md) | 계층별 검증 책임, trim 정책, 안티패턴 |
| [Swagger API 명세 작성 가이드](./005-swagger-api-docs.md) | Swagger UI에 노출되는 API 명세 작성 기준 |

## 파일명 / 제목 규칙

- 같은 폴더 안에서는 파일명과 문서 제목에 읽는 순서 숫자를 붙입니다.
- 예: `001-team.md`, `# 001. 팀 협업 컨벤션`
