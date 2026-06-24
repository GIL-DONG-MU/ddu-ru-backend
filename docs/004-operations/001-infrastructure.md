# 001. 인프라 명세

> 이 문서를 보면 DDU-RU Backend가 로컬과 운영에서 어떤 인프라 컴포넌트로 동작하는지 파악할 수 있습니다.

---

## 한 줄 요약

로컬은 MySQL/Redis를 Docker Compose로 띄우고, 운영은 ECR 이미지와 EC2 Docker Compose, RDS MySQL, Redis, Nginx blue-green 라우팅으로 구성합니다.

---

## 1. 구성 요소

| 구성 | 로컬/dev | 운영/prod |
|---|---|---|
| Application | IDE 또는 `./gradlew bootRun` | Docker container `app_blue`, `app_green` |
| Database | MySQL 8 container, `localhost:3307` | AWS RDS MySQL |
| Cache/Token | Redis 7 container, `localhost:6379` | Redis container |
| Reverse Proxy | 없음 | Nginx container |
| Image Registry | 없음 | AWS ECR |
| Storage | S3 설정값 필요 | AWS S3 |
| SMS | CoolSMS 설정값 필요 | CoolSMS |

---

## 2. 로컬 개발 환경

`docker-compose.yml`:

- `mysql`: MySQL 8, host port `3307`, database `dduru`, root password `root`
- `redis`: Redis 7 alpine, host port `6379`

`application-dev.yml`:

- JDBC URL: `jdbc:mysql://localhost:3307/dduru`
- Flyway disabled
- JPA `ddl-auto=update`
- SQL logging enabled

---

## 3. 운영 환경

`docker-compose.prod.yml`:

- `app_blue`, `app_green`: 동일 이미지 템플릿을 사용하는 blue-green app container
- `redis`: Redis container
- `nginx`: 80/443 listener, active upstream include 파일로 blue/green 전환

운영 DB는 compose 내부가 아니라 RDS 환경변수로 연결합니다.

필수 주요 환경변수:

- `RDS_ENDPOINT`, `RDS_PORT`, `RDS_USERNAME`, `RDS_PASSWORD`, `RDS_DATABASE_NAME`
- `DDL_AUTO` (기본값 `validate`)
- `JWT_SECRET`, `JWT_EXPIRATION`, `JWT_REFRESH_EXPIRATION` (`30m`, `7d`, `PT1H` 같은 duration 형식 권장)
- `CORS_ALLOWED_ORIGIN_PATTERNS`, `WEBSOCKET_ALLOWED_ORIGIN_PATTERNS` (comma-separated allowlist)
- `REDIS_HOST`, `REDIS_PORT`
- `AWS_REGION`, `AWS_S3_BUCKET`, `AWS_S3_ACCESS_KEY`, `AWS_S3_SECRET_KEY`
- `COOLSMS_API_KEY`, `COOLSMS_API_SECRET`, `COOLSMS_FROM_NUMBER`
- `KAKAO_*`, `GOOGLE_*`
- `FCM_SERVICE_ACCOUNT_PATH` — Firebase 서비스 계정 JSON 경로 (`fcm.service-account-path`, 기본값 `firebase/service-account.json`)
- `ECR_REGISTRY`, `ECR_REPOSITORY`, `IMAGE_TAG`

---

## 4. Nginx Blue-Green

관련 파일:

- `deployment/nginx/nginx.conf`
- `deployment/nginx/conf.d/default.conf`
- `deployment/nginx/conf.d/upstreams/active-upstream.blue.inc`
- `deployment/nginx/conf.d/upstreams/active-upstream.green.inc`
- `deployment/nginx/conf.d/upstreams/active-upstream.inc` (운영 서버에서 활성 upstream으로 생성/교체)

전환 방식:

1. 현재 active 색상을 `active-upstream.inc`에서 확인합니다.
2. idle app container에 새 이미지를 띄웁니다.
3. readiness가 통과하면 active upstream include 파일을 idle 색상으로 교체합니다.
4. `nginx -t` 후 reload합니다.
5. drain 시간 이후 이전 app container를 중지합니다.

---

## 5. 현재 한계와 개선 후보

- 운영 환경변수가 GitHub Actions SSH step에서 직접 export됩니다.
- 자동 rollback은 Nginx 전환 실패/신규 컨테이너 readiness 실패 방어 중심입니다.
- DB migration 실패 시 애플리케이션 readiness가 실패하며 `deploy.sh`가 신규 app container 로그를 출력합니다.
- HTTPS 인증서 운영 방식은 `deployment/nginx/ssl`, `certbot` 경로 기준으로 추가 문서화가 필요합니다.

> TODO: 실제 EC2 스펙, 보안그룹, 도메인, HTTPS 인증서 발급/갱신 절차를 보강하면 운영 문서가 완성됩니다.
