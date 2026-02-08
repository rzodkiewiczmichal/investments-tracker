#!/usr/bin/env bash
set -euo pipefail

COMPOSE_CMD="docker compose"
CONTAINERS=(investments-tracker-postgres investments-tracker-tempo investments-tracker-grafana)
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
    echo "  infra     Start only infrastructure (PostgreSQL, Tempo, Grafana)"
    echo ""
    echo "Services:"
    echo "  Frontend      http://localhost:4200"
    echo "  Backend API   http://localhost:8080"
    echo "  Grafana       http://localhost:3000"
    echo "  Tempo UI      http://localhost:3200"
    echo "  PostgreSQL    localhost:5432"
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

    echo "Starting infrastructure (PostgreSQL, Tempo, Grafana)..."
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
