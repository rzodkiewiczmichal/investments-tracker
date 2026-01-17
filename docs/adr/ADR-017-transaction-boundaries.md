# ADR-017: Transaction Boundaries

## Status
Accepted

## Context

Clear transaction management required for:
- Data consistency (atomic operations)
- Concurrency control
- Performance optimization
- Clean hexagonal architecture boundaries

## Decision

### Transaction Boundary: Application Service Layer

Place `@Transactional` at the **application service (use case)** level.

**Rationale:**
- Each use case = one atomic business operation
- Application layer orchestrates domain and infrastructure
- Clear boundary for transaction scope
- Domain remains transaction-agnostic (pure)

**Example:**
```java
@Service
@RequiredArgsConstructor
public class ManualEntryService {
    private final PositionRepository positionRepository;
    private final AccountRepository accountRepository;

    @Transactional  // ← Transaction starts here
    public PositionDetailDTO addPosition(AddPositionCommand command) {
        // 1. Validate account exists (read)
        Account account = accountRepository.findById(command.accountId())
            .orElseThrow(() -> new AccountNotFoundException(command.accountId()));

        // 2. Check position doesn't exist (read)
        if (positionRepository.existsBySymbol(command.instrumentSymbol())) {
            throw new PositionAlreadyExistsException(command.instrumentSymbol());
        }

        // 3. Create domain entity (pure domain logic)
        Position position = Position.create(
            InstrumentName.of(command.instrumentName()),
            InstrumentSymbol.of(command.instrumentSymbol()),
            Quantity.of(command.quantity()),
            Money.of(command.averageCost())
        );

        // 4. Save (write)
        Position saved = positionRepository.save(position);

        // 5. Return DTO
        return PositionMapper.toDetailDTO(saved, account);
    }
    // Transaction commits on successful return
    // Transaction rolls back on exception
}
```

### Where NOT to Use @Transactional

**Domain Layer (Entities, Value Objects, Domain Services):**
- Must be transaction-agnostic
- Pure business logic, no infrastructure concerns
- Testable without Spring context

```java
// Domain service - NO @Transactional
public class PriceCalculationService {
    public Money calculateCurrentValue(Position position, Money currentPrice) {
        return currentPrice.multiply(position.quantity().value());
    }
}
```

**Repository Interfaces (Ports):**
- Interfaces should not dictate transaction behavior
- Spring Data repositories automatically join ambient transactions

```java
// Port - NO @Transactional
public interface PositionRepository {
    Position save(Position position);
    Optional<Position> findById(PositionId id);
}
```

**REST Controllers:**
- Transaction should not span HTTP request/response processing
- Delegate to application services that manage transactions

```java
@RestController
@RequestMapping("/api/v1/positions")
@RequiredArgsConstructor
public class PositionController {  // NO @Transactional
    private final ManualEntryService manualEntryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PositionDetailDTO createPosition(@Valid @RequestBody AddPositionCommand command) {
        return manualEntryService.addPosition(command);  // Transaction here
    }
}
```

### Read-Only Transactions

Use `@Transactional(readOnly = true)` for query operations.

**Benefits:**
- Hibernate skips dirty checking (performance)
- Database can optimize read-only queries
- Intent clarity
- Fail-fast if write attempted

```java
@Service
@RequiredArgsConstructor
public class PositionQueryService {
    private final PositionRepository positionRepository;

    @Transactional(readOnly = true)
    public PortfolioSummaryDTO getPortfolioSummary() {
        List<Position> positions = positionRepository.findAll();
        // Aggregate and return metrics
    }

    @Transactional(readOnly = true)
    public PositionDetailDTO getPositionById(PositionId id) {
        return positionRepository.findById(id)
            .map(PositionMapper::toDetailDTO)
            .orElseThrow(() -> new PositionNotFoundException(id));
    }
}
```

### Transaction Propagation

**REQUIRED (Default):**
Join existing transaction if present, create new if not. Use for 99% of cases.

```java
@Transactional  // Implicitly: propagation = Propagation.REQUIRED
public PositionDetailDTO addPosition(AddPositionCommand command) {
    // Joins caller's transaction if exists, creates new otherwise
}
```

**REQUIRES_NEW:**
Always create new transaction, suspend existing. Use for independent operations.

```java
@Service
public class AuditService {
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAction(String action, String userId) {
        // Commits even if caller's transaction rolls back
        auditRepository.save(new AuditLog(action, userId));
    }
}
```

**When to use REQUIRES_NEW:**
- ✅ Audit logging (must persist even if main operation fails)
- ✅ Independent operations (email sending, analytics)
- ❌ Business logic (use REQUIRED)

**Warning:** REQUIRES_NEW can cause deadlocks if outer transaction holds locks on same tables.

### Isolation Levels

**READ_COMMITTED (Default):**
Use for most operations. Good performance, prevents dirty reads.

```java
@Transactional  // Implicitly: isolation = Isolation.DEFAULT (READ_COMMITTED)
public List<PositionSummaryDTO> getAllPositions() {
    return positionRepository.findAll().stream()
        .map(PositionMapper::toSummaryDTO)
        .toList();
}
```

**REPEATABLE_READ:**
Use for critical consistency requirements (e.g., financial calculations).

```java
@Transactional(isolation = Isolation.REPEATABLE_READ)
public RebalanceReportDTO analyzePortfolio() {
    // Multiple queries see consistent snapshot
    List<Position> positions1 = positionRepository.findAll();
    // ... calculations ...
    List<Position> positions2 = positionRepository.findAll();
    // Guaranteed: positions1 == positions2
}
```

| Isolation | Dirty Reads | Non-Repeatable Reads | Phantom Reads | Performance |
|-----------|-------------|----------------------|---------------|-------------|
| READ_COMMITTED | ❌ | ✅ | ✅ | High |
| REPEATABLE_READ | ❌ | ❌ | ✅ | Medium |
| SERIALIZABLE | ❌ | ❌ | ❌ | Low |

**Decision:** Don't use SERIALIZABLE in MVP (too conservative, high cost).

### Exception Handling and Rollback

**Default Spring Behavior:**
- Rollback: RuntimeException and subclasses
- Commit: Checked exceptions

**Explicit Rollback Rules:**
```java
@Transactional(rollbackFor = Exception.class)  // Roll back on ANY exception
public PositionDetailDTO addPosition(AddPositionCommand command) {
    // All exceptions (checked + unchecked) trigger rollback
}
```

**No Rollback for Expected Failures:**
```java
@Transactional(noRollbackFor = InvalidDataFormatException.class)
public ImportResultDTO importPositions(MultipartFile file) {
    List<ImportError> errors = new ArrayList<>();
    for (PositionRow row : parseFile(file)) {
        try {
            positionRepository.save(createPosition(row));
        } catch (InvalidDataFormatException e) {
            errors.add(new ImportError(row, e.getMessage()));
            // Don't rollback entire import
        }
    }
    return new ImportResultDTO(errors);
}
```

### Timeout Configuration

Set timeouts for long-running operations to prevent runaway transactions.

```java
@Transactional(timeout = 300)  // 5 minutes max
public ImportResultDTO importPositions(MultipartFile file) {
    // Large file import
}
```

**Recommended Timeouts:**
- Simple CRUD: No timeout (< 1 second)
- Batch operations: 300 seconds (5 minutes)
- Reports: 60 seconds
- Background jobs: No timeout

### Best Practices

**1. Keep Transactions Short:**
Minimize time between transaction start and commit.

```java
// ❌ Bad: External API call inside transaction
@Transactional
public void updatePrices() {
    List<Position> positions = positionRepository.findAll();
    for (Position p : positions) {
        Money price = externalAPI.getPrice(p.symbol());  // Slow!
        p.updatePrice(price);
    }
    positionRepository.saveAll(positions);
}

// ✅ Good: Fetch prices outside transaction
public void updatePrices() {
    List<Position> positions = positionRepository.findAll();
    Map<String, Money> prices = externalAPI.getPrices(getAllSymbols());
    updatePricesInTransaction(positions, prices);
}

@Transactional
void updatePricesInTransaction(List<Position> positions, Map<String, Money> prices) {
    positions.forEach(p -> p.updatePrice(prices.get(p.symbol())));
    positionRepository.saveAll(positions);
}
```

**2. Avoid Self-Calls:**
Spring AOP doesn't intercept self-calls.

```java
// ❌ Bad: Self-call bypasses @Transactional
@Service
public class PositionService {
    @Transactional
    public void publicMethod() {
        privateMethod();  // NOT transactional!
    }

    @Transactional
    private void privateMethod() {
        // @Transactional ignored (self-call)
    }
}

// ✅ Good: Inject dependency
@Service
@RequiredArgsConstructor
public class PositionService {
    private final AnotherService anotherService;

    @Transactional
    public void publicMethod() {
        anotherService.transactionalMethod();  // Works!
    }
}
```

**3. Test Transactional Behavior:**
```java
@SpringBootTest
@Transactional  // Rollback after each test
class ManualEntryServiceTest extends IntegrationTestBase {
    @Test
    void addPosition_shouldRollbackOnException() {
        assertThatThrownBy(() -> service.addPosition(invalidCommand))
            .isInstanceOf(InvalidQuantityException.class);

        assertThat(positionRepository.findAll()).isEmpty();  // Rolled back
    }
}
```

### Common Patterns

**Command-Query Separation:**
```java
// Commands (writes)
@Service
public class ManualEntryService {
    @Transactional
    public PositionDetailDTO addPosition(AddPositionCommand cmd) { }

    @Transactional
    public void updatePosition(UpdatePositionCommand cmd) { }
}

// Queries (reads)
@Service
public class PositionQueryService {
    @Transactional(readOnly = true)
    public PortfolioSummaryDTO getPortfolioSummary() { }

    @Transactional(readOnly = true)
    public PositionDetailDTO getPositionById(PositionId id) { }
}
```

## Consequences

### Positive

- Clear transaction boundaries at use case level
- Domain layer remains pure (no transaction concerns)
- Declarative with `@Transactional` (no boilerplate)
- Read-only optimization for queries
- Testable transactional behavior
- Spring Boot integration seamless

### Negative

- Self-calls don't trigger `@Transactional` (AOP limitation)
- Learning curve for propagation and isolation
- Transaction boundaries not visible in code flow
- Deadlocks possible with concurrent transactions

### Mitigation

- Extract to separate services to avoid self-calls
- Document common patterns
- Use Spring Debug logging for transaction boundaries
- Keep transactions short to minimize deadlock risk
- Implement retry logic for serialization failures

## Alternatives Considered

**Programmatic Transaction Management:**
Rejected - verbose, error-prone, declarative `@Transactional` cleaner

**Transactions at Repository Level:**
Rejected - too fine-grained, no atomicity across multiple repository calls

**No Transactions (Auto-Commit):**
Rejected - data integrity risk, no atomicity

## Related Decisions

- [ADR-004: Package Structure](ADR-004-package-structure.md)
- [ADR-012: Test Architecture](ADR-012-test-architecture.md)
- [ADR-016: Database Migration Strategy](ADR-016-database-migration-strategy.md)

## References

- Spring Transaction Management: https://docs.spring.io/spring-framework/reference/data-access/transaction.html
- PostgreSQL Isolation Levels: https://www.postgresql.org/docs/16/transaction-iso.html
