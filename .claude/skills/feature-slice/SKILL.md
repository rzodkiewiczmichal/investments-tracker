---
name: feature-slice
description: Implement a complete feature as a vertical slice through all layers (scenario, domain, application, persistence, REST, frontend). Use when the user asks to implement a functional requirement or a feature that touches more than one layer, e.g. "implement FR-072", "add XIRR", "add account-level position view".
---

# Implement a vertical feature slice

Each layer's conventions load automatically from `.claude/rules/` as you touch its files. This skill gives only the **order of work** and the checkpoints.

## 0. Frame the work
- **Issue and branch.** Find the GitHub issue (and its epic) for the work. If none exists, propose one under the right epic and wait for the user's confirmation before creating it. Then branch off main as `rzodkiewiczmichal/<epic#>-<issue#>-<short-description>`.
- **Read the context:**
  - the FR(s) and their target version,
  - the relevant ADRs (`docs/adr/README.md`),
  - the existing code in the area.
- **Design checkpoint (stop and present to the user):**
  - aggregates and value objects touched or added,
  - port placement,
  - new use cases,
  - endpoint shape,
  - migration needs,
  - state conflicts with existing data,
  - open questions.

  Flag every questionable DDD decision explicitly. Don't write production code before the user confirms.

## 1. Acceptance first
Use the `bdd-scenario` skill to write the scenarios for the FR. They must fail at this point.

## 2. Domain
- Value objects and entities in `domain/model` / `domain/model/value`, written test-first. Domain unit tests are pure Java with no mocks.
- A domain service only for logic that spans aggregates.
- Ports in `domain/repository`.

## 3. Application
- A `*UseCase` + `*UseCaseService` (query vs command), with a unit test that uses mocked ports.
- DTOs and mappers in `application/dto`.

## 4. Infrastructure
- If needed, a Flyway migration plus the persistence trio (entity, `JdbcRepository`, mapper → adapter), with an integration test.
- The controller in `infrastructure/web/controller`. Update `docs/api/openapi.yaml`.

## 5. Frontend (if the feature is user-facing)
Model and service in `core/`, component in `features/<area>/`, route in `app.routes.ts`. Run `npm run lint && npm run build`.

## 6. Green and done
- `./gradlew spotlessApply && ./gradlew clean build`: everything passes, including the new scenarios and ArchUnit.
- Smoke test with `./dev.sh start` when the feature has UI.
- Update docs that are affected: diagrams, FR status, the version tag filter in `RunCucumberTest`.
- Then follow the mandatory post-implementation review from `CLAUDE.md`.
