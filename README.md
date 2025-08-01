# DDU-RU Backend 🌟

> 여행 동행자 매칭 플랫폼 DDU-RU의 백엔드 서버

## 📖 프로젝트 소개

DDU-RU는 혼자 여행하기 부담스러운 사람들을 위한 **여행 동행자 매칭 플랫폼**입니다.  
사용자들이 여행 계획을 공유하고, 취향이 맞는 동행자를 찾을 수 있는 서비스를 제공합니다.

## ✨ MVP 기능

### 🔐 **1. 로그인/회원가입**
- **소셜 로그인**: 구글, 카카오 OAuth2 인증
- **JWT 기반**: 토큰 기반 세션 관리

### 📝 **2. 동행 모집 게시판**
- **모집글 작성**: 여행 계획 및 동행자 모집
- **지원 시스템**: 관심있는 모집글에 지원하기
- **매칭 로직**: 모집자가 지원자 중 선택하여 매칭
- **댓글/대댓글**: 게시글 소통 기능

### 💬 **3. 채팅 기능**
- **그룹 채팅**: 매칭된 동행자들 간 실시간 채팅
- **방장 권한**: 모집자가 방장으로 멤버 관리 (초대/퇴출)
- **선택적 초대**: 지원자 중 원하는 인원만 채팅방 초대

### 📅 **4. 여행 일정 관리** (예정)
- **일정 공유**: 채팅 내에서 여행 일정 등록/공유
- **구글 캘린더 연동**: 외부 캘린더 서비스 활용

### 🗺️ **5. 여행 경로 저장** (예정)
- **실시간 위치 공유**: 구글 지도 기반 위치 공유
- **경로 기록**: 여행 중 이동 경로 자동 저장
- **경로 시각화**: 지도에서 여행 동선 확인 가능

### 👥 **6. 팔로우/팔로잉** (예정)
- **사용자 팔로우**: 마음에 드는 여행자 팔로우
- **친구 초대**: 팔로잉 관계를 통한 여행 초대

### ✍️ **7. 여행 후기** (예정)
- **후기 작성**: 여행 완료 후 경험 공유
- **평점 시스템**: 동행자 평가 및 신뢰도 구축

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

### Future Stack (예정)
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
# MySQL만 Docker로 실행
docker-compose up mysql -d

# IDE에서 실행 또는
./gradlew bootRun
```

#### 4. 전체 스택 테스트
```bash
# 전체 서비스 Docker로 실행
docker-compose up -d
```

### 애플리케이션 접속
- **Local**: http://localhost:8080
- **Health Check**: http://localhost:8080/actuator/health

## 📚 문서

- **[배포 가이드](README.Docker.md)**: Docker 및 프로덕션 배포 방법
- **API 문서**: http://localhost:8080/swagger-ui.html (개발 예정)

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

## 🎯 개발 로드맵

### ✅ **Phase 1 - MVP Core (진행중)**
- [x] 프로젝트 세팅 및 배포 환경 구축
- [x] OAuth2 소셜 로그인 (카카오, 구글)
- [x] 게시글 CRUD (모집글 기본 기능)
- [x] JWT 인증 시스템
- [ ] 지원/매칭 시스템
- [ ] 댓글/대댓글 기능

### 🔄 **Phase 2 - Communication (예정)**
- [ ] WebSocket 실시간 채팅
- [ ] 채팅방 관리 (초대/퇴출)
- [ ] 알림 시스템 (FCM)

### 🗺️ **Phase 3 - Travel Features (예정)**
- [ ] 구글 캘린더 일정 연동
- [ ] 구글 지도 위치 공유
- [ ] 여행 경로 저장/시각화

### 👥 **Phase 4 - Social Features (예정)**
- [ ] 팔로우/팔로잉 시스템
- [ ] 여행 후기 및 평점
- [ ] 사용자 신뢰도 시스템

## 🤝 협업하기

1. 이 저장소를 Fork합니다
2. 기능 issue를 생성합니다 (`#30`)
3. 기능 브랜치를 생성합니다 (`git checkout -b feature/#30-amazing-feature`)
4. 변경사항을 커밋합니다 (`git commit -m '[#30] feat(??): Add amazing feature'`)
5. 브랜치에 Push합니다 (`git push origin feature/#30-amazing-feature`)
6. Pull Request를 생성합니다

## 📄 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다.

## 👥 팀

- **Backend Developer**: [@shinheekim](https://github.com/shinheekim), [@kimjaejoong](https://github.com/jaejoong0529)

---

<div align="center">

**DDU-RU Backend** - 함께하는 여행의 시작, 뚜르 🌍

</div>