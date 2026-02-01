# ADR-019: Factory Methods and With-Methods in Domain Model

## Status
**Draft** - To be revisited after implementing more domain logic

## Context

During domain model implementation, a question arose about whether domain entities should have:
1. **Factory methods** like `empty()`, `fromPositions()`, `of()`
2. **With-methods** like `withPrice()`, `withName()` for state changes
3. **Domain operation methods** like `addHolding()`, `removeHolding()`

The concern is whether these methods add unnecessary API surface or provide valuable domain expressiveness.

## Analysis

### Three Types of Methods Identified

After analyzing all usages in the codebase, we identified three distinct types:

#### Type 1: Pure State Change (with* methods)
Methods that simply copy fields with one changed value.

```java
// Current
position = position.withPrice(Price.pln("550"));

// Alternative (constructor)
position = new Position(position.symbol(), position.holdings(), Price.pln("550"));
```

**Characteristics:**
- No business logic
- CAN be replaced with constructor
- Trade-off: API size vs readability

#### Type 2: Domain Operations
Methods that contain business logic and enforce invariants.

```java
// Current
Position updated = position.addHolding(accountId, quantity, costBasis);

// Cannot replace with constructor - contains business logic:
// - Merges holdings for same account with weighted average
// - Validates invariants
// - Calculates derived values
```

**Characteristics:**
- Contains business logic (validation, calculation, merging)
- CANNOT be replaced with constructor
- MUST exist in domain model

#### Type 3: Derived State Factories
Factory methods that calculate derived values.

```java
// Current
Portfolio portfolio = Portfolio.fromPositions(positions);

// Cannot simply use constructor because:
// - Metrics need to be calculated from positions
// - Calculation logic must live somewhere
```

**Characteristics:**
- Calculates derived state (metrics, aggregations)
- Design choice: factory method vs separate service vs on-demand calculation

### Current Usages

| Method | Type | Usages | Can Replace? |
|--------|------|--------|--------------|
| `Position.withPrice()` | State change | 6 (tests) | Yes |
| `Account.withName()` | State change | 1 (test) | Yes |
| `Account.withBrokerName()` | State change | 1 (test) | Yes |
| `Instrument.withPrice()` | State change | TBD | Yes |
| `Instrument.withName()` | State change | TBD | Yes |
| `Position.addHolding()` | Domain operation | 6 (tests) | **NO** |
| `Portfolio.empty()` | Simple factory | 2 (tests) | Yes |
| `Portfolio.fromPositions()` | Derived state | 9 (1 prod) | **NO** |

## Preliminary Decision

### Definite Keep
- **Domain operation methods** (`addHolding`, `removeHolding`) - contain essential business logic

### Under Consideration
- **With-methods** for state changes - readability benefit vs minimal API principle
- **Simple factories** (`empty()`) - convenience vs constructor clarity
- **Derived state factories** (`fromPositions()`) - may require architectural redesign

## Open Questions

1. **Readability vs Minimalism**: Is constructor verbosity acceptable in tests?
   ```java
   // With method (concise)
   position.withPrice(Price.pln("550"))

   // Constructor (explicit but verbose)
   new Position(position.symbol(), position.holdings(), Price.pln("550"))
   ```

2. **Portfolio Metrics**: Should metrics be:
   - A field calculated by `fromPositions()` (current)
   - Calculated on-demand (no metrics field)
   - Calculated by a separate domain service

3. **Test Readability**: Tests are primary users of with-methods. Does removing them significantly hurt test clarity?

## Next Steps

- [ ] Implement more domain logic to see patterns emerge
- [ ] Evaluate test readability with/without with-methods
- [ ] Decide on Portfolio metrics architecture
- [ ] Finalize this ADR with concrete decision

## Related Decisions

- [ADR-018: Domain Model Implementation Rules](ADR-018-domain-model-implementation-rules.md) - Rule 4 currently says "No factory methods for entities"

## References

- See `/temp/FACTORY-METHOD-ANALYSIS.md` for detailed usage comparison
