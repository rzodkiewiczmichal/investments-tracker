-- ============================================================================
-- MIGRATION V15: Add additional LSE instruments
-- Description: Seed CSPX, CNDX, and EGLN ETFs traded on London Stock Exchange.
--              These are iShares UCITS ETFs denominated in USD.
-- ============================================================================

INSERT INTO instruments (symbol, name, instrument_type, currency, market, version)
VALUES
    ('CSPX.UK', 'iShares Core S&P 500 UCITS ETF',    'ETF', 'USD', 'UK', 0),
    ('CNDX.UK', 'iShares NASDAQ 100 UCITS ETF',      'ETF', 'USD', 'UK', 0),
    ('EGLN.UK', 'iShares Physical Gold ETC',          'ETF', 'USD', 'UK', 0)
ON CONFLICT (symbol) DO NOTHING;

-- ============================================================================
-- END OF MIGRATION V15
-- Total: 3 LSE ETF instruments (CSPX, CNDX, EGLN)
-- ============================================================================
