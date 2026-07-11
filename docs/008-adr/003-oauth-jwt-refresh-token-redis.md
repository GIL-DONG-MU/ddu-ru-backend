# ADR-003: OAuth JWT + Redis Refresh Token

- 상태: Proposed
- 날짜: TODO
- 결정자: TODO

> TODO: OAuth provider 범위, refresh token 저장소로 Redis를 선택한 배경, access/refresh 만료 정책의 결정 근거를 추가합니다.

## 맥락

현재 인증은 Kakao/Google OAuth ID Token을 검증해 사용자를 찾거나 생성하고, access token과 refresh token을 발급합니다. Refresh token은 Redis에 저장하며, 재발급 시 저장된 token과 요청 token을 비교합니다.

## 결정

- OAuth provider는 Kakao와 Google을 지원합니다.
- Access token은 JWT로 발급하고 인증 필터에서 검증합니다.
- Refresh token은 JWT이면서 Redis에 저장해 서버 측 무효화를 가능하게 합니다.
- Access/refresh 만료 설정은 `Duration`으로 관리하고, refresh 재발급은 JWT 형식/타입 검증 이후 Redis 저장값을 비교합니다.
- Admin API는 JWT role claim만 신뢰하지 않고 DB role을 다시 조회합니다.

## 대안

| 대안 | 기각/보류 이유 |
|---|---|
| 서버 세션 기반 인증 | 모바일/API 중심 구조와 맞지 않고 stateless 운영 장점이 줄어듦 |
| Refresh token을 DB에 저장 | 영속성은 높지만 토큰 만료/삭제 처리에 Redis보다 무거움 |
| Access token role claim만 항상 신뢰 | 관리자 권한 변경 반영이 늦어질 수 있음 |

## 결과

- 일반 API는 JWT 기반으로 가볍게 인증됩니다.
- Refresh token 탈취/로그아웃 대응을 Redis 삭제로 처리할 수 있습니다.
- Redis 장애 시 로그인/refresh/logout 일부 흐름에 영향이 있으므로 운영 모니터링이 필요합니다.

## 관련 문서

- [공통 기반 사용 가이드](../005-architecture/002-common-foundation.md)
- [인프라 명세](../006-operations/003-infrastructure.md)
