-- ============================================================================
-- MIGRATION V13: Persistent broker instrument mappings
-- Description: Stores resolved broker instrument name → catalog symbol
--              mappings for reuse across future imports.
--              Composite primary key: (broker, broker_instrument_name)
-- ============================================================================

CREATE TABLE broker_instrument_mappings (
    broker                  VARCHAR(50) NOT NULL,
    broker_instrument_name  VARCHAR(100) NOT NULL,
    catalog_symbol          VARCHAR(50) NOT NULL,

    PRIMARY KEY (broker, broker_instrument_name),

    CONSTRAINT fk_bim_catalog_symbol
        FOREIGN KEY (catalog_symbol) REFERENCES instruments(symbol) ON DELETE RESTRICT
);

CREATE INDEX idx_bim_catalog_symbol ON broker_instrument_mappings(catalog_symbol);

COMMENT ON TABLE broker_instrument_mappings IS 'Persistent broker-to-catalog instrument mappings, reused across imports';
COMMENT ON COLUMN broker_instrument_mappings.broker IS 'Broker identifier (e.g., mBank, XTB)';
COMMENT ON COLUMN broker_instrument_mappings.broker_instrument_name IS 'Instrument name as used by the broker';
COMMENT ON COLUMN broker_instrument_mappings.catalog_symbol IS 'Resolved catalog symbol in TICKER.MARKET format';

-- ============================================================================
-- END OF MIGRATION V13
-- ============================================================================
