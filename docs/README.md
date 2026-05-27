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

---

## 문서 구조

```text
docs/
├── overview.md
├── 001-design/
│   ├── 001-domain.md
│   ├── 002-data.md
│   └── flows/
├── 002-architecture/
│   ├── 001-package-structure.md
│   ├── 002-jpa.md
│   └── 003-common-foundation.md
├── 003-api/
│   ├── 001-chat-message-retrieve.md
│   └── 002-chat-read-receipt.md
├── 004-operations/
│   ├── 001-infrastructure.md
│   ├── 002-deployment.md
│   ├── 003-docker.md
│   └── 004-flyway.md
├── 005-conventions/
│   ├── 001-team.md
│   ├── 002-exception.md
│   └── 003-faq.md
├── 006-adr/
└── 007-troubleshooting/
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
- 같은 폴더 안의 문서 파일명과 문서 제목에는 읽는 순서대로 숫자를 붙입니다. 예: `001-domain.md`, `# 001. 도메인 명세`
- 코드에서 확인할 수 없는 결정 배경은 추정하지 않고 `TODO`로 남깁니다.

---

## 공통 문서 양식

새 문서를 만들 때 아래 양식을 기본으로 사용하고, 폴더별 README에 있는 전용 양식으로 보완합니다.

```markdown
# 001. 문서 제목

> 이 문서를 보면 무엇을 알 수 있는지 한 문장으로 설명합니다.
>
> 언제 다시 보면 좋은지도 필요하면 적습니다.

---

## 한 줄 요약

문서의 결론이나 핵심 규칙을 한 문장으로 씁니다.

---

## 1. 배경

이 문서가 필요한 이유와 다루는 범위를 적습니다.

## 2. 현재 상태

현재 코드, 설정, 운영 방식에서 확인한 사실을 적습니다.

## 3. 규칙 / 설계 / 절차

팀이 따라야 할 규칙이나 구현/운영 절차를 적습니다.

## 4. 주의사항

예외, 한계, 아직 알 수 없는 부분을 적습니다.

> TODO: 코드만으로 확인할 수 없는 의사결정 배경, 담당자, 운영 정보는 여기에 남깁니다.

## 관련 문서

- TODO
```
