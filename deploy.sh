#!/bin/bash
set -Eeuo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

load_dotenv() {
  local env_file="$1"
  [ -f "$env_file" ] || return 0
  while IFS= read -r line || [ -n "$line" ]; do
    line="${line%$'\r'}"
    [[ "$line" =~ ^[[:space:]]*# ]] && continue
    [[ -z "${line//[[:space:]]/}" ]] && continue
    [[ "$line" != *=* ]] && continue
    local key="${line%%=*}"
    local value="${line#*=}"
    key="${key%"${key##*[![:space:]]}"}"
    key="${key#"${key%%[![:space:]]*}"}"
    [[ "$key" =~ ^[A-Za-z_][A-Za-z0-9_]*$ ]] || continue
    export "$key"="$value"
  done <"$env_file"
}
load_dotenv "${SCRIPT_DIR}/.env"

COMPOSE=(docker compose -f "${SCRIPT_DIR}/docker-compose.prod.yml")
UPSTREAM_DIR="${SCRIPT_DIR}/deployment/nginx/conf.d/upstreams"
ACTIVE_UPSTREAM_FILE="${UPSTREAM_DIR}/active-upstream.inc"
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

require_command() {
  local cmd="$1"
  if ! command -v "$cmd" >/dev/null 2>&1; then
    echo "필요한 명령을 찾을 수 없습니다: ${cmd}" >&2
    exit 1
  fi
}

require_compose_v2() {
  if ! docker compose version >/dev/null 2>&1; then
    echo "docker compose v2가 필요합니다." >&2
    exit 1
  fi
}

require_file() {
  local path="$1"
  if [ ! -f "$path" ]; then
    echo "필수 파일이 없습니다: ${path}" >&2
    exit 1
  fi
}

bootstrap_active_upstream() {
  if [ -f "$ACTIVE_UPSTREAM_FILE" ]; then
    return 0
  fi

  cp "${UPSTREAM_DIR}/active-upstream.blue.inc" "$ACTIVE_UPSTREAM_FILE" || {
    echo "active upstream 초기화에 실패했습니다: ${UPSTREAM_DIR}/active-upstream.blue.inc" >&2
    exit 1
  }
  log "ℹ️ active upstream 파일이 없어 blue로 초기화했습니다."
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
  local target_color="$1"
  local rollback_color="$2"

  cp "${UPSTREAM_DIR}/active-upstream.${target_color}.inc" "$ACTIVE_UPSTREAM_FILE" || {
    log "❌ upstream 파일 복사 실패: ${UPSTREAM_DIR}/active-upstream.${target_color}.inc"
    return 1
  }

  if ! "${COMPOSE[@]}" exec -T nginx nginx -t >/dev/null 2>&1; then
    cp "${UPSTREAM_DIR}/active-upstream.${rollback_color}.inc" "$ACTIVE_UPSTREAM_FILE" || true
    "${COMPOSE[@]}" exec -T nginx nginx -t >/dev/null 2>&1 || true
    log "❌ nginx 설정 검증 실패. upstream를 ${rollback_color} 로 복구"
    return 1
  fi

  if ! "${COMPOSE[@]}" exec -T nginx nginx -s reload >/dev/null 2>&1; then
    cp "${UPSTREAM_DIR}/active-upstream.${rollback_color}.inc" "$ACTIVE_UPSTREAM_FILE" || true
    "${COMPOSE[@]}" exec -T nginx nginx -t >/dev/null 2>&1 || true
    "${COMPOSE[@]}" exec -T nginx nginx -s reload >/dev/null 2>&1 || true
    log "❌ nginx reload 실패. upstream를 ${rollback_color} 로 복구"
    return 1
  fi

  log "🔀 active upstream -> ${target_color}"
}

log "🚀 DDU-RU Backend Blue-Green 배포 시작"

require_env AWS_REGION
require_env ECR_REGISTRY
require_env ECR_REPOSITORY
require_env IMAGE_TAG
require_command aws
require_command docker
require_compose_v2
require_file "${SCRIPT_DIR}/docker-compose.prod.yml"
require_file "${SCRIPT_DIR}/deployment/nginx/nginx.conf"
require_file "${SCRIPT_DIR}/deployment/nginx/conf.d/default.conf"
require_file "${UPSTREAM_DIR}/active-upstream.blue.inc"
require_file "${UPSTREAM_DIR}/active-upstream.green.inc"
bootstrap_active_upstream

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
  log "❌ ${idle_service} readiness 실패. 새 색상 중지 후 종료"
  "${COMPOSE[@]}" logs --tail=200 "${idle_service}" || true
  "${COMPOSE[@]}" stop "${idle_service}" || true
  exit 1
fi

log "🧱 Nginx 기동 보장"
"${COMPOSE[@]}" up -d nginx

log "🌐 Nginx upstream 전환 (${idle_color})"
if ! switch_upstream "${idle_color}" "${active_color}"; then
  "${COMPOSE[@]}" stop "${idle_service}" || true
  exit 1
fi

log "🕒 기존 색상 커넥션 드레인 대기 (${DRAIN_SECONDS}s)"
sleep "${DRAIN_SECONDS}"

log "🛑 이전 색상 중지 (${active_service})"
"${COMPOSE[@]}" stop "${active_service}" || true

log "✅ 배포 완료: active=${idle_color}, inactive=${active_color}"
"${COMPOSE[@]}" ps
