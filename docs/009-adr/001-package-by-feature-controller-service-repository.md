# ADR-001: Package by Feature + Controller/Service/Repository 구조

- 상태: Proposed
- 날짜: TODO
- 결정자: TODO

> TODO: 이 구조를 팀이 언제/왜 선택했는지, 초기 설계 회의나 PR 링크를 추가합니다.

## 맥락

현재 코드는 `com.dduru.gildongmu` 아래에 `post`, `journey`, `chat`, `profile` 같은 도메인별 최상위 패키지를 두고, 각 도메인 안에 `controller`, `service`, `repository`, `domain`, `dto`, `exception`을 배치합니다.

## 결정

도메인별 패키지 구조를 유지하고, 내부 계층은 Spring Boot에서 익숙한 Controller/Service/Repository 중심으로 구성합니다.

```text
{domain}/
├── controller
├── service
├── repository
├── domain
├── dto
└── exception
```

## 대안

| 대안 | 기각/보류 이유 |
|---|---|
| 최상위 `controller/service/repository` 계층형 | 도메인별 응집도가 낮아지고 기능 단위 탐색이 어려움 |
| DDD/Hexagonal 전면 적용 | 현재 코드와 맞지 않고 추가 추상화 비용이 큼 |
| 멀티모듈 분리 | 운영/빌드 복잡도가 커져 현 단계에서는 보류 |

## 결과

- 기능 단위로 관련 파일을 찾기 쉽습니다.
- 도메인별 작업 범위가 명확합니다.
- 엄격한 dependency rule은 없으므로, 도메인 간 직접 의존이 늘어날 때 리뷰 기준이 필요합니다.

## 관련 문서

- [패키지 구조 가이드](../006-architecture/001-package-structure.md)
