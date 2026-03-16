-- ============================================================================
-- MIGRATION V14: Rename US instruments to TICKER.US format
-- Description: Aligns US instrument symbols with the TICKER.MARKET convention
--              established in V12. Finnhub-synced instruments stored as bare
--              tickers (MSFT, AAPL) become MSFT.US, AAPL.US.
--              Dotted share-class symbols (BRK.B) are flattened: BRK.B → BRKB.US.
-- ============================================================================

-- Step 1: Drop FK constraints (same pattern as V12)
ALTER TABLE account_holdings DROP CONSTRAINT fk_account_holdings_position;
ALTER TABLE positions DROP CONSTRAINT fk_positions_instrument;

-- Step 2: Delete bare duplicates where the .US version already exists.
-- V12 renamed some instruments (e.g., BABA → BABA.US). Finnhub sync may have
-- re-created bare versions (BABA). Delete the bare duplicate before renaming.
-- First, cascade deletions from child tables.
DELETE FROM account_holdings
WHERE instrument_symbol IN (
    SELECT symbol FROM instruments
    WHERE market = 'US' AND symbol NOT LIKE '%.US'
      AND REPLACE(symbol, '.', '') || '.US' IN (SELECT symbol FROM instruments WHERE market = 'US')
);

DELETE FROM positions
WHERE instrument_symbol IN (
    SELECT symbol FROM instruments
    WHERE market = 'US' AND symbol NOT LIKE '%.US'
      AND REPLACE(symbol, '.', '') || '.US' IN (SELECT symbol FROM instruments WHERE market = 'US')
);

DELETE FROM import_session_mappings
WHERE catalog_symbol IN (
    SELECT symbol FROM instruments
    WHERE market = 'US' AND symbol NOT LIKE '%.US'
      AND REPLACE(symbol, '.', '') || '.US' IN (SELECT symbol FROM instruments WHERE market = 'US')
);

DELETE FROM instruments
WHERE market = 'US' AND symbol NOT LIKE '%.US'
  AND REPLACE(symbol, '.', '') || '.US' IN (SELECT symbol FROM instruments WHERE market = 'US');

-- Step 3: Rename remaining US instruments that don't already have .US suffix.
-- REPLACE(symbol, '.', '') flattens dotted share-class symbols (BRK.B → BRKB).
-- For plain symbols (MSFT), REPLACE is a no-op.

-- Update instruments table
UPDATE instruments
SET symbol = REPLACE(symbol, '.', '') || '.US'
WHERE market = 'US' AND symbol NOT LIKE '%.US';

-- Cascade rename to positions table
UPDATE positions p
SET instrument_symbol = REPLACE(instrument_symbol, '.', '') || '.US'
WHERE EXISTS (
    SELECT 1 FROM instruments i
    WHERE i.symbol = REPLACE(p.instrument_symbol, '.', '') || '.US'
      AND i.market = 'US'
)
AND instrument_symbol NOT LIKE '%.US';

-- Cascade rename to account_holdings table
UPDATE account_holdings ah
SET instrument_symbol = REPLACE(instrument_symbol, '.', '') || '.US'
WHERE EXISTS (
    SELECT 1 FROM instruments i
    WHERE i.symbol = REPLACE(ah.instrument_symbol, '.', '') || '.US'
      AND i.market = 'US'
)
AND instrument_symbol NOT LIKE '%.US';

-- Cascade rename to import_session_mappings table
UPDATE import_session_mappings ism
SET catalog_symbol = REPLACE(catalog_symbol, '.', '') || '.US'
WHERE catalog_symbol IS NOT NULL
  AND catalog_symbol NOT LIKE '%.US'
  AND EXISTS (
    SELECT 1 FROM instruments i
    WHERE i.symbol = REPLACE(ism.catalog_symbol, '.', '') || '.US'
      AND i.market = 'US'
);

-- Step 4: Re-add FK constraints
ALTER TABLE positions
    ADD CONSTRAINT fk_positions_instrument
    FOREIGN KEY (instrument_symbol) REFERENCES instruments(symbol) ON DELETE RESTRICT;

ALTER TABLE account_holdings
    ADD CONSTRAINT fk_account_holdings_position
    FOREIGN KEY (instrument_symbol) REFERENCES positions(instrument_symbol) ON DELETE CASCADE;

-- ============================================================================
-- END OF MIGRATION V14
-- Renamed US instruments to TICKER.US format:
--   - Plain symbols: MSFT → MSFT.US, AAPL → AAPL.US
--   - Dotted share-class symbols: BRK.B → BRKB.US
-- ============================================================================
