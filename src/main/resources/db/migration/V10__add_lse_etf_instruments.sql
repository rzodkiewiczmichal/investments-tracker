-- ============================================================================
-- ADD LSE ETF INSTRUMENTS TO CATALOG
-- Version: V10
-- Description: Add FXC and NDIA ETFs traded on London Stock Exchange,
--              encountered in mBank transaction history imports.
-- ============================================================================

INSERT INTO instruments (symbol, name, instrument_type, currency, version)
VALUES
    ('FXC',  'iShares China Large Cap UCITS ETF',  'ETF', 'GBP', 0),
    ('NDIA', 'iShares MSCI India UCITS ETF',       'ETF', 'USD', 0)
ON CONFLICT (symbol) DO NOTHING;

-- ============================================================================
-- END OF MIGRATION V10
-- Total: 2 LSE ETF instruments
-- ============================================================================
