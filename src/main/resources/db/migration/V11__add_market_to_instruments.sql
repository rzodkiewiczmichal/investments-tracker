-- ============================================================================
-- MIGRATION V11: Add market column to instruments table
-- Description: Instruments need a market identifier for price provider routing.
--              GPW instruments use Stooq, US instruments use Finnhub.
-- See: ADR-031 (amended) - Instrument Price Providers
-- ============================================================================

ALTER TABLE instruments ADD COLUMN market VARCHAR(10) NOT NULL DEFAULT 'GPW';

COMMENT ON COLUMN instruments.market IS 'Market where the instrument is traded (GPW, US). Used for price provider routing.';
