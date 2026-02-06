# ADR-026: Application Layer Orchestrates Cross-Aggregate Operations

## Status
Accepted

## Context

The Investment Tracker requires operations that span multiple aggregates:
- **Get position with current value**: Position (holdings) + Instrument (price)
- **Portfolio summary**: All Positions + All Instruments (for prices)
- **Add position**: Create Position + Create/Update Instrument

The question is: **Which layer is responsible for coordinating multiple aggregates?**

### Architectural Layers

```
┌─────────────────────────────────────────┐
│          Infrastructure Layer           │  ← REST Controllers, JPA Adapters
├─────────────────────────────────────────┤
│           Application Layer             │  ← Use Cases (orchestration)
├─────────────────────────────────────────┤
│             Domain Layer                │  ← Aggregates, Domain Services
└─────────────────────────────────────────┘
```

Each layer has specific responsibilities:
- **Infrastructure**: Adapters for external systems (HTTP, database, APIs)
- **Application**: Use case orchestration, transaction boundaries
- **Domain**: Business logic, invariants, calculations

## Options Considered

### Option A: Controllers Orchestrate

```java
@RestController
public class PositionController {
    private final PositionRepository positionRepo;
    private final InstrumentRepository instrumentRepo;
    private final PositionCalculationService calcService;

    @GetMapping("/{symbol}")
    public PositionDetailResponse getPosition(@PathVariable String symbol) {
        // Controller fetches and composes
        Position position = positionRepo.findBySymbol(symbol);
        Instrument instrument = instrumentRepo.findBySymbol(symbol);
        CurrentValue value = calcService.calculateCurrentValue(position, price);
        return mapper.toResponse(position, instrument, value);
    }
}
```

**Pros:**
- Direct, simple for small applications
- Fewer classes

**Cons:**
- **Controller does too much** - violates single responsibility
- **Duplicate orchestration** - same logic repeated if called from CLI or events
- **No reusable use cases** - business workflow tied to HTTP layer
- **Transaction management** - @Transactional on controllers is anti-pattern
- **Testing complexity** - integration tests required for business logic

### Option B: Domain Services Orchestrate

```java
@Service  // Domain service with repository dependencies?
public class PositionViewService {
    private final PositionRepository positionRepo;    // Infrastructure!
    private final InstrumentRepository instrumentRepo;

    public PositionWithPrice getPositionWithPrice(InstrumentSymbol symbol) {
        // Domain service fetching data
        Position position = positionRepo.findBySymbol(symbol);
        Instrument instrument = instrumentRepo.findBySymbol(symbol);
        return new PositionWithPrice(position, instrument, ...);
    }
}
```

**Pros:**
- Keeps controller thin

**Cons:**
- **Domain depends on infrastructure** - repositories are adapters (infrastructure)
- **Pollutes domain layer** - domain services should be pure business logic
- **Testing requires mocks** - can't test domain service without mocking repos
- **Violates hexagonal architecture** - domain should not know about persistence
- **Mixed concerns** - data access is not domain logic

### Option C: Application Layer (Use Cases) Orchestrate ✓ CHOSEN

```java
// Application layer - orchestrates aggregates
@Service
@Transactional(readOnly = true)
public class PositionQueryUseCaseService implements PositionQueryUseCase {
    private final PositionRepository positionRepo;
    private final InstrumentRepository instrumentRepo;
    private final PositionCalculationService calcService;  // Domain service

    public PositionWithPrice getPositionWithPrice(InstrumentSymbol symbol) {
        // 1. Fetch aggregates (coordination)
        Position position = positionRepo.findBySymbol(symbol)
            .orElseThrow(() -> new PositionNotFoundException(symbol));
        Instrument instrument = instrumentRepo.findBySymbol(symbol)
            .orElseThrow(() -> new InstrumentNotFoundException(symbol));

        // 2. Delegate calculation to domain service
        Price price = instrument.currentPrice().orElse(null);
        CurrentValue value = (price != null)
            ? calcService.calculateCurrentValue(position, price)
            : null;
        ProfitAndLoss pnl = (price != null)
            ? calcService.calculateProfitAndLoss(position, price)
            : null;

        // 3. Return view model
        return new PositionWithPrice(position, instrument, value, pnl);
    }
}

// Controller delegates to use case
@RestController
public class PositionController {
    private final PositionQueryUseCase queryUseCase;

    @GetMapping("/{symbol}")
    public PositionDetailResponse getPosition(@PathVariable String symbol) {
        PositionWithPrice result = queryUseCase.getPositionWithPrice(
            new InstrumentSymbol(symbol));
        return mapper.toResponse(result);
    }
}
```

**Pros:**
- **Clear separation** - each layer has one job
- **Reusable use cases** - same use case callable from REST, CLI, events
- **Testable** - use cases tested with mock repos, domain services tested purely
- **Transaction boundaries** - @Transactional at use case level (correct place)
- **Domain stays pure** - domain services don't fetch data
- **Explicit orchestration** - data flow is visible and traceable

**Cons:**
- More classes (use case interface + implementation)
- Use cases can become verbose for complex workflows

## Decision

**Adopt Option C: Application Layer (Use Cases) Orchestrates Cross-Aggregate Operations**

### Responsibilities by Layer

| Layer | Responsibility | Examples |
|-------|---------------|----------|
| **Infrastructure** | Adapt external systems | Controllers map HTTP ↔ domain; Adapters map JPA ↔ domain |
| **Application** | Orchestrate use cases | Fetch aggregates, call domain services, compose results |
| **Domain** | Business logic | Calculations, invariants, aggregate behavior |

### Use Case Patterns

#### Query Use Case (Read Operations)

```java
@Service
@Transactional(readOnly = true)
public class PositionQueryUseCaseService implements PositionQueryUseCase {
    private final PositionRepository positionRepo;
    private final InstrumentRepository instrumentRepo;
    private final PositionCalculationService calcService;

    @Override
    public Position getPosition(InstrumentSymbol symbol) {
        return positionRepo.findBySymbol(symbol)
            .orElseThrow(() -> new PositionNotFoundException(symbol));
    }

    @Override
    public Collection<Position> listPositions() {
        return positionRepo.findAll();
    }

    /**
     * Cross-aggregate query returning enriched view.
     */
    public PositionWithPrice getPositionWithPrice(InstrumentSymbol symbol) {
        Position position = positionRepo.findBySymbol(symbol)
            .orElseThrow(() -> new PositionNotFoundException(symbol));
        Instrument instrument = instrumentRepo.findBySymbol(symbol)
            .orElseThrow(() -> new InstrumentNotFoundException(symbol));

        // Domain service for calculations
        Price price = instrument.currentPrice().orElse(null);
        CurrentValue value = (price != null)
            ? calcService.calculateCurrentValue(position, price)
            : null;
        ProfitAndLoss pnl = (price != null)
            ? calcService.calculateProfitAndLoss(position, price)
            : null;

        return new PositionWithPrice(position, instrument, value, pnl);
    }
}
```

#### Command Use Case (Write Operations)

```java
@Service
@Transactional
public class PositionCommandUseCaseService implements PositionCommandUseCase {
    private final PositionRepository positionRepo;
    private final InstrumentRepository instrumentRepo;
    private final AccountRepository accountRepo;

    @Override
    public Position addPosition(
            InstrumentSymbol symbol,
            AccountId accountId,
            Quantity quantity,
            CostBasis costBasis,
            Price currentPrice) {

        // 1. Validate account exists
        if (!accountRepo.existsById(accountId)) {
            throw new AccountNotFoundException(accountId);
        }

        // 2. Create or update Instrument (separate aggregate)
        Instrument instrument = instrumentRepo.findBySymbol(symbol)
            .map(existing -> updateInstrumentPrice(existing, currentPrice))
            .orElseGet(() -> createInstrument(symbol, currentPrice));
        instrumentRepo.save(instrument);

        // 3. Create Position aggregate
        AccountHolding holding = new AccountHolding(accountId, quantity, costBasis);
        Position position = new Position(symbol, List.of(holding));

        // 4. Save and return
        return positionRepo.save(position);
    }

    private Instrument updateInstrumentPrice(Instrument existing, Price newPrice) {
        return new Instrument(
            existing.symbol(),
            existing.name(),
            existing.type(),
            newPrice);
    }

    private Instrument createInstrument(InstrumentSymbol symbol, Price price) {
        return new Instrument(
            symbol,
            new InstrumentName("Unknown"),
            InstrumentType.STOCK,
            price);
    }
}
```

### View Models for Cross-Aggregate Queries

```java
/**
 * View model for Position enriched with Instrument data.
 * This is NOT a domain model - it's a read model for queries.
 * Lives in application layer.
 */
public record PositionWithPrice(
    Position position,
    Instrument instrument,
    CurrentValue currentValue,
    ProfitAndLoss profitLoss
) {
    public InstrumentSymbol symbol() {
        return position.symbol();
    }

    public Optional<Price> currentPrice() {
        return instrument.currentPrice();
    }
}
```

### Data Flow

```
┌───────────────────────────────────────────────────────────────────────┐
│                         REST Controller                                │
│  1. Receives HTTP request                                              │
│  2. Maps path/query params to domain types                            │
│  3. Calls use case                                                     │
│  4. Maps result to HTTP response                                       │
└───────────────────────────────┬───────────────────────────────────────┘
                                │
                                ▼
┌───────────────────────────────────────────────────────────────────────┐
│                    Use Case (Application Layer)                        │
│  1. Fetches Position from PositionRepository                          │
│  2. Fetches Instrument from InstrumentRepository                      │
│  3. Calls PositionCalculationService with data from both              │
│  4. Composes PositionWithPrice view model                             │
│  5. Returns to controller                                              │
└─────────┬─────────────────────────────────────────┬───────────────────┘
          │                                         │
          ▼                                         ▼
┌─────────────────────┐                 ┌─────────────────────┐
│  PositionRepository │                 │ InstrumentRepository │
│  (returns Position) │                 │ (returns Instrument) │
└─────────────────────┘                 └─────────────────────┘
          │                                         │
          ▼                                         ▼
┌─────────────────────┐                 ┌─────────────────────┐
│  Position Aggregate │                 │ Instrument Aggregate │
│  - symbol           │                 │ - symbol             │
│  - holdings         │                 │ - currentPrice       │
└─────────────────────┘                 └─────────────────────┘
```

## Consequences

### Positive

1. **Clean layer separation** - Each layer has single responsibility
2. **Reusable use cases** - REST, CLI, events all call same use case
3. **Testable at each level**:
   - Domain services: pure unit tests
   - Use cases: mock repositories
   - Controllers: mock use cases
4. **Transaction boundaries correct** - @Transactional on use cases
5. **Explicit data flow** - Easy to trace where data comes from
6. **Domain stays pure** - No infrastructure dependencies in domain layer

### Negative

1. **More classes** - Interface + implementation for each use case
2. **Verbose orchestration** - Multiple repository calls visible in code
3. **View model maintenance** - Additional records for cross-aggregate results

### Anti-Patterns to Avoid

1. **Controller orchestration** - Don't fetch multiple repos in controller
2. **Domain service with repositories** - Don't inject repos into domain services
3. **Fat use cases** - Extract complex domain logic to domain services
4. **Returning entities through layers** - Use DTOs at boundaries

## Related Decisions

- [ADR-003: Domain vs Application Services](ADR-003-domain-vs-application-services.md)
- [ADR-017: Transaction Boundaries](ADR-017-transaction-boundaries.md)
- [ADR-022: Use Case Dependencies](ADR-022-use-case-dependencies.md)
- [ADR-023: Cross-Aggregate Data Access](ADR-023-cross-aggregate-data-access.md)
- [ADR-024: Domain Services for Cross-Aggregate Calculations](ADR-024-domain-services-cross-aggregate-calculations.md)
- [ADR-025: Repository Adapter Single Aggregate](ADR-025-repository-adapter-single-aggregate.md)

## References

- Clean Architecture by Robert C. Martin - Use Case Layer
- Implementing Domain-Driven Design by Vaughn Vernon - Application Services
- Hexagonal Architecture by Alistair Cockburn - Ports and Adapters
- Learning Domain-Driven Design by Vlad Khononov - Application Layer Patterns
