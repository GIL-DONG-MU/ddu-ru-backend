# 001. 프로젝트 개요

> 이 문서를 보면 DDU-RU Backend가 어떤 서비스를 지원하고, 어떤 기술과 핵심 정책으로 구성되는지 빠르게 파악할 수 있습니다.

---

## 한 줄 요약

**여행 모집글을 중심으로 동행자를 찾고, 승인된 멤버가 나의 여정과 채팅으로 협업하는 여행 동행자 매칭 플랫폼 백엔드**

---

## 1. 프로젝트 정보

| 항목 | 내용 |
|---|---|
| 서비스 | DDU-RU |
| 주제 | 여행 동행자 매칭 및 여행 워크스페이스 |
| 서버 | Spring Boot Monolith |
| 기본 패키지 | `com.dduru.gildongmu` |
| 기본 프로필 | `dev` |
| 로컬 API 문서 | `http://localhost:8080/swagger-ui.html` |
| Health Check | `http://localhost:8080/actuator/health` |

---

## 2. 서비스 범위

| 영역 | 설명 |
|---|---|
| 인증 | Kakao/Google OAuth ID Token 로그인, JWT access/refresh token 발급 |
| 프로필/온보딩 | 닉네임, 성별, 생년월일, 프로필 이미지, 설문 완료 상태 관리 |
| 설문 | 여행 성향 4축 점수, 활동 태그, 아바타 프로필 매칭 |
| 여행 모집글 | 여행지, 일정, 모집 정원, 선호 조건, 태그, 좋아요, 슈퍼호스트 노출 |
| 참여 신청 | 신청, 연락중 전환, 승인/거절, 취소 |
| 나의 여정 | 승인 후 협업 워크스페이스, 멤버십, 여정 게시판, 공지 |
| 채팅 | 1:1 채팅, 그룹 채팅, 메시지 조회, 읽음 처리, WebSocket/STOMP 전송 |
| 신고/관리자 | 게시글 신고, 관리자 신고 처리, 관리자 사용자/게시글 조회 |
| 파일 | S3 presigned URL 기반 이미지 업로드 |
| 휴대폰 인증 | CoolSMS 기반 인증번호 발송/검증 |

---

## 3. 기술 스택

| 구분 | 기술 |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.5.3 |
| Web | Spring MVC, Spring WebFlux(WebClient), WebSocket/STOMP |
| Security | Spring Security, JWT(`jjwt` 0.11.5), OAuth ID Token 검증 |
| ORM | Spring Data JPA, Hibernate, Querydsl |
| Database | MySQL 8, H2(test) |
| Migration | Flyway |
| Cache/Token Store | Redis, Lettuce |
| API Docs | springdoc-openapi |
| Storage | AWS S3 SDK |
| SMS | CoolSMS |
| Build/Test | Gradle, JUnit 5 |
| Deploy | Docker, Docker Compose, AWS ECR, AWS EC2, Nginx |

---

## 4. 핵심 정책

### 인증/인가

- Access token은 JWT이며 사용자 ID와 role claim을 포함합니다.
- Refresh token은 Redis에 저장하고, refresh 요청 시 저장된 token과 요청 token을 비교합니다.
- `/api/v1/admin/**` 요청은 DB에서 사용자 role을 다시 조회해 관리자 권한을 검증합니다.
- 일반 API는 JWT role claim을 우선 사용하고, role claim이 없거나 파싱 실패 시 DB 조회로 대체합니다.

### 데이터/시간

- 운영 DB는 MySQL, 테스트는 H2 MySQL mode를 사용합니다.
- 운영 프로필은 Flyway를 켜고 `classpath:db/migration`을 적용합니다.
- 공통 생성/수정 시간은 `BaseTimeEntity`의 `createdAt`, `modifiedAt`을 사용합니다.
- Soft delete는 도메인별로 구현 방식이 다릅니다. 예: `posts`, `journey_posts`는 `is_deleted`, `deleted_at`, `deleted_by`를 직접 관리합니다.

### API 응답/예외

- 성공 응답은 `ApiResult<T>` 형태로 `status`, `data`를 내려줍니다.
- 실패 응답은 `ErrorResponse` 형태로 `status`, `data.errorCode`, `data.field`, `data.message`를 내려줍니다.
- 비즈니스 예외는 중앙 `ErrorCode`와 개별 `BusinessException` 하위 클래스를 조합합니다.

---

## 다음 읽기

- 도메인별 책임과 흐름: [도메인 명세](./003-design/001-domain.md)
- 테이블/마이그레이션: [데이터 명세](./003-design/002-data.md)
- 코드 구조: [패키지 구조 가이드](./006-architecture/001-package-structure.md)
- 배포 구조: [배포 파이프라인](./007-operations/004-deployment.md)
