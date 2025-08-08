# DDU-RU Backend

## 🚀 개발 환경 가이드

### 개발자 표준 환경

모든 개발자가 동일한 DB 환경에서 작업할 수 있도록 표준화된 개발 환경입니다.

```bash
# 1. 개발용 데이터베이스 시작
docker compose -f docker-compose.dev.yml up -d

# 2. Spring Boot 애플리케이션 실행
./gradlew bootRun
# yml에 `spring.profiles.active = dev` 설정해두어서 따로 프로파일 설정 없이 실행 가능
# [참고용] ./gradlew bootRun --args='--spring.profiles.active=dev'

# 또는 IDE에서 바로 실행 (기본 프로파일: dev)
```

#### 표준 개발 환경 설정
- **데이터베이스**: MySQL 8 (포트 3308)
- **접속 정보**: 
  - Host: localhost:3308
  - Username: root
  - Password: root
  - Database: dduru
- **프로파일**: dev
- **특징**: 환경변수 설정 불필요, 즉시 사용 가능

#### 개발용 데이터베이스 관리
```bash
# DB 중지
docker compose -f docker-compose.dev.yml down

# DB 데이터까지 완전 삭제 (초기화)
docker compose -f docker-compose.dev.yml down -v

# DB 상태 확인
docker compose -f docker-compose.dev.yml ps
```

### 프로덕션 배포

#### 전체 스택 배포 (프로덕션)
```bash
# .env 파일 설정 필요
docker compose --env-file .env up -d
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
