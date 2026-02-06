# ADR-024: Domain Services for Cross-Aggregate Calculations

## Status
Accepted

## Context

Some business calculations require data from multiple aggregates. In the Investment Tracker:

- **CurrentValue** = Quantity (from Position) × Price (from Instrument)
- **ProfitAndLoss** = CurrentValue - InvestedAmount (from Position)

These calculations are core domain logic, not application orchestration. The question is: **Where should cross-aggregate calculation logic live?**

### Constraints

From [ADR-023](ADR-023-cross-aggregate-data-access.md):
- Aggregates must not embed data from other aggregates
- Position aggregate does NOT contain currentPrice

From [ADR-003](ADR-003-domain-vs-application-services.md):
- Domain services contain pure business logic
- Application services orchestrate, they don't calculate

## Options Considered

### Option A: Calculation Methods in Aggregate (with parameters)

```java
public record Position(...) {
    public CurrentValue calculateCurrentValue(Price currentPrice) {
        return CurrentValue.calculate(calculateTotalQuantity(), currentPrice);
    }
}
```

**Pros:**
- Calculation "belongs" to Position conceptually
- Rich domain model

**Cons:**
- **Leaky abstraction** - Position needs external data to complete its calculation
- **API confusion** - Some methods work standalone, others need parameters
- **Testing asymmetry** - Different test setups for different methods
- **Aggregate pollution** - Position knows about Instrument's data structure (Price)

### Option B: Calculation in Use Case (Application Layer)

```java
@Service
public class PositionQueryUseCaseService {
    public PositionWithPrice getPositionWithPrice(InstrumentSymbol symbol) {
        Position position = positionRepo.findBySymbol(symbol);
        Instrument instrument = instrumentRepo.findBySymbol(symbol);

        // Calculate in use case
        Quantity qty = position.calculateTotalQuantity();
        CurrentValue value = CurrentValue.calculate(qty, instrument.currentPrice());

        return new PositionWithPrice(position, instrument, value, ...);
    }
}
```

**Pros:**
- Simple, direct approach
- All data available in one place

**Cons:**
- **Business logic in application layer** - violates hexagonal architecture
- **Duplicated calculations** - same formula repeated across use cases
- **Anemic domain** - domain model doesn't express business rules
- **Hard to test in isolation** - need Spring context for calculation tests
- **Framework coupling** - calculations tied to @Service classes

### Option C: Domain Service (Stateless Calculator) ✓ CHOSEN

```java
// Domain service - pure business logic, no infrastructure
public class PositionCalculationService {

    public CurrentValue calculateCurrentValue(Position position, Price currentPrice) {
        Objects.requireNonNull(position, "position cannot be null");
        Objects.requireNonNull(currentPrice, "currentPrice cannot be null");

        Quantity totalQuantity = position.calculateTotalQuantity();
        return CurrentValue.calculate(totalQuantity, currentPrice);
    }

    public ProfitAndLoss calculateProfitAndLoss(Position position, Price currentPrice) {
        CurrentValue currentValue = calculateCurrentValue(position, currentPrice);
        InvestedAmount investedAmount = position.calculateInvestedAmount();
        return ProfitAndLoss.calculate(currentValue, investedAmount);
    }
}
```

**Pros:**
- **Pure domain logic** - no infrastructure dependencies
- **Explicit cross-aggregate nature** - parameters show what data is needed
- **Highly testable** - plain JUnit tests, no mocks needed
- **Reusable** - any use case can call the service
- **Single responsibility** - aggregate stores data, service calculates
- **Domain language** - `PositionCalculationService` expresses business capability

**Cons:**
- Additional class to maintain
- Caller must provide all parameters

### Option D: Static Utility Methods

```java
public class PositionCalculations {
    public static CurrentValue calculateCurrentValue(Quantity qty, Price price) {
        return CurrentValue.calculate(qty, price);
    }
}
```

**Pros:**
- Simple, no instantiation needed

**Cons:**
- **Not a domain concept** - utilities don't express business capabilities
- **Hard to extend** - can't add dependencies if needed later
- **Testing challenges** - can't mock static methods easily
- **No domain language** - "PositionCalculations" vs "PositionCalculationService"

## Decision

**Adopt Option C: Domain Service for Cross-Aggregate Calculations**

### Rules

1. **Domain services are stateless** - No instance variables, pure functions

2. **Parameters from different aggregates** - Service accepts data from multiple sources
   ```java
   CurrentValue calculateCurrentValue(Position position, Price currentPrice)
   // Position from Position aggregate, Price from Instrument aggregate
   ```

3. **Return domain value objects** - Results are domain types (CurrentValue, ProfitAndLoss)

4. **No infrastructure dependencies** - Domain services don't inject repositories
   ```java
   // WRONG
   public class PositionCalculationService {
       private final InstrumentRepository instrumentRepo;  // NO!
   }

   // CORRECT
   public class PositionCalculationService {
       // No dependencies - pure calculation
   }
   ```

5. **Use cases call domain services** - Application layer orchestrates
   ```java
   @Service
   public class PositionQueryUseCaseService {
       private final PositionCalculationService calcService;  // Injected

       public PositionWithPrice getPositionWithPrice(...) {
           // Fetch aggregates, then delegate calculation
           return calcService.calculateCurrentValue(position, price);
       }
   }
   ```

### Position Aggregate Methods

After this decision, Position aggregate contains only self-sufficient methods:

| Method | Data Source | Location |
|--------|-------------|----------|
| `calculateTotalQuantity()` | holdings (Position only) | Position aggregate |
| `calculateWeightedAverageCostBasis()` | holdings (Position only) | Position aggregate |
| `calculateInvestedAmount()` | holdings (Position only) | Position aggregate |
| `calculateCurrentValue(price)` | Position + Instrument | ~~Position~~ → Domain Service |
| `calculateProfitAndLoss(price)` | Position + Instrument | ~~Position~~ → Domain Service |

### Domain Service Interface

```java
/**
 * Domain service for Position calculations requiring external data.
 * Handles cross-aggregate calculations that need Price from Instrument.
 */
public class PositionCalculationService {

    /**
     * Calculates current market value of a position.
     *
     * @param position the position aggregate
     * @param currentPrice current market price from Instrument aggregate
     * @return the calculated current value
     */
    public CurrentValue calculateCurrentValue(Position position, Price currentPrice);

    /**
     * Calculates profit and loss for a position.
     *
     * @param position the position aggregate
     * @param currentPrice current market price from Instrument aggregate
     * @return the calculated profit and loss with percentage
     */
    public ProfitAndLoss calculateProfitAndLoss(Position position, Price currentPrice);
}
```

## Consequences

### Positive

1. **Pure domain layer** - No infrastructure pollution in domain services
2. **Testable in isolation** - Plain JUnit tests without Spring
3. **Clear boundaries** - Aggregate methods vs service methods clearly separated
4. **Explicit dependencies** - Parameters show exactly what data is needed
5. **Reusable** - Multiple use cases can share calculation logic
6. **Domain language** - Service name expresses business capability

### Negative

1. **More classes** - Additional domain service class
2. **Caller responsibility** - Use cases must fetch all required data
3. **Parameter passing** - More verbose than `position.calculateCurrentValue()`

### Testing Strategy

```java
class PositionCalculationServiceTest {

    private final PositionCalculationService service = new PositionCalculationService();

    @Test
    void shouldCalculateCurrentValue() {
        // Given - pure domain objects, no mocks
        Position position = createPositionWithQuantity(100);
        Price price = Price.pln("50.00");

        // When
        CurrentValue result = service.calculateCurrentValue(position, price);

        // Then
        assertThat(result.money().amount()).isEqualByComparingTo("5000.00");
    }

    @Test
    void shouldCalculateProfitAndLoss() {
        // Given
        Position position = createPositionWithInvestedAmount("4000.00");
        Price price = Price.pln("50.00");  // qty=100 → value=5000

        // When
        ProfitAndLoss result = service.calculateProfitAndLoss(position, price);

        // Then
        assertThat(result.amount().amount()).isEqualByComparingTo("1000.00");
        assertThat(result.percentage().value()).isEqualByComparingTo("25.00");
    }
}
```

## Related Decisions

- [ADR-001: Aggregate Boundaries](ADR-001-aggregate-boundaries.md) - Position and Instrument as separate aggregates
- [ADR-003: Domain vs Application Services](ADR-003-domain-vs-application-services.md) - Service layer responsibilities
- [ADR-023: Cross-Aggregate Data Access](ADR-023-cross-aggregate-data-access.md) - Use cases orchestrate aggregates

## References

- Domain-Driven Design by Eric Evans - Chapter 5: Services
- Implementing Domain-Driven Design by Vaughn Vernon - Domain Services
- Learning Domain-Driven Design by Vlad Khononov - Stateless Domain Services
