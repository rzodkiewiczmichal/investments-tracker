# Investment Tracker

Private app tracking investments across multiple broker accounts (`rzodkiewiczmichal/investments-tracker`).
**Main goal:** practice clean architecture — DDD, Cucumber BDD, backend craftsmanship. **Minor goal:** a working app for personal use.
Stack: Java 21 / Spring Boot 3.4 / Spring Data JDBC / PostgreSQL 16 / Redis 7 · Angular 19 / PrimeNG 19. Hexagonal architecture + DDD, base package `com.investments.tracker`.

## How project context is organized
This file holds only always-on rules and an index. Detailed conventions live in `.claude/rules/*.md` and load **automatically** when you touch matching paths. Read the documents in the index **on demand**.

## Always-on rules
- **Branches:** never commit to `main`. Branch name: `rzodkiewiczmichal/<epic#>-<issue#>-<short-description>` (primary issue if several).
- **Build must pass before presenting work:** `./gradlew spotlessApply && ./gradlew clean build` (needs Docker; `./gradlew test` = unit only, no Docker). Frontend: `cd frontend && npm run lint && npm run build`.
- **Post-implementation review (mandatory):** when an implementation is finished and the build passes, run `/ddd-java` and `/effective-java` on the changed files, apply the fixes, and ask the user about any ambiguous trade-off. Write `post-implementation.md` (issues, fixes, open questions). **Never commit it.**
- **Post-push issue closure (mandatory):** after a merge or push to `main`, find the open issues the work resolved and close them with a comment naming the PR(s). If no issue matches, ask whether to create and close one for traceability.
- **Track work in GitHub Issues:** epics with native sub-issues; link with "Closes #N" / "Part of #N"; close with `state_reason: completed`.
- **No hardcoded domain data** (brokers, accounts) in frontend or backend. Prefer free text with server-side validation. Dropdowns need a persistent, user-managed source. Exception: the instrument catalog, which is seeded by Flyway (ADR-033).
- **Never guess critical data:** no fuzzy or heuristic matching of instrument symbols, accounts, or amounts. Return unmatched items to the user to resolve.
- **Validate against real data:** before designing any parser or import logic, analyze all sample files (`.context/attachments/`, test resources).
- **Think through state conflicts:** for any create, update, delete, or import, raise up front what happens when the data already exists from another source.
- **DDD-aware design:** flag questionable layer placement, aggregate boundaries, or port/adapter placement proactively. Don't wait for a review.
- **Atomic renames:** update every reference in the same step, then grep for zero leftovers before building.
- **Output types:** permanent artifacts go in the repo. Long, throwaway analyses go in `temp/`. Short answers stay in the terminal.
- **Single source of truth:** every fact has one home. Reference it rather than copying it.
- **User communicates tersely:** act on the obvious intent of short messages ("push this", "create them").

## Local development
`./dev.sh start|stop|restart|reset|clear|infra`. Always use `dev.sh` rather than raw Docker or Gradle commands.
- `reset`: rebuild and clear positions, imports, and mappings.
- `clear`: clear the DB only.
- `infra`: start the containers only.
- The instrument catalog survives both `reset` and `clear`.

Ports: backend 8080, frontend 4200 (proxies `/api`), Postgres 5432, Redis 6379, Grafana 3000, Tempo 3200/4317/4318.
Backend runs with profile `local`. Finnhub needs `FINNHUB_API_KEY`.
- `docker-compose.yml` keeps its fixed `name: investments-tracker`, so volumes persist across Conductor workspaces.
- `src/test/resources/docker-java.properties` pins Docker API 1.44 so Testcontainers works with Docker 29+.

## Index — where to find things
| Topic | Location |
|---|---|
| Layering, package placement, ArchUnit naming | `.claude/rules/architecture.md` (auto: `src/main/java/**`) |
| Java style (records, nullability, Optional, exceptions) | `.claude/rules/java-style.md` (auto: `**/*.java`) |
| Domain model rules, multi-currency | `.claude/rules/domain-model.md` (auto: `domain/**`) |
| Use cases, CQRS-lite, transactions | `.claude/rules/application-layer.md` (auto: `application/**`) |
| Persistence (Spring Data JDBC), cache-aside, Spring config | `.claude/rules/infrastructure.md` (auto: `infrastructure/**`) |
| Price providers (Stooq, Finnhub, NBP), symbol ACL | `.claude/rules/external-integrations.md` (auto: `external/**`, `cache/**`) |
| Broker import (mBank, XTB, DEGIRO), FIFO | `.claude/rules/import.md` (auto: import and broker paths) |
| Tests, Gradle tasks, Cucumber, ArchUnit | `.claude/rules/testing.md` (auto: `src/test/**`) |
| Flyway migrations | `.claude/rules/flyway.md` (auto: `db/migration/**`) |
| Requirements and planning docs | `.claude/rules/docs.md` (auto: `requirements/**`, `planning/**`, `docs/**`) |
| Frontend | `.claude/rules/frontend.md` (auto: `frontend/**`) |
| Functional / non-functional requirements | `requirements/functional/functional-requirements.md`, `requirements/non-functional/non-functional-requirements.md` |
| Ubiquitous language, personas | `requirements/functional/ubiquitous-language.md`, `requirements/functional/user-personas.md` |
| Roadmap and requirement→version map | `planning/versions-roadmap.md`, `planning/requirements-by-version.md`, `planning/scenarios-to-requirements.md` |
| ADRs | `docs/adr/README.md` (index) |
| API contract | `docs/api/openapi.yaml` |
| Domain model and DB diagrams | `docs/diagrams/domain-model.md`, `docs/diagrams/database-schema.md` |
| Broker file format analyses | `docs/import-formats/` |
