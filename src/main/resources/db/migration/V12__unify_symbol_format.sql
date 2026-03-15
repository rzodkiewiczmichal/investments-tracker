-- ============================================================================
-- MIGRATION V12: Unify instrument symbols to TICKER.MARKET format
-- Description: All instrument symbols become TICKER.MARKET (e.g., CDR.PL,
--              MSFT.US, EMIM.UK). This enables supporting the same company
--              on different exchanges as distinct instruments.
--              Also updates the market column for foreign instruments.
-- See plan: XTB Import with Instrument Masterdata & Symbol Unification
-- ============================================================================

-- Step 1: Drop FK constraints (no ON UPDATE CASCADE available)
ALTER TABLE account_holdings DROP CONSTRAINT fk_account_holdings_position;
ALTER TABLE positions DROP CONSTRAINT fk_positions_instrument;

-- Step 2: Rename specific foreign instruments with correct market suffixes
-- and update their market column.

-- LSE ETFs (from V9 and V10)
UPDATE instruments SET symbol = 'EMIM.UK', market = 'UK' WHERE symbol = 'EMIM';
UPDATE instruments SET symbol = 'SWDA.UK', market = 'UK' WHERE symbol = 'SWDA';
UPDATE instruments SET symbol = 'ECAR.UK', market = 'UK' WHERE symbol = 'ECAR';
UPDATE instruments SET symbol = 'RBOT.UK', market = 'UK' WHERE symbol = 'RBOT';
UPDATE instruments SET symbol = 'USPY.UK', market = 'UK' WHERE symbol = 'USPY';
UPDATE instruments SET symbol = 'FXC.UK',  market = 'UK' WHERE symbol = 'FXC';
UPDATE instruments SET symbol = 'NDIA.UK', market = 'UK' WHERE symbol = 'NDIA';

-- XETRA ETFs (from V8 and V9)
UPDATE instruments SET symbol = 'NQSE.DE', market = 'DE' WHERE symbol = 'NQSE';
UPDATE instruments SET symbol = 'DTLE.DE', market = 'DE' WHERE symbol = 'DTLE';

-- NYSE stocks (from V9)
UPDATE instruments SET symbol = 'BABA.US', market = 'US' WHERE symbol = 'BABA';

-- Cascade renames to positions table
UPDATE positions SET instrument_symbol = 'EMIM.UK' WHERE instrument_symbol = 'EMIM';
UPDATE positions SET instrument_symbol = 'SWDA.UK' WHERE instrument_symbol = 'SWDA';
UPDATE positions SET instrument_symbol = 'ECAR.UK' WHERE instrument_symbol = 'ECAR';
UPDATE positions SET instrument_symbol = 'RBOT.UK' WHERE instrument_symbol = 'RBOT';
UPDATE positions SET instrument_symbol = 'USPY.UK' WHERE instrument_symbol = 'USPY';
UPDATE positions SET instrument_symbol = 'FXC.UK'  WHERE instrument_symbol = 'FXC';
UPDATE positions SET instrument_symbol = 'NDIA.UK' WHERE instrument_symbol = 'NDIA';
UPDATE positions SET instrument_symbol = 'NQSE.DE' WHERE instrument_symbol = 'NQSE';
UPDATE positions SET instrument_symbol = 'DTLE.DE' WHERE instrument_symbol = 'DTLE';
UPDATE positions SET instrument_symbol = 'BABA.US' WHERE instrument_symbol = 'BABA';

-- Cascade renames to account_holdings table
UPDATE account_holdings SET instrument_symbol = 'EMIM.UK' WHERE instrument_symbol = 'EMIM';
UPDATE account_holdings SET instrument_symbol = 'SWDA.UK' WHERE instrument_symbol = 'SWDA';
UPDATE account_holdings SET instrument_symbol = 'ECAR.UK' WHERE instrument_symbol = 'ECAR';
UPDATE account_holdings SET instrument_symbol = 'RBOT.UK' WHERE instrument_symbol = 'RBOT';
UPDATE account_holdings SET instrument_symbol = 'USPY.UK' WHERE instrument_symbol = 'USPY';
UPDATE account_holdings SET instrument_symbol = 'FXC.UK'  WHERE instrument_symbol = 'FXC';
UPDATE account_holdings SET instrument_symbol = 'NDIA.UK' WHERE instrument_symbol = 'NDIA';
UPDATE account_holdings SET instrument_symbol = 'NQSE.DE' WHERE instrument_symbol = 'NQSE';
UPDATE account_holdings SET instrument_symbol = 'DTLE.DE' WHERE instrument_symbol = 'DTLE';
UPDATE account_holdings SET instrument_symbol = 'BABA.US' WHERE instrument_symbol = 'BABA';

-- Cascade renames to import_session_mappings table
UPDATE import_session_mappings SET catalog_symbol = 'EMIM.UK' WHERE catalog_symbol = 'EMIM';
UPDATE import_session_mappings SET catalog_symbol = 'SWDA.UK' WHERE catalog_symbol = 'SWDA';
UPDATE import_session_mappings SET catalog_symbol = 'ECAR.UK' WHERE catalog_symbol = 'ECAR';
UPDATE import_session_mappings SET catalog_symbol = 'RBOT.UK' WHERE catalog_symbol = 'RBOT';
UPDATE import_session_mappings SET catalog_symbol = 'USPY.UK' WHERE catalog_symbol = 'USPY';
UPDATE import_session_mappings SET catalog_symbol = 'FXC.UK'  WHERE catalog_symbol = 'FXC';
UPDATE import_session_mappings SET catalog_symbol = 'NDIA.UK' WHERE catalog_symbol = 'NDIA';
UPDATE import_session_mappings SET catalog_symbol = 'NQSE.DE' WHERE catalog_symbol = 'NQSE';
UPDATE import_session_mappings SET catalog_symbol = 'DTLE.DE' WHERE catalog_symbol = 'DTLE';
UPDATE import_session_mappings SET catalog_symbol = 'BABA.US' WHERE catalog_symbol = 'BABA';

-- Step 3: Rename all remaining instruments (GPW) — append '.PL' suffix.
-- These are all instruments that don't already have a dot in their symbol
-- (i.e., haven't been renamed in step 2).
UPDATE instruments
SET symbol = symbol || '.PL'
WHERE symbol NOT LIKE '%.%';

UPDATE positions
SET instrument_symbol = instrument_symbol || '.PL'
WHERE instrument_symbol NOT LIKE '%.%';

UPDATE account_holdings
SET instrument_symbol = instrument_symbol || '.PL'
WHERE instrument_symbol NOT LIKE '%.%';

UPDATE import_session_mappings
SET catalog_symbol = catalog_symbol || '.PL'
WHERE catalog_symbol IS NOT NULL
  AND catalog_symbol NOT LIKE '%.%';

-- Step 4: Re-add FK constraints
ALTER TABLE positions
    ADD CONSTRAINT fk_positions_instrument
    FOREIGN KEY (instrument_symbol) REFERENCES instruments(symbol) ON DELETE RESTRICT;

ALTER TABLE account_holdings
    ADD CONSTRAINT fk_account_holdings_position
    FOREIGN KEY (instrument_symbol) REFERENCES positions(instrument_symbol) ON DELETE CASCADE;

-- ============================================================================
-- END OF MIGRATION V12
-- Renamed ~750 instruments to TICKER.MARKET format:
--   - ~740 GPW instruments: TICKER → TICKER.PL
--   - 7 LSE instruments: TICKER → TICKER.UK
--   - 2 XETRA instruments: TICKER → TICKER.DE
--   - 1 NYSE instrument: TICKER → TICKER.US
-- Updated market column for non-GPW instruments (UK, DE, US)
-- ============================================================================
