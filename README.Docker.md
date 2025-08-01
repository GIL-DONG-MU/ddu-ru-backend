# DDU-RU Backend

## 🚀 배포 가이드

### 로컬 개발 환경

#### 개발용 (권장)
```bash
# DB만 실행
docker-compose up mysql -d

# IDE에서 앱 실행 또는
./gradlew bootRun
```

#### 전체 스택 테스트
```bash
docker-compose up -d
```

### 운영 환경 배포

#### 1단계: 환경변수 설정
```bash
# .env.prod.example을 복사하여 .env.prod 생성
cp .env.prod.example .env.prod

# 실제 운영 값으로 수정
vim .env.prod
```

#### 2단계: 배포 실행
```bash
# 간단 배포
./deploy.sh

# 또는 직접 실행
docker-compose -f docker-compose.prod.yml --env-file .env.prod up -d
```

### GitHub Actions 자동 배포 설정

Repository Settings > Secrets에 다음 값들 추가:
- `HOST`: 서버 IP 주소
- `USERNAME`: SSH 사용자명  
- `PRIVATE_KEY`: SSH 개인키
- `PORT`: SSH 포트 (기본: 22)

## 🛠 기술 스택

- **Backend**: Spring Boot 3.5.3, Java 17
- **Database**: MySQL 8
- **Authentication**: OAuth2 (Kakao, Google), JWT
- **Containerization**: Docker, Docker Compose
