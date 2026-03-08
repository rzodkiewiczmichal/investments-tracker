-- Import session tables for broker transaction history import (FR-032)

CREATE TABLE import_sessions (
    id              UUID PRIMARY KEY,
    status          VARCHAR(20) NOT NULL,
    broker          VARCHAR(50) NOT NULL,
    account_name    VARCHAR(100) NOT NULL,
    created_at      TIMESTAMP NOT NULL,
    completed_at    TIMESTAMP,
    version         BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE import_session_transactions (
    id                      BIGSERIAL PRIMARY KEY,
    import_session_id       UUID NOT NULL REFERENCES import_sessions(id) ON DELETE CASCADE,
    broker_instrument_name  VARCHAR(100) NOT NULL,
    transaction_type        VARCHAR(10) NOT NULL,
    quantity                NUMERIC(19, 8) NOT NULL,
    unit_price_amount       NUMERIC(19, 4) NOT NULL,
    unit_price_currency     VARCHAR(3) NOT NULL,
    commission_amount       NUMERIC(19, 4) NOT NULL,
    commission_currency     VARCHAR(3) NOT NULL,
    currency                VARCHAR(3) NOT NULL
);

CREATE TABLE import_session_mappings (
    import_session_id       UUID NOT NULL REFERENCES import_sessions(id) ON DELETE CASCADE,
    broker_instrument_name  VARCHAR(100) NOT NULL,
    catalog_symbol          VARCHAR(20),
    PRIMARY KEY (import_session_id, broker_instrument_name)
);

-- Missing instruments needed for mBank import
INSERT INTO instruments (symbol, name, instrument_type, currency, version) VALUES
    ('ETFBSPXPL', 'Beta ETF S&P500 PLN-Hedged', 'ETF', 'PLN', 0),
    ('DTLE', 'iShares EUR Gov Bond 15-30yr UCITS ETF', 'ETF', 'EUR', 0);
