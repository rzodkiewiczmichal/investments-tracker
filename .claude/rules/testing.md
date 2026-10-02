---
paths:
  - "src/test/**"
  - "build.gradle.kts"
---
# Testing

| Gradle task | Runs | Docker |
|---|---|---|
| `test` | unit tests only (excludes `*IntegrationTest`, `*IT`, `*RunCucumber*`, `*LiveTest`) | no |
| `integrationTest` | Testcontainers (PostgreSQL **and** Redis) | yes |
| `cucumberTest` | Cucumber BDD | yes |
| `liveTest` | real external APIs, never in CI | yes + network |
| `check` / `build` | test + integrationTest + cucumberTest | yes |

CI (GitHub Actions) runs spotlessCheck, then build, then a JaCoCo gate of at least 70%. JaCoCo excludes `InvestmentTrackerApplication`, `dto/**`, `config/**` and `*JdbcEntity`.

## Cucumber (`src/test/resources/features/`, steps in `cucumber/`)
- **Step patterns are global.** Two step classes must never share a pattern, so prefix by context (e.g. `position P&L` vs `P&L`).
- **Given steps insert data via `JdbcTemplate`**, never through the REST API.
- Scenarios are tagged with a version (`@v0.1`, `@v0.2`, ...) and an FR (`@FR-021`). To enable a version, update the filter in `RunCucumberTest`: `(@v0.1 or @v0.2) and not @ignored`.
- When a scenario covers a requirement, keep `planning/scenarios-to-requirements.md` in sync.

## Other
- Test helpers: `IntegrationTestBase`, `TestDataBuilder`, and the stubs `StubCurrentPriceProvider`, `StubExchangeRateProvider`.
- ArchUnit tests that may legitimately run on empty packages use `.allowEmptyShould(true)`.
