# DB 마이그레이션 (Flyway)

## 개요

애플리케이션 기동 시 `src/main/resources/db/migration` 아래 SQL 스크립트가 **버전 순서대로** MySQL에 적용됩니다. Hibernate보다 **먼저** 실행됩니다.

## 스크립트 위치·이름

| 경로 | 설명 |
|------|------|
| `src/main/resources/db/migration/` | Flyway 기본 위치 (`classpath:db/migration`) |

파일명 규칙: `V{버전}__설명.sql` (예: `V5__add_column_foo.sql`). 버전은 **한 번만** 적용되며, 이미 적용된 버전은 건너뜁니다.

## 로컬(dev)에서 쓰는 방법

1. MySQL이 떠 있고 `application-dev.yml`의 JDBC URL·계정이 맞는지 확인합니다.
2. `./gradlew bootRun` (기본 프로파일 `dev`)으로 실행합니다.
3. 첫 기동 시 `flyway_schema_history` 테이블이 생기고, 미적용 마이그레이션이 실행됩니다.

## 테스트

`application-test.yml`에서 `spring.flyway.enabled: false`로 두었습니다. 테스트는 H2 + JPA `ddl-auto`를 사용하므로 **Flyway 스크립트는 테스트에서 실행되지 않습니다.**

## 운영(prod)

- 배포 시 같은 방식으로 마이그레이션이 실행됩니다.
- **이미 수동으로 만든 스키마**가 있는 DB에 처음 붙일 때는 백업 후 적용하고, `baseline-on-migrate` 등 동작을 검토하는 것이 안전합니다.
- 스키마를 마이그레이션으로만 관리하려면 `jpa.hibernate.ddl-auto`를 `validate` 또는 `none`으로 두는 것을 권장합니다.

## Flyway 끄기 (임시)

환경 변수 또는 설정:

```yaml
spring:
  flyway:
    enabled: false
```

## 새 마이그레이션 추가 절차

1. `db/migration`에 다음 버전 번호의 파일을 추가합니다. (현재 `V4`까지 있으면 `V5__...sql`)
2. MySQL 문법에 맞게 작성합니다.
3. 로컬에서 기동해 적용 여부를 확인한 뒤 커밋합니다.

## 의존성

`build.gradle`에 `flyway-core`, `flyway-mysql`이 포함되어 있습니다.
