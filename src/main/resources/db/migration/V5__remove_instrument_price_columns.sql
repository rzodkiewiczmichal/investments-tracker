-- ============================================================================
-- MIGRATION V5: Remove price columns from instruments table
-- Description: Prices are now served from Redis (PriceCache), not stored
--              alongside instrument reference data in PostgreSQL.
-- See: ADR-032 (revised) - External Data Caching Strategy
-- ============================================================================

-- Drop index on price_updated_at (no longer needed)
DROP INDEX IF EXISTS idx_instruments_price_updated;

-- Drop price-positive constraint
ALTER TABLE instruments DROP CONSTRAINT IF EXISTS instruments_price_positive;

-- Drop price columns
ALTER TABLE instruments DROP COLUMN IF EXISTS current_price_amount;
ALTER TABLE instruments DROP COLUMN IF EXISTS current_price_currency;
ALTER TABLE instruments DROP COLUMN IF EXISTS price_updated_at;

-- Drop comments on removed columns (PostgreSQL drops them automatically with columns)
