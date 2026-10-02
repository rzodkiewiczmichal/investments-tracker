---
paths:
  - "src/main/java/**/infrastructure/**"
---
# Infrastructure layer

## Spring wiring
Domain services have no annotations. Register them as `@Bean` in `infrastructure/config/DomainServiceConfig`.

## Persistence: Spring Data JDBC, not JPA (migrated in PR #27, ADR-028)
- Each aggregate uses three classes in `infrastructure.persistence.*`:
  - `*JdbcRepository`, which extends `ListCrudRepository`,
  - `*PersistenceMapper`, which converts between entity and domain record,
  - `*RepositoryAdapter`, which implements the domain port.
- `*JdbcEntity` are records annotated with `@Table` / `@Id` / `@Column` / `@Version`. Domain records carry no version, so the adapter must read the current `@Version` from the DB before saving.
- No lazy loading or cascades. One adapter per aggregate (ADR-025).
- Ordered data uses `List` plus an ordering column. A `Set` would silently drop identical transactions.

## Cache-aside (ADR-032)
- Lookup order: Redis first, then the external API on a miss. Results are cached with a 24h TTL.
- Keys: `price:current:{symbol}`, `rate:pln:{CURRENCY}`. Everything goes through `StringRedisSerializer`; no Java serialization.
- **Only the outer `Caching*Adapter` implements the domain port.** The inner `Redis*Adapter` is a plain class, otherwise Spring finds two candidate beans.

## REST
Base path `/api/v1`. Use `@Valid` on request bodies. Errors are handled by `GlobalExceptionHandler`, and the trace id is propagated for Tempo.
