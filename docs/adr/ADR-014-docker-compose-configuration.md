# ADR-014: Docker Compose Configuration

## Status
Accepted

## Context

The Investment Tracker requires a local development environment with:
- PostgreSQL 16 database (NFR-036)
- OTLP tracing with Grafana Tempo (NFR-051)
- Spring Boot application
- One-command startup for developers

## Decision

### Four-Service Architecture

```yaml
version: '3.8'

services:
  # PostgreSQL 16 - Primary database
  postgres:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: ${POSTGRES_DB:-investments_tracker}
      POSTGRES_USER: ${POSTGRES_USER:-tracker_user}
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD:-tracker_password}
    ports:
      - "${POSTGRES_PORT:-5432}:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${POSTGRES_USER:-tracker_user}"]
      interval: 10s
      timeout: 5s
      retries: 5
    networks:
      - tracker-network

  # Grafana Tempo - OTLP trace collector
  tempo:
    image: grafana/tempo:latest
    command: ["-config.file=/etc/tempo.yaml"]
    volumes:
      - ./infrastructure/observability/tempo.yaml:/etc/tempo.yaml:ro
      - tempo_data:/tmp/tempo
    ports:
      - "3200:3200"   # Tempo UI
      - "4317:4317"   # OTLP gRPC
      - "4318:4318"   # OTLP HTTP
    networks:
      - tracker-network

  # Grafana - Observability visualization
  grafana:
    image: grafana/grafana:latest
    environment:
      - GF_AUTH_ANONYMOUS_ENABLED=true
      - GF_AUTH_ANONYMOUS_ORG_ROLE=Admin
      - GF_AUTH_DISABLE_LOGIN_FORM=true
    ports:
      - "3000:3000"
    volumes:
      - ./infrastructure/observability/grafana-datasources.yml:/etc/grafana/provisioning/datasources/datasources.yaml:ro
      - grafana_data:/var/lib/grafana
    depends_on:
      - tempo
    networks:
      - tracker-network

  # Spring Boot Application (Optional - can run locally via ./gradlew bootRun)
  app:
    build:
      context: .
      dockerfile: Dockerfile
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/${POSTGRES_DB:-investments_tracker}
      - SPRING_DATASOURCE_USERNAME=${POSTGRES_USER:-tracker_user}
      - SPRING_DATASOURCE_PASSWORD=${POSTGRES_PASSWORD:-tracker_password}
      - MANAGEMENT_OTLP_TRACING_ENDPOINT=http://tempo:4318/v1/traces
    ports:
      - "8080:8080"
    depends_on:
      postgres:
        condition: service_healthy
      tempo:
        condition: service_started
    networks:
      - tracker-network

networks:
  tracker-network:
    driver: bridge

volumes:
  postgres_data:    # Database persistence
  tempo_data:       # Trace storage
  grafana_data:     # Dashboard configuration
```

### Key Design Decisions

**PostgreSQL:**
- Alpine variant for reduced image size
- Named volume for data persistence across restarts
- Health check ensures database ready before app starts
- Port 5432 exposed for direct access (psql, IDE connections)

**Grafana Tempo:**
- Lightweight OTLP collector with minimal configuration
- Supports both gRPC (4317) and HTTP (4318) endpoints
- External config file (`tempo.yaml`) for customization
- Tempo UI on port 3200 for trace debugging

**Grafana:**
- Anonymous access enabled (local dev only)
- Pre-configured Tempo datasource via provisioning
- Persistent volume preserves custom dashboards

**Spring Boot Application:**
- Optional service (prefer `./gradlew bootRun` during active development)
- Uses Docker DNS for service discovery (`postgres`, `tempo`)
- Depends on postgres health check to avoid startup failures
- Separate `docker` profile for container-specific configuration

**Environment Variables:**
- `.env` file for local customization (not committed to git)
- Default values prevent startup failures if `.env` missing
- Override ports and credentials per developer

### Configuration Files

**`infrastructure/observability/tempo.yaml`:**
```yaml
server:
  http_listen_port: 3200

distributor:
  receivers:
    otlp:
      protocols:
        grpc:
          endpoint: 0.0.0.0:4317
        http:
          endpoint: 0.0.0.0:4318

storage:
  trace:
    backend: local
    local:
      path: /tmp/tempo/traces

query_frontend:
  search:
    max_duration: 0s  # No search duration limit
```

**`infrastructure/observability/grafana-datasources.yml`:**
```yaml
apiVersion: 1

datasources:
  - name: Tempo
    type: tempo
    access: proxy
    url: http://tempo:3200
    isDefault: true
    editable: true
```

**`.env.example`:**
```env
# Database
POSTGRES_DB=investments_tracker
POSTGRES_USER=tracker_user
POSTGRES_PASSWORD=tracker_password
POSTGRES_PORT=5432

# Application
SPRING_PROFILES_ACTIVE=local
SERVER_PORT=8080

# Observability
OTLP_ENDPOINT=http://localhost:4318/v1/traces
```

## Consequences

### Positive

- One-command setup: `docker-compose up -d`
- Consistent environment across developers (same PostgreSQL, Tempo, Grafana versions)
- Data persistence via named volumes
- Built-in observability with OTLP tracing
- Flexible workflow: run app locally or in Docker
- Isolated network prevents conflicts with other projects
- Easy cleanup: `docker-compose down -v`

### Negative

- Requires Docker and Docker Compose installation
- ~500MB memory usage for 4 containers
- 30-60 second startup time
- Port conflicts possible (3000, 3200, 4317, 4318, 5432, 8080)
- Orphaned volumes can accumulate

### Mitigation

- Document Docker installation in README
- Use Alpine images to minimize resource usage
- Health checks ensure services ready before app starts
- Document port customization via `.env`
- Document volume cleanup: `docker-compose down -v`

## Alternatives Considered

**Kubernetes with Minikube:**
Rejected - over-engineering for local development, steeper learning curve, slower startup

**Manual Setup (no containers):**
Rejected - inconsistent environments, difficult onboarding, version conflicts

**Jaeger instead of Tempo:**
Rejected - requires multiple containers, more complex, Tempo sufficient for MVP

**Application always in Docker:**
Rejected - slower iteration during development, harder debugging, prefer flexibility

## Related Decisions

- [ADR-015: OTLP Observability Strategy](ADR-015-otlp-observability-strategy.md)
- [ADR-016: Database Migration Strategy](ADR-016-database-migration-strategy.md)

## References

- Docker Compose: https://docs.docker.com/compose/
- Grafana Tempo: https://grafana.com/docs/tempo/latest/
- PostgreSQL Docker: https://hub.docker.com/_/postgres
- NFR-036, NFR-051
