---
paths:
  - "src/main/resources/db/migration/**"
---
# Flyway migrations (ADR-016)

- Name files `V{n}__{description}.sql` (double underscore). Never edit an applied migration; add a new one.
- Instrument master data is seeded through migrations, which is why it survives `dev.sh reset` and `clear`. Make inserts idempotent with `ON CONFLICT ... DO NOTHING`.
- Symbols must use the `TICKER.MARKET` format (since V12).
- Money columns: `DECIMAL(19,4)` (ADR-006).
- After a schema change, update `docs/diagrams/database-schema.md`.
