# ADR-002: 중앙 ErrorCode + 개별 BusinessException 클래스

- 상태: Proposed
- 날짜: TODO
- 결정자: TODO

> TODO: 중앙 `ErrorCode`를 유지하기로 한 이유와 도메인별 ErrorCode 분리를 보류한 이유를 팀 결정 기록으로 보강합니다.

## 맥락

현재 공통 예외 처리는 `common/exception/ErrorCode.java` 중앙 enum과 `BusinessException`을 사용합니다. 각 도메인은 `PostNotFoundException`, `InvalidTokenException`, `ChatRoomNotFoundException`처럼 개별 예외 클래스를 두고 중앙 `ErrorCode`를 참조합니다.

## 결정

중앙 `ErrorCode` enum을 클라이언트 에러 식별과 메시지 관리의 단일 출처로 두고, 서비스/도메인 코드에서는 개별 예외 클래스를 던집니다.

## 대안

| 대안 | 기각/보류 이유 |
|---|---|
| `new BusinessException(ErrorCode.X)` 직접 사용 | 호출부 의미가 약해지고 예외 클래스명으로 로그 식별이 어려움 |
| 도메인별 ErrorCode enum 분리 | 파일 충돌은 줄지만 현재 구현 변경 범위가 큼 |
| 단순 `IllegalArgumentException` 사용 | HTTP status/errorCode/message 일관성이 깨짐 |

## 결과

- 서비스 코드에서 `throw new PostNotFoundException()`처럼 의도가 분명합니다.
- `GlobalExceptionHandler`는 `BusinessException` 하나로 비즈니스 예외를 처리합니다.
- 중앙 `ErrorCode`가 커지는 단점이 있어, 도메인이 더 커지면 분리 ADR이 필요합니다.

## 관련 문서

- [예외 처리 전략](../008-conventions/003-exception.md)
- [공통 기반 사용 가이드](../006-architecture/002-common-foundation.md)
