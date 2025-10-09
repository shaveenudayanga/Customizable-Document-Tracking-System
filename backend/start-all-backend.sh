#!/usr/bin/env bash
# start-all-backend.sh
# Usage: ./start-all-backend.sh start|stop|status
# Starts all backend services in background using ./mvnw spring-boot:run -DskipTests

set -euo pipefail

BASE_DIR="/home/shaveen/Customizable-Document-Tracking-System/backend"
SERVICES=(
  "api-gateway"
  "user-service"
  "document-service"
  "tracking-service"
  "workflow-service"
)
LOG_DIR="${BASE_DIR}/logs"
PID_DIR="${BASE_DIR}/pids"

mkdir -p "${LOG_DIR}" "${PID_DIR}"

start_service() {
  svc=$1
  svc_dir="${BASE_DIR}/${svc}"
  pid_file="${PID_DIR}/${svc}.pid"
  log_file="${LOG_DIR}/${svc}.log"

  if [ -f "${pid_file}" ]; then
    pid=$(cat "${pid_file}" 2>/dev/null || echo "")
    if [ -n "$pid" ] && kill -0 "$pid" 2>/dev/null; then
      echo "[SKIP] ${svc} already running (pid $pid)"
      return
    else
      echo "[CLEAN] Removing stale pidfile for ${svc}"
      rm -f "${pid_file}"
    fi
  fi

  if [ ! -d "${svc_dir}" ]; then
    echo "[ERR] Service directory not found: ${svc_dir}" >&2
    return 1
  fi

  echo "[START] ${svc} -> logs: ${log_file}"
  pushd "${svc_dir}" > /dev/null || return 1

  # Prefer the wrapper if present
  if [ -x "./mvnw" ]; then
    CMD=("./mvnw" "spring-boot:run" "-DskipTests")
  else
    CMD=("mvn" "spring-boot:run" "-DskipTests")
  fi

  # Start in background detached and save pid
  nohup setsid "${CMD[@]}" > "${log_file}" 2>&1 &
  svc_pid=$!
  echo "$svc_pid" > "${pid_file}"
  sleep 0.5
  if kill -0 "$svc_pid" 2>/dev/null; then
    echo "[OK] ${svc} started (pid ${svc_pid})"
  else
    echo "[WARN] ${svc} may have failed to start; check ${log_file}"
  fi

  popd > /dev/null || return 1
}

stop_service() {
  svc=$1
  pid_file="${PID_DIR}/${svc}.pid"
  if [ ! -f "${pid_file}" ]; then
    echo "[SKIP] ${svc} not running (no pidfile)"
    return
  fi
  pid=$(cat "${pid_file}" 2>/dev/null || echo "")
  if [ -n "$pid" ] && kill -0 "$pid" 2>/dev/null; then
    echo "[STOP] Killing ${svc} (pid $pid)"
    kill "$pid" && sleep 1
    if kill -0 "$pid" 2>/dev/null; then
      echo "[STOP] PID still alive, sending SIGKILL"
      kill -9 "$pid" || true
    fi
  else
    echo "[CLEAN] Removing stale pidfile for ${svc}"
  fi
  rm -f "${pid_file}" || true
}

status_service() {
  svc=$1
  pid_file="${PID_DIR}/${svc}.pid"
  if [ ! -f "${pid_file}" ]; then
    echo "${svc}: stopped"
    return
  fi
  pid=$(cat "${pid_file}" 2>/dev/null || echo "")
  if [ -n "$pid" ] && kill -0 "$pid" 2>/dev/null; then
    echo "${svc}: running (pid $pid)"
  else
    echo "${svc}: pidfile present but process not running"
  fi
}

case "${1:-}" in
  start)
    echo "Starting all backend services..."
    for s in "${SERVICES[@]}"; do
      start_service "$s" || true
      sleep 0.5
    done
    echo "Done. Tail logs with: tail -F ${LOG_DIR}/*.log"
    ;;
  stop)
    echo "Stopping all backend services..."
    for s in "${SERVICES[@]}"; do
      stop_service "$s" || true
    done
    echo "Done."
    ;;
  status)
    for s in "${SERVICES[@]}"; do
      status_service "$s"
    done
    ;;
  restart)
    "$0" stop
    sleep 1
    "$0" start
    ;;
  *)
    echo "Usage: $0 {start|stop|status|restart}"
    exit 2
    ;;
esac
