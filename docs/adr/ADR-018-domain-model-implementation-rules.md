# ADR-018: Domain Model Implementation Rules

## Status
Accepted

## Context

The domain model is the heart of the application and must follow strict DDD principles to ensure:
1. **Infrastructure Independence**: Domain model has no knowledge of persistence, frameworks, or external systems
2. **Immutability**: All domain types are immutable to prevent bugs and ensure thread safety
3. **Type Safety**: Rich value types prevent primitive obsession and encode business rules
4. **Pure Java**: Domain model uses only Java standard library, no framework annotations

This ADR consolidates implementation rules for the domain layer, complementing:
- ADR-002: Value Objects and Entities (classification)
- ADR-004: Package Structure (organization)
- ADR-006: Money Representation (financial precision)

## Decision

### Rule 1: All Domain Types Are Java Records

**Both value objects AND entities are implemented as Java records.**

```java
// Value Object
public record Money(BigDecimal amount, Currency currency) { }

// Entity (also a record - identity defined by specific field)
public record Account(AccountId id, AccountName name, BrokerName brokerName) { }
```

**Rationale**:
- Records enforce immutability by design
- Records provide equals/hashCode/toString automatically
- Records prevent accidental mutability
- Simpler, more concise code

### Rule 2: No Primitives in Domain Model

**All attributes use value types, never primitives or standard library types directly.**

| Instead of | Use |
|------------|-----|
| `String name` | `AccountName name` |
| `String brokerName` | `BrokerName brokerName` |
| `Long id` | `AccountId id` |
| `String symbol` | `InstrumentSymbol symbol` |
| `BigDecimal amount` | `Money amount` |

**Rationale**:
- Value types encode business rules and validation
- Prevents mixing incompatible values (e.g., AccountName with BrokerName)
- Self-documenting code
- Validation at construction time

### Rule 3: No Infrastructure References

**Domain model must not reference or mention:**
- Database, persistence, JPA, Hibernate
- Spring, frameworks, annotations
- Repositories (in entities themselves)
- "Generated", "sequence", "surrogate key"

**Wrong**:
```java
// DON'T: References database concepts
/**
 * Account identified by database-generated surrogate key.
 */
public record Account(Long id, ...) { }
```

**Correct**:
```java
// DO: Pure domain language
/**
 * A brokerage account holding investments.
 */
public record Account(AccountId id, AccountName name, BrokerName brokerName) { }
```

### Rule 4: Canonical Constructors Only

**No factory methods like `create()`, `reconstitute()`, `of()` for entities.**

Value objects MAY have static factory methods for convenience (e.g., `Money.pln("100")`), but entities use only the canonical record constructor.

**Wrong**:
```java
public record Account(...) {
    public static Account create(AccountName name, BrokerName broker) { ... }
    public static Account reconstitute(AccountId id, ...) { ... }
}
```

**Correct**:
```java
public record Account(AccountId id, AccountName name, BrokerName brokerName) {
    public Account {
        Objects.requireNonNull(id, "Account ID cannot be null");
        Objects.requireNonNull(name, "Account name cannot be null");
        Objects.requireNonNull(brokerName, "Broker name cannot be null");
    }
}
```

### Rule 5: No Mutating Methods

**Entities do not have update/set methods. State changes create new instances.**

**Wrong**:
```java
public record Account(...) {
    public void updateName(AccountName newName) { ... }  // Impossible anyway
    public void setId(AccountId id) { ... }              // Impossible anyway
}
```

**Correct**:
```java
// State changes happen by creating new instances
Account updated = new Account(existing.id(), newName, existing.brokerName());

// Or provide with* methods that return new instances
public record Account(...) {
    public Account withName(AccountName newName) {
        return new Account(this.id, newName, this.brokerName);
    }
}
```

### Rule 6: Validation in Canonical Constructor

**All validation happens in the compact canonical constructor.**

```java
public record AccountName(String value) {
    public AccountName {
        Objects.requireNonNull(value, "Account name cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("Account name cannot be blank");
        }
        value = value.trim();  // Normalize
    }
}
```

### Rule 7: Aggregates and Aggregate Roots Override equals/hashCode

**Entities, aggregates, and aggregate roots MUST override equals/hashCode to use identity-based comparison.**

**The Problem with Default Record Equality**:

Java records implement `equals()` based on ALL fields (structural equality). In DDD, entities should be equal based on **identity only**. This matters because:

1. An entity with changed state is still "the same entity"
2. Repository lookups and updates rely on identity comparison
3. Collections (Set, Map) need consistent identity behavior

**Example - Why Default Equality is Wrong**:

```java
// With default record equality:
Position p1 = new Position(symbol, List.of(holding1), price1);
Position p2 = new Position(symbol, List.of(holding1, holding2), price2);

p1.equals(p2);  // FALSE - but they represent the SAME position (same symbol)!
```

**Required Implementation**:

```java
public record Position(InstrumentSymbol symbol, List<AccountHolding> holdings, Price currentPrice) {

    // ... constructor and methods ...

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Position other)) return false;
        return symbol.equals(other.symbol);  // Identity-only comparison
    }

    @Override
    public int hashCode() {
        return symbol.hashCode();  // Consistent with equals
    }
}
```

**When to Apply This Rule**:

| Type | Override equals/hashCode? | Reason |
|------|---------------------------|--------|
| Value Object | NO | Structural equality is correct (Money(100, PLN) == Money(100, PLN)) |
| Entity | YES | Identity-based equality (same ID = same entity) |
| Aggregate Root | YES | Same as Entity - identity defines equality |

**Note**: Value objects keep default record equality because they ARE defined by their attributes. Two Money objects with the same amount and currency ARE the same value.

### Rule 8: Aggregate Roots Return New Aggregates

**Operations on aggregates return new aggregate instances.**

```java
public record Position(InstrumentSymbol symbol, List<AccountHolding> holdings, Price currentPrice) {

    public Position addHolding(AccountHolding newHolding) {
        var newHoldings = new ArrayList<>(this.holdings);
        newHoldings.add(newHolding);
        return new Position(this.symbol, List.copyOf(newHoldings), this.currentPrice);
    }

    public Position withPrice(Price newPrice) {
        return new Position(this.symbol, this.holdings, newPrice);
    }
}
```

## Consequences

### Positive

1. **True Immutability**: Records enforce immutability at language level
2. **No Accidental Mutation**: Impossible to have setter bugs
3. **Thread Safety**: Immutable objects are inherently thread-safe
4. **Clear Domain Model**: No infrastructure leakage
5. **Simpler Testing**: Immutable objects are easy to test
6. **Functional Style**: Works well with streams and functional programming
7. **Correct DDD Semantics**: Custom equals/hashCode ensures entities compare by identity, not state

### Negative

1. **More Object Creation**: State changes create new instances (mitigated by JVM optimizations)
2. **Verbose Updates**: Need to copy all fields when changing one (mitigated by `with*` methods)
3. **Learning Curve**: Developers used to mutable entities need adjustment
4. **Unusual for Records**: Overriding equals/hashCode in records is unconventional, but necessary for DDD entity semantics

### Migration Notes

When converting existing mutable entities to records:
1. Remove all `update*`, `set*` methods
2. Remove factory methods like `create()`, `reconstitute()`
3. Add `with*` methods for state transitions that return new instances
4. Convert String fields to value types
5. **Override equals/hashCode for entities and aggregates to use identity-only comparison**
6. Update tests to work with immutable model and identity-based equality

## Related Decisions

- [ADR-002: Value Objects and Entities](ADR-002-value-objects-and-entities.md)
- [ADR-004: Package Structure](ADR-004-package-structure.md)

## References

- Domain-Driven Design by Eric Evans
- Functional Domain Modeling
- Java Records (JEP 395)
