# ADR-033: Instrument Catalog as System-Managed Master Data

## Status
Proposed (2026-02-15)

## Context

### The Problem: Freetext Symbols Don't Match Provider Tickers

When adding a position, users type an instrument symbol as freetext. The system creates the instrument implicitly if it doesn't exist (`PositionCommandUseCaseService.addPosition()` auto-creates instruments on first use). There is no validation that the symbol matches the external price provider's ticker format.

After deploying the Stooq price adapter (#32), this caused silent price fetch failures:

| User-typed symbol | Stooq ticker | Result |
|-------------------|-------------|--------|
| `ATREM` | `ATR` | Stooq returns B/D (no data) — price never resolves |
| `ELEKTROTI` | `ELT` | Stooq returns B/D (no data) — price never resolves |
| `PKO` | `PKO` | Works — happens to match |

The position exists in the database, but the portfolio shows no current value, no P&L — the instrument is effectively orphaned from market data.

### Why This Happens

1. **No source of truth for valid symbols.** The `instruments` table accepts any string as a symbol. There is no list of known-valid tickers to check against.
2. **Different providers use different conventions.** Stooq uses short GPW tickers (`ATR`, `ELT`, `PKO`). Finnhub (future, #33) uses standard US tickers (`AAPL`, `MSFT`). Yahoo Finance uses suffixed tickers (`.WA`). There is no universal symbol namespace.
3. **Instruments are created as a side effect of position entry.** The user never explicitly says "I want to track instrument ATR" — they just type a string when adding a position, and the system silently creates whatever they typed.

### Current Flow

```
User types "ATREM" → Instrument("ATREM") auto-created → Position references it
                     → Stooq asked for "ATREM" → B/D (unknown) → no price forever
```

### What We Need

```
System knows instrument ATR (Atrem S.A., PLN, STOCK, Stooq)
User selects ATR from catalog → Position references it
                               → Stooq asked for "ATR" → 58.80 PLN ✓
```

## Decision

### 1. Instruments Are System-Managed Catalog Entries

The `instruments` table becomes a **catalog of known instruments** — master data that must exist before positions can reference it. Instruments are no longer created implicitly during position entry.

The `Instrument` entity already has the right shape: `(symbol, name, type, currency)`. What changes is **ownership**: the system owns the catalog, the user selects from it.

### 2. `InstrumentSymbol` Is the Canonical Provider Ticker

The `InstrumentSymbol` value stored in the `instruments` table must match the external price provider's ticker format exactly:

| Market | Provider | Symbol format | Examples |
|--------|----------|--------------|----------|
| GPW (Polish) | Stooq | Short GPW ticker | `ATR`, `PKO`, `ELT`, `CDR` |
| US | Finnhub | Standard US ticker | `AAPL`, `MSFT`, `TSLA` |

This is the symbol sent to the price API. It is also the primary key for positions and holdings.

### 3. Initial Catalog Feed: Stooq Bulk Data → Flyway SQL Migration

The initial catalog is populated from **Stooq's bulk historical data download** (`stooq.com/db/h/`), which provides complete CSVs of all GPW-listed instruments. The process:

1. **Download** the daily Polish stock data archive from `stooq.com/db/h/` (requires manual browser download — Stooq enforces CAPTCHA since December 2020, blocking automated access)
2. **Extract** ticker symbols, instrument names, and types from the downloaded CSV files (the archive contains one directory per instrument with the ticker as the directory/filename)
3. **Generate** a Flyway migration (`V6__seed_instrument_catalog.sql`) with `INSERT` statements for all instruments the user tracks

This is a one-time manual step. The resulting migration is version-controlled and deterministic.

**Why Stooq bulk data as the source:**
- Stooq is the same provider used for price fetching — tickers are guaranteed to match
- The bulk download contains the complete GPW instrument universe (~500 stocks, ~50 ETFs)
- Instrument names in the CSV files correspond to official GPW names
- No need to guess or manually research ticker-to-name mappings

**Why Flyway migration as the delivery:**
- Deterministic and version-controlled — part of the deployment
- No runtime infrastructure needed (no admin API, no external sync)
- v0.1 is a personal project with ~10-20 instruments — a SQL file is sufficient
- Matches existing data management pattern (schema + seed in migrations)

**Example seed:**
```sql
INSERT INTO instruments (symbol, name, instrument_type, currency)
VALUES
    ('PKO', 'PKO Bank Polski', 'STOCK', 'PLN'),
    ('ATR', 'Atrem S.A.', 'STOCK', 'PLN'),
    ('ELT', 'Elektrotim S.A.', 'STOCK', 'PLN'),
    ('CDR', 'CD Projekt', 'STOCK', 'PLN'),
    ('ETFSP500', 'Beta ETF S&P 500', 'ETF', 'PLN')
ON CONFLICT (symbol) DO NOTHING;
```

### 4. Runtime Catalog Growth: Admin API (Future)

When the user wants to track a new instrument not yet in the catalog, a management endpoint will allow adding it at runtime:

```
POST /api/v1/instruments
{
    "symbol": "KGH",
    "name": "KGHM Polska Miedź",
    "type": "STOCK",
    "currency": "PLN"
}
```

This is tracked as a separate issue and is not part of the initial catalog implementation.

### 5. Position Creation Requires Catalog Instrument

The `PositionCommandUseCaseService.addPosition()` method changes:

**Before (auto-create):**
```java
if (!instrumentRepository.existsBySymbol(symbol)) {
    Instrument instrument = new Instrument(symbol, instrumentName, instrumentType, currency);
    instrumentRepository.save(instrument);
}
```

**After (catalog lookup):**
```java
if (!instrumentRepository.existsBySymbol(symbol)) {
    throw new ResourceNotFoundException("Instrument", "symbol", symbol.value());
}
```

The instrument name, type, and currency are no longer passed by the caller — they come from the catalog. The `addPosition()` signature simplifies to just `(symbol, accountId, quantity, costBasis)`.

### 6. Frontend: Autocomplete from Catalog

The position entry form replaces the freetext instrument field with a typeahead/autocomplete component that queries the instrument catalog. The user types a few characters and selects from matching results.

A new API endpoint supports this:
```
GET /api/v1/instruments?q=atr → [{symbol: "ATR", name: "Atrem S.A.", type: "STOCK", currency: "PLN"}]
```

## Consequences

### Positive

1. **Price fetch reliability** — every instrument in the catalog has a validated provider-compatible ticker
2. **Clean data ownership** — master data (instruments) owned by system, transactional data (positions) owned by user
3. **Foundation for multi-provider routing** — catalog metadata (currency, type) determines which price provider serves each instrument
4. **Better UX** — autocomplete is faster and less error-prone than freetext
5. **Instrument search** — enables browsing/searching available instruments

### Negative

1. **Friction for new instruments** — user cannot add positions for instruments not yet in the catalog; must add the instrument first
2. **Catalog maintenance** — someone must seed and maintain the instrument list (mitigated: admin API planned)
3. **Breaking change** — existing positions with invalid symbols (e.g., `ATREM`) need data migration to correct tickers

### Mitigations

1. **Admin API** (planned) removes the friction of adding new instruments — single API call before adding a position
2. **Data migration** (V6) corrects existing invalid symbols and seeds the catalog simultaneously
3. **Catalog is small** — personal project with ~10-20 instruments, maintenance burden is minimal

## Alternatives Considered

### Symbol Mapping Table

Keep freetext entry, add a mapping table (`user_symbol → provider_ticker`). Rejected: adds complexity without solving the root cause. The user would still type arbitrary strings, and someone would still need to create the mapping manually.

### Automated Ongoing Sync from Providers

Automatically sync the catalog with Stooq/Finnhub on a schedule (e.g., nightly fetch of all available instruments). Rejected for v0.1: Stooq's bulk download requires CAPTCHA (no programmatic access). Finnhub has `GET /stock/symbol?exchange=US` but returns ~10,000 symbols — overkill for tracking 20 instruments. The initial catalog is populated from a one-time manual Stooq bulk download; ongoing growth is handled via admin API. Automated sync could be revisited if the catalog needs to track hundreds of instruments.

### Freetext with Validation Warning

Keep freetext but show a warning if the symbol isn't recognized by the price provider (try a test fetch). Rejected: still allows invalid data to be persisted, and the "validation" depends on an external API call that may fail for other reasons (rate limit, timeout).

## Related Decisions

- [ADR-031: Instrument Price Providers](ADR-031-instrument-price-providers.md) — price provider conventions that instruments must match
- [ADR-032: External Data Caching Strategy](ADR-032-external-data-caching-strategy.md) — cached prices keyed by `InstrumentSymbol`
- [ADR-005: Database Schema Design](ADR-005-database-schema.md) — instruments table with `symbol` as natural key

## References

- GitHub Issue #47: Instrument Catalog — replace freetext with validated selection
- GitHub Issue #32: Stooq adapter (revealed the symbol mismatch problem)
