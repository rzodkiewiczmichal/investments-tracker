-- ============================================================================
-- MIGRATION V17: Add transaction date column to import session transactions
-- Description: Stores the original transaction timestamp from broker exports.
--              Required for FIFO cost basis calculation — sells must consume
--              the oldest buy lots first.
-- ============================================================================

ALTER TABLE import_session_transactions
    ADD COLUMN transaction_date TIMESTAMP;

-- ============================================================================
-- END OF MIGRATION V17
-- ============================================================================
