# ADR-029: Local Development Startup Script

## Status
Accepted

## Context

Running the local development environment requires multiple manual steps:

1. `docker compose up -d` to start infrastructure (PostgreSQL, Tempo, Grafana)
2. `./gradlew bootRun --args='--spring.profiles.active=local'` to start the application

This multi-step process leads to friction:
- **Container name conflicts**: If containers from a previous session were not cleaned up, `docker compose up` fails with name conflicts (e.g., `/investments-tracker-postgres` already in use)
- **Forgotten dependencies**: Developers start the app without infrastructure, getting connection errors (OTLP export failures, database connection refused)
- **Multiple terminal commands**: No single entry point to start the full ecosystem

As the project grows (future services, frontend), the startup sequence will become more complex.

## Decision

Create a `dev.sh` shell script in the project root that orchestrates the full local development ecosystem with a single command.

### Commands

| Command | Behavior |
|---------|----------|
| `./dev.sh start` (or `./dev.sh`) | Start infrastructure + application |
| `./dev.sh stop` | Stop all Docker containers |
| `./dev.sh restart` | Stop + start |
| `./dev.sh infra` | Start only infrastructure (no app) |

### Start Sequence

1. Run `docker compose down` to clean up stale containers
2. Run `docker compose up -d` to start infrastructure
3. Wait for PostgreSQL health check to pass
4. Start Spring Boot with `local` profile in foreground

### Design Decisions

**Shell script over Makefile**: A bash script is simpler, more readable, and doesn't require Make installation. The project has no existing Makefile convention.

**Script in project root**: Maximizes discoverability. Developers clone and immediately see `dev.sh`.

**`docker compose` v2 syntax**: Uses the modern `docker compose` plugin syntax (not legacy `docker-compose`).

**App runs in foreground**: Spring Boot logs stream directly to the terminal. Ctrl+C stops the app cleanly while infrastructure keeps running.

**`docker compose down` before `up`**: Prevents container name conflicts by always cleaning up first. This is idempotent (safe if no containers exist).

## Consequences

### Positive

- Single command to start everything: `./dev.sh`
- Eliminates container name conflicts
- Ensures infrastructure is ready before app starts
- Easy to extend with future services (frontend, message broker, etc.)
- Self-documenting (script has usage help)

### Negative

- One more file in the project root
- Bash-only (Windows developers need WSL or Git Bash)

## Related Decisions

- [ADR-014: Docker Compose Configuration](ADR-014-docker-compose-configuration.md) - Infrastructure services
- [ADR-015: OTLP Observability Strategy](ADR-015-otlp-observability-strategy.md) - Tempo/Grafana setup
