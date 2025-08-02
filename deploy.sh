#!/bin/bash

# DDU-RU Backend 배포 스크립트

set -e  # 에러 발생시 스크립트 중단

echo "🚀 DDU-RU Backend 배포 시작..."

# .env 파일 존재 확인
if [ ! -f ".env" ]; then
    echo "❌ .env 파일이 없습니다!"
    echo "💡 다음 명령어로 생성하세요: touch .env"
    echo "💡 그리고 운영 환경에 맞게 값들을 수정하세요."
    exit 1
fi

echo "📋 현재 실행중인 컨테이너 확인..."
docker-compose ps

echo "🛑 기존 컨테이너 중지 및 제거..."
docker-compose --env-file .env down

echo "🗂️ 이미지 업데이트..."
docker-compose --env-file .env pull

echo "🚀 서비스 시작..."
docker-compose --env-file .env --profile app --profile infra up -d

echo "⏳ 서비스 상태 확인 중..."
sleep 10

echo "📊 배포 완료 상태:"
docker-compose --env-file .env ps

echo "🏥 헬스체크 확인..."
echo "앱: http://${SERVER_IP:-localhost}:8080/actuator/health"

echo "✅ 배포 완료!"
echo "🔍 로그 확인: docker-compose --env-file .env logs -f"
