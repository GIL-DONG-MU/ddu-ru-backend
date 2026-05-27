# 003. Docker 운영 가이드

> 이 문서를 보면 로컬 개발용 Docker Compose와 운영용 Compose 구성을 파악할 수 있습니다.

---

## 한 줄 요약

로컬은 MySQL 8과 Redis만 Docker로 띄우고 애플리케이션은 IDE 또는 Gradle로 실행합니다. 운영은 `docker-compose.prod.yml`에서 `app_blue`, `app_green`, `redis`, `nginx`를 관리합니다.

---

## 1. 로컬 개발 환경

```bash
docker compose up -d
./gradlew bootRun
```

`application.yml`의 기본 active profile은 `dev`입니다.

### 로컬 인프라

| 서비스 | 이미지 | Host Port | Container Port | 비고 |
|---|---|---:|---:|---|
| MySQL | `mysql:8` | `3307` | `3306` | database `dduru`, root password `root` |
| Redis | `redis:7-alpine` | `6379` | `6379` | appendonly enabled |

### DB 접속 정보

| 항목 | 값 |
|---|---|
| Host | `localhost` |
| Port | `3307` |
| Username | `root` |
| Password | `root` |
| Database | `dduru` |
| JDBC URL | `jdbc:mysql://localhost:3307/dduru?characterEncoding=UTF-8&useUnicode=true&serverTimezone=Asia/Seoul` |

---

## 2. 로컬 정리

```bash
docker compose down
```

볼륨까지 삭제하려면:

```bash
docker compose down -v
```

---

## 3. 운영 Compose

운영 파일:

```bash
docker compose -f docker-compose.prod.yml up -d
```

운영 구성:

- `app_blue`
- `app_green`
- `redis`
- `nginx`

운영 배포는 직접 `up -d`를 실행하기보다 GitHub Actions와 `deploy.sh`를 통해 blue-green 방식으로 수행합니다.

---

## 4. 운영 환경변수

운영 compose는 아래 계열의 환경변수를 필요로 합니다.

- ECR: `ECR_REGISTRY`, `ECR_REPOSITORY`, `IMAGE_TAG`
- RDS: `RDS_ENDPOINT`, `RDS_PORT`, `RDS_USERNAME`, `RDS_PASSWORD`, `RDS_DATABASE_NAME`
- JPA: `DDL_AUTO`
- OAuth/JWT: `KAKAO_*`, `GOOGLE_*`, `JWT_*`
- Redis: `REDIS_HOST`, `REDIS_PORT`
- S3: `AWS_REGION`, `AWS_S3_BUCKET`, `AWS_S3_ACCESS_KEY`, `AWS_S3_SECRET_KEY`
- SMS: `COOLSMS_API_KEY`, `COOLSMS_API_SECRET`, `COOLSMS_FROM_NUMBER`
- Profile: `PROFILE_DEFAULT_IMAGE_URL`

자세한 배포 흐름은 [배포 파이프라인](./002-deployment.md)을 확인합니다.

---

## 5. 확인 명령

```bash
docker compose ps
curl http://localhost:8080/actuator/health
```

운영 서버에서는 compose 파일을 명시합니다.

```bash
docker compose -f docker-compose.prod.yml ps
```
