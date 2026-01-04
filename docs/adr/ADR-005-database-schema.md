# ADR-005: Database Schema Design

## Status
Accepted

## Context

The Investment Tracker requires a PostgreSQL database schema that:

1. **Aligns with Domain Model**: Tables must map to aggregates defined in ADR-001
2. **Supports Aggregate Boundaries**: Position aggregate owns AccountHoldings (composition)
3. **Enables Multi-Account**: Position aggregates holdings from multiple accounts
4. **Maintains Referential Integrity**: Foreign keys with appropriate CASCADE/RESTRICT
5. **Supports Audit Trail**: Complete change tracking (NFR-028)
6. **Enables ACID Transactions**: Clear transaction boundaries (NFR-035)

### Domain Model from ADRs

From ADR-001 (Aggregate Boundaries):
- **Position Aggregate**: Root (Position) + Children (List<AccountHolding>)
- **Account Aggregate**: Simple aggregate with Long id
- **Instrument**: Reference data entity with InstrumentSymbol natural key
- **Portfolio**: Query-based aggregate (no table - derived from positions)

From ADR-002 (Value Objects and Entities):
- **Natural Keys**: Position uses InstrumentSymbol
- **Surrogate Keys**: Account uses Long (BIGSERIAL)
- **Composite Keys**: AccountHolding uses (InstrumentSymbol + AccountId)
- **Value Objects**: Money, Quantity, Price, CostBasis (embedded in entities)

### Requirements

- **v0.1 MVP Scope**: FR-001 to FR-020 (manual entry, portfolio viewing, position details)
- **Audit Trail**: NFR-028 requires complete audit trail of all changes
- **ACID Compliance**: NFR-035 requires transactional consistency
- **Read-Heavy Workload**: Portfolio viewing is primary operation
- **Multi-Account Support**: Same instrument across accounts = single aggregated position

## Decision

### Table Design

We create **5 core tables** that directly map to domain aggregates and entities:

#### 1. accounts (Account Aggregate Root)

```sql
CREATE TABLE accounts (
    id                  BIGSERIAL PRIMARY KEY,
    name                VARCHAR(255) NOT NULL,
    broker_name         VARCHAR(255) NOT NULL,
    account_type        VARCHAR(50) NOT NULL CHECK (account_type IN ('IKE', 'IKZE', 'NORMAL')),
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version             BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT accounts_name_not_empty CHECK (TRIM(name) <> ''),
    CONSTRAINT accounts_broker_not_empty CHECK (TRIM(broker_name) <> '')
);
```

**Rationale**:
- `BIGSERIAL id`: Surrogate key aligns with ADR-002
- `version`: Optimistic locking for JPA @Version
- `account_type`: Enum stored as VARCHAR for type safety
- Constraints prevent empty strings (business rule enforcement)

#### 2. instruments (Reference Data Entity)

```sql
CREATE TABLE instruments (
    symbol              VARCHAR(50) PRIMARY KEY,
    name                VARCHAR(255) NOT NULL,
    instrument_type     VARCHAR(50) NOT NULL CHECK (instrument_type IN ('STOCK', 'ETF', 'BOND_ETF', 'POLISH_GOV_BOND')),
    current_price_amount    DECIMAL(19, 4),
    current_price_currency  VARCHAR(3) DEFAULT 'PLN',
    price_updated_at        TIMESTAMP,
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT instruments_symbol_not_empty CHECK (TRIM(symbol) <> ''),
    CONSTRAINT instruments_name_not_empty CHECK (TRIM(name) <> ''),
    CONSTRAINT instruments_price_positive CHECK (current_price_amount IS NULL OR current_price_amount > 0)
);
```

**Rationale**:
- `VARCHAR(50) symbol` PRIMARY KEY: Natural key, supports tickers (10 chars) and ISINs (12 chars)
- `current_price_amount`: Nullable - may not be set initially
- `DECIMAL(19, 4)`: Money precision decision (see ADR-006)
- `price_updated_at`: Track staleness for price refresh

#### 3. positions (Position Aggregate Root)

```sql
CREATE TABLE positions (
    instrument_symbol       VARCHAR(50) PRIMARY KEY,
    total_quantity          DECIMAL(19, 8) NOT NULL,
    avg_cost_basis_amount   DECIMAL(19, 4) NOT NULL,
    avg_cost_basis_currency VARCHAR(3) NOT NULL DEFAULT 'PLN',
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version                 BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT positions_quantity_positive CHECK (total_quantity > 0),
    CONSTRAINT positions_cost_positive CHECK (avg_cost_basis_amount > 0),
    CONSTRAINT fk_positions_instrument FOREIGN KEY (instrument_symbol)
        REFERENCES instruments(symbol) ON DELETE RESTRICT
);
```

**Rationale**:
- `VARCHAR(50) instrument_symbol` PRIMARY KEY: Natural key aligns with ADR-002
- `total_quantity DECIMAL(19, 8)`: Sum of all account holdings (8 decimals for fractional shares)
- `avg_cost_basis_amount DECIMAL(19, 4)`: Weighted average cost across all holdings
- `version`: Optimistic locking to prevent concurrent modification
- `ON DELETE RESTRICT`: Cannot delete instrument if positions exist (safety check)

#### 4. account_holdings (Entity within Position Aggregate)

```sql
CREATE TABLE account_holdings (
    instrument_symbol   VARCHAR(50) NOT NULL,
    account_id          BIGINT NOT NULL,
    quantity            DECIMAL(19, 8) NOT NULL,
    cost_basis_amount   DECIMAL(19, 4) NOT NULL,
    cost_basis_currency VARCHAR(3) NOT NULL DEFAULT 'PLN',
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (instrument_symbol, account_id),

    CONSTRAINT account_holdings_quantity_positive CHECK (quantity > 0),
    CONSTRAINT account_holdings_cost_positive CHECK (cost_basis_amount > 0),
    CONSTRAINT fk_account_holdings_position FOREIGN KEY (instrument_symbol)
        REFERENCES positions(instrument_symbol) ON DELETE CASCADE,
    CONSTRAINT fk_account_holdings_account FOREIGN KEY (account_id)
        REFERENCES accounts(id) ON DELETE RESTRICT
);
```

**Rationale**:
- **Composite PRIMARY KEY** (instrument_symbol, account_id): Natural identifier for holding
- `ON DELETE CASCADE` on position: Child entities deleted with aggregate root (aggregate boundary)
- `ON DELETE RESTRICT` on account: Safety check - cannot delete account with active holdings
- Quantity and cost constraints enforce business rules at database level

#### 5. audit_log (Audit Trail)

```sql
CREATE TABLE audit_log (
    id                  BIGSERIAL PRIMARY KEY,
    entity_type         VARCHAR(100) NOT NULL,
    entity_id           VARCHAR(255) NOT NULL,
    operation           VARCHAR(20) NOT NULL CHECK (operation IN ('INSERT', 'UPDATE', 'DELETE')),
    changed_by          VARCHAR(100) DEFAULT 'system',
    changed_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    change_details      JSONB NOT NULL,

    CONSTRAINT audit_log_entity_type_not_empty CHECK (TRIM(entity_type) <> ''),
    CONSTRAINT audit_log_entity_id_not_empty CHECK (TRIM(entity_id) <> '')
);
```

**Rationale**:
- `JSONB change_details`: Flexible structure for before/after values
- `entity_type`: 'Position', 'Account', 'Instrument', 'AccountHolding'
- `entity_id VARCHAR(255)`: Supports both Long (accounts) and String (positions) identifiers
- Application-level auditing (not database triggers) for better hexagonal architecture alignment

### Relationships and Cascade Strategy

```
instruments (reference data)
    ↑ RESTRICT
positions (aggregate root)
    ↑ CASCADE        ↑ RESTRICT
account_holdings     accounts
```

**Cascade Rules**:
1. **Delete Position → CASCADE to AccountHoldings**: Aggregate boundary - children deleted with root
2. **Delete Account → RESTRICT if Holdings exist**: Safety check - prevent accidental account deletion
3. **Delete Instrument → RESTRICT if Positions exist**: Safety check - prevent orphaned positions

### Index Strategy

#### Primary Keys (Automatic Indexes)
- `accounts(id)` - Clustered B-tree index
- `instruments(symbol)` - Clustered B-tree index
- `positions(instrument_symbol)` - Clustered B-tree index
- `account_holdings(instrument_symbol, account_id)` - Composite clustered B-tree index

#### Foreign Key Indexes

```sql
CREATE INDEX idx_account_holdings_account ON account_holdings(account_id);
CREATE INDEX idx_account_holdings_instrument ON account_holdings(instrument_symbol);
CREATE INDEX idx_accounts_broker_name ON accounts(broker_name);
```

**Rationale**:
- `account_holdings(account_id)`: For "view all holdings in account X" queries
- `account_holdings(instrument_symbol)`: For loading Position aggregate with all holdings
- `accounts(broker_name)`: For filtering accounts by broker

#### Query Performance Indexes

```sql
CREATE INDEX idx_instruments_type ON instruments(instrument_type);
CREATE INDEX idx_instruments_price_updated ON instruments(price_updated_at);
CREATE INDEX idx_positions_updated_at ON positions(updated_at);
CREATE INDEX idx_positions_created_at ON positions(created_at);
```

**Rationale**:
- `instruments(instrument_type)`: Filter by STOCK, ETF, BOND_ETF
- `instruments(price_updated_at)`: Detect stale prices needing refresh
- `positions(updated_at)`: Recent position changes
- `positions(created_at)`: Newly added positions

#### Audit Log Indexes

```sql
CREATE INDEX idx_audit_log_entity ON audit_log(entity_type, entity_id);
CREATE INDEX idx_audit_log_changed_at ON audit_log(changed_at DESC);
CREATE INDEX idx_audit_log_operation ON audit_log(operation);
CREATE INDEX idx_audit_log_details ON audit_log USING GIN (change_details);
```

**Rationale**:
- `(entity_type, entity_id)`: Find all changes to specific entity
- `changed_at DESC`: Recent changes first (time-based queries)
- `operation`: Filter by INSERT/UPDATE/DELETE
- `GIN (change_details)`: JSONB queries (e.g., find price changes > 10%)

### Timestamps and Triggers

```sql
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_accounts_updated_at BEFORE UPDATE ON accounts
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_instruments_updated_at BEFORE UPDATE ON instruments
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_positions_updated_at BEFORE UPDATE ON positions
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_account_holdings_updated_at BEFORE UPDATE ON account_holdings
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
```

**Rationale**:
- Automatic `updated_at` maintenance
- No application code needed to track updates
- Consistent timestamp semantics

### Soft Delete Decision

**Decision**: NO soft delete for v0.1 MVP

**Rationale**:
1. **Requirements**: FR-092 specifies "current position tracking only" - no historical positions
2. **Audit Sufficient**: `audit_log` captures all deletions with full before/after state
3. **Simpler Queries**: No `WHERE deleted_at IS NULL` on every query
4. **ACID Compliance**: Hard delete with FK cascades maintains referential integrity
5. **Performance**: Smaller indexes, fewer rows to scan

**Future Consideration**: If historical tracking is needed in v2.0, add `closed_at TIMESTAMP` to positions and use partial indexes.

### Data Type Choices

| Column Purpose | Type | Rationale |
|---------------|------|-----------|
| Money Amount | `DECIMAL(19, 4)` | See ADR-006 - exact decimal arithmetic |
| Quantity | `DECIMAL(19, 8)` | 8 decimals support fractional shares |
| Currency | `VARCHAR(3)` | ISO 4217 code (PLN) |
| Instrument Symbol | `VARCHAR(50)` | Tickers (≤10) + ISINs (12) + buffer |
| Account ID | `BIGSERIAL` | Auto-increment surrogate key |
| Timestamps | `TIMESTAMP` | Local time (no timezone needed in v0.1) |
| Enums | `VARCHAR(50)` | Type-safe with CHECK constraints |
| Audit Details | `JSONB` | Flexible, indexable JSON structure |

## Consequences

### Positive

1. **Strong Domain Alignment**: Tables directly map to aggregates from ADR-001
2. **Referential Integrity**: Foreign keys with CASCADE/RESTRICT enforce aggregate boundaries
3. **Business Rule Enforcement**: CHECK constraints prevent invalid data at database level
4. **Read Performance**: Indexes support read-heavy portfolio viewing workload
5. **Audit Compliance**: Complete audit trail meets NFR-028
6. **Natural Keys**: InstrumentSymbol as Position PK aligns with ubiquitous language
7. **Optimistic Locking**: Version columns prevent lost updates
8. **Type Safety**: Enums in CHECK constraints prevent invalid values
9. **Flexibility**: JSONB audit log allows evolution without schema changes

### Negative

1. **No Soft Delete**: Deleted positions cannot be undeleted (mitigated by audit log)
2. **Natural Key Limitation**: If InstrumentSymbol changes, Position must be recreated
3. **No Timezone Support**: TIMESTAMP without timezone limits multi-timezone deployments (v2.0 concern)
4. **Currency Column Overhead**: VARCHAR(3) for always-PLN adds storage (mitigated - prepares for v2.0)
5. **JSONB Size**: Audit log can grow large (mitigated by archiving strategy in future)

### Mitigation Strategies

1. **Natural Key Changes**: Very rare for ISIN/ticker symbols; can handle via Position recreation
2. **Audit Log Growth**: Plan archiving/partitioning for v2.0+ when log is large
3. **Timezone Support**: Use TIMESTAMPTZ in v2.0 for cloud deployment
4. **Soft Delete**: Can add `closed_at` column if historical tracking needed

## Alternatives Considered

### Alternative 1: Denormalized Single Table for Positions

Store Position + all AccountHoldings in single table with JSONB array.

**Rejected**:
- Violates aggregate boundary (AccountHolding is entity, not value object)
- Cannot enforce FK constraints on account_id
- Harder to query individual holdings
- Loses type safety for quantity/cost

### Alternative 2: Soft Delete with deleted_at Column

Add `deleted_at TIMESTAMP` to positions and account_holdings.

**Rejected for v0.1**:
- Adds complexity to every query
- Current positions only in MVP scope
- Audit log sufficient for historical record
- Can add in v2.0 if needed

### Alternative 3: UUID for All Primary Keys

Use `UUID` instead of `BIGSERIAL` for accounts and `VARCHAR` for positions.

**Rejected for v0.1**:
- Single-instance deployment doesn't need distributed IDs
- BIGSERIAL simpler and more readable
- Can migrate to UUID in v2.0 for cloud deployment

### Alternative 4: Separate Transaction Table

Create `transactions` table for buy/sell history.

**Deferred to v0.3**:
- Not needed for v0.1 MVP (manual entry only)
- Will add when XIRR calculation requires transaction history
- Simpler schema for MVP

## Related Decisions

- [ADR-001: Aggregate Boundaries](ADR-001-aggregate-boundaries.md) - Defines Position/AccountHolding relationship
- [ADR-002: Value Objects and Entities](ADR-002-value-objects-and-entities.md) - Defines identity strategy
- [ADR-006: Money Representation](ADR-006-money-representation.md) - DECIMAL precision rationale

## Implementation Notes

### JPA Entity Mapping

Position aggregate root:
```java
@Entity
@Table(name = "positions")
public class Position {
    @EmbeddedId
    private InstrumentSymbol symbol;  // Natural key as embedded value object

    @Column(name = "total_quantity", precision = 19, scale = 8, nullable = false)
    private BigDecimal totalQuantity;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "instrument_symbol")
    private List<AccountHolding> holdings = new ArrayList<>();

    @Version
    private Long version;
}
```

### Migration Strategy (Flyway)

File: `src/main/resources/db/migration/V1__create_schema.sql`

Contains all CREATE TABLE, CREATE INDEX, CREATE TRIGGER statements. See actual migration file for complete SQL.

## References

- PostgreSQL DECIMAL Documentation
- Domain-Driven Design by Eric Evans
- Implementing Domain-Driven Design by Vaughn Vernon
- NFR-028: Complete audit trail
- NFR-035: ACID transaction compliance
- NFR-036: PostgreSQL database
- FR-092: Current position tracking only
