# ADR-028: Spring Data JDBC as Persistence Mechanism

## Status
Accepted

## Context

ADR-027 decided to replace Hibernate/JPA due to its fundamental conflict with immutable domain records. This ADR evaluates the available replacement options and selects the best fit.

### Requirements

The replacement must:
1. Have no entity tracking (no session, no identity map, no dirty checking)
2. Support immutable Java records natively
3. Handle aggregate collections (Position with List of AccountHoldings)
4. Support optimistic locking (`@Version`)
5. Integrate well with Spring Boot, Spring transactions, and Flyway
6. Work with the existing PostgreSQL schema (no migration changes needed)

## Options Considered

### Option 1: Spring Data JDBC

Spring Data's lightweight alternative to JPA, explicitly designed without entity tracking. Part of the Spring Data family but fundamentally different from Spring Data JPA.

**Key characteristics:**
- No entity tracking by design — "if you want to save it, call `save()`"
- No session, no proxies, no lazy loading
- Designed around DDD aggregate concept — aggregates loaded and saved as a unit
- First-class immutability support (designed for Java records)
- Built-in `@Version` optimistic locking
- Supports 1-1 and 1-n relationships within aggregates
- References between aggregates by ID only (enforces aggregate boundaries)
- `CrudRepository` interface (familiar Spring Data pattern)

**Aggregate collection handling:**
```java
@Table("positions")
public record PositionJdbcEntity(
    @Id String instrumentSymbol,
    BigDecimal totalQuantity,
    // ...
    List<AccountHoldingRef> holdings  // Loaded/saved with aggregate
) {}
```

### Option 2: jOOQ

Type-safe SQL DSL that generates Java code from the database schema. Not an ORM — a SQL builder.

**Key characteristics:**
- No entity tracking — operates at SQL level
- Compile-time SQL verification (schema changes break compilation)
- Excellent for complex queries, reporting, analytics
- Manual collection handling required (no aggregate support)
- Optimistic locking supported via generated record version fields
- Code generation setup required (from Flyway migrations via Testcontainers)

**Collection handling:**
```java
// Must handle manually — write JOIN, aggregate results
public Position save(Position position) {
    dsl.insertInto(POSITIONS).set(...).onConflict(POSITIONS.INSTRUMENT_SYMBOL).doUpdate().set(...).execute();
    dsl.deleteFrom(ACCOUNT_HOLDINGS).where(ACCOUNT_HOLDINGS.INSTRUMENT_SYMBOL.eq(...)).execute();
    for (AccountHolding h : position.holdings()) {
        dsl.insertInto(ACCOUNT_HOLDINGS).set(...).execute();
    }
}
```

### Option 3: Spring JdbcClient / JdbcTemplate

Raw JDBC with Spring's thin abstraction layer. Maximum control, minimum magic.

**Key characteristics:**
- No entity tracking — pure SQL execution
- SQL is plain strings (no compile-time safety)
- No collection handling (fully manual)
- No built-in optimistic locking (must implement manually)
- Perfect for Java records (constructor-based mapping)
- Minimal learning curve (just SQL)

### Option 4: MyBatis

SQL mapper framework. Maps SQL queries to Java methods.

**Key characteristics:**
- No entity tracking
- Fair immutability support (reflection issues with final fields)
- Collection handling via `@One`/`@Many` annotations
- No built-in optimistic locking
- XML or annotation configuration

## Decision

**Adopt Spring Data JDBC.**

### Rationale

| Requirement | Spring Data JDBC | jOOQ | JdbcClient | MyBatis |
|---|---|---|---|---|
| No entity tracking | Yes (by design) | Yes | Yes | Yes |
| Immutable records | Excellent | Excellent | Excellent | Fair (quirks) |
| Aggregate collections | Built-in (1-n) | Manual | Manual | @One/@Many |
| `@Version` locking | Built-in | Built-in | Manual | Manual |
| Spring Boot integration | Excellent | Excellent | Built-in | Excellent |
| DDD alignment | Designed for it | N/A | N/A | N/A |

Spring Data JDBC is the strongest match because:

1. **DDD aggregate design:** The project's domain already models aggregates correctly (Position owns AccountHoldings, aggregates reference each other by ID). Spring Data JDBC is built around this exact pattern.

2. **Immutability first-class:** Designed for immutable objects. No workarounds needed.

3. **Collection handling:** Position's `List<AccountHolding>` is persisted automatically as part of the aggregate — the specific collection pattern that motivated this migration.

4. **Built-in optimistic locking:** `@Version` works out of the box, matching the existing schema.

5. **Minimal migration:** Same `CrudRepository` interface pattern, same Spring Data conventions. Domain model and ports require zero changes.

6. **Explicit saves:** No dirty checking — `save()` is explicit. Matches the project's "no magic" philosophy.

jOOQ was the runner-up and would be a strong choice for a future addition to handle complex read queries (portfolio analytics, reporting). The two can coexist on the same `DataSource`.

## Consequences

### Positive
- Persistence layer fully aligned with immutable domain design
- Aggregate collections handled natively
- No entity-tracking bugs possible
- Explicit, predictable save behavior
- Simpler infrastructure code (no dual mapping, no session management)

### Negative
- No lazy loading — aggregates always loaded completely (acceptable for small aggregates)
- No cross-aggregate joins in repositories — cross-aggregate queries use `@Query` with SQL strings (or add jOOQ later)
- Schema must match Spring Data JDBC naming conventions (or use explicit `@Table`/`@Column`)

### Migration scope
- **Zero domain changes** — domain records and repository ports are framework-agnostic
- **Infrastructure only** — JPA entities, JPA repositories, mappers, adapters, build config, test config
- **Schema unchanged** — Flyway migrations work as-is; DB triggers handle version/timestamp management

## Related Decisions

- [ADR-027: Impedance Mismatch](ADR-027-immutable-domain-vs-hibernate-entity-tracking.md) — the problem this solves
- [ADR-018: Domain Model Implementation Rules](ADR-018-domain-model-implementation-rules.md) — immutability requirement
- [ADR-025: Repository Adapter Single Aggregate](ADR-025-repository-adapter-single-aggregate.md) — adapter structure (to be updated)
