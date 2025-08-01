# DDU-RU Backend

## 🚀 배포 가이드

### 로컬 개발 환경

#### 개발용 (권장)
```bash
# 1. app 컨테이너는 실행 X
docker-compose up -d --scale app=0
# 2. 특정 서비스만 실행
docker-compose up mysql redis -d

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
# .env.prod 파일 수정 (이미 생성됨)
vim .env.prod

# 다음 값들을 실제 운영 값으로 변경:
# - KAKAO_CLIENT_ID, KAKAO_CLIENT_SECRET
# - GOOGLE_CLIENT_ID, GOOGLE_CLIENT_SECRET  
# - JWT_SECRET (새로운 강력한 키 생성)
# - MYSQL_ROOT_PASSWORD (강력한 패스워드)
# - 도메인 URL들
```

#### 2단계: 배포 실행
```bash
# 자동 배포 스크립트 (권장)
./deploy.sh

# 또는 수동 실행
docker-compose --env-file .env.prod up -d
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
