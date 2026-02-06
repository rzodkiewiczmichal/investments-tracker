# ADR-023: Cross-Aggregate Data Access Patterns

## Status
Accepted

## Context

The Investment Tracker application has two related aggregates:
- **Position aggregate**: Contains symbol and holdings (quantity, cost basis per account)
- **Instrument aggregate**: Contains symbol, name, type, and current market price

Many use cases need data from both aggregates. For example, calculating `CurrentValue` requires:
- `Quantity` from Position aggregate
- `Price` from Instrument aggregate

The question is: **Where should cross-aggregate data composition happen?**

### The Problem

Position needs current price for calculations like:
- `CurrentValue = Quantity × Price`
- `ProfitAndLoss = CurrentValue - InvestedAmount`

Three potential approaches exist for accessing price when working with Position.

## Options Considered

### Option A: Embed Price in Position Aggregate

```java
public record Position(
    InstrumentSymbol symbol,
    List<AccountHolding> holdings,
    Price currentPrice  // Embedded from Instrument
) {
    public CurrentValue calculateCurrentValue() {
        return CurrentValue.calculate(calculateTotalQuantity(), currentPrice);
    }
}
```

**Pros:**
- Convenient API - `position.calculateCurrentValue()` works directly
- All data available in one object
- Simple to use in controllers

**Cons:**
- **Violates aggregate boundaries** - Price belongs to Instrument, not Position
- **Consistency problems** - Which aggregate is authoritative for price?
- **Stale data risk** - Position may hold outdated price
- **Repository coupling** - PositionRepository must somehow fetch Instrument data
- **Testing complexity** - Tests must provide price even for quantity-only operations

### Option B: Repository Fetches Price (Cross-Aggregate in Infrastructure)

```java
@Repository
public class PositionRepositoryAdapter implements PositionRepository {
    private final PositionJpaRepository positionJpaRepo;
    private final InstrumentJpaRepository instrumentJpaRepo;  // Cross-aggregate!

    public Position findBySymbol(InstrumentSymbol symbol) {
        PositionJpaEntity entity = positionJpaRepo.findById(symbol.value());
        InstrumentJpaEntity instrument = instrumentJpaRepo.findById(symbol.value());
        return mapper.toDomain(entity, instrument.getCurrentPrice());
    }
}
```

**Pros:**
- Single call to get "enriched" Position
- Convenience for callers

**Cons:**
- **Repository handles multiple aggregates** - violates single responsibility
- **Hidden coupling** - callers don't see the cross-aggregate dependency
- **Transaction scope creep** - single transaction spans multiple aggregates
- **Testability problems** - repository tests need two JPA repositories
- **Mapper complexity** - persistence mapper needs cross-aggregate parameters

### Option C: Use Cases Orchestrate (Strict DDD) ✓ CHOSEN

```java
@Service
@Transactional(readOnly = true)
public class PositionQueryUseCaseService {
    private final PositionRepository positionRepo;      // One aggregate
    private final InstrumentRepository instrumentRepo;  // Another aggregate
    private final PositionCalculationService calcService;

    public PositionWithPrice getPositionWithPrice(InstrumentSymbol symbol) {
        // 1. Fetch each aggregate independently
        Position position = positionRepo.findBySymbol(symbol).orElseThrow();
        Instrument instrument = instrumentRepo.findBySymbol(symbol).orElseThrow();

        // 2. Use domain service for cross-aggregate calculations
        Price price = instrument.currentPrice().orElse(null);
        CurrentValue currentValue = (price != null)
            ? calcService.calculateCurrentValue(position, price)
            : null;

        // 3. Return view model (not domain model)
        return new PositionWithPrice(position, instrument, currentValue, ...);
    }
}
```

**Pros:**
- **Pure aggregate boundaries** - each aggregate is self-contained
- **Explicit composition** - cross-aggregate access is visible in use case
- **Single responsibility** - repositories handle one aggregate each
- **Testable** - repositories can be tested in isolation
- **Flexible** - easy to change where price comes from
- **Consistent with DDD** - use cases orchestrate, domain services calculate

**Cons:**
- More code in use case layer
- Controllers must work with view models instead of "enriched" domain objects

## Decision

**Adopt Option C: Use Cases Orchestrate Cross-Aggregate Data Access**

### Rules

1. **Aggregates are self-contained** - Position contains only Position data, Instrument contains only Instrument data

2. **Repositories handle ONE aggregate** - PositionRepository never accesses Instrument data

3. **Use cases orchestrate** - Application layer fetches multiple aggregates and composes them

4. **Domain services calculate** - Cross-aggregate calculations live in domain services that accept parameters from multiple aggregates

5. **View models for composition** - Create read models (like `PositionWithPrice`) for cross-aggregate query results

### Data Flow

```
┌─────────────────────────────────────────────────────────────────┐
│                        Use Case Layer                            │
│                                                                   │
│  1. positionRepo.findBySymbol(symbol)     → Position             │
│  2. instrumentRepo.findBySymbol(symbol)   → Instrument           │
│  3. calcService.calculateCurrentValue(position, price)           │
│  4. return PositionWithPrice(position, instrument, metrics)      │
└─────────────────────────────────────────────────────────────────┘
           │                    │
           ▼                    ▼
  ┌─────────────────┐  ┌─────────────────┐
  │ Position Repo   │  │ Instrument Repo │
  │ (one aggregate) │  │ (one aggregate) │
  └─────────────────┘  └─────────────────┘
```

## Consequences

### Positive

1. **Clean aggregate boundaries** - Each aggregate is pure and self-contained
2. **Testable repositories** - No cross-aggregate mocking needed
3. **Explicit dependencies** - Use cases clearly show what data they need
4. **Flexible caching** - Each aggregate can be cached independently
5. **Independent evolution** - Position and Instrument can change independently
6. **Clear transaction boundaries** - Each aggregate has its own transaction scope

### Negative

1. **More verbose use cases** - Must explicitly fetch and compose
2. **N+1 query risk** - Listing positions requires fetching instruments too
3. **View model maintenance** - Additional classes for cross-aggregate views

### Mitigation Strategies

1. **N+1 queries**: Use bulk fetch methods (`instrumentRepo.findAllBySymbols(symbols)`)
2. **View model proliferation**: Limit view models to frequently-used compositions
3. **Code duplication**: Extract common composition patterns to helper methods

## Related Decisions

- [ADR-001: Aggregate Boundaries](ADR-001-aggregate-boundaries.md) - Defines aggregates
- [ADR-003: Domain vs Application Services](ADR-003-domain-vs-application-services.md) - Service responsibilities
- [ADR-024: Domain Services for Cross-Aggregate Calculations](ADR-024-domain-services-cross-aggregate-calculations.md)
- [ADR-025: Repository Adapter Single Aggregate Rule](ADR-025-repository-adapter-single-aggregate.md)

## References

- Domain-Driven Design by Eric Evans - Aggregates and Repositories
- Implementing Domain-Driven Design by Vaughn Vernon - Aggregate Design Rules
- Learning Domain-Driven Design by Vlad Khononov - Chapter on Aggregate Patterns
