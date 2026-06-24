# 001. 패키지 구조 가이드

> 이 문서를 보면 새 기능을 만들 때 어떤 파일을 어디에 둬야 하는지 파악할 수 있습니다.

---

## 한 줄 요약

DDU-RU Backend는 **Package by Feature + Controller/Service/Repository 중심 Layered 구조**입니다. 도메인별 최상위 패키지 아래에 `controller`, `service`, `repository`, `domain`, `dto`, `exception`을 둡니다.

---

## 1. 기본 구조

```text
src/main/java/com/dduru/gildongmu/
├── auth/
├── user/
├── profile/
├── survey/
├── post/
├── participation/
├── journey/
├── chat/
├── report/
├── superhost/
├── destination/
├── s3/
├── verification/
├── admin/
├── notification/
├── fcm/
└── common/
```

도메인 내부의 일반적인 구조:

```text
{domain}/
├── controller/      # REST 또는 WebSocket controller, *ApiDocs 인터페이스
├── service/         # 트랜잭션, 비즈니스 흐름, 도메인 조합
├── repository/      # Spring Data JPA, Querydsl custom repository
├── domain/          # JPA entity, enum
├── dto/             # request/response/query/ws DTO
└── exception/       # BusinessException 하위 개별 예외
```

---

## 2. 코드 배치 기준

| 만들 것 | 위치 | 예시 |
|---|---|---|
| REST Controller | `{domain}/controller` | `PostController` |
| Swagger 문서 인터페이스 | `{domain}/controller` | `PostApiDocs` |
| Service | `{domain}/service` | `PostService`, `JourneyQueryService` |
| JPA Entity | `{domain}/domain` | `Post`, `JourneyMember` |
| Domain enum | `{domain}/domain/enums` | `PostStatus`, `ParticipationStatus` |
| Repository | `{domain}/repository` | `PostRepository`, `PostRepositoryImpl` |
| Request DTO | `{domain}/dto/request` | `PostCreateRequest` |
| Response DTO | `{domain}/dto/response` | `PostDetailResponse` |
| Query DTO | `{domain}/dto/query` | `ParticipationRetrieveQueryResult` |
| WebSocket DTO | `{domain}/dto/ws` | `ChatMessageBroadcastPayload` |
| 도메인 예외 | `{domain}/exception` | `PostNotFoundException` |
| 전역 설정 | `common/config` | `SecurityConfig`, `SwaggerConfig` |
| JWT | `common/jwt` | `JwtTokenProvider`, `JwtAuthenticationFilter` |
| 공통 응답/예외 | `common/dto`, `common/exception` | `ApiResult`, `ErrorResponse` |

---

## 3. 계층 역할

| 계층 | 책임 |
|---|---|
| Controller | HTTP/WebSocket 입력, 인증 사용자 주입, 요청 검증, 응답 포맷 적용 |
| ApiDocs | Swagger 어노테이션과 에러 응답 문서화 |
| Service | 트랜잭션, 권한 검증, 도메인 규칙 실행, 외부 서비스 조합 |
| Domain | JPA 매핑, 상태 변경 메서드, 핵심 불변식 |
| Repository | DB 조회/저장, Querydsl 조건 조회 |
| Exception | 비즈니스 실패를 `BusinessException`으로 표현 |
| Common | 여러 도메인이 공유하는 설정/응답/예외/JWT/validation |

---

## 4. 의존성 방향

일반적인 흐름:

```text
controller -> service -> repository -> domain
                      -> 다른 도메인 service/repository
```

현재 코드에서는 도메인 간 JPA 연관관계를 일부 직접 사용합니다. 예를 들어 `Post`는 `User`, `Destination`을 `@ManyToOne`으로 참조하고, `Journey`는 `Post`를 `@OneToOne`으로 참조합니다.

새 코드 작성 시 기준:

- 컨트롤러는 서비스에 위임하고 비즈니스 로직을 두지 않습니다.
- 서비스는 트랜잭션 경계와 권한 검증을 담당합니다.
- 엔티티는 상태 변경 메서드를 제공하고, 단순 setter 노출을 피합니다.
- 다른 도메인의 저장소를 직접 사용할 때는 순환 의존과 조회 비용을 검토합니다.
- 공통화는 실제로 여러 도메인에서 반복된 뒤 `common`으로 올립니다.

---

## 5. Swagger 문서 패턴

대부분의 컨트롤러는 `{Domain}ApiDocs` 인터페이스를 구현합니다.

```text
PostController implements PostApiDocs
ChatController implements ChatApiDocs
```

이 방식은 컨트롤러 구현과 Swagger 문서 어노테이션을 분리하기 위한 패턴입니다.

---

## 체크리스트

- [ ] 새 API에 `*Controller`와 `*ApiDocs`를 함께 추가했는가?
- [ ] 요청/응답 DTO를 `dto/request`, `dto/response`에 분리했는가?
- [ ] 비즈니스 예외는 개별 exception 클래스로 표현했는가?
- [ ] 공통 응답은 `ApiResult`를 사용했는가?
- [ ] 실패 응답은 `@ApiErrorResponses`로 Swagger에 문서화했는가?
- [ ] JPA 연관관계 추가 시 fetch 전략과 순환 참조 위험을 확인했는가?
