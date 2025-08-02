# DDU-RU Backend

## 🚀 배포 가이드

### 로컬 개발 환경

#### 개발용 (권장)
```bash
# .env 파일 생성(.env.prod 참고)
cp .env.prod .env

# app 컨테이너는 실행 X
docker compose --profile infra --env-file .env up -d
  # docker-compose up mysql redis -d

# IDE에서 앱 실행 또는
./gradlew bootRun
```

#### 전체 스택 테스트
```bash
docker-compose up -d
```

### GitHub Actions 자동 배포 설정

Repository Settings > Secrets에 다음 값들 추가:
- `DOCKER_USERNAME`: Docker Hub 사용자명
- `DOCKER_PASSWORD`: Docker Hub 비밀번호
- `DOCKER_REPOSITORY`: Docker Hub 저장소 이름
- `AWS_ACCESS_KEY_ID`: AWS 액세스 키

## 🛠 기술 스택

- **Backend**: Spring Boot 3.5.3, Java 17
- **Database**: MySQL 8
- **Authentication**: OAuth2 (Kakao, Google), JWT
- **Containerization**: Docker, Docker Compose
