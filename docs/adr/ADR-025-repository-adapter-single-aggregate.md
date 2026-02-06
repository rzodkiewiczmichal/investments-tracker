# ADR-025: Repository Adapters Handle Single Aggregate Only

## Status
Accepted

## Context

In hexagonal architecture, repository adapters implement domain repository interfaces (ports). They translate between domain models and persistence mechanisms (JPA entities).

The Investment Tracker has multiple aggregates:
- **Position** (with embedded AccountHoldings)
- **Instrument** (with currentPrice)
- **Account**

When implementing `PositionRepositoryAdapter`, a question arises: Should the adapter fetch related data from other aggregates (e.g., current price from Instrument)?

### The Temptation

It's tempting to "enrich" Position with price data in the repository:

```java
// TEMPTING BUT WRONG
@Repository
public class PositionRepositoryAdapter implements PositionRepository {
    private final PositionJpaRepository positionJpaRepo;
    private final InstrumentJpaRepository instrumentJpaRepo;  // Cross-aggregate!

    public Position findBySymbol(InstrumentSymbol symbol) {
        PositionJpaEntity positionEntity = positionJpaRepo.findById(symbol.value());
        InstrumentJpaEntity instrumentEntity = instrumentJpaRepo.findById(symbol.value());

        // Mapper receives data from TWO aggregates
        return mapper.toDomain(positionEntity, instrumentEntity.getCurrentPrice());
    }
}
```

This approach conflates two aggregates in the infrastructure layer.

## Options Considered

### Option A: Repository Fetches Multiple Aggregates

```java
@Repository
public class PositionRepositoryAdapter implements PositionRepository {
    private final PositionJpaRepository positionJpaRepo;
    private final InstrumentJpaRepository instrumentJpaRepo;
    private final PositionPersistenceMapper mapper;

    public Position findBySymbol(InstrumentSymbol symbol) {
        PositionJpaEntity entity = positionJpaRepo.findById(symbol);
        Price price = instrumentJpaRepo.findById(symbol).map(this::toPrice);
        return mapper.toDomain(entity, price);  // Cross-aggregate mapping
    }
}
```

**Pros:**
- Convenient for callers - Position comes with price
- Single call returns "complete" data

**Cons:**
- **Violates single responsibility** - adapter handles two aggregates
- **Hidden dependency** - callers don't see Instrument involvement
- **Testing complexity** - tests must mock two JPA repositories
- **Mapper pollution** - persistence mapper needs cross-aggregate parameters
- **Transaction scope creep** - single transaction spans multiple aggregates
- **Consistency confusion** - which aggregate "owns" the price?
- **Cache invalidation** - Position cache must invalidate when Instrument changes

### Option B: JOIN in JPA Query

```java
public interface PositionJpaRepository extends JpaRepository<...> {
    @Query("SELECT p, i FROM PositionJpaEntity p JOIN InstrumentJpaEntity i " +
           "ON p.instrumentSymbol = i.symbol WHERE p.instrumentSymbol = :symbol")
    Object[] findPositionWithInstrument(@Param("symbol") String symbol);
}
```

**Pros:**
- Single database query
- Efficient for read operations

**Cons:**
- **Same problems as Option A** plus:
- **Awkward return type** - Object[] or Tuple is not domain language
- **JPA complexity** - custom queries harder to maintain
- **Aggregate boundary violation** - JPA layer conflates aggregates

### Option C: Single Aggregate per Repository ✓ CHOSEN

```java
@Repository
public class PositionRepositoryAdapter implements PositionRepository {
    private final PositionJpaRepository positionJpaRepo;
    private final PositionPersistenceMapper mapper;
    // NO InstrumentJpaRepository!

    public Optional<Position> findBySymbol(InstrumentSymbol symbol) {
        return positionJpaRepo.findById(symbol.value())
            .map(mapper::toDomain);  // Pure single-aggregate mapping
    }
}

// Separate adapter for Instrument aggregate
@Repository
public class InstrumentRepositoryAdapter implements InstrumentRepository {
    private final InstrumentJpaRepository instrumentJpaRepo;
    private final InstrumentPersistenceMapper mapper;

    public Optional<Instrument> findBySymbol(InstrumentSymbol symbol) {
        return instrumentJpaRepo.findById(symbol.value())
            .map(mapper::toDomain);
    }
}
```

**Pros:**
- **Single responsibility** - each adapter handles one aggregate
- **Clear ownership** - Position adapter owns Position, Instrument adapter owns Instrument
- **Simple testing** - each adapter tested in isolation
- **Pure mappers** - persistence mapper only knows its own aggregate
- **Independent caching** - each aggregate cached separately
- **Explicit composition** - use cases clearly show what they fetch
- **Consistent with DDD** - respects aggregate boundaries

**Cons:**
- Multiple repository calls for cross-aggregate queries
- More code in use case layer

## Decision

**Adopt Option C: Each Repository Adapter Handles Exactly One Aggregate**

### Rules

1. **One adapter per aggregate root**
   ```
   PositionRepositoryAdapter → Position aggregate
   InstrumentRepositoryAdapter → Instrument aggregate
   AccountRepositoryAdapter → Account aggregate
   ```

2. **No cross-aggregate injection in adapters**
   ```java
   // WRONG
   public class PositionRepositoryAdapter {
       private final PositionJpaRepository positionJpaRepo;
       private final InstrumentJpaRepository instrumentJpaRepo;  // NO!
   }

   // CORRECT
   public class PositionRepositoryAdapter {
       private final PositionJpaRepository positionJpaRepo;
       private final PositionPersistenceMapper mapper;
       // Only Position-related dependencies
   }
   ```

3. **Persistence mapper handles ONE aggregate**
   ```java
   // WRONG
   public Position toDomain(PositionJpaEntity entity, Price price) { }

   // CORRECT
   public Position toDomain(PositionJpaEntity entity) { }
   ```

4. **Use cases compose multiple aggregates**
   ```java
   @Service
   public class PositionQueryUseCaseService {
       private final PositionRepository positionRepo;    // One aggregate
       private final InstrumentRepository instrumentRepo; // Another aggregate

       public PositionWithPrice getPositionWithPrice(InstrumentSymbol symbol) {
           Position position = positionRepo.findBySymbol(symbol);
           Instrument instrument = instrumentRepo.findBySymbol(symbol);
           // Compose at application layer
       }
   }
   ```

### Adapter Structure

```java
@Repository
public class PositionRepositoryAdapter implements PositionRepository {

    private final PositionJpaRepository jpaRepository;
    private final PositionPersistenceMapper mapper;

    public PositionRepositoryAdapter(
            PositionJpaRepository jpaRepository,
            PositionPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<Position> findBySymbol(InstrumentSymbol symbol) {
        return jpaRepository.findById(symbol.value())
            .map(mapper::toDomain);
    }

    @Override
    public Collection<Position> findAll() {
        return jpaRepository.findAll().stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public Position save(Position position) {
        PositionJpaEntity entity = mapper.toEntity(position);
        PositionJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public void deleteBySymbol(InstrumentSymbol symbol) {
        jpaRepository.deleteById(symbol.value());
    }

    @Override
    public boolean existsBySymbol(InstrumentSymbol symbol) {
        return jpaRepository.existsById(symbol.value());
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }
}
```

### Persistence Mapper Structure

```java
@Component
public class PositionPersistenceMapper {

    /**
     * Maps JPA entity to domain Position.
     * Only handles Position aggregate - no Instrument data.
     */
    public Position toDomain(PositionJpaEntity entity) {
        List<AccountHolding> holdings = entity.getHoldings().stream()
            .map(this::toAccountHolding)
            .toList();

        return new Position(
            new InstrumentSymbol(entity.getInstrumentSymbol()),
            holdings
            // NO currentPrice - it belongs to Instrument aggregate
        );
    }

    public PositionJpaEntity toEntity(Position position) {
        PositionJpaEntity entity = new PositionJpaEntity();
        entity.setInstrumentSymbol(position.symbol().value());
        // Map holdings...
        return entity;
    }

    private AccountHolding toAccountHolding(AccountHoldingEmbeddable emb) {
        return new AccountHolding(
            new AccountId(emb.getAccountId()),
            Quantity.of(emb.getQuantity()),
            CostBasis.of(Money.of(emb.getCostBasisAmount(),
                        Currency.valueOf(emb.getCostBasisCurrency())))
        );
    }
}
```

## Consequences

### Positive

1. **Single responsibility** - Each adapter is focused and simple
2. **Independent testing** - Test each adapter without mocking other aggregates
3. **Clear ownership** - No confusion about which adapter owns what
4. **Maintainability** - Changes to Instrument don't affect Position adapter
5. **Cacheable** - Each aggregate can be cached independently
6. **Consistent boundaries** - Infrastructure respects domain aggregate boundaries
7. **Simple mappers** - No cross-aggregate parameter passing

### Negative

1. **Multiple database calls** - Fetching Position + Instrument = 2 queries
2. **N+1 risk** - Listing positions with prices requires batch fetching
3. **Use case complexity** - More orchestration code

### Mitigation Strategies

1. **Batch fetching** - Add `findAllBySymbols(List<InstrumentSymbol>)` to repositories
   ```java
   public interface InstrumentRepository {
       Map<InstrumentSymbol, Instrument> findAllBySymbols(Collection<InstrumentSymbol> symbols);
   }
   ```

2. **Query optimization** - Use case can batch fetch instruments after loading positions
   ```java
   Collection<Position> positions = positionRepo.findAll();
   Set<InstrumentSymbol> symbols = positions.stream()
       .map(Position::symbol)
       .collect(toSet());
   Map<InstrumentSymbol, Instrument> instruments = instrumentRepo.findAllBySymbols(symbols);
   ```

3. **Caching** - Cache instruments (prices) since they're frequently accessed

## Related Decisions

- [ADR-001: Aggregate Boundaries](ADR-001-aggregate-boundaries.md) - Defines aggregate roots
- [ADR-023: Cross-Aggregate Data Access](ADR-023-cross-aggregate-data-access.md) - Composition in use cases
- [ADR-024: Domain Services](ADR-024-domain-services-cross-aggregate-calculations.md) - Cross-aggregate calculations

## References

- Domain-Driven Design by Eric Evans - Repository Pattern
- Implementing Domain-Driven Design by Vaughn Vernon - Aggregate Persistence
- Learning Domain-Driven Design by Vlad Khononov - Repository Design
- Hexagonal Architecture by Alistair Cockburn - Adapters
