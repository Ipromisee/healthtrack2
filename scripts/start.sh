#!/usr/bin/env bash
# HealthTrack launcher: start MySQL (if possible) and run the Spring Boot backend.
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
ENV_FILE="$ROOT_DIR/scripts/.env.local"

SUDO_CMD=()

log() { printf '[healthtrack start] %s\n' "$*"; }
fail() { printf 'ERROR: %s\n' "$*" >&2; exit 1; }

init_sudo() {
  if command -v sudo >/dev/null 2>&1; then
    SUDO_CMD=(sudo)
  else
    SUDO_CMD=()
  fi
}

# Defaults can be overridden via env vars or scripts/.env.local (created by install.sh).
SPRING_DATASOURCE_URL="${SPRING_DATASOURCE_URL:-jdbc:mysql://localhost:3306/healthtrack?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC}"
SPRING_DATASOURCE_USERNAME="${SPRING_DATASOURCE_USERNAME:-root}"
SPRING_DATASOURCE_PASSWORD="${SPRING_DATASOURCE_PASSWORD:-}"
SERVER_PORT="${SERVER_PORT:-8080}"
SERVE_FRONTEND="${SERVE_FRONTEND:-0}"
FRONTEND_PORT="${FRONTEND_PORT:-4173}"

if [[ -f "$ENV_FILE" ]]; then
  log "Loading environment overrides from $ENV_FILE"
  # shellcheck source=/dev/null
  source "$ENV_FILE"
fi

while [[ $# -gt 0 ]]; do
  case "$1" in
    --frontend|--with-frontend)
      SERVE_FRONTEND=1
      ;;
    --frontend-port)
      shift
      FRONTEND_PORT="${1:-$FRONTEND_PORT}"
      ;;
    --no-frontend)
      SERVE_FRONTEND=0
      ;;
    *)
      fail "Unknown option: $1"
      ;;
  esac
  shift
done

cleanup() {
  [[ -n "${BACKEND_PID:-}" ]] && kill "$BACKEND_PID" >/dev/null 2>&1 || true
  [[ -n "${FRONTEND_PID:-}" ]] && kill "$FRONTEND_PID" >/dev/null 2>&1 || true
}
trap cleanup EXIT INT TERM

is_mysql_running() {
  pgrep mysqld >/dev/null 2>&1
}

start_mysql_service() {
  if is_mysql_running; then
    log "MySQL already running."
    return
  fi

  if [[ "$(id -u)" -ne 0 ]] && [[ ${#SUDO_CMD[@]} -eq 0 ]]; then
    fail "MySQL is not running and sudo is not available. Start MySQL manually or rerun as root."
  fi

  if command -v systemctl >/dev/null 2>&1 && [[ -d /run/systemd/system ]]; then
    "${SUDO_CMD[@]}" systemctl start mysql >/dev/null 2>&1 || "${SUDO_CMD[@]}" systemctl start mysqld >/dev/null 2>&1 || true
  fi

  if ! is_mysql_running && command -v service >/dev/null 2>&1; then
    "${SUDO_CMD[@]}" service mysql start >/dev/null 2>&1 || "${SUDO_CMD[@]}" service mysqld start >/dev/null 2>&1 || true
  fi

  if ! is_mysql_running && command -v brew >/dev/null 2>&1; then
    brew services start mysql >/dev/null 2>&1 || true
  fi

  sleep 2

  if ! is_mysql_running; then
    fail "Failed to start MySQL. In containers without systemd, try: service mysql start"
  fi
}

start_frontend_server() {
  command -v python3 >/dev/null 2>&1 || fail "python3 is required to serve the frontend (or skip with --no-frontend)."
  log "Serving frontend via python3 -m http.server on http://localhost:${FRONTEND_PORT} (hit Ctrl+C to stop)"
  (cd "$ROOT_DIR/frontend" && python3 -m http.server "$FRONTEND_PORT") &
  FRONTEND_PID=$!
}

start_backend() {
  command -v mvn >/dev/null 2>&1 || fail "Maven is required. Run scripts/install.sh first."
  log "Starting backend on port ${SERVER_PORT}..."
  (
    cd "$ROOT_DIR/backend"
    SPRING_DATASOURCE_URL="$SPRING_DATASOURCE_URL" \
    SPRING_DATASOURCE_USERNAME="$SPRING_DATASOURCE_USERNAME" \
    SPRING_DATASOURCE_PASSWORD="$SPRING_DATASOURCE_PASSWORD" \
    SERVER_PORT="$SERVER_PORT" \
    mvn spring-boot:run
  ) &
  BACKEND_PID=$!
  wait "$BACKEND_PID"
}

main() {
  init_sudo
  start_mysql_service

  if [[ "$SERVE_FRONTEND" == "1" ]]; then
    start_frontend_server
  fi

  log "Backend datasource -> $SPRING_DATASOURCE_URL (user: $SPRING_DATASOURCE_USERNAME)"
  start_backend
}

main "$@"
