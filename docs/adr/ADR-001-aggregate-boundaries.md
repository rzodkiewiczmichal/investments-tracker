# ADR-001: Aggregate Boundaries

## Status
Accepted

## Context

The Investment Tracker application manages positions across multiple brokerage accounts with complex aggregation logic. We need to define clear aggregate boundaries to:

1. Protect business invariants (weighted average cost, position aggregation)
2. Define transaction boundaries for data consistency
3. Support concurrent updates without conflicts
4. Enable independent testing and evolution of domain concepts

Key domain concepts identified:
- **Position**: Aggregated view of same instrument across all accounts (FR-005, FR-093)
- **AccountHolding**: Individual holding of instrument in specific account
- **Portfolio**: Complete view of all positions with metrics (FR-001)
- **Account**: Brokerage account metadata
- **Instrument**: Financial security (stock, ETF, bond)

Key business rules:
- Same instrument across multiple accounts = single aggregated position
- Weighted average cost calculation: `(qty1 × cost1 + qty2 × cost2) / (qty1 + qty2)`
- Portfolio metrics = aggregation of all position metrics
- All quantities must be > 0, all costs must be > 0

## Decision

We define the following aggregate roots and boundaries:

### 1. Position Aggregate (Root: Position)

**Aggregate Root**: Position entity

**Contains**:
- Position (root entity)
- List&lt;AccountHolding&gt; (child entities within aggregate)

**Identity**: InstrumentSymbol (natural key - ticker or ISIN)

**Invariants Protected**:
- Total quantity equals sum of all AccountHolding quantities
- Weighted average cost basis correctly calculated from all holdings
- All quantities must be > 0
- All account holdings reference valid accounts
- No duplicate account holdings for same account

**Transaction Boundary**: One Position aggregate = one database transaction

**Rationale**:
- Position represents "all holdings of specific instrument across accounts"
- AccountHoldings have no meaning outside their Position context
- Aggregation logic (weighted average) is complex invariant requiring protection
- Aligns with ubiquitous language: "Apple position" not "Apple holdings"
- Bounded to manageable size (max 6 accounts per requirements)

### 2. Portfolio Aggregate (Root: Portfolio)

**Aggregate Root**: Portfolio entity

**Contains**:
- Portfolio (root entity)
- References to Positions (by InstrumentSymbol, NOT contained)

**Identity**: Singleton in v0.1 (single user), UserId in future versions

**Invariants Protected**:
- Portfolio metrics (total value, P&L, XIRR) consistent with referenced positions
- All referenced positions exist
- XIRR calculation validity (v0.3+)

**Transaction Boundary**: Portfolio updates independent of Position updates

**Rationale**:
- Portfolio doesn't "own" positions - it aggregates their metrics
- Positions can be queried/updated independently
- Portfolio-level calculations are derived, not authoritative
- Eventual consistency acceptable for portfolio metrics
- Allows parallel position updates without portfolio lock

### 3. Account Aggregate (Root: Account)

**Aggregate Root**: Account entity

**Contains**:
- Account (root entity only)

**Identity**: AccountId (Long, database-generated sequence)

**Invariants Protected**:
- Account identifier uniqueness
- Broker name is required and non-empty
- Account type is valid enum value

**Transaction Boundary**: One Account = one transaction

**Rationale**:
- Account is reference data with simple invariants
- No complex business logic beyond identity
- Referenced by AccountHolding but doesn't participate in aggregation logic
- Small, focused aggregate supporting multi-account scenarios

### 4. Instrument (NOT an Aggregate - Reference Data)

**Classification**: Shared entity referenced by ID

**Identity**: InstrumentSymbol (String)

**Rationale**:
- Instrument is reference/lookup data, not domain aggregate
- Current price is external data managed separately
- No domain logic specific to instrument management in MVP
- Price updates affect ALL positions simultaneously - not owned by any aggregate
- Positions reference by InstrumentSymbol value object (immutable reference)
- Simplifies consistency model

## Consequences

### Positive

1. **Clear Transaction Boundaries**: One Position = one transaction, excellent for concurrency
2. **Strong Invariants**: Weighted average cost and quantity aggregation protected at aggregate boundary
3. **Independent Updates**: Different positions can be updated in parallel without conflicts
4. **Natural Fit with Use Cases**: Import process creates/updates positions atomically
5. **Scalable Design**: Limited number of AccountHoldings per Position (6 accounts max)
6. **Testable**: Each aggregate can be tested independently with clear responsibilities
7. **Future-Proof**: Design supports v0.2 multi-account scenarios from the start

### Negative

1. **Eventual Consistency for Portfolio**: Portfolio-level invariants are weaker (derived metrics)
2. **Cannot Enforce Cross-Position Rules**: Portfolio total ≠ sum of positions not enforced in single transaction
3. **Multiple Transactions for Batch Import**: Importing multiple positions requires multiple transactions
4. **Position Growth**: Position aggregate grows with number of accounts (mitigated by 6 account limit)

### Mitigation Strategies

1. **Accept Eventual Consistency**: Portfolio metrics are derived, not authoritative - eventual consistency is acceptable
2. **Domain Events**: Use PositionUpdated events for portfolio recalculation if needed
3. **Optimistic Locking**: Use version field on Position to prevent concurrent modification conflicts
4. **Batch Import as Application Service**: Application layer orchestrates multi-position import with proper error handling

### Implementation Guidelines

1. **Position Aggregate**:
   - Position.addAccountHolding(AccountHolding) recalculates weighted average cost
   - Position.removeAccountHolding(AccountId) recalculates weighted average cost
   - Use PositionAggregationService for complex weighted average calculation
   - JPA: Position as root entity with @OneToMany AccountHoldings

2. **Portfolio Aggregate**:
   - Portfolio doesn't persist references - queries positions dynamically
   - PortfolioCalculationService computes metrics from all positions
   - Consider Portfolio as read model / projection

3. **Account Aggregate**:
   - Simple CRUD operations
   - Validate before creating AccountHolding reference

4. **Instrument Reference Data**:
   - Manage in separate table
   - Position references by InstrumentSymbol value object
   - Consider caching for performance

### Alternatives Considered

**Alternative 1: Portfolio as God Aggregate (containing all Positions)**
- **Rejected**: Massive aggregate with poor concurrency, single transaction bottleneck
- Violates "keep aggregates small" DDD principle
- Impossible to parallelize position imports

**Alternative 2: AccountHolding as Aggregate Root**
- **Rejected**: Creates anemic Position model, violates ubiquitous language
- Position aggregation logic would live in application service
- Cannot protect weighted average cost invariant

**Alternative 3: Instrument as Aggregate Root**
- **Rejected**: Reference data without complex invariants, unnecessary complexity
- All positions would depend on single aggregate = bottleneck

## Related Decisions

- [ADR-002: Value Objects and Entities](ADR-002-value-objects-and-entities.md) - Defines identity strategy for aggregates
- [ADR-003: Domain vs Application Services](ADR-003-domain-vs-application-services.md) - Defines service responsibilities
- [ADR-004: Package Structure](ADR-004-package-structure.md) - Physical organization of aggregates

## References

- Domain-Driven Design by Eric Evans (Aggregates chapter)
- Implementing Domain-Driven Design by Vaughn Vernon (Aggregate design rules)
- FR-005: Multi-account position aggregation
- FR-093: Aggregated position view
- FR-085: Weighted average cost calculation
- requirements/functional/ubiquitous-language.md
