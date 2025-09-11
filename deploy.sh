#!/bin/bash

# DDU-RU Backend ECR 배포 스크립트

set -e  # 에러 발생시 스크립트 중단

echo "🚀 DDU-RU Backend ECR 배포 시작..."

# .env 파일 존재 확인
cd ~/app
if [ ! -f ".env" ]; then
    echo "❌ .env 파일이 없습니다!"
    exit 1
fi

echo "📥 ECR에서 최신 이미지 가져오는 중..."
docker-compose pull app

echo "📋 현재 실행중인 컨테이너 확인..."
docker-compose ps

echo "🛑 기존 컨테이너 중지 및 제거..."
docker-compose down

echo "🧹 사용하지 않는 Docker 이미지 정리..."
docker image prune -f

echo "🚀 서비스 시작..."
docker-compose up -d

echo "⏳ 서비스 상태 확인 중..."
sleep 10

# 애플리케이션 상태 확인
if curl -f http://localhost:8080/actuator/health > /dev/null 2>&1; then
  echo "✅ 애플리케이션이 정상적으로 실행 중입니다!"
else
  echo "❌ 애플리케이션이 정상적으로 실행되지 않았습니다!"
  echo "🔍 로그 확인:"
  docker-compose logs app -f
  exit 1
fi
echo "🎉 ECR 배포 스크립트 완료!"
