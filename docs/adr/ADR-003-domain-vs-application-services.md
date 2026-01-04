# ADR-003: Domain Services vs Application Services

## Status
Accepted

## Context

In hexagonal architecture with Domain-Driven Design, logic is distributed across entities, value objects, domain services, and application services. Clear separation is critical for:

1. **Testability**: Domain services should be testable without mocks or infrastructure
2. **Framework Independence**: Domain logic must not depend on Spring, JPA, or other frameworks
3. **Maintainability**: Business logic should be centralized and easy to change
4. **Reusability**: Domain services should be reusable across multiple use cases

Key challenges:
- Portfolio calculations span multiple positions (cross-aggregate logic)
- Weighted average cost calculation is complex domain logic
- Import and price fetching involve infrastructure (CSV, API)
- Some operations need transaction management
- Application must orchestrate complex workflows (import, reconciliation)

Domain concepts requiring services:
- Position aggregation (weighted average cost calculation)
- Financial metrics (P&L, current value, invested amount)
- XIRR calculation (complex financial algorithm)
- Portfolio summary (aggregate all positions)
- Import workflow (CSV → Positions)
- Reconciliation (compare system vs broker)

Hexagonal architecture principles:
- Domain defines ports (interfaces), infrastructure implements adapters
- Application layer orchestrates, domain layer contains business logic
- Transaction boundaries at application service level

## Decision

### Domain Services (Pure Domain Logic)

Domain services contain **pure business logic** with **no infrastructure dependencies**:

#### 1. PositionAggregationService

**Responsibility**: Calculate weighted average cost when combining AccountHoldings

**Location**: `com.investments.tracker.domain.service`

**Interface**:
```java
public interface PositionAggregationService {
    CostBasis calculateWeightedAverageCost(List<AccountHolding> holdings);
    Quantity calculateTotalQuantity(List<AccountHolding> holdings);
}
```

**Rationale**:
- Complex calculation logic (FR-085): `(qty1 × cost1 + qty2 × cost2) / (qty1 + qty2)`
- Pure domain rule, no infrastructure needed
- Used by Position aggregate when adding/removing holdings
- Stateless pure function
- Testable with plain JUnit, no Spring context

#### 2. CalculationService

**Responsibility**: Financial calculations (P&L, current value, invested amount)

**Location**: `com.investments.tracker.domain.service`

**Interface**:
```java
public interface CalculationService {
    ProfitAndLoss calculateProfitAndLoss(InvestedAmount invested, CurrentValue current);
    CurrentValue calculateCurrentValue(Quantity quantity, Price price);
    InvestedAmount calculateInvestedAmount(Quantity quantity, CostBasis costBasis);
}
```

**Rationale**:
- Fundamental business logic (FR-081 to FR-084)
- Pure functions, no side effects
- Used by both Position and Portfolio
- Domain experts can validate formulas
- Framework-agnostic, testable in isolation

#### 3. XIRRCalculationService (v0.3+)

**Responsibility**: Calculate Extended Internal Rate of Return

**Location**: `com.investments.tracker.domain.service`

**Interface**:
```java
public interface XIRRCalculationService {
    XIRR calculateXIRR(List<Transaction> transactions, CurrentValue finalValue, LocalDate endDate);
}
```

**Rationale**:
- Complex financial algorithm (FR-089)
- Pure domain logic (may wrap Apache Commons Math library)
- Exposes domain interface hiding implementation details
- Algorithm complexity justifies separate service

**Note**: Implementation may use external library but interface is domain concept

### Application Services (Orchestration Layer)

Application services **orchestrate use cases** with **thin coordination logic**:

#### 1. PortfolioViewingService

**Responsibility**: Orchestrate "View Portfolio" use case (FR-001)

**Location**: `com.investments.tracker.application.service`

**Interface**:
```java
public interface PortfolioViewingService {
    PortfolioSummaryDTO viewPortfolio();
    List<PositionSummaryDTO> listPositions();
}
```

**Orchestration**:
1. Load all positions from PositionRepository
2. For each position, fetch current price (via PriceFetchingPort)
3. Calculate position metrics using CalculationService
4. Aggregate into portfolio metrics using CalculationService
5. Convert to DTO and return

**Characteristics**:
- Thin coordinator - delegates to domain services
- Defines transaction boundary (@Transactional read-only)
- Converts domain model to DTOs
- No business logic - only orchestration

#### 2. ManualEntryService

**Responsibility**: Orchestrate "Add Position Manually" use case (FR-041, FR-042)

**Location**: `com.investments.tracker.application.service`

**Interface**:
```java
public interface ManualEntryService {
    void addPosition(AddPositionCommand command);
    void updatePosition(UpdatePositionCommand command);
}
```

**Orchestration**:
1. Validate input data (command validation)
2. Load or create Position aggregate
3. Add AccountHolding to Position
4. Position recalculates weighted average cost (uses PositionAggregationService)
5. Save via PositionRepository
6. Emit domain event (if applicable)

**Characteristics**:
- Transaction boundary (@Transactional)
- Validates command before delegating to domain
- No business logic in service itself
- Handles exceptions and maps to application errors

#### 3. ImportService (v0.2+)

**Responsibility**: Orchestrate CSV import process (FR-021)

**Location**: `com.investments.tracker.application.service`

**Interface**:
```java
public interface ImportService {
    ImportResultDTO importPositions(ImportFileCommand command);
}
```

**Orchestration**:
1. Read CSV file via FileReaderPort (infrastructure adapter)
2. Validate each row using domain validation rules
3. For each row:
   - Fetch current price via PriceFetchingPort
   - Create or update Position aggregate
   - Position calculates metrics
4. Save all positions (may use multiple transactions)
5. Generate import result summary
6. Handle errors with rollback

**Characteristics**:
- Complex workflow orchestration
- Uses multiple infrastructure ports (file, API)
- Batch operation with error handling
- Application-level concern, not domain logic

#### 4. ReconciliationService (v0.7)

**Responsibility**: Orchestrate reconciliation process (FR-061 to FR-065)

**Location**: `com.investments.tracker.application.service`

**Interface**:
```java
public interface ReconciliationService {
    ReconciliationReportDTO reconcile(BrokerStatementDTO brokerStatement);
}
```

**Orchestration**:
1. Load current positions from PositionRepository
2. Parse broker statement data
3. Compare positions using domain comparison logic
4. Calculate value differences and apply tolerance
5. Generate reconciliation report
6. Store reconciliation history

**Characteristics**:
- Read-only operation (no transaction needed)
- Compares two data sources
- Uses domain logic for tolerance calculation
- Application flow coordination

### Ports and Adapters (Hexagonal Architecture)

Ports are **domain interfaces**, adapters are **infrastructure implementations**:

#### 1. PriceFetchingPort (Domain Port)

**Location**: `com.investments.tracker.domain.port.out`

**Interface**:
```java
public interface PriceFetchingPort {
    Price fetchCurrentPrice(InstrumentSymbol symbol) throws PriceUnavailableException;
    Map<InstrumentSymbol, Price> fetchPrices(List<InstrumentSymbol> symbols);
}
```

**Adapter**: YahooFinanceAdapter (v0.2+)

**Location**: `com.investments.tracker.infrastructure.external`

**Rationale**:
- External API dependency (Yahoo Finance)
- Infrastructure concern, not domain logic
- Need to mock for tests
- May swap implementations (different price source)
- Domain defines contract, infrastructure implements

#### 2. PositionRepository (Domain Port)

**Location**: `com.investments.tracker.domain.repository`

**Interface**:
```java
public interface PositionRepository {
    void save(Position position);
    Optional<Position> findBySymbol(InstrumentSymbol symbol);
    List<Position> findAll();
    void delete(Position position);
}
```

**Adapter**: JpaPositionRepository

**Location**: `com.investments.tracker.infrastructure.persistence.jpa`

**Rationale**:
- Database is infrastructure concern
- Repository interface is domain concept (DDD repository pattern)
- JPA implementation details hidden from domain
- Testable with in-memory implementation

#### 3. FileReaderPort (Domain Port - v0.2+)

**Location**: `com.investments.tracker.domain.port.out`

**Interface**:
```java
public interface FileReaderPort {
    List<ImportRowData> readCsv(FilePath path) throws FileReadException;
}
```

**Adapter**: CsvFileReaderAdapter

**Location**: `com.investments.tracker.infrastructure.file`

**Rationale**:
- File I/O is infrastructure concern
- Domain defines data contract (ImportRowData)
- Can swap CSV library without affecting domain

## Consequences

### Positive

1. **Framework-Independent Domain**: Domain services have zero Spring/JPA dependencies (NFR-064)
2. **Highly Testable Domain**: Domain services tested with pure JUnit, no mocks (NFR-071)
3. **Thin Application Layer**: Application services are coordinators, easy to understand
4. **Clear Responsibility Boundaries**: Business logic in domain, orchestration in application
5. **Reusable Domain Logic**: CalculationService used by multiple aggregates and use cases
6. **Swappable Infrastructure**: Ports allow changing implementations (Yahoo Finance → Bloomberg)
7. **Transaction Boundaries Explicit**: @Transactional only in application services
8. **Easy to Change**: Business rule changes isolated to domain services

### Negative

1. **More Classes/Interfaces**: Domain service + application service for some features
2. **Risk of Anemic Domain**: Must resist putting domain logic in application services
3. **Service Proliferation**: Many service interfaces to maintain
4. **Learning Curve**: Team must understand layering discipline

### Mitigation Strategies

1. **Code Reviews**: Ensure domain logic stays in domain layer
2. **ArchUnit Tests**: Enforce that domain has no Spring dependencies
3. **Clear Guidelines**: Document what belongs in each layer (this ADR)
4. **Training**: Team education on hexagonal architecture and DDD

### Implementation Guidelines

#### Domain Service Rules

1. **No Infrastructure Dependencies**:
   - Never inject repositories or adapters
   - Never use @Service, @Transactional, @Autowired in domain layer
   - Only depend on other domain services, entities, value objects

2. **Pure Functions Preferred**:
   - Stateless whenever possible
   - Deterministic (same inputs = same outputs)
   - No side effects (don't modify passed objects)

3. **Return Domain Objects**:
   - Return entities, value objects, domain exceptions
   - Never return DTOs or infrastructure types

4. **Domain Language**:
   - Use ubiquitous language in method names
   - Parameters and returns are domain concepts

#### Application Service Rules

1. **Thin Coordinators Only**:
   - Never contain business logic
   - Always delegate calculations to domain
   - Only orchestrate: fetch data, call domain, save results

2. **Transaction Management**:
   - @Transactional annotations here
   - Define transaction scope (read-only vs read-write)
   - Handle transaction rollback on exceptions

3. **DTO Conversion**:
   - Convert between domain objects and DTOs
   - DTOs only in application and infrastructure layers
   - Never expose domain objects directly to web layer

4. **Exception Mapping**:
   - Catch domain exceptions
   - Map to application-level exceptions
   - Include user-friendly messages

#### Port Definition Rules

1. **Domain Defines Ports**:
   - Interfaces in domain layer
   - Use domain types in signatures
   - Infrastructure implements ports

2. **Naming Convention**:
   - Output ports (driven): SomethingPort (e.g., PriceFetchingPort)
   - Repositories: SomethingRepository
   - Input ports: Use case interfaces (optional)

3. **Infrastructure Adapters**:
   - Implement ports in infrastructure layer
   - Name: SomethingAdapter (e.g., YahooFinanceAdapter)
   - Can depend on external libraries

### Testing Strategy

#### Domain Service Tests
```java
class CalculationServiceTest {
    private CalculationService calculationService = new CalculationServiceImpl();

    @Test
    void shouldCalculateProfitAndLoss() {
        // Pure unit test, no Spring, no mocks
        var invested = new InvestedAmount(new Money(BigDecimal.valueOf(10000), Currency.PLN));
        var current = new CurrentValue(new Money(BigDecimal.valueOf(12000), Currency.PLN));

        var pnl = calculationService.calculateProfitAndLoss(invested, current);

        assertThat(pnl.amount()).isEqualTo(new Money(BigDecimal.valueOf(2000), Currency.PLN));
        assertThat(pnl.percentage().value()).isEqualTo(new BigDecimal("20.00"));
    }
}
```

#### Application Service Tests
```java
@SpringBootTest
class PortfolioViewingServiceTest {

    @Autowired
    private PortfolioViewingService portfolioViewingService;

    @MockBean
    private PositionRepository positionRepository;

    @Test
    void shouldViewPortfolioWithPositions() {
        // Integration test with Spring context
        given(positionRepository.findAll()).willReturn(createTestPositions());

        var portfolio = portfolioViewingService.viewPortfolio();

        assertThat(portfolio.totalValue()).isNotNull();
    }
}
```

#### Port Adapter Tests
```java
@DataJpaTest
class JpaPositionRepositoryTest {

    @Autowired
    private JpaPositionRepository repository;

    @Test
    void shouldSaveAndFindPosition() {
        // Integration test with real database (Testcontainers)
        var position = createTestPosition();

        repository.save(position);
        var found = repository.findBySymbol(position.getSymbol());

        assertThat(found).isPresent();
    }
}
```

### Package Organization

```
com.investments.tracker/
├── domain/
│   ├── service/                      # Domain Services (pure logic)
│   │   ├── PositionAggregationService.java
│   │   ├── CalculationService.java
│   │   └── XIRRCalculationService.java
│   ├── port/out/                     # Output Ports (driven)
│   │   ├── PriceFetchingPort.java
│   │   └── FileReaderPort.java
│   └── repository/                   # Repository Ports
│       └── PositionRepository.java
├── application/
│   ├── service/                      # Application Services (orchestration)
│   │   ├── PortfolioViewingService.java
│   │   ├── ManualEntryService.java
│   │   ├── ImportService.java
│   │   └── ReconciliationService.java
│   ├── port/in/                      # Input Ports (driving - optional)
│   └── dto/                          # Data Transfer Objects
└── infrastructure/
    ├── persistence/
    │   └── jpa/
    │       └── JpaPositionRepository.java    # Repository Adapter
    ├── external/
    │   └── YahooFinanceAdapter.java           # Price Fetching Adapter
    └── file/
        └── CsvFileReaderAdapter.java          # File Reader Adapter
```

### Alternatives Considered

**Alternative 1: Put All Logic in Aggregates**
- **Rejected**: Weighted average cost calculation spans multiple AccountHoldings, awkward in Position
- Some cross-aggregate logic doesn't belong to any single aggregate
- Domain services provide better separation

**Alternative 2: Single Service Layer (No Domain Services)**
- **Rejected**: Would mix orchestration with business logic
- Domain would be anemic
- Testing would require infrastructure setup
- Business logic scattered across application services

**Alternative 3: CalculationService as Application Service**
- **Rejected**: Calculations are pure domain logic
- No infrastructure needed, shouldn't be in application layer
- Domain layer would be weaker
- Testing would be harder (Spring context required)

**Alternative 4: Combine Import and Price Fetching**
- **Rejected**: Violates Single Responsibility Principle
- Price fetching is reusable outside import context
- Better as separate port with adapter
- More flexible for future use cases

**Alternative 5: No Ports, Direct Dependencies**
- **Rejected**: Domain would depend on infrastructure
- Cannot swap implementations
- Testing requires real infrastructure
- Violates hexagonal architecture principles

## Related Decisions

- [ADR-001: Aggregate Boundaries](ADR-001-aggregate-boundaries.md) - Services operate on aggregates
- [ADR-002: Value Objects and Entities](ADR-002-value-objects-and-entities.md) - Services use value objects
- [ADR-004: Package Structure](ADR-004-package-structure.md) - Physical organization

## References

- Domain-Driven Design by Eric Evans (Services chapter)
- Implementing Domain-Driven Design by Vaughn Vernon
- Hexagonal Architecture by Alistair Cockburn
- NFR-064: Framework-independent domain model
- NFR-071: DDD best practices
- requirements/functional/functional-requirements.md (FR-001, FR-041, FR-021, FR-061)
