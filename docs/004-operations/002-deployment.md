# 002. 배포 파이프라인

> 이 문서를 보면 CI/CD가 어떤 조건에서 실행되고, ECR 이미지가 어떻게 EC2 blue-green 배포로 이어지는지 파악할 수 있습니다.

---

## 한 줄 요약

CI가 성공한 `dev` 브랜치 push 또는 수동 실행을 기준으로 Docker 이미지를 ECR에 push하고, EC2에서 `deploy.sh`가 idle app container를 띄운 뒤 Nginx upstream을 전환합니다.

---

## 1. CI

파일: `.github/workflows/ci.yml`

트리거:

- `main`, `dev` branch push
- `main`, `dev` target pull request
- PR에서는 `**.md` 변경만 있는 경우 무시

주요 단계:

1. Checkout
2. final newline check
3. JDK 17 setup
4. `docker compose up -d --wait`
5. `./gradlew clean build -Duser.language=ko -Duser.country=KR`
6. `docker compose down -v`

---

## 2. CD

파일: `.github/workflows/cd.yml`

트리거:

- `CI - Continuous Integration` workflow가 `dev` 브랜치 push에서 성공
- `workflow_dispatch` 수동 실행

주요 단계:

1. AWS credentials 설정
2. ECR login
3. Docker image build
4. ECR에 `${github.sha}`, `latest` tag push
5. `deploy.sh`, `docker-compose.prod.yml`, `deployment/`을 tar bundle로 패키징
6. EC2 `~/app`으로 bundle 복사
7. EC2에서 환경변수 export 후 `deploy.sh` 실행

---

## 3. 배포 스크립트

파일: `deploy.sh`

핵심 동작:

- 필수 환경변수(`AWS_REGION`, `ECR_REGISTRY`, `ECR_REPOSITORY`, `IMAGE_TAG`, `CORS_ALLOWED_ORIGIN_PATTERNS`, `WEBSOCKET_ALLOWED_ORIGIN_PATTERNS`) 검증
- `aws`, `docker`, Docker Compose v2 존재 확인
- Nginx upstream 파일 존재 확인
- ECR login
- active/idle color 계산
- Redis 기동 보장
- idle app 이미지 pull 및 container 기동
- `/actuator/health/readiness` readiness 확인
- Nginx active upstream 전환
- readiness 또는 Nginx 전환 실패 시 app/nginx 최근 로그 출력
- drain 후 이전 색상 app 중지

---

## 4. Readiness

운영 app container healthcheck:

```text
curl -fs http://localhost:8080/actuator/health/readiness
```

`application-prod.yml`의 readiness group:

- `readinessState`
- `db`
- `redis`

즉 DB 또는 Redis 연결 문제가 있으면 신규 container가 active upstream으로 전환되지 않습니다.

---

## 5. 롤백/장애 대응

현재 자동 rollback 범위:

- idle app readiness 실패 시 idle container stop 후 배포 실패.
- Nginx 설정 검증 또는 reload 실패 시 upstream 파일을 이전 색상으로 복구.

수동 대응:

- 문제가 된 커밋 revert 후 다시 `dev`에 push.
- 이전 ECR image tag를 알고 있으면 `IMAGE_TAG`를 이전 sha로 지정해 수동 배포.
- upstream만 되돌려야 하면 `active-upstream.{이전색상}.inc`를 `active-upstream.inc`로 복사한 뒤 `nginx -t`와 reload를 수행합니다.
- EC2에서 `docker compose -f docker-compose.prod.yml ps`와 app/nginx logs 확인.

---

## 6. 확인 포인트

| 위치 | 확인 내용 |
|---|---|
| GitHub Actions CI | Gradle build/test 성공 여부 |
| GitHub Actions CD | ECR push, scp, ssh deploy 성공 여부 |
| ECR | sha/latest tag push 여부 |
| EC2 | `docker compose ps`, app logs, nginx logs |
| App | `/actuator/health/readiness` |
| Swagger | `/swagger-ui.html` |

---

## 개선 후보

- CD concurrency 설정.
- 배포 실패 시 GitHub Actions artifact로 compose logs 자동 수집.
- 이전 `IMAGE_TAG`를 자동 기록하는 rollback runbook 보강.
- 운영 `.env`/Secrets 관리 정책 분리.
- HTTPS 인증서 갱신 runbook 추가.
