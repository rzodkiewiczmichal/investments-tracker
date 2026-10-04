---
name: bdd-scenario
description: Write or extend Cucumber BDD scenarios and step definitions for a functional requirement (FR-xxx). Use when adding acceptance tests, turning a requirement into Gherkin, enabling scenarios for a new version tag, or fixing failing Cucumber tests.
---

# Write a Cucumber scenario from a requirement

Cucumber practice is a primary goal of this project, so scenarios come **before** implementation. Conventions are in `.claude/rules/testing.md`.

## Steps
1. **Read the requirement:**
   - its FR entry in `requirements/functional/functional-requirements.md`,
   - its version in `planning/requirements-by-version.md`,
   - the related terms in `requirements/functional/ubiquitous-language.md`. Gherkin uses that vocabulary exactly.
2. **Choose the feature file** in `src/test/resources/features/`: `account-management`, `import`, `manual-entry`, `portfolio-viewing` or `position-details`. Create a new file only for a new capability area.
3. **Reuse steps before writing new ones.** Run `scripts/list-steps.sh` (optionally with a grep filter) and reuse matching patterns. Step patterns are global, so a new one must not collide with an existing one; prefix it by context if needed.
4. **Write the scenario.**
   - Business language only: no endpoints, JSON or SQL.
   - Use concrete example values that make the calculation checkable by hand.
   - Tags: area (`@import`), version (`@v0.2`), requirement(s) (`@FR-021`), and the broker where relevant (`@xtb`).
   - Use `Scenario Outline` for validation variants.
5. **Implement the steps** in the matching class in `src/test/java/**/cucumber/steps/`. Shared steps go in `CommonSteps`, helpers in `CucumberTestHelper`.
   - **Given** steps insert data with `JdbcTemplate`, never through the REST API.
   - **When** steps call the REST API.
   - **Then** steps assert on API responses.
6. **Run it red first, then green.**
   - Run `./gradlew cucumberTest`; the HTML report is at `build/reports/cucumber/cucumber-report.html`.
   - If the version tag isn't enabled yet, update the filter in `RunCucumberTest` only when the feature for that version is actually being delivered.
7. **Keep traceability in sync:**
   - add or adjust the scenario entry in `planning/scenarios-to-requirements.md`,
   - fill in the "Related Scenarios" and "Cucumber Tags" fields of the FR.
