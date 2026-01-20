#!/bin/bash

# DDU-RU Backend ECR 배포 스크립트

set -e # 에러 발생시 스크립트 중단

echo "🚀 DDU-RU Backend ECR 배포 시작..."

echo "🔐 ECR에 Docker 로그인..."
aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${ECR_REGISTRY}

echo "📥 ECR에서 최신 이미지 가져오는 중..."
docker-compose -f docker-compose.prod.yml pull app

echo "📋 현재 실행중인 컨테이너 확인..."
docker-compose -f docker-compose.prod.yml ps

echo "🛑 기존 컨테이너 중지 및 제거..."
docker-compose -f docker-compose.prod.yml down

echo "🧹 사용하지 않는 Docker 이미지 정리..."
docker image prune -f

echo "🚀 서비스 시작..."
docker-compose -f docker-compose.prod.yml up -d

echo "⏳ 서비스 상태 확인 중..."
MAX_RETRIES=12
RETRY_INTERVAL=5
for i in $(seq 1 $MAX_RETRIES); do
  # nginx를 통해 health check (HTTP 또는 HTTPS)
  if curl -fs http://localhost/actuator/health > /dev/null 2>&1 || curl -fs https://localhost/actuator/health > /dev/null 2>&1; then
    echo "✅ 애플리케이션이 정상적으로 실행 중입니다!"
    echo "🎉 ECR 배포 스크립트 완료!"
    exit 0
  fi
  echo "Attempt $i/$MAX_RETRIES: 아직 실행되지 않았습니다. $RETRY_INTERVAL초 후 재시도..."
  sleep $RETRY_INTERVAL
done

echo "❌ 애플리케이션이 정상적으로 실행되지 않았습니다!"
echo "🔍 로그 확인:"
docker-compose -f docker-compose.prod.yml logs app
exit 1
