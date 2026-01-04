# ADR-006: Money Representation

## Status
Accepted

## Context

Financial applications require precise decimal arithmetic to avoid rounding errors. The Investment Tracker tracks monetary values in Polish Zloty (PLN) and must:

1. **Prevent Rounding Errors**: Financial calculations must be exact (no floating-point imprecision)
2. **Support Polish Currency**: Store amounts to grosz precision (0.01 PLN)
3. **Enable Complex Calculations**: Weighted average cost requires extra precision beyond display
4. **Map to Java BigDecimal**: Seamless conversion between database and domain model
5. **Handle Stock Prices**: Support fractional pricing (e.g., 123.4567 PLN per share)
6. **Support Fractional Shares**: Some brokers allow 0.0001 share quantities

### Requirements

- **Currency**: PLN (Polish Zloty) in v0.1, multi-currency in v2.0+ (NFR-089)
- **Precision**: Exact decimal arithmetic for financial calculations
- **Performance**: Efficient storage and indexing
- **Portability**: Standard SQL type (no database-specific extensions)

### Industry Standards

- **ISO 4217**: PLN has 2 decimal places (grosz)
- **Financial Systems**: Commonly use 4 decimal places for prices
- **Quantity Precision**: 8 decimal places for fractional shares

### Technology Stack

- **Database**: PostgreSQL 15+ with DECIMAL/NUMERIC support
- **Java**: BigDecimal with configurable scale
- **JPA**: @Column precision and scale attributes

## Decision

### Money Amount Precision: DECIMAL(19, 4)

```sql
current_price_amount    DECIMAL(19, 4)
avg_cost_basis_amount   DECIMAL(19, 4)
cost_basis_amount       DECIMAL(19, 4)
```

**Breakdown**:
- **Total Digits**: 19
- **Decimal Places**: 4
- **Integer Part**: Up to 15 digits (999,999,999,999,999)
- **Fractional Part**: 4 decimal places (0.0001)

**Rationale**:

1. **Grosz Precision (0.01 PLN)**:
   - 2 decimal places for Polish currency standard
   - Supports prices like 123.45 PLN

2. **Extra Calculation Precision**:
   - 2 additional decimal places (0.0001) for intermediate calculations
   - Prevents rounding errors in weighted average cost calculations
   - Example: (50 × 500.12 + 30 × 520.34) / 80 = 508.5275 PLN

3. **Stock Price Support**:
   - Stock prices can have fractional cents: 123.4567 PLN
   - 4 decimals handle both display and calculation precision

4. **Storage Efficiency**:
   - DECIMAL(19, 4) = 9-16 bytes (depending on value)
   - More efficient than arbitrary precision types
   - Indexed efficiently by PostgreSQL

5. **Large Portfolio Support**:
   - Max value: 999,999,999,999,999.9999 PLN (almost 1 quadrillion)
   - Supports billion-PLN portfolios without overflow
   - Personal investment tracking rarely exceeds millions

### Quantity Precision: DECIMAL(19, 8)

```sql
quantity         DECIMAL(19, 8)
total_quantity   DECIMAL(19, 8)
```

**Breakdown**:
- **Total Digits**: 19
- **Decimal Places**: 8
- **Integer Part**: Up to 11 digits
- **Fractional Part**: 8 decimal places (0.00000001)

**Rationale**:

1. **Fractional Shares**:
   - Some brokers allow 0.0001 share increments
   - 8 decimals safely support up to 0.00000001 shares

2. **Weighted Average Precision**:
   - More precision than price to avoid rounding in calculations
   - Quantity × Price won't lose precision

3. **Example**:
   - Quantity: 123.45678901 shares (8 decimals)
   - Price: 567.8900 PLN (4 decimals)
   - Value: 70,090.1839979289 PLN (12 decimals before rounding to 4)

4. **Safety Margin**:
   - Most positions have whole shares or simple fractions
   - 8 decimals provide safety for edge cases

### Currency Storage: VARCHAR(3)

```sql
current_price_currency  VARCHAR(3) DEFAULT 'PLN'
avg_cost_basis_currency VARCHAR(3) NOT NULL DEFAULT 'PLN'
cost_basis_currency     VARCHAR(3) NOT NULL DEFAULT 'PLN'
```

**Rationale**:

1. **ISO 4217 Standard**:
   - 3-letter currency codes: PLN, USD, EUR, etc.
   - Industry standard for currency representation

2. **v0.1 Simplification**:
   - Default to 'PLN' for all amounts
   - Single-currency portfolio in MVP

3. **v2.0+ Multi-Currency**:
   - Column exists but always PLN in v0.1
   - No application logic change needed to support multi-currency
   - Can add conversion rates table in future

4. **Type Safety**:
   - Could use CHECK constraint: `CHECK (currency = 'PLN')` in v0.1
   - Remove constraint when adding multi-currency support

### Rounding Mode: HALF_EVEN (Banker's Rounding)

**PostgreSQL Behavior**:
- DECIMAL/NUMERIC uses HALF_EVEN rounding by default
- Also called "banker's rounding" or "round half to even"
- Minimizes cumulative rounding bias

**Java BigDecimal Configuration**:
```java
public record Money(BigDecimal amount, Currency currency) {
    public Money add(Money other) {
        // ... currency check ...
        return new Money(
            this.amount.add(other.amount),
            this.currency
        );
    }

    public Money multiply(BigDecimal factor) {
        return new Money(
            this.amount.multiply(factor).setScale(4, RoundingMode.HALF_EVEN),
            this.currency
        );
    }

    public Money divide(BigDecimal divisor) {
        return new Money(
            this.amount.divide(divisor, 4, RoundingMode.HALF_EVEN),
            this.currency
        );
    }
}
```

**Rationale**:
- Matches PostgreSQL behavior
- Minimizes bias in repeated calculations
- Industry standard for financial systems
- Deterministic results

### Data Type Comparison

| Type | Storage | Max Value | Decimal Places | Use Case |
|------|---------|-----------|----------------|----------|
| `DECIMAL(19, 4)` | 9-16 bytes | 999,999,999,999,999.9999 | 4 | Money amounts, prices |
| `DECIMAL(19, 8)` | 9-16 bytes | 99,999,999,999.99999999 | 8 | Quantities, shares |
| `DECIMAL(10, 2)` | 5-8 bytes | 99,999,999.99 | 2 | Too small for large portfolios |
| `DECIMAL(15, 2)` | 7-12 bytes | 9,999,999,999,999.99 | 2 | Sufficient but no calc precision |
| `FLOAT/DOUBLE` | 4/8 bytes | Varies | Imprecise | ❌ NEVER for money |

## Consequences

### Positive

1. **Exact Arithmetic**: No floating-point rounding errors
2. **Calculation Precision**: 4 decimals support intermediate calculations
3. **Fractional Shares**: 8 decimals handle broker edge cases
4. **Large Portfolios**: 19 total digits support billion-PLN values
5. **Java Compatibility**: Maps cleanly to BigDecimal
6. **Standard SQL**: DECIMAL supported by all databases
7. **Efficient Storage**: 9-16 bytes per value
8. **Future-Proof**: Currency column ready for multi-currency (v2.0+)
9. **Consistent Rounding**: HALF_EVEN matches PostgreSQL and financial standards

### Negative

1. **Storage Overhead**: DECIMAL larger than FLOAT (but necessary for correctness)
2. **Always 4 Decimals**: Display logic must format to 2 decimals for PLN
3. **Currency Column Unused**: VARCHAR(3) always 'PLN' in v0.1 (but prepares for v2.0)
4. **Scale Fixed**: Cannot change precision without migration (but unlikely to need)

### Mitigation Strategies

1. **Display Formatting**: Application layer rounds to 2 decimals for display:
   ```java
   public String formatForDisplay() {
       return amount.setScale(2, RoundingMode.HALF_EVEN).toString() + " " + currency;
   }
   ```

2. **Validation**: Constructor enforces scale:
   ```java
   public Money {
       if (amount.scale() > 4) {
           throw new IllegalArgumentException("Money amount cannot exceed 4 decimal places");
       }
   }
   ```

3. **Currency Check**: Validate PLN-only in v0.1:
   ```java
   public Money {
       if (!"PLN".equals(currency)) {
           throw new IllegalArgumentException("Only PLN supported in v0.1");
       }
   }
   ```

## Alternatives Considered

### Alternative 1: DECIMAL(15, 2)

Store amounts with only 2 decimal places (grosz precision).

**Rejected**:
- Loses precision in weighted average calculations
- Cannot store fractional stock prices (123.4567 PLN)
- Cumulative rounding errors in portfolio calculations
- Industry standard is 4 decimals for financial systems

### Alternative 2: Store as BIGINT (Cents)

Store amounts as integer cents/grosz (multiply by 100).

**Rejected**:
- Requires application-level conversion everywhere
- Loses semantic meaning (not obviously money)
- Still needs 4 decimals for calculations → BIGINT of ten-thousandths?
- More complex than DECIMAL
- No advantage in PostgreSQL (DECIMAL is efficient)

### Alternative 3: DECIMAL(19, 6) or DECIMAL(19, 10)

Use even more decimal places.

**Rejected**:
- 4 decimals sufficient for all known use cases
- Extra precision adds storage overhead
- No broker supports >8 decimal quantity precision
- Over-engineering

### Alternative 4: FLOAT/DOUBLE

Use floating-point types.

**Rejected**:
- ❌ NEVER use floating-point for money
- Rounding errors accumulate
- Not exact decimal arithmetic
- Fails financial accuracy requirements

### Alternative 5: Separate Currency Table

Create `currencies` table with conversion rates.

**Deferred to v2.0**:
- Not needed for single-currency v0.1
- Will add when multi-currency support required
- Simpler MVP without foreign key to currency table

## Implementation Examples

### PostgreSQL

```sql
-- Money amounts
CREATE TABLE positions (
    avg_cost_basis_amount   DECIMAL(19, 4) NOT NULL,
    avg_cost_basis_currency VARCHAR(3) NOT NULL DEFAULT 'PLN',
    total_quantity          DECIMAL(19, 8) NOT NULL,

    CONSTRAINT positions_cost_positive CHECK (avg_cost_basis_amount > 0),
    CONSTRAINT positions_quantity_positive CHECK (total_quantity > 0)
);
```

### Java Domain Model

```java
package com.investments.tracker.domain.model.value;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record Money(BigDecimal amount, String currency) {

    // Canonical constructor with validation
    public Money {
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }
        if (amount.scale() > 4) {
            throw new IllegalArgumentException("Amount cannot exceed 4 decimal places");
        }
        if (!"PLN".equals(currency)) {
            throw new IllegalArgumentException("Only PLN supported in v0.1");
        }
    }

    // Factory methods
    public static Money pln(String amount) {
        return new Money(new BigDecimal(amount), "PLN");
    }

    public static Money pln(BigDecimal amount) {
        return new Money(amount.setScale(4, RoundingMode.HALF_EVEN), "PLN");
    }

    // Operations (return new instances - immutable)
    public Money add(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot add different currencies");
        }
        return new Money(this.amount.add(other.amount), this.currency);
    }

    public Money subtract(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot subtract different currencies");
        }
        return new Money(this.amount.subtract(other.amount), this.currency);
    }

    public Money multiply(BigDecimal factor) {
        return new Money(
            this.amount.multiply(factor).setScale(4, RoundingMode.HALF_EVEN),
            this.currency
        );
    }

    public Money divide(BigDecimal divisor) {
        return new Money(
            this.amount.divide(divisor, 4, RoundingMode.HALF_EVEN),
            this.currency
        );
    }

    // Display formatting (2 decimals for PLN)
    public String formatForDisplay() {
        return amount.setScale(2, RoundingMode.HALF_EVEN) + " " + currency;
    }
}
```

### JPA Embeddable

```java
@Embeddable
public record Money(
    @Column(name = "amount", precision = 19, scale = 4, nullable = false)
    BigDecimal amount,

    @Column(name = "currency", length = 3, nullable = false)
    String currency
) {
    // Same validation and operations as above
}
```

### Usage in Position Entity

```java
@Entity
@Table(name = "positions")
public class Position {
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "avg_cost_basis_amount")),
        @AttributeOverride(name = "currency", column = @Column(name = "avg_cost_basis_currency"))
    })
    private Money avgCostBasis;

    @Column(name = "total_quantity", precision = 19, scale = 8, nullable = false)
    private BigDecimal totalQuantity;  // Using BigDecimal directly for quantity

    // Calculated methods
    public Money calculateInvestedAmount() {
        return avgCostBasis.multiply(totalQuantity);
    }
}
```

## Related Decisions

- [ADR-002: Value Objects and Entities](ADR-002-value-objects-and-entities.md) - Money as value object
- [ADR-005: Database Schema](ADR-005-database-schema.md) - Uses DECIMAL(19,4) in tables

## References

- [PostgreSQL NUMERIC/DECIMAL Documentation](https://www.postgresql.org/docs/current/datatype-numeric.html)
- [Java BigDecimal](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/math/BigDecimal.html)
- [ISO 4217 Currency Codes](https://en.wikipedia.org/wiki/ISO_4217)
- Martin Fowler - Patterns of Enterprise Application Architecture (Money pattern)
- NFR-089: Single currency (PLN) in v0.1
