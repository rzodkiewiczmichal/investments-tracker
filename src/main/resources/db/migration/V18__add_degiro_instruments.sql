-- ============================================================================
-- MIGRATION V18: Add DEGIRO-specific instruments
-- Description: Adds instruments needed for DEGIRO import that are not
--              already in the catalog.
-- ============================================================================

INSERT INTO instruments (symbol, name, instrument_type, currency, market, version)
VALUES ('DTLE.UK', 'iShares USD Treasury Bond 20+yr UCITS ETF EUR Hedged (Dist)', 'BOND_ETF', 'EUR', 'UK', 0)
ON CONFLICT (symbol) DO NOTHING;

-- ============================================================================
-- END OF MIGRATION V18
-- ============================================================================
