-- Drop PLN-only CHECK constraints to support multi-currency instruments
ALTER TABLE instruments DROP CONSTRAINT instruments_currency_pln;
ALTER TABLE positions DROP CONSTRAINT positions_currency_pln;
ALTER TABLE account_holdings DROP CONSTRAINT account_holdings_currency_pln;
