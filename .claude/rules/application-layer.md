---
paths:
  - "src/main/java/**/application/**"
---
# Application layer

- **Naming:** interface `*UseCase`, implementation `*UseCaseService`. Both live in `application/usecase/`. Use cases return the domain model (ADR-022). DTO mapping happens in `dto/mapper`.
- **CQRS-lite where it fits:** `*QueryUseCase` uses `@Transactional(readOnly = true)`, `*CommandUseCase` uses `@Transactional`. Multi-step flows get one use case per step (import: `InitiateImport`, `ConfirmImport`, `ProvideImportPrices`, `GetImportSession`).
- **Transactions (ADR-017):** `@Transactional` only at use-case level, isolation `READ_COMMITTED`. The domain layer stays transaction-agnostic.
- **Orchestration lives in use cases**, not in controllers. That covers cross-aggregate composition (ADR-026), market data enrichment, and multi-step flows. Controllers stay thin.
- **Application exceptions** (`ResourceNotFoundException` → 404, `ResourceAlreadyExistsException` → 409) belong in `application.exception`. Domain exceptions are for domain rule violations only.
- **DTOs** that cross layer boundaries need a justification for where they sit. Controllers never return domain objects.
