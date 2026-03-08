#!/usr/bin/env bash
# Clears all positions, account holdings, and import sessions from the local database.
# Useful during import testing to reset state between runs.
#
# Usage: ./scripts/clear-positions.sh

set -euo pipefail

CONTAINER="${POSTGRES_CONTAINER:-investments-tracker-postgres}"
DB_NAME="${POSTGRES_DB:-investments_tracker}"
DB_USER="${POSTGRES_USER:-tracker_user}"

echo "Clearing positions and import data from ${DB_NAME} (container: ${CONTAINER})..."

docker exec -i "$CONTAINER" psql -U "$DB_USER" -d "$DB_NAME" -v ON_ERROR_STOP=1 <<'SQL'
BEGIN;

DELETE FROM import_session_transactions;
DELETE FROM import_session_mappings;
DELETE FROM import_sessions;
DELETE FROM account_holdings;
DELETE FROM positions;

COMMIT;

SELECT 'Cleared:' AS status,
       (SELECT count(*) FROM positions) AS positions,
       (SELECT count(*) FROM account_holdings) AS holdings,
       (SELECT count(*) FROM import_sessions) AS import_sessions;
SQL

echo "Done."
