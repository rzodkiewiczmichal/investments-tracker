# ADR-027: Impedance Mismatch Between Immutable Domain Records and Hibernate Entity Tracking

## Status
Problem identified — solution pending

## Context

Two foundational architectural decisions in this project are in tension:

1. **ADR-018 (Domain Model Rules):** All domain types are immutable Java records. State changes return new instances.
2. **Hibernate/JPA:** Entities are mutable objects managed by the Hibernate session. Hibernate tracks entity identity by Java object reference within a transaction, and detects changes by comparing the managed object's current state to a snapshot taken at load time.

### How the conflict manifests

Consider the update flow for Position (adding a second account holding to an existing position):

```
PositionCommandUseCaseService.addPosition("ATREM", accountB, ...)
│
│  // Step 1: Load existing position
│  positionRepository.findBySymbol("ATREM")
│      └─ jpaRepository.findById("ATREM")
│          └─ Hibernate loads PositionJpaEntity@aaa into session (version=0)
│      └─ mapper.toDomain(@aaa) → Position record (immutable)
│
│  // Step 2: Domain logic — returns a NEW Position instance
│  existing.addHolding(accountB, qty, cost)
│      └─ Returns brand-new Position record (old one discarded)
│
│  // Step 3: Save updated position
│  positionRepository.save(newPositionRecord)
│      └─ mapper.toEntity(newPositionRecord)
│          └─ Creates PositionJpaEntity@bbb (version=null, same @Id="ATREM")
│      └─ jpaRepository.save(@bbb)
│          └─ Spring Data: isNew()? version==null → yes → persist() (INSERT)
│              └─ FAILS: session already tracks @aaa for @Id="ATREM"
```

The error: `NonUniqueObjectException: A different object with the same identifier value was already associated with the session`

### Root cause analysis

The problem has three interacting parts:

**1. Immutable domain records discard the original object.**
`Position.addHolding()` returns a new `Position` instance — it has no reference to the JPA entity it was constructed from. The mapper then creates a completely new `PositionJpaEntity` from this new domain object.

**2. The new JPA entity has no version.**
`mapper.toEntity()` creates a fresh `PositionJpaEntity` with `@Version version = null`. Spring Data's `JpaRepository.save()` uses the `@Version` field to decide insert vs. update:
- `version == null` → entity is new → `EntityManager.persist()` (INSERT)
- `version != null` → entity exists → `EntityManager.merge()` (UPDATE)

**3. Hibernate session tracks the old entity.**
The `findBySymbol()` call earlier in the same transaction loaded `PositionJpaEntity@aaa` into the Hibernate session. When `persist()` is called with `@bbb` (a different Java object with the same `@Id`), Hibernate rejects it because it already manages `@aaa` for that ID.

Even if the session were empty (no prior `findBySymbol`), `persist()` would still fail at the database level with a duplicate primary key constraint violation, because it always issues an INSERT.

### Scope of the problem

This affects **every aggregate repository adapter that supports updates**, not just Position. Any adapter where:
1. The use case loads an aggregate (entity enters Hibernate session)
2. Domain logic produces a new immutable instance
3. The adapter maps the new domain object to a new JPA entity and saves it

...will hit this same issue. Currently this includes `PositionRepositoryAdapter`. The `InstrumentRepositoryAdapter` and `AccountRepositoryAdapter` would face it too if they gain update operations.

### Observations

- The problem exists specifically because we use **Hibernate/JPA with `@Version`-based newness detection**. A plain JDBC or jOOQ approach would not have this issue, because there is no session/entity tracking.
- ADR-025 shows a `save()` implementation using the naive `mapper.toEntity()` + `jpaRepository.save()` pattern, which is the exact code that triggers this bug.
- The domain layer is correct and should remain immutable. The problem is entirely in the infrastructure adapter layer — how it bridges immutable domain records back to Hibernate's mutable managed entities.

## Options Considered

### Option A: Adapter-level workaround (load-then-mutate managed entity)

Keep Hibernate/JPA. In each repository adapter's `save()`, load the existing managed entity from the Hibernate session (or DB), mutate it to match the new domain state, and let Hibernate dirty-check and flush. Only create a new JPA entity on the INSERT path.

```java
public Position save(Position position) {
    PositionJpaEntity entity = jpaRepository.findById(position.symbol().value())
            .map(existing -> {
                mapper.updateEntity(existing, position);  // mutate managed entity
                return existing;
            })
            .orElseGet(() -> mapper.toEntity(position));  // new entity for INSERT
    PositionJpaEntity saved = jpaRepository.save(entity);
    return mapper.toDomain(saved, position.currentPrice());
}
```

**Pros:**
- No architectural change — fix stays within the adapter layer
- Hibernate features (dirty checking, optimistic locking, cascade) still available
- Each adapter encapsulates the workaround; domain and application layers untouched
- Pattern is well-known in Spring Data JPA projects

**Cons:**
- Every adapter that supports updates must implement the load-then-mutate pattern
- Extra DB query on every update (`findById` inside `save()`) — though Hibernate's first-level cache often serves it from memory
- Mapper needs both `toEntity()` (for insert) and `updateEntity()` (for update) — dual mapping logic
- Easy to forget in new adapters, leading to the same bug again
- Conceptual mismatch remains: mutable JPA entities coexist with immutable domain records

### Option B: Replace Hibernate with Spring JDBC / jOOQ

Drop Hibernate/JPA entirely. Use Spring `JdbcTemplate` or jOOQ for persistence. The adapter translates domain records directly to SQL INSERT/UPDATE statements. No entity tracking, no session, no identity map.

```java
public Position save(Position position) {
    if (existsBySymbol(position.symbol())) {
        jdbcTemplate.update(
            "UPDATE positions SET total_quantity=?, avg_cost_basis_amount=?, ... WHERE instrument_symbol=?",
            position.calculateTotalQuantity().toBigDecimal(),
            ...
            position.symbol().value());
        // Delete and re-insert holdings
    } else {
        jdbcTemplate.update("INSERT INTO positions ...", ...);
    }
    // Or use a single UPSERT: INSERT ... ON CONFLICT (instrument_symbol) DO UPDATE
    return position;
}
```

**Pros:**
- Eliminates the entire category of entity-tracking bugs
- No impedance mismatch — immutable domain records map directly to SQL
- Simpler mental model: no session, no managed/detached states, no `persist()` vs `merge()`
- Full control over SQL — UPSERT, batch operations, etc.
- Lighter dependency footprint
- Aligns with Khononov's recommendation that rich domain models don't need an ORM

**Cons:**
- Significant migration effort — rewrite all adapters, mappers, and JPA repositories
- Lose Hibernate features: dirty checking, lazy loading, cascade, `@Version` optimistic locking
- Must implement optimistic locking manually (version check in UPDATE WHERE clause)
- Collection mappings (account_holdings) require manual JOIN logic
- More SQL to write and maintain
- Flyway migrations remain unchanged, but JPA entity classes become obsolete

### Option C: Event Sourcing

Instead of persisting current aggregate state, persist domain events (`HoldingAdded`, `HoldingRemoved`). Reconstruct the aggregate by replaying events. No entity tracking because there are no mutable entities to track.

```java
// Domain produces events
public record HoldingAdded(InstrumentSymbol symbol, AccountId accountId,
                           Quantity quantity, CostBasis costBasis) implements DomainEvent {}

// Repository appends events
public void save(Position position, List<DomainEvent> events) {
    for (DomainEvent event : events) {
        jdbcTemplate.update("INSERT INTO position_events (symbol, event_type, payload, ...) VALUES ...");
    }
}

// Reconstitution
public Position findBySymbol(InstrumentSymbol symbol) {
    List<DomainEvent> events = loadEvents(symbol);
    return Position.reconstitute(events);  // replay to build current state
}
```

**Pros:**
- Completely eliminates the mismatch — no mutable entities at all
- Full audit trail of every change
- Natural fit for immutable domain records
- Enables temporal queries ("what was the position on date X?")

**Cons:**
- Dramatic architectural change — fundamentally different persistence model
- Requires event store, event replay, potentially snapshots for performance
- Reconstitution from events adds latency (mitigated by snapshots)
- Querying current state requires projections (read models), adding CQRS complexity
- Overkill for the project's current scope and goals
- Steep learning curve; shifts the project focus away from practicing DDD + hexagonal with JPA

## Decision

**Adopt Option B: Replace Hibernate with a lightweight persistence mechanism.**

Hibernate's entity tracking model (session, identity map, dirty checking, `persist()` vs `merge()`) adds complexity that fundamentally conflicts with the project's immutable domain record design (ADR-018). The adapter-level workaround (Option A) is viable but treats the symptom — every adapter must work around Hibernate's assumptions, and the mismatch will resurface as new aggregates gain update operations.

Replacing Hibernate eliminates the entire class of entity-tracking bugs and aligns the persistence layer with the domain's immutability principle. The domain model and domain repository ports require zero changes — the migration is entirely within the infrastructure layer.

The specific replacement technology is documented in [ADR-028](ADR-028-spring-data-jdbc-persistence.md).

## Consequences

### Positive
- Eliminates entity-tracking bugs permanently (no `NonUniqueObjectException`, no detached entity issues)
- Removes the need for dual mapping (`toEntity` + `updateEntity`) in every adapter
- Simpler mental model for persistence — no session states, no `persist()` vs `merge()` distinction
- Infrastructure layer aligns with the domain's immutability principle

### Negative
- Migration effort to rewrite all persistence adapters, entities, and repositories
- Loss of Hibernate features (dirty checking, lazy loading, cascade)
- New persistence mechanism to learn

## Related Decisions

- [ADR-018: Domain Model Implementation Rules](ADR-018-domain-model-implementation-rules.md) — immutability requirement
- [ADR-025: Repository Adapter Single Aggregate](ADR-025-repository-adapter-single-aggregate.md) — adapter structure (contains the buggy pattern)
- [ADR-017: Transaction Boundaries](ADR-017-transaction-boundaries.md) — transaction = Hibernate session scope
