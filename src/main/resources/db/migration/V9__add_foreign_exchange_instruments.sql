-- ============================================================================
-- ADD FOREIGN-EXCHANGE INSTRUMENTS TO CATALOG
-- Version: V9
-- Description: Add LSE, XETRA, and NYSE instruments encountered in mBank
--              transaction history imports. These are ETFs and stocks traded
--              on foreign exchanges, not available on GPW.
-- ============================================================================

INSERT INTO instruments (symbol, name, instrument_type, currency, version)
VALUES
    -- === LSE (London Stock Exchange) ETFs - GBP ===
    ('EMIM', 'iShares Core MSCI EM IMI UCITS ETF',           'ETF',   'GBP', 0),
    ('SWDA', 'iShares Core MSCI World UCITS ETF',            'ETF',   'GBP', 0),

    -- === LSE ETFs - USD ===
    ('ECAR', 'iShares Electric Vehicles and Driving Tech UCITS ETF', 'ETF', 'USD', 0),
    ('RBOT', 'iShares Automation & Robotics UCITS ETF',      'ETF',   'USD', 0),
    ('USPY', 'iShares S&P 500 Equal Weight UCITS ETF',       'ETF',   'USD', 0),

    -- === XETRA (Deutsche Boerse) ETFs - EUR ===
    ('NQSE', 'iShares NASDAQ-100 UCITS ETF',                 'ETF',   'EUR', 0),

    -- === NYSE stocks - USD ===
    ('BABA', 'Alibaba Group Holding Ltd',                    'STOCK', 'USD', 0)
ON CONFLICT (symbol) DO NOTHING;

-- ============================================================================
-- END OF MIGRATION V9
-- Total: 7 foreign-exchange instruments (6 ETFs + 1 stock)
-- ============================================================================
