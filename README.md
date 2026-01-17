# Investment Tracker

Application for tracking investments across multiple broker accounts with support for stocks, ETFs, and bonds.

## Overview

Investment Tracker is a personal finance application built with:
- **Architecture:** Hexagonal (Ports & Adapters) with Domain-Driven Design
- **Backend:** Java 25 LTS + Spring Boot 3.4.x
- **Database:** PostgreSQL 16 with encryption at rest
- **Frontend:** Angular 19 (planned)
- **Observability:** OTLP tracing with Grafana Tempo
- **Testing:** Cucumber BDD, JUnit 5, ArchUnit

### Key Features (v0.1 MVP)
- Manual entry of positions (stocks/ETFs)
- Portfolio summary with P&L metrics
- Position details with performance tracking
- Single account support
- Real-time observability with distributed tracing

## Quick Start

### Prerequisites

- **Docker** & **Docker Compose** (for local development)
- **Java 25** (JDK 25 or later)
- **Gradle** (uses Gradle Wrapper - no installation required)

### 1. Start Infrastructure Services

```bash
# Start PostgreSQL, Grafana Tempo, and Grafana
docker-compose up -d
```

This starts:
- **PostgreSQL 16** on `localhost:5432`
- **Grafana Tempo** (OTLP collector) on `localhost:3200` (UI), `4317` (gRPC), `4318` (HTTP)
- **Grafana** (visualization) on `localhost:3000`

### 2. Run Application Locally

```bash
# Run Spring Boot application
./gradlew bootRun --args='--spring.profiles.active=local'
```

Application starts on `http://localhost:8080`

### 3. Access Services

| Service | URL | Credentials |
|---------|-----|-------------|
| Application | http://localhost:8080 | N/A |
| Grafana | http://localhost:3000 | None (anonymous access enabled) |
| Tempo UI | http://localhost:3200 | N/A |
| PostgreSQL | localhost:5432 | `tracker_user` / `tracker_password` |

### 4. View Traces in Grafana

1. Open http://localhost:3000
2. Navigate to **Explore** → **Tempo**
3. Search for traces:
   - By service: `investments-tracker`
   - By operation: `GET /api/v1/positions`
   - By trace ID (from logs or error responses)

## Development Workflow

### Build & Test

```bash
# Build the project (compile, run tests, create JAR)
./gradlew build

# Clean build artifacts and rebuild from scratch
./gradlew clean build

# Run tests only
./gradlew test

# Run specific test
./gradlew test --tests "ArchitectureLayerTest"

# Check dependencies
./gradlew dependencies
```

**Note for Windows users:** Use `gradlew.bat` instead of `./gradlew`

### Database Access

```bash
# Connect to PostgreSQL via psql
psql -h localhost -U tracker_user -d investments_tracker

# View tables
\dt

# View Flyway migration history
SELECT * FROM flyway_schema_history;
```

### Stop Services

```bash
# Stop all Docker services (preserve data)
docker-compose down

# Stop and remove volumes (clean slate)
docker-compose down -v
```

## Project Structure

```
investments-tracker/
├── docs/
│   ├── adr/                        # Architecture Decision Records
│   └── api/                        # OpenAPI specification
├── infrastructure/
│   └── observability/              # Tempo & Grafana configs
├── requirements/                   # Functional & non-functional requirements
├── src/
│   ├── main/
│   │   ├── java/com/investments/tracker/
│   │   │   ├── domain/            # Domain model (entities, value objects, services)
│   │   │   ├── application/       # Use cases, ports, DTOs
│   │   │   └── infrastructure/    # Adapters (REST, JPA, observability)
│   │   └── resources/
│   │       └── db/migration/      # Flyway database migrations
│   └── test/
│       ├── java/com/investments/tracker/
│       │   ├── architecture/      # ArchUnit tests
│       │   ├── cucumber/          # BDD tests
│       │   └── ...                # Unit & integration tests
│       └── resources/
│           └── features/          # Cucumber feature files
├── docker-compose.yml             # Local development environment
├── .env.example                   # Environment variables template
└── build.gradle.kts               # Gradle build configuration
```

## Architecture

This project follows **Hexagonal Architecture** (Ports & Adapters) with strict layer boundaries:

```
┌─────────────────────────────────────────────────────┐
│                   Infrastructure                     │
│  (REST Controllers, JPA Repositories, OTLP Tracing) │
│                         ▼                            │
│                    Application                       │
│         (Use Cases, Ports, DTOs, Mappers)           │
│                         ▼                            │
│                      Domain                          │
│   (Entities, Value Objects, Domain Services)        │
└─────────────────────────────────────────────────────┘
```

**Key Principles:**
- Domain layer has **zero dependencies** on infrastructure
- Application layer depends only on domain
- Infrastructure adapters implement ports defined in application layer
- Enforced by ArchUnit tests

See [ADR-001: Hexagonal Architecture](docs/adr/ADR-001-hexagonal-architecture.md) for details.

## Documentation

### Architecture Decision Records (ADRs)
- [ADR-001: Hexagonal Architecture](docs/adr/ADR-001-hexagonal-architecture.md)
- [ADR-002: Java Records for Value Objects](docs/adr/ADR-002-java-records-value-objects.md)
- [ADR-003: Application Services as Use Cases](docs/adr/ADR-003-application-services-use-cases.md)
- [ADR-004: Package Structure](docs/adr/ADR-004-package-structure.md)
- [ADR-005: Technology Stack](docs/adr/ADR-005-technology-stack.md)
- [ADR-006: Database Precision Configuration](docs/adr/ADR-006-database-precision-configuration.md)
- [ADR-007: API Versioning Strategy](docs/adr/ADR-007-api-versioning-strategy.md)
- [ADR-008: Dependency Management](docs/adr/ADR-008-dependency-management.md)
- [ADR-009: REST API Structure](docs/adr/ADR-009-rest-api-structure.md)
- [ADR-010: Error Handling Strategy](docs/adr/ADR-010-error-handling-strategy.md)
- [ADR-011: Data Validation Strategy](docs/adr/ADR-011-data-validation-strategy.md)
- [ADR-012: Test Architecture](docs/adr/ADR-012-test-architecture.md)
- [ADR-013: Mock vs Real Dependencies Strategy](docs/adr/ADR-013-mock-vs-real-dependencies.md)
- [ADR-014: Docker Compose Configuration](docs/adr/ADR-014-docker-compose-configuration.md)
- [ADR-015: OTLP Observability Strategy](docs/adr/ADR-015-otlp-observability-strategy.md)
- [ADR-016: Database Migration Strategy](docs/adr/ADR-016-database-migration-strategy.md)
- [ADR-017: Transaction Boundaries](docs/adr/ADR-017-transaction-boundaries.md)

### Requirements
- [Functional Requirements](requirements/functional/functional-requirements.md) - 57 requirements
- [Non-Functional Requirements](requirements/non-functional/non-functional-requirements.md) - 48 requirements
- [Ubiquitous Language](requirements/functional/ubiquitous-language.md)
- [User Personas](requirements/functional/user-personas.md)

### API Documentation
- [OpenAPI 3.0 Specification](docs/api/openapi.yaml)

### Planning
- [Version Roadmap](planning/versions-roadmap.md) - v0.1 through v2.1+
- [Requirements by Version](planning/requirements-by-version.md)

## Testing

### Run All Tests

```bash
./gradlew test
```

### Test Types

1. **Architecture Tests** (ArchUnit)
   - Enforce hexagonal architecture boundaries
   - Validate naming conventions
   - Verify dependency rules

2. **Domain Unit Tests**
   - Test domain logic in isolation
   - No mocks (pure Java)
   - Fast execution

3. **Application Unit Tests**
   - Test use cases with mocked ports
   - Business logic validation

4. **Integration Tests** (Testcontainers)
   - Real PostgreSQL database
   - Test adapters (JPA repositories, REST controllers)
   - Flyway migrations applied

5. **Cucumber BDD Tests**
   - End-to-end scenarios
   - Real infrastructure
   - Feature files in `src/test/resources/features/`

See [ADR-012: Test Architecture](docs/adr/ADR-012-test-architecture.md) for details.

## Configuration

### Environment Variables

Copy `.env.example` to `.env` and customize:

```bash
cp .env.example .env
```

**Available Variables:**
```properties
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
TRACING_SAMPLING_RATE=1.0
```

### Spring Profiles

| Profile | Usage | Description |
|---------|-------|-------------|
| `local` | Local development | 100% trace sampling, verbose logging |
| `docker` | Docker container | Uses Docker service names |
| `test` | Integration tests | Testcontainers PostgreSQL |
| `prod` | Production | 10% sampling, minimal logging |

## Troubleshooting

### Port Conflicts

If ports 3000, 3200, 4317, 4318, 5432, or 8080 are in use, you can change them in `.env`:

```properties
POSTGRES_PORT=5433
SERVER_PORT=8081
```

### Database Connection Issues

```bash
# Check if PostgreSQL is running
docker-compose ps

# View PostgreSQL logs
docker-compose logs postgres

# Restart PostgreSQL
docker-compose restart postgres
```

### Traces Not Appearing in Grafana

1. Verify Tempo is running: `docker-compose ps tempo`
2. Check Tempo logs: `docker-compose logs tempo`
3. Verify OTLP endpoint in application logs
4. Ensure Spring Boot Actuator dependencies are present

### Reset Database

```bash
# Stop services and remove volumes
docker-compose down -v

# Start fresh
docker-compose up -d
./gradlew bootRun --args='--spring.profiles.active=local'
```

## License

Private project - not licensed for public use.
