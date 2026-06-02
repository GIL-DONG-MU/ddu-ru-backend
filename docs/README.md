# 설계 문서 인덱스

> 이 폴더는 DDU-RU Backend의 설계, 아키텍처, 운영, 협업 문서를 모아둔 곳입니다.
> Delicious_food_delivery 문서 양식처럼 주제별 폴더와 각 폴더 인덱스로 정리합니다.

---

## 빠르게 찾기

| 상황 | 읽을 문서 |
|---|---|
| 처음 봐요 | [프로젝트 개요](./overview.md) -> [도메인 명세](./001-design/001-domain.md) -> [패키지 구조](./002-architecture/001-package-structure.md) |
| 도메인/권한/상태 흐름 | [도메인 명세](./001-design/001-domain.md) |
| 테이블/마이그레이션 기준 | [데이터 명세](./001-design/002-data.md) -> [Flyway 가이드](./004-operations/004-flyway.md) |
| "파일 어디 두지?" | [패키지 구조 가이드](./002-architecture/001-package-structure.md) |
| 공통 응답/예외/JWT | [공통 기반 사용 가이드](./002-architecture/003-common-foundation.md) |
| 로컬 실행/Docker | [Docker 운영 가이드](./004-operations/003-docker.md) |
| 배포/Blue-Green | [배포 파이프라인](./004-operations/002-deployment.md) |
| PR/커밋/브랜치 | [팀 협업 컨벤션](./005-conventions/001-team.md) |
| 채팅 API 설계 | [API 설계 문서](./003-api/) |
| 의사결정 기록 | [ADR](./006-adr/) |
| 새 문서 작성 | [공통 문서 템플릿](./000-template.md) |

---

## 문서 구조

```text
docs/
├── 000-template.md
├── overview.md
├── 001-design/
│   ├── 000-template.md
│   ├── 001-domain.md
│   ├── 002-data.md
│   └── flows/
├── 002-architecture/
│   ├── 000-template.md
│   ├── 001-package-structure.md
│   ├── 002-jpa.md
│   └── 003-common-foundation.md
├── 003-api/
│   ├── 000-template.md
│   ├── 001-chat-message-retrieve.md
│   └── 002-chat-read-receipt.md
├── 004-operations/
│   ├── 000-template.md
│   ├── 001-infrastructure.md
│   ├── 002-deployment.md
│   ├── 003-docker.md
│   └── 004-flyway.md
├── 005-conventions/
│   ├── 000-template.md
│   ├── 001-team.md
│   ├── 002-exception.md
│   └── 003-faq.md
├── 006-adr/
│   ├── 000-template.md
│   ├── 001-package-by-feature-controller-service-repository.md
│   ├── 002-central-error-code-with-individual-exceptions.md
│   ├── 003-oauth-jwt-refresh-token-redis.md
│   ├── 004-participation-journey-membership-separation.md
│   └── 005-blue-green-deployment-with-nginx-ecr.md
└── 007-troubleshooting/
    └── 000-template.md
```

### 001-design - 설계

서비스가 무엇을 하는지, 어떤 도메인과 데이터를 다루는지 정의합니다.

- [도메인 명세](./001-design/001-domain.md)
- [데이터 명세](./001-design/002-data.md)
- [제품/기능 흐름](./001-design/flows/)

### 002-architecture - 코드 구조

실제 코드가 어떤 패키지 규칙과 공통 기반 위에서 작성되는지 설명합니다.

- [패키지 구조 가이드](./002-architecture/001-package-structure.md)
- [JPA 엔티티 가이드](./002-architecture/002-jpa.md)
- [공통 기반 사용 가이드](./002-architecture/003-common-foundation.md)

### 003-api - API 설계

Swagger보다 상세한 API별 처리 흐름, 예외, 테스트 케이스를 정리합니다.

- [API 설계 문서](./003-api/)

### 004-operations - 운영/배포

로컬 실행, 운영 인프라, 배포 파이프라인, DB 마이그레이션을 설명합니다.

- [인프라 명세](./004-operations/001-infrastructure.md)
- [배포 파이프라인](./004-operations/002-deployment.md)
- [Docker 운영 가이드](./004-operations/003-docker.md)
- [Flyway 가이드](./004-operations/004-flyway.md)

### 005-conventions - 협업 규칙

팀이 함께 지키는 브랜치, 커밋, PR, 예외 처리, 자주 묻는 질문을 정리합니다.

- [팀 협업 컨벤션](./005-conventions/001-team.md)
- [예외 처리 전략](./005-conventions/002-exception.md)
- [팀 FAQ](./005-conventions/003-faq.md)

### 006-adr - 의사결정 기록

- [ADR](./006-adr/) - 주요 기술/설계 의사결정 기록

### 007-troubleshooting - 트러블슈팅

- [트러블슈팅](./007-troubleshooting/) - 이슈 해결 기록

---

## 업데이트 규칙

- 설계나 운영 방식이 바뀌면 관련 문서를 같은 PR에서 수정합니다.
- 새 문서를 추가하면 해당 폴더 `README.md`와 이 인덱스에 링크를 추가합니다.
- `000-template.md`는 작성 양식입니다. 실제 내용 문서는 `001-*`부터 시작합니다.
- 같은 폴더 안의 문서 파일명과 문서 제목에는 읽는 순서대로 숫자를 붙입니다. 예: `001-domain.md`, `# 001. 도메인 명세`
- 코드에서 확인할 수 없는 결정 배경은 추정하지 않고 `TODO`로 남깁니다.

---

## 템플릿

- [공통 문서 템플릿](./000-template.md)
- 각 폴더의 전용 템플릿은 해당 폴더의 `000-template.md`를 사용합니다.
