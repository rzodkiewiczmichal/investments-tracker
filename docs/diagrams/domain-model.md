# Domain Model Diagram

Simple visual representation of the Investment Tracker domain model.

```mermaid
classDiagram
    %% Entities (Aggregate Roots)
    class Position {
        <<Aggregate Root>>
        InstrumentSymbol symbol
        List~AccountHolding~ holdings
    }

    class Portfolio {
        <<Aggregate Root>>
        List~InstrumentSymbol~ positionRefs
    }

    class Account {
        <<Aggregate Root>>
        Long id
        String name
        String brokerName
    }

    class Instrument {
        <<Entity>>
        InstrumentSymbol symbol
        String name
        Price currentPrice
    }

    class AccountHolding {
        <<Entity>>
        AccountId accountId
        Quantity quantity
        CostBasis costBasis
    }

    %% Value Objects
    class Money {
        <<Value Object>>
        BigDecimal amount
        Currency currency
    }

    class Quantity {
        <<Value Object>>
        BigDecimal value
    }

    class Price {
        <<Value Object>>
        Money amount
    }

    class CostBasis {
        <<Value Object>>
        Money amount
    }

    class ProfitAndLoss {
        <<Value Object>>
        Money amount
        Percentage percentage
    }

    class InstrumentSymbol {
        <<Value Object>>
        String value
    }

    %% Relationships
    Position "1" *-- "1..*" AccountHolding : contains
    Portfolio "1" o-- "0..*" Position : references
    AccountHolding --> Account : belongs to
    Position --> Instrument : tracks

    AccountHolding --> Quantity
    AccountHolding --> CostBasis
    Instrument --> InstrumentSymbol
    Instrument --> Price

    Price --> Money
    CostBasis --> Money

    %% Styling
    style Position fill:#e1f5ff
    style Portfolio fill:#e1f5ff
    style Account fill:#e1f5ff
    style Money fill:#fff4e6
    style Quantity fill:#fff4e6
    style Price fill:#fff4e6
    style CostBasis fill:#fff4e6
    style ProfitAndLoss fill:#fff4e6
    style InstrumentSymbol fill:#fff4e6
```

**Legend**:
- 🔵 Blue = Entities/Aggregate Roots
- 🟡 Yellow = Value Objects
- Solid line with filled diamond (`*--`) = Composition (owns)
- Solid line with empty diamond (`o--`) = Aggregation (references)
- Dashed arrow (`-->`) = Uses/depends on

**Key Points**:
- **Position** is aggregate root containing **AccountHoldings**
- **Portfolio** references positions by InstrumentSymbol
- **Account** is simple aggregate root
- **Instrument** is reference data (not an aggregate)
- All financial amounts use **Money** value object
- All value objects are immutable

---

**Related Documentation**:
- [ADR-001: Aggregate Boundaries](../adr/ADR-001-aggregate-boundaries.md)
- [ADR-002: Value Objects and Entities](../adr/ADR-002-value-objects-and-entities.md)
