---
paths:
  - "src/main/java/**/infrastructure/external/mbank/**"
  - "src/main/java/**/infrastructure/external/xtb/**"
  - "src/main/java/**/infrastructure/external/degiro/**"
  - "src/main/java/**/application/port/out/**"
  - "src/main/java/**/*Import*.java"
  - "src/main/java/**/domain/model/*Mapping*.java"
  - "src/test/**/*Import*"
  - "src/test/**/*import*"
  - "frontend/src/app/features/import/**"
---
# Broker import

**Flow** (`/api/v1/imports`):
1. Upload & parse: a `TransactionHistoryParser` adapter returns a `ParseResult`, which holds `RawTransaction`s and optional ticker hints.
2. Map: the user maps each unmatched `BrokerInstrumentName` to a catalog `InstrumentSymbol`.
3. Prices (optional): status `PENDING_PRICES` when an instrument has no automatic price provider.
4. Confirm: positions are computed and persisted as a **full replacement per account**.

- `BrokerInstrumentMapping` is persistent and survives across imports. `InstrumentMapping` is scoped to a single session.
- **Never auto-match symbols heuristically.** Unmatched items always go back to the user.

**Cost basis:** FIFO in `ImportCalculationService`. Sells consume the oldest `BuyLot`s first (a `LinkedList`). The remaining cost is the weighted average of the unsold lots.

**Parsers** (`infrastructure/external/{broker}/`, port in `application/port/out/`, all read an `InputStream`):
- mBank: CSV, Windows-1250, Polish number format.
- XTB: XLSX via Apache POI. Take buy/sell direction from the **comment field**, because the Type column is unreliable. Tickers are cross-referenced from the Closed Positions sheet. Format analysis: `docs/import-formats/xtb-format-analysis.md`.
- DEGIRO: CSV. The sign of the quantity gives the direction.

**Gotchas:**
- The currency in a broker file is unreliable. Always resolve currency from the instrument catalog.
- Validate every parser change against real sample files (`.context/attachments/`, test resources).
- Before changing import semantics, think through what happens to data that already exists from another broker or from manual entry.
