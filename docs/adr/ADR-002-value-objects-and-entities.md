# ADR-002: Value Objects and Entities

## Status
Accepted

## Context

Domain-Driven Design distinguishes between **entities** (objects with identity and lifecycle) and **value objects** (immutable objects defined entirely by their attributes). Proper classification is critical for:

1. **Correctness**: Entities need identity-based equality, value objects need value-based equality
2. **Immutability**: Value objects should be immutable to prevent bugs and enable safe sharing
3. **Type Safety**: Rich value objects prevent primitive obsession
4. **Testability**: Clear distinction simplifies unit testing

Domain concepts identified from requirements:
- Financial values: Money amounts, quantities, prices, percentages
- Identifiers: Instrument symbols, account IDs
- Calculations: Invested amount, current value, P&L, XIRR
- Core entities: Position, Account, Portfolio, Instrument
- Composition: AccountHolding

Key considerations:
- Java 25 LTS provides records for immutable value objects
- Hexagonal architecture requires framework-independent domain model
- Need natural keys for some entities (Position) and surrogate keys for others (Account)

## Decision

### Entity Classification

Entities have **identity**, **lifecycle**, and are **mutable**:

#### 1. Position (Entity)
- **Identity**: InstrumentSymbol (natural key - String)
- **Equality**: By InstrumentSymbol (same instrument = same position)
- **Mutability**: Quantity and holdings change through transactions
- **Lifecycle**: Created, updated, closed
- **Rationale**: Business asks "show me the Apple position" - identity matters

#### 2. AccountHolding (Entity)
- **Identity**: Composite key (InstrumentSymbol + AccountId)
- **Equality**: By composite key
- **Mutability**: Quantity can change
- **Lifecycle**: Managed by Position aggregate
- **Rationale**: Specific holding in specific account has identity

#### 3. Account (Entity)
- **Identity**: AccountId (Long, database-generated sequence)
- **Equality**: By AccountId
- **Mutability**: Name and broker can change
- **Lifecycle**: Created, updated, archived
- **Rationale**: Account identity independent of attributes

#### 4. Portfolio (Entity)
- **Identity**: Singleton in v0.1 (implicit), UserId in future
- **Equality**: By identity
- **Mutability**: Metrics change as positions change
- **Lifecycle**: Exists for duration of user account
- **Rationale**: Single mutable portfolio per user

#### 5. Instrument (Entity - Reference Data)
- **Identity**: InstrumentSymbol (String - ticker or ISIN)
- **Equality**: By symbol
- **Mutability**: Price changes over time
- **Lifecycle**: Created when first imported, never deleted
- **Rationale**: Instrument identity matters, price is mutable

### Value Object Classification

Value objects are **immutable**, have **no identity**, and **value-based equality**:

#### 1. Money (Value Object)
```java
record Money(BigDecimal amount, Currency currency) {
    // Validation, operations
}
```
- **Immutability**: Amount and currency never change
- **Equality**: Two Money(100, PLN) are identical
- **Operations**: add(), subtract(), multiply() return new Money instances
- **Validation**: Non-null amount and currency
- **Rationale**: 100 PLN is always 100 PLN, identity doesn't matter

#### 2. Quantity (Value Object)
```java
record Quantity(BigDecimal value) {
    // Validates > 0
}
```
- **Immutability**: Value never changes
- **Equality**: Two Quantity(50) are identical
- **Validation**: Must be > 0 (constructor enforces)
- **Rationale**: Number of shares is pure value

#### 3. Price (Value Object)
```java
record Price(Money amount) {
    // Validates > 0
}
```
- **Immutability**: Price value never changes
- **Equality**: Value-based
- **Validation**: Must be > 0
- **Rationale**: Price per unit is immutable value

#### 4. CostBasis (Value Object)
```java
record CostBasis(Money amount) {
    // Validates > 0
}
```
- **Immutability**: Average cost never changes once calculated
- **Equality**: Value-based
- **Validation**: Must be > 0
- **Rationale**: Average cost per unit is value

#### 5. InvestedAmount (Value Object)
```java
record InvestedAmount(Money amount) {
    // Derived from Quantity × CostBasis
}
```
- **Immutability**: Calculated value
- **Equality**: Value-based
- **Rationale**: Completely determined by inputs, no identity

#### 6. CurrentValue (Value Object)
```java
record CurrentValue(Money amount) {
    // Derived from Quantity × Price
}
```
- **Immutability**: Snapshot of calculation
- **Equality**: Value-based
- **Rationale**: Derived value, no lifecycle

#### 7. ProfitAndLoss (Value Object)
```java
record ProfitAndLoss(Money amount, Percentage percentage) {
    // Derived from CurrentValue - InvestedAmount
}
```
- **Immutability**: Snapshot of calculation
- **Equality**: Value-based
- **Composite**: Contains both amount and percentage
- **Rationale**: P&L is derived metric, immutable snapshot

#### 8. XIRR (Value Object)
```java
record XIRR(Percentage value) {
    // Annualized return calculation (v0.3+)
}
```
- **Immutability**: Calculated from transaction history
- **Equality**: Value-based
- **Rationale**: Pure calculation result

#### 9. InstrumentSymbol (Value Object)
```java
record InstrumentSymbol(String value) {
    // Validates format (ticker or ISIN)
}
```
- **Immutability**: Symbol never changes
- **Equality**: Value-based
- **Used as Natural Key**: Even though it identifies Position/Instrument
- **Validation**: Non-empty, valid format
- **Rationale**: Symbol itself is immutable value, DDD allows VOs as identifiers

#### 10. Percentage (Value Object)
```java
record Percentage(BigDecimal value) {
    // Generic percentage value
}
```
- **Immutability**: Percentage value never changes
- **Equality**: Value-based
- **Usage**: P&L percentage, XIRR, reconciliation tolerance
- **Rationale**: Pure numeric value

### Identity Strategy

#### Natural Key: Position

**Decision**: Use InstrumentSymbol (String) as natural key for Position

**Rationale**:
- Aligns with ubiquitous language: "Apple position", "Tesla position"
- Symbol is stable and unique per position
- No need for surrogate key - symbol is natural identifier
- Simplifies queries and references
- JPA: `@Id private InstrumentSymbol symbol;`

**Trade-off**: If symbol changes (very rare), Position must be recreated

#### Surrogate Key: Account

**Decision**: Use AccountId (Long, database-generated sequence) for Account

**Rationale**:
- Account names can change, broker can change
- No natural stable identifier in domain
- Long is simpler than UUID for v0.1:
  - Easier to read in logs and debugging
  - Smaller database indexes
  - Sequential generation is sufficient for single-instance deployment
- Can migrate to UUID in v2.0 if distributed ID generation needed

**JPA**: `@Id @GeneratedValue(strategy = GenerationType.SEQUENCE)`

#### Composite Key: AccountHolding

**Decision**: Composite key (InstrumentSymbol + AccountId)

**Rationale**:
- AccountHolding is uniquely identified by instrument + account
- Embedded within Position aggregate, no separate table needed in some designs
- Natural composite identifier from domain

**JPA**: `@IdClass` or `@EmbeddedId` depending on implementation

#### No Identity: Value Objects

**Decision**: Value objects never have @Id

**Rationale**:
- Equality by value comparison
- Embedded in entities using @Embeddable or inline fields
- No lifecycle management needed

## Consequences

### Positive

1. **Clear Immutability Boundaries**: All value objects immutable, preventing accidental mutation bugs
2. **Type Safety**: Money instead of BigDecimal prevents mixing amounts with prices
3. **Self-Validating**: Value objects validate in constructor (Quantity > 0, Price > 0)
4. **Safe Sharing**: Immutable value objects can be shared without defensive copying
5. **Natural Domain Model**: InstrumentSymbol as Position key aligns with business language
6. **Simple IDs for MVP**: Long IDs for Account are developer-friendly
7. **Rich Behavior**: Value objects encapsulate operations (Money.add(), Quantity.multiply())
8. **Testable**: Clear equality rules simplify unit testing

### Negative

1. **Position Recreation**: If InstrumentSymbol changes, Position must be recreated (acceptable - very rare)
2. **No JPA @GeneratedValue for Position**: Must assign symbol manually (application controls it)
3. **Class Proliferation**: Many value object classes (but improves type safety and clarity)
4. **Long ID Migration**: May need UUID for cloud distributed systems in v2.0 (deferred)

### Implementation Guidelines

#### Value Objects (Java Records)

```java
package com.investments.tracker.domain.model.value;

import java.math.BigDecimal;

public record Money(BigDecimal amount, Currency currency) {

    public Money {
        if (amount == null) throw new IllegalArgumentException("Amount cannot be null");
        if (currency == null) throw new IllegalArgumentException("Currency cannot be null");
    }

    public Money add(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot add different currencies");
        }
        return new Money(this.amount.add(other.amount), this.currency);
    }

    public Money multiply(BigDecimal factor) {
        return new Money(this.amount.multiply(factor), this.currency);
    }
}
```

#### Entities (Classes with Identity)

```java
package com.investments.tracker.domain.model;

public class Position {
    private InstrumentSymbol symbol; // Identity
    private List<AccountHolding> holdings;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position position = (Position) o;
        return Objects.equals(symbol, position.symbol); // Equality by ID only
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbol); // Hash by ID only
    }
}
```

#### JPA Mapping

```java
@Entity
@Table(name = "positions")
public class Position {

    @EmbeddedId
    private InstrumentSymbol symbol; // Natural key

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AccountHolding> holdings = new ArrayList<>();

    // No @Version for optimistic locking
}

@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "account_seq")
    @SequenceGenerator(name = "account_seq", sequenceName = "account_id_seq", allocationSize = 1)
    private Long id; // Surrogate key

    @Version
    private Long version; // Optimistic locking
}
```

### Validation Strategy

1. **Constructor Validation**: All value objects validate in constructor
2. **Fail Fast**: Throw IllegalArgumentException for invalid values
3. **Immutability**: No setters, all fields final
4. **Factory Methods**: Consider static factory methods for complex construction

### Testing Strategy

1. **Value Object Tests**:
   - Test validation (Quantity > 0 throws exception)
   - Test value equality (two Money(100, PLN) are equal)
   - Test immutability (operations return new instances)
   - Test operations (Money.add() produces correct result)

2. **Entity Tests**:
   - Test identity equality (same ID = equal)
   - Test lifecycle operations
   - Test invariants (Position total quantity = sum of holdings)

### Alternatives Considered

**Alternative 1: UUID for all Entity IDs**
- **Rejected for v0.1**: Adds complexity without benefit (single-instance deployment)
- **Deferred to v2.0**: Will reconsider for cloud distributed systems
- Natural key for Position still preferred

**Alternative 2: Instrument as Value Object**
- **Rejected**: Instrument has identity and mutable price
- Symbol alone is value object, but Instrument entity is needed for reference data

**Alternative 3: ProfitAndLoss as Entity**
- **Rejected**: No lifecycle, completely derived from other values
- Value object keeps model clean and simple

**Alternative 4: Mutable Money Class**
- **Rejected**: Violates value object principles, enables bugs
- Immutability prevents accidental mutations and enables safe sharing

**Alternative 5: Primitive Obsession (BigDecimal everywhere)**
- **Rejected**: No type safety, no validation, no domain behavior
- Value objects provide much richer domain model

## Related Decisions

- [ADR-001: Aggregate Boundaries](ADR-001-aggregate-boundaries.md) - Defines which entities are aggregate roots
- [ADR-003: Domain vs Application Services](ADR-003-domain-vs-application-services.md) - Uses value objects in service interfaces
- [ADR-004: Package Structure](ADR-004-package-structure.md) - Physical organization of value objects

## References

- Domain-Driven Design by Eric Evans (Value Objects chapter)
- Implementing Domain-Driven Design by Vaughn Vernon (Value Object design)
- Java Records (JEP 395)
- requirements/functional/ubiquitous-language.md
