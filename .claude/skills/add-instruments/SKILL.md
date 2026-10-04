---
name: add-instruments
description: Add instruments to the system-managed instrument catalog via a Flyway migration. Use when the user wants to add tickers/ETFs/stocks to the catalog, when an import or position entry fails because an instrument is missing, or when a new broker/market needs catalog coverage.
---

# Add instruments to the catalog

The catalog is system-managed master data (ADR-033), seeded only through Flyway. Conventions live in `.claude/rules/flyway.md` and `.claude/rules/domain-model.md`.

## Steps

1. **Collect the instruments.** For each one, gather ticker, full name, type (`STOCK` | `ETF` | `BOND_ETF` | `POLISH_GOV_BOND`), trading currency and market (`GPW` | `US` | `UK` | `DE`). Ask the user for anything you can't confirm. Never guess an ISIN-to-ticker mapping.
2. **Build canonical symbols** in the form `TICKER.MARKET`, e.g. `CDR.PL` (the market suffix for GPW is `PL`), `AAPL.US`, `CSPX.UK`. Flatten dotted tickers: `BRK.B` → `BRKB.US`.
3. **Skip duplicates.** Grep `src/main/resources/db/migration/` for each symbol and drop the ones that already exist.
4. **Verify prices.** Run `scripts/check-price.sh SYMBOL...`. For every symbol it reports, show the user one of:
   - a price was returned,
   - no data,
   - no provider exists (DE).

   Never silently add an instrument that has no price source. The user decides whether it's acceptable.
5. **Write the migration.**
   - Get the next number from `scripts/next-migration.sh`.
   - Name the file `V{n}__add_<context>_instruments.sql` and use `V18__add_degiro_instruments.sql` as the template: header comment, then `INSERT INTO instruments (symbol, name, instrument_type, currency, market, version) VALUES (...) ON CONFLICT (symbol) DO NOTHING;`.
   - In the header, say why these instruments are needed (which broker or import).
6. **Verify.** Run `./gradlew spotlessApply && ./gradlew clean build`. Flyway runs in the integration tests.
7. **Apply locally** if the user wants it: `./dev.sh restart` (the catalog survives `reset`/`clear`).

## Don'ts
- Don't edit an existing migration.
- Don't add instruments at runtime or from frontend code (issue #48 tracks runtime catalog management).
