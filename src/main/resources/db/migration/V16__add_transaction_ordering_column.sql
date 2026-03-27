-- ============================================================================
-- MIGRATION V16: Add ordering column for import session transactions
-- Description: Spring Data JDBC requires an index column for List-typed
--              collections. Without this, duplicate transactions (same
--              instrument, type, quantity, price) were silently deduplicated
--              when stored in a Set.
-- ============================================================================

ALTER TABLE import_session_transactions
    ADD COLUMN import_sessions_key INTEGER;

-- Backfill existing rows with sequential ordering per session
UPDATE import_session_transactions t
SET import_sessions_key = sub.rn
FROM (
    SELECT id, ROW_NUMBER() OVER (PARTITION BY import_session_id ORDER BY id) - 1 AS rn
    FROM import_session_transactions
) sub
WHERE t.id = sub.id;

ALTER TABLE import_session_transactions
    ALTER COLUMN import_sessions_key SET NOT NULL;

-- ============================================================================
-- END OF MIGRATION V16
-- ============================================================================
