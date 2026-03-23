# DDU-RU Backend 🌟

> 여행 동행자 매칭 플랫폼 DDU-RU의 백엔드 서버

## 📖 프로젝트 소개

DDU-RU는 혼자 여행하기 부담스러운 사람들을 위한 **여행 동행자 매칭 플랫폼**입니다.  
사용자들이 여행 계획을 공유하고, 취향이 맞는 동행자를 찾을 수 있는 서비스를 제공합니다.

## 🛠 기술 스택

### Backend
- **Framework**: Spring Boot 3.5.3
- **Language**: Java 17
- **Security**: Spring Security + JWT
- **Database**: MySQL 8
- **ORM**: Spring Data JPA

### Authentication
- **OAuth2**: Kakao, Google
- **JWT**: 토큰 기반 인증

### DevOps
- **Containerization**: Docker, Docker Compose
- **CI/CD**: GitHub Actions
- **Deployment**: AWS EC2
- **Build Tool**: Gradle

### Future Stack
- **Real-time Communication**: WebSocket (채팅)
- **Maps Integration**: Google Maps API (위치/경로)
- **Calendar Integration**: Google Calendar API (일정)
- **Image Storage**: AWS S3 (사진 업로드)
- **Push Notification**: FCM (알림)

## 🚀 빠른 시작

### 로컬 개발 환경 설정

#### 1. 저장소 클론
```bash
git clone git@github.com:GIL-DONG-MU/ddu-ru-backend.git
cd ddu-ru-backend
```

#### 2. 환경변수 설정
```bash
# .env 파일이 이미 설정되어 있습니다
# 필요시 OAuth 설정값 수정
```

#### 3. 개발 서버 실행 (권장)
```bash
# MySQL, Redis만 Docker로 실행
docker-compose up -d

# IDE에서 실행 또는
./gradlew bootRun
```

### 애플리케이션 접속
- **Local**: http://localhost:8080
- **Health Check**: http://localhost:8080/actuator/health

## 📚 문서

- **[배포 가이드](README.Docker.md)**: Docker 및 프로덕션 배포 방법
- **[DB 마이그레이션 (Flyway)](FLYWAY.md)**: 스키마 마이그레이션 사용 방법
- **API 문서**: http://localhost:8080/swagger-ui.html

## 🏗 프로젝트 구조

```
src/
├── main/
│   ├── java/com/dduru/gildongmu/
│   │   ├── auth/           # 인증/인가 (OAuth2, JWT)
│   │   ├── post/           # 게시글 관리 (모집글 CRUD)
│   │   ├── user/           # 사용자 관리 (프로필, 팔로우) - 예정
│   │   ├── chat/           # 채팅 기능 (WebSocket) - 예정  
│   │   ├── travel/         # 여행 관련 (일정, 경로) - 예정
│   │   ├── review/         # 후기 시스템 - 예정
│   │   ├── common/         # 공통 유틸리티
│   │   └── config/         # 설정 클래스
│   └── resources/
│       ├── application.yml
│       ├── application-dev.yml
│       └── application-prod.yml
└── test/                   # 테스트 코드
```

## 🤝 협업 규칙

1. 이 저장소를 Fork합니다
2. 기능 issue를 생성합니다 (`#30`)
3. 기능 브랜치를 생성합니다 (`git checkout -b feature/#30-feature-name`)
4. 변경사항을 커밋합니다 (`git commit -m '[#30] feat(???): Add amazing feature'`)
5. 브랜치에 Push합니다 (`git push origin feat/#30-feature-name`)
6. Pull Request를 생성합니다

## 📄 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다.

## 👥 팀

- **Backend Developer**: [@shinheekim](https://github.com/shinheekim), [@kimjaejoong](https://github.com/jaejoong0529)

---

<div align="center">

**DDU-RU Backend** - 함께하는 여행의 시작, 뚜르 🌍

</div>
