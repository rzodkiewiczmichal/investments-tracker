-- ============================================================================
-- INVESTMENT TRACKER - INITIAL SCHEMA
-- Version: V1
-- Database: PostgreSQL 15+
-- Description: Create core tables for Position, Account, Instrument aggregates
-- ============================================================================

-- Enable UUID extension for potential future use
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================================================
-- TABLE: accounts
-- Aggregate Root: Account
-- Identity: BIGSERIAL (surrogate key)
-- ============================================================================

CREATE TABLE accounts (
    id                  BIGSERIAL PRIMARY KEY,
    name                VARCHAR(255) NOT NULL,
    broker_name         VARCHAR(255) NOT NULL,
    account_type        VARCHAR(50) NOT NULL CHECK (account_type IN ('IKE', 'IKZE', 'NORMAL')),
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version             BIGINT NOT NULL DEFAULT 0,  -- Optimistic locking (JPA @Version)

    CONSTRAINT accounts_name_not_empty CHECK (TRIM(name) <> ''),
    CONSTRAINT accounts_broker_not_empty CHECK (TRIM(broker_name) <> '')
);

CREATE INDEX idx_accounts_broker_name ON accounts(broker_name);
CREATE INDEX idx_accounts_created_at ON accounts(created_at);

COMMENT ON TABLE accounts IS 'Brokerage accounts holding investments (Aggregate Root)';
COMMENT ON COLUMN accounts.id IS 'Surrogate key (Long, sequence-based)';
COMMENT ON COLUMN accounts.name IS 'User-defined account name';
COMMENT ON COLUMN accounts.broker_name IS 'Name of the brokerage firm (e.g., mBank, XTB, Revolut)';
COMMENT ON COLUMN accounts.account_type IS 'IKE (tax-advantaged retirement), IKZE (tax-deductible retirement), or NORMAL (regular account)';
COMMENT ON COLUMN accounts.version IS 'Optimistic locking version (JPA @Version)';

-- ============================================================================
-- TABLE: instruments
-- Entity: Instrument (Reference Data)
-- Identity: symbol (natural key - ticker or ISIN)
-- ============================================================================

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

CREATE INDEX idx_instruments_type ON instruments(instrument_type);
CREATE INDEX idx_instruments_price_updated ON instruments(price_updated_at);

COMMENT ON TABLE instruments IS 'Financial instruments (stocks, ETFs, bonds) - Reference data';
COMMENT ON COLUMN instruments.symbol IS 'Natural key: Ticker symbol (e.g., AAPL) or ISIN (e.g., US0378331005)';
COMMENT ON COLUMN instruments.instrument_type IS 'STOCK, ETF, BOND_ETF, or POLISH_GOV_BOND';
COMMENT ON COLUMN instruments.current_price_amount IS 'Current price per unit in currency (nullable - may not be set initially)';
COMMENT ON COLUMN instruments.current_price_currency IS 'Currency of price (always PLN in v0.1)';
COMMENT ON COLUMN instruments.price_updated_at IS 'Timestamp when price was last fetched from external source';

-- ============================================================================
-- TABLE: positions
-- Aggregate Root: Position
-- Identity: instrument_symbol (natural key)
-- Contains: List of AccountHoldings (one-to-many relationship)
-- ============================================================================

CREATE TABLE positions (
    instrument_symbol       VARCHAR(50) PRIMARY KEY,
    total_quantity          DECIMAL(19, 8) NOT NULL,
    avg_cost_basis_amount   DECIMAL(19, 4) NOT NULL,
    avg_cost_basis_currency VARCHAR(3) NOT NULL DEFAULT 'PLN',
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version                 BIGINT NOT NULL DEFAULT 0,  -- Optimistic locking (JPA @Version)

    CONSTRAINT positions_quantity_positive CHECK (total_quantity > 0),
    CONSTRAINT positions_cost_positive CHECK (avg_cost_basis_amount > 0),
    CONSTRAINT fk_positions_instrument FOREIGN KEY (instrument_symbol)
        REFERENCES instruments(symbol) ON DELETE RESTRICT
);

CREATE INDEX idx_positions_updated_at ON positions(updated_at);
CREATE INDEX idx_positions_created_at ON positions(created_at);

COMMENT ON TABLE positions IS 'Aggregated positions across all accounts (Aggregate Root)';
COMMENT ON COLUMN positions.instrument_symbol IS 'Natural key - references instruments.symbol';
COMMENT ON COLUMN positions.total_quantity IS 'Sum of all account holdings quantities (8 decimal precision for fractional shares)';
COMMENT ON COLUMN positions.avg_cost_basis_amount IS 'Weighted average cost per unit across all holdings (4 decimal precision)';
COMMENT ON COLUMN positions.avg_cost_basis_currency IS 'Currency of cost basis (always PLN in v0.1)';
COMMENT ON COLUMN positions.version IS 'Optimistic locking version (JPA @Version)';

-- ============================================================================
-- TABLE: account_holdings
-- Entity: AccountHolding (child entity within Position aggregate)
-- Identity: Composite key (instrument_symbol + account_id)
-- ============================================================================

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

CREATE INDEX idx_account_holdings_account ON account_holdings(account_id);
CREATE INDEX idx_account_holdings_instrument ON account_holdings(instrument_symbol);

COMMENT ON TABLE account_holdings IS 'Individual holdings within Position aggregate (composite key)';
COMMENT ON COLUMN account_holdings.instrument_symbol IS 'Part of composite key, references position';
COMMENT ON COLUMN account_holdings.account_id IS 'Part of composite key, references account';
COMMENT ON COLUMN account_holdings.quantity IS 'Number of shares/units in this specific account (8 decimal precision)';
COMMENT ON COLUMN account_holdings.cost_basis_amount IS 'Average cost per unit for this holding (4 decimal precision)';
COMMENT ON COLUMN account_holdings.cost_basis_currency IS 'Currency of cost basis (always PLN in v0.1)';

-- ============================================================================
-- TABLE: audit_log
-- Audit trail for all data changes (NFR-028: Complete audit trail)
-- No foreign keys - stores entity type and ID as strings for flexibility
-- ============================================================================

CREATE TABLE audit_log (
    id                  BIGSERIAL PRIMARY KEY,
    entity_type         VARCHAR(100) NOT NULL,
    entity_id           VARCHAR(255) NOT NULL,
    operation           VARCHAR(20) NOT NULL CHECK (operation IN ('INSERT', 'UPDATE', 'DELETE')),
    changed_by          VARCHAR(100) DEFAULT 'system',  -- Future: actual user identification
    changed_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    change_details      JSONB NOT NULL,

    CONSTRAINT audit_log_entity_type_not_empty CHECK (TRIM(entity_type) <> ''),
    CONSTRAINT audit_log_entity_id_not_empty CHECK (TRIM(entity_id) <> '')
);

CREATE INDEX idx_audit_log_entity ON audit_log(entity_type, entity_id);
CREATE INDEX idx_audit_log_changed_at ON audit_log(changed_at DESC);
CREATE INDEX idx_audit_log_operation ON audit_log(operation);
CREATE INDEX idx_audit_log_details ON audit_log USING GIN (change_details);

COMMENT ON TABLE audit_log IS 'Complete audit trail of all data changes (NFR-028)';
COMMENT ON COLUMN audit_log.entity_type IS 'Type of entity changed: Position, Account, Instrument, AccountHolding';
COMMENT ON COLUMN audit_log.entity_id IS 'ID of the changed entity (string to support both Long and String IDs)';
COMMENT ON COLUMN audit_log.operation IS 'Type of operation: INSERT, UPDATE, DELETE';
COMMENT ON COLUMN audit_log.changed_by IS 'User or system that made the change (default: system)';
COMMENT ON COLUMN audit_log.change_details IS 'JSONB with before/after values, reason, source, etc.';

-- ============================================================================
-- TRIGGERS: Update timestamps automatically
-- ============================================================================

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

COMMENT ON FUNCTION update_updated_at_column() IS 'Automatically update updated_at timestamp on row update';

-- ============================================================================
-- SCHEMA VALIDATION QUERIES (for testing)
-- ============================================================================

-- Verify all tables exist
DO $$
BEGIN
    ASSERT (SELECT COUNT(*) FROM information_schema.tables
            WHERE table_schema = 'public'
            AND table_name IN ('accounts', 'instruments', 'positions', 'account_holdings', 'audit_log')) = 5,
           'Expected 5 tables to be created';
END $$;

-- ============================================================================
-- END OF MIGRATION V1
-- ============================================================================
