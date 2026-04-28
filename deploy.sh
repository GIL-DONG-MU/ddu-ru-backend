#!/bin/bash

# DDU-RU Backend Blue-Green 배포 스크립트

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
COMPOSE_FILE="${SCRIPT_DIR}/docker-compose.prod.yml"
UPSTREAM_DIR="${SCRIPT_DIR}/deployment/nginx/conf.d/upstreams"
ACTIVE_UPSTREAM_FILE="${UPSTREAM_DIR}/active-upstream.inc"
READINESS_PATH="http://localhost:8080/actuator/health/readiness"
MAX_RETRIES=24
RETRY_INTERVAL=5
DRAIN_SECONDS=10

compose() {
  docker compose -f "${COMPOSE_FILE}" "$@"
}

wait_for_readiness() {
  local service="$1"

  echo "⏳ ${service} readiness 확인 중..."
  for i in $(seq 1 "${MAX_RETRIES}"); do
    if compose exec -T "$service" sh -c "curl -fsS ${READINESS_PATH} | grep -q '\"status\":\"UP\"'" >/dev/null 2>&1; then
      echo "✅ ${service} readiness 통과"
      return 0
    fi

    echo "Attempt ${i}/${MAX_RETRIES}: 아직 준비되지 않았습니다. ${RETRY_INTERVAL}초 후 재시도..."
    sleep "${RETRY_INTERVAL}"
  done

  return 1
}

switch_upstream() {
  local target_color="$1"
  local rollback_color="$2"

  cp "${UPSTREAM_DIR}/active-upstream.${target_color}.inc" "${ACTIVE_UPSTREAM_FILE}" || {
    echo "❌ upstream 파일 복사 실패: ${UPSTREAM_DIR}/active-upstream.${target_color}.inc"
    return 1
  }

  if ! compose exec -T nginx nginx -t >/dev/null 2>&1; then
    cp "${UPSTREAM_DIR}/active-upstream.${rollback_color}.inc" "${ACTIVE_UPSTREAM_FILE}" || true
    compose exec -T nginx nginx -t >/dev/null 2>&1 || true
    echo "❌ nginx 설정 검증 실패. upstream를 ${rollback_color} 로 복구합니다."
    return 1
  fi

  if ! compose exec -T nginx nginx -s reload >/dev/null 2>&1; then
    cp "${UPSTREAM_DIR}/active-upstream.${rollback_color}.inc" "${ACTIVE_UPSTREAM_FILE}" || true
    compose exec -T nginx nginx -t >/dev/null 2>&1 || true
    compose exec -T nginx nginx -s reload >/dev/null 2>&1 || true
    echo "❌ nginx reload 실패. upstream를 ${rollback_color} 로 복구합니다."
    return 1
  fi

  echo "🔀 active upstream -> ${target_color}"
}

echo "🚀 DDU-RU Backend Blue-Green 배포 시작..."

for key in AWS_REGION ECR_REGISTRY ECR_REPOSITORY IMAGE_TAG; do
  if [ -z "${!key:-}" ]; then
    echo "❌ 환경변수 ${key} 가 비어 있습니다." >&2
    exit 1
  fi
done

for cmd in aws docker; do
  if ! command -v "${cmd}" >/dev/null 2>&1; then
    echo "❌ 필요한 명령을 찾을 수 없습니다: ${cmd}" >&2
    exit 1
  fi
done

if ! docker compose version >/dev/null 2>&1; then
  echo "❌ docker compose v2가 필요합니다." >&2
  exit 1
fi

for file in \
  "${COMPOSE_FILE}" \
  "${SCRIPT_DIR}/deployment/nginx/nginx.conf" \
  "${SCRIPT_DIR}/deployment/nginx/conf.d/default.conf" \
  "${UPSTREAM_DIR}/active-upstream.blue.inc" \
  "${UPSTREAM_DIR}/active-upstream.green.inc"; do
  if [ ! -f "${file}" ]; then
    echo "❌ 필수 파일이 없습니다: ${file}" >&2
    exit 1
  fi
done

if [ ! -f "${ACTIVE_UPSTREAM_FILE}" ]; then
  cp "${UPSTREAM_DIR}/active-upstream.blue.inc" "${ACTIVE_UPSTREAM_FILE}" || {
    echo "❌ active upstream 초기화 실패: ${UPSTREAM_DIR}/active-upstream.blue.inc" >&2
    exit 1
  }
  echo "ℹ️ active upstream 파일이 없어 blue로 초기화했습니다."
fi

echo "🧾 배포 컨텍스트 확인"
echo "AWS_REGION=${AWS_REGION}"
echo "ECR_REGISTRY=${ECR_REGISTRY}"
echo "ECR_REPOSITORY=${ECR_REPOSITORY}"
echo "IMAGE_TAG=${IMAGE_TAG}"

echo "🔐 ECR에 Docker 로그인..."
aws ecr get-login-password --region "${AWS_REGION}" | docker login --username AWS --password-stdin "${ECR_REGISTRY}"

if grep -q "app_green:8080" "${ACTIVE_UPSTREAM_FILE}"; then
  ACTIVE_COLOR="green"
  IDLE_COLOR="blue"
else
  ACTIVE_COLOR="blue"
  IDLE_COLOR="green"
fi

ACTIVE_SERVICE="app_${ACTIVE_COLOR}"
IDLE_SERVICE="app_${IDLE_COLOR}"

echo "현재 활성 색상: ${ACTIVE_COLOR}, 배포 대상: ${IDLE_COLOR}"

echo "🧱 필수 인프라(redis) 기동 보장..."
compose up -d redis

echo "📥 새 버전 이미지 가져오는 중... (${IDLE_SERVICE})"
compose pull "${IDLE_SERVICE}"

echo "🚀 대상 색상 기동... (${IDLE_SERVICE})"
compose up -d --no-deps "${IDLE_SERVICE}"

if ! wait_for_readiness "${IDLE_SERVICE}"; then
  echo "❌ ${IDLE_SERVICE} readiness 실패"
  echo "🔍 로그 확인:"
  compose logs --tail=200 "${IDLE_SERVICE}" || true
  compose stop "${IDLE_SERVICE}" || true
  exit 1
fi

echo "🧱 Nginx 기동 보장..."
compose up -d nginx

echo "🌐 Nginx upstream 전환... (${IDLE_COLOR})"
if ! switch_upstream "${IDLE_COLOR}" "${ACTIVE_COLOR}"; then
  compose stop "${IDLE_SERVICE}" || true
  exit 1
fi

echo "🕒 기존 색상 연결 정리 대기... (${DRAIN_SECONDS}s)"
sleep "${DRAIN_SECONDS}"

echo "🛑 이전 색상 중지... (${ACTIVE_SERVICE})"
compose stop "${ACTIVE_SERVICE}" || true

echo "✅ 배포 완료: active=${IDLE_COLOR}, inactive=${ACTIVE_COLOR}"
compose ps
