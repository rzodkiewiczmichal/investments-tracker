ALTER TABLE instruments ADD COLUMN currency VARCHAR(3) NOT NULL DEFAULT 'PLN';
COMMENT ON COLUMN instruments.currency IS 'Native currency of the instrument (PLN, EUR, GBP, USD)';
