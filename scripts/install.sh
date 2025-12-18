#!/usr/bin/env bash
# HealthTrack setup: install dependencies, configure MySQL, and seed data.
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
SQL_DIR="$ROOT_DIR/sql"
ENV_FILE="$ROOT_DIR/scripts/.env.local"

# Configurable via env vars when running the script.
DB_NAME="${DB_NAME:-healthtrack}"
DB_USER="${DB_USER:-healthtrack_app}"
DB_PASSWORD="${DB_PASSWORD:-healthtrack123}"
MYSQL_ROOT_PASSWORD="${MYSQL_ROOT_PASSWORD:-}"

SUDO_CMD=()

log() { printf '[healthtrack setup] %s\n' "$*"; }
fail() { printf 'ERROR: %s\n' "$*" >&2; exit 1; }

init_sudo() {
  if command -v sudo >/dev/null 2>&1; then
    SUDO_CMD=(sudo)
  elif [[ "$(id -u)" -eq 0 ]]; then
    SUDO_CMD=()
  else
    fail "Root privileges are required to install packages/start MySQL. Please rerun as root or install sudo."
  fi
}

detect_pkg_manager() {
  if command -v apt-get >/dev/null 2>&1; then
    echo "apt"
  elif command -v brew >/dev/null 2>&1; then
    echo "brew"
  else
    echo ""
  fi
}

install_dependencies() {
  local pm="$1"
  if [[ "$pm" == "apt" ]]; then
    log "Installing dependencies with apt (Java 17, Maven, MySQL server/client)..."
    "${SUDO_CMD[@]}" apt-get update -y
    DEBIAN_FRONTEND=noninteractive "${SUDO_CMD[@]}" apt-get install -y openjdk-17-jdk maven mysql-server mysql-client
  elif [[ "$pm" == "brew" ]]; then
    log "Installing dependencies with Homebrew (Java 17, Maven, MySQL)..."
    brew update
    brew install openjdk@17 maven mysql
    log "If Java 17 is newly installed via Homebrew, ensure it is on PATH (e.g. export PATH=\"$(brew --prefix)/opt/openjdk@17/bin:$PATH\")."
  else
    fail "Unsupported package manager. Please install Java 17, Maven, and MySQL manually, then rerun."
  fi
}

is_mysql_running() {
  pgrep mysqld >/dev/null 2>&1
}

start_mysql_service() {
  if is_mysql_running; then
    log "MySQL already running."
    return
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

  # Give the server a moment to come up.
  sleep 2

  if ! is_mysql_running; then
    fail "Failed to start MySQL. In containers without systemd, try: service mysql start"
  fi
}

prepare_mysql_command() {
  MYSQL_CMD=(mysql)
  MYSQL_ARGS=(-uroot)
  if [[ -n "$MYSQL_ROOT_PASSWORD" ]]; then
    MYSQL_ARGS+=(-p"$MYSQL_ROOT_PASSWORD")
  fi

  local err
  for _ in $(seq 1 10); do
    if err="$("${MYSQL_CMD[@]}" "${MYSQL_ARGS[@]}" -e "SELECT 1;" 2>&1)"; then
      return
    fi
    sleep 1
  done

  # Fall back to sudo socket auth if root password auth fails.
  if command -v sudo >/dev/null 2>&1; then
    if sudo mysql -e "SELECT 1;" >/dev/null 2>&1; then
      MYSQL_CMD=(sudo mysql)
      MYSQL_ARGS=()
      return
    fi
  fi

  if ! is_mysql_running; then
    fail "MySQL is not running. Start it (e.g. service mysql start) and rerun."
  fi

  if echo "$err" | grep -Eqi "access denied|authentication"; then
    fail "Access denied for MySQL root. Set MYSQL_ROOT_PASSWORD and rerun."
  fi

  err="$(echo "$err" | tr '\n' ' ')"
  fail "Cannot connect to MySQL as root. ${err}"
}

write_env_file() {
  local old_umask
  old_umask="$(umask)"
  umask 077
  cat >"$ENV_FILE" <<EOF
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/${DB_NAME}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
SPRING_DATASOURCE_USERNAME=${DB_USER}
SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}
SERVER_PORT=8080
EOF
  umask "$old_umask"
  chmod 600 "$ENV_FILE"
  log "Wrote backend env overrides to $ENV_FILE (kept at 600 permissions)."
}

seed_database() {
  log "Creating database/user and loading schema + seed data..."
  "${MYSQL_CMD[@]}" "${MYSQL_ARGS[@]}" <<SQL
CREATE DATABASE IF NOT EXISTS \`${DB_NAME}\` DEFAULT CHARACTER SET utf8mb4 DEFAULT COLLATE utf8mb4_general_ci;
CREATE USER IF NOT EXISTS '${DB_USER}'@'localhost' IDENTIFIED BY '${DB_PASSWORD}';
ALTER USER '${DB_USER}'@'localhost' IDENTIFIED BY '${DB_PASSWORD}';
GRANT ALL PRIVILEGES ON \`${DB_NAME}\`.* TO '${DB_USER}'@'localhost';
  FLUSH PRIVILEGES;
SQL

  local tmp_schema tmp_seed
  tmp_schema="$(mktemp)"
  tmp_seed="$(mktemp)"

  # Strip explicit USE/CREATE DATABASE statements so we can target any DB_NAME.
  awk '
    /^CREATE DATABASE IF NOT EXISTS healthtrack/ {skip=1; next}
    skip && /;/ {skip=0; next}
    skip {next}
    /^USE healthtrack;$/ {next}
    {print}
  ' "$SQL_DIR/01_schema.sql" >"$tmp_schema"

  sed -e '/^USE healthtrack;$/d' \
      "$SQL_DIR/02_seed.sql" >"$tmp_seed"

  "${MYSQL_CMD[@]}" "${MYSQL_ARGS[@]}" "$DB_NAME" <"$tmp_schema"
  "${MYSQL_CMD[@]}" "${MYSQL_ARGS[@]}" "$DB_NAME" <"$tmp_seed"
  rm -f "$tmp_schema" "$tmp_seed"
}

warm_backend_dependencies() {
  log "Pre-downloading backend Maven dependencies (skip tests)..."
  (cd "$ROOT_DIR/backend" && mvn -B -q -DskipTests package >/dev/null)
}

main() {
  local pm
  init_sudo
  pm="$(detect_pkg_manager)"
  install_dependencies "$pm"
  start_mysql_service
  prepare_mysql_command
  seed_database
  write_env_file
  warm_backend_dependencies
  log "Setup complete. You can now run scripts/start.sh to launch the app."
}

main "$@"
