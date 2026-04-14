#!/bin/bash

set -Eeuo pipefail

# 프로젝트 루트의 .env는 docker compose가 치환용으로만 읽고, 셸에는 자동 주입되지 않음
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
if [ -f "${SCRIPT_DIR}/.env" ]; then
  set -a
  # shellcheck disable=SC1091
  . "${SCRIPT_DIR}/.env"
  set +a
fi

COMPOSE=(docker compose -f docker-compose.prod.yml)
UPSTREAM_DIR="deployment/nginx/conf.d/upstreams"
ACTIVE_UPSTREAM_FILE="${UPSTREAM_DIR}/active-upstream.inc"
NGINX_UPSTREAM_PATH_IN_CONTAINER="/etc/nginx/conf.d/upstreams/active-upstream.inc"
READINESS_PATH="http://localhost:8080/actuator/health/readiness"
MAX_RETRIES=24
RETRY_INTERVAL=5
DRAIN_SECONDS=10

log() {
  echo "[$(date '+%Y-%m-%d %H:%M:%S')] $*"
}

require_env() {
  local key="$1"
  if [ -z "${!key:-}" ]; then
    echo "환경변수 ${key} 가 비어 있습니다." >&2
    exit 1
  fi
}

wait_for_readiness() {
  local service="$1"
  local i=1
  while [ "$i" -le "$MAX_RETRIES" ]; do
    if "${COMPOSE[@]}" exec -T "$service" sh -c "curl -fsS ${READINESS_PATH} | grep -q '\"status\":\"UP\"'" >/dev/null 2>&1; then
      log "✅ ${service} readiness 통과"
      return 0
    fi
    log "⏳ ${service} readiness 대기 중... (${i}/${MAX_RETRIES})"
    sleep "$RETRY_INTERVAL"
    i=$((i + 1))
  done
  return 1
}

current_active_color() {
  if [ -f "$ACTIVE_UPSTREAM_FILE" ] && grep -q "app_green:8080" "$ACTIVE_UPSTREAM_FILE"; then
    echo "green"
  else
    echo "blue"
  fi
}

switch_upstream() {
  local color="$1"
  cp "${UPSTREAM_DIR}/active-upstream.${color}.inc" "$ACTIVE_UPSTREAM_FILE"
  "${COMPOSE[@]}" exec -T nginx nginx -s reload
  log "🔀 active upstream -> ${color}"
}

log "🚀 DDU-RU Backend Blue-Green 배포 시작"

require_env AWS_REGION
require_env ECR_REGISTRY
require_env ECR_REPOSITORY
require_env IMAGE_TAG

log "🔐 ECR 로그인"
aws ecr get-login-password --region "${AWS_REGION}" | docker login --username AWS --password-stdin "${ECR_REGISTRY}"

active_color="$(current_active_color)"
if [ "$active_color" = "blue" ]; then
  idle_color="green"
else
  idle_color="blue"
fi

active_service="app_${active_color}"
idle_service="app_${idle_color}"

log "현재 활성 색상: ${active_color}, 배포 대상: ${idle_color}"

log "🧱 필수 인프라(redis) 기동 보장"
"${COMPOSE[@]}" up -d redis

log "📥 새 버전 이미지 pull (${idle_service})"
"${COMPOSE[@]}" pull "${idle_service}"

log "🚀 대상 색상 기동 (${idle_service})"
"${COMPOSE[@]}" up -d --no-deps "${idle_service}"

if ! wait_for_readiness "${idle_service}"; then
  log "❌ ${idle_service} readiness 실패. 롤백 없이 종료"
  "${COMPOSE[@]}" logs --tail=200 "${idle_service}" || true
  exit 1
fi

log "🧱 Nginx 기동 보장"
"${COMPOSE[@]}" up -d nginx

log "🌐 Nginx upstream 전환 (${idle_color})"
switch_upstream "${idle_color}"

if ! "${COMPOSE[@]}" exec -T nginx sh -c "test -f ${NGINX_UPSTREAM_PATH_IN_CONTAINER}"; then
  log "❌ Nginx 내부 upstream 파일 확인 실패. 이전 색상으로 롤백"
  switch_upstream "${active_color}"
  exit 1
fi

log "🕒 기존 색상 커넥션 드레인 대기 (${DRAIN_SECONDS}s)"
sleep "${DRAIN_SECONDS}"

log "🛑 이전 색상 중지 (${active_service})"
"${COMPOSE[@]}" stop "${active_service}" || true

log "✅ 배포 완료: active=${idle_color}, inactive=${active_color}"
"${COMPOSE[@]}" ps
