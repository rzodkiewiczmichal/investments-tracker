#!/usr/bin/env bash
set -euo pipefail

# Load .env if present (exports vars like FINNHUB_API_KEY for bootRun)
if [ -f "$( cd "$(dirname "${BASH_SOURCE[0]}")" && pwd )/.env" ]; then
    set -a
    source "$( cd "$(dirname "${BASH_SOURCE[0]}")" && pwd )/.env"
    set +a
fi

COMPOSE_CMD="docker compose"
CONTAINERS=(investments-tracker-postgres investments-tracker-redis investments-tracker-tempo investments-tracker-grafana)
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_PID_FILE="$SCRIPT_DIR/.backend.pid"
FRONTEND_PID_FILE="$SCRIPT_DIR/.frontend.pid"

print_usage() {
    echo "Usage: ./dev.sh [command]"
    echo ""
    echo "Commands:"
    echo "  start     Start infrastructure, backend, and frontend (default)"
    echo "  stop      Stop everything (app processes + Docker containers)"
    echo "  restart   Stop and start everything"
    echo "  reset     Stop apps, clear DB (positions/imports), rebuild and start fresh"
    echo "  clear     Clear positions and import data from DB (infra must be running)"
    echo "  infra     Start only infrastructure (PostgreSQL, Redis, Tempo, Grafana)"
    echo ""
    echo "Services:"
    echo "  Frontend      http://localhost:4200"
    echo "  Backend API   http://localhost:8080"
    echo "  Grafana       http://localhost:3000"
    echo "  Tempo UI      http://localhost:3200"
    echo "  PostgreSQL    localhost:5432"
    echo "  Redis         localhost:6379"
}

remove_stale_containers() {
    for name in "${CONTAINERS[@]}"; do
        if docker container inspect "$name" > /dev/null 2>&1; then
            echo "  Removing stale container: $name"
            docker rm -f "$name" > /dev/null 2>&1 || true
        fi
    done
}

stop_process() {
    local pid_file="$1"
    local label="$2"
    if [ -f "$pid_file" ]; then
        local pid
        pid=$(cat "$pid_file")
        if kill -0 "$pid" 2>/dev/null; then
            echo "Stopping $label (PID $pid)..."
            kill "$pid" 2>/dev/null || true
            wait "$pid" 2>/dev/null || true
        fi
        rm -f "$pid_file"
    fi
}

start_infra() {
    echo "Cleaning up stale containers..."
    $COMPOSE_CMD down 2>/dev/null || true
    remove_stale_containers

    echo "Starting infrastructure (PostgreSQL, Redis, Tempo, Grafana)..."
    $COMPOSE_CMD up -d

    echo "Waiting for PostgreSQL to be ready..."
    until $COMPOSE_CMD exec -T postgres pg_isready -U tracker_user -d investments_tracker > /dev/null 2>&1; do
        sleep 1
    done
    echo "PostgreSQL is ready."
}

start_backend() {
    echo "Starting Spring Boot backend..."
    ./gradlew bootRun --args='--spring.profiles.active=local' &
    echo $! > "$BACKEND_PID_FILE"
}

start_frontend() {
    if [ ! -d "frontend/node_modules" ]; then
        echo "Installing frontend dependencies..."
        (cd frontend && npm install)
    fi
    echo "Starting Angular frontend..."
    (cd frontend && npm start) &
    echo $! > "$FRONTEND_PID_FILE"
}

stop_apps() {
    stop_process "$BACKEND_PID_FILE" "backend"
    stop_process "$FRONTEND_PID_FILE" "frontend"
}

clear_db() {
    local container="${POSTGRES_CONTAINER:-investments-tracker-postgres}"
    local db="${POSTGRES_DB:-investments_tracker}"
    local user="${POSTGRES_USER:-tracker_user}"

    echo "Clearing positions and import data from ${db} (container: ${container})..."

    docker exec -i "$container" psql -U "$user" -d "$db" -v ON_ERROR_STOP=1 <<'SQL'
DO $$
BEGIN
    -- Skip if tables don't exist yet (fresh DB before Flyway)
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'positions') THEN
        RAISE NOTICE 'Tables not found (fresh DB) — skipping clear.';
        RETURN;
    END IF;

    DELETE FROM import_session_transactions;
    DELETE FROM import_session_mappings;
    DELETE FROM import_sessions;
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'broker_instrument_mappings') THEN
        DELETE FROM broker_instrument_mappings;
    END IF;
    DELETE FROM account_holdings;
    DELETE FROM positions;
    DELETE FROM accounts;
END $$;
SQL

    echo "Flushing Redis cache..."
    docker exec investments-tracker-redis redis-cli FLUSHDB > /dev/null 2>&1 || echo "  (Redis not running — skipped)"

    echo "Done."
}

stop_all() {
    stop_apps
    echo "Stopping Docker containers..."
    $COMPOSE_CMD down
    echo "Done."
}

cleanup() {
    echo ""
    echo "Shutting down..."
    stop_apps
    echo "Infrastructure containers are still running. Run './dev.sh stop' to stop them."
    exit 0
}

case "${1:-start}" in
    start)
        start_infra
        trap cleanup INT TERM
        start_backend
        start_frontend
        echo ""
        echo "All services started. Press Ctrl+C to stop the applications."
        wait
        ;;
    stop)
        stop_all
        ;;
    restart)
        stop_all
        start_infra
        trap cleanup INT TERM
        start_backend
        start_frontend
        echo ""
        echo "All services started. Press Ctrl+C to stop the applications."
        wait
        ;;
    reset)
        stop_apps
        start_infra
        clear_db
        echo "Building project..."
        ./gradlew spotlessApply clean build -x test
        echo "Restarting apps..."
        trap cleanup INT TERM
        start_backend
        start_frontend
        echo ""
        echo "All services started (DB cleared). Press Ctrl+C to stop the applications."
        wait
        ;;
    clear)
        clear_db
        ;;
    infra)
        start_infra
        echo "Infrastructure is running. Start apps manually with:"
        echo "  ./gradlew bootRun --args='--spring.profiles.active=local'"
        echo "  cd frontend && npm start"
        ;;
    -h|--help|help)
        print_usage
        ;;
    *)
        echo "Unknown command: $1"
        print_usage
        exit 1
        ;;
esac
