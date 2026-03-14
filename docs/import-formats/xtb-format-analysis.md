# XTB (xStation) Export Format Analysis

## File Characteristics

| Property | Value |
|----------|-------|
| **Format** | XLSX (Excel) |
| **Import sheet** | "Cash Operations" (transaction log) |
| **Encoding** | N/A (binary Excel format) |

XTB exports contain two sheets: "Closed Positions" and "Cash Operations". We import from **Cash Operations only** because it's a **complete transaction log** — it contains all buy and sell operations for both currently open and already closed positions, matching our `RawTransaction` model.

The Closed Positions sheet is **not used for import** but serves as a lookup table for ticker resolution (it maps instrument names to tickers).

## Cash Operations Sheet

### Header Structure

| Row | Content |
|-----|---------|
| 1 | `Account number` / number |
| 2 | `Cash Operations` / empty |
| 3 | `Date from (UTC)` / date |
| 4 | `Date to (UTC)` / date |
| 5 | Column headers |
| 6+ | Data rows |
| Last | `Total` summary row |

### Columns (7 total)

| # | Column | Type | Example |
|---|--------|------|---------|
| A | Type | String | "Stock purchase", "Stock sell", "Dividend", "Deposit", etc. |
| B | Instrument | String | "Hims & Hers Health", "Microsoft" | Full name, no ticker |
| C | Time | DateTime | 2026-03-09 15:29:30.273 |
| D | Amount | Decimal | -5263.27, 1636.66 | Negative = outflow, positive = inflow. Always in PLN |
| E | ID | String | "1168861539" | Transaction ID |
| F | Comment | String | "OPEN BUY 20/60 @ 22.31", "MSFT.US USD 0.9100/ SHR" |
| G | Product | String | "My Trades" |

### Operation Types

| Type | Count | Import Relevant? |
|------|-------|-----------------|
| Stock purchase | 374 | **Yes** → maps to BUY |
| Stock sell | 362 | **Yes** → maps to SELL |
| Dividend | 109 | Future (v0.3+) |
| Withholding tax | 109 | Future (v0.3+) |
| Close trade | 84 | No (CFD close — uses different operation type than stocks) |
| Deposit | 36 | No |
| Swap | 35 | No (CFD swap costs) |
| SEC fee | 29 | No |
| Free funds interest | 23 | No |
| Free funds interest tax | 23 | No |
| Others (Tax IFTT, Adjustment fee, Dividend equivalent) | 6 | No |

### Comment Field Parsing

The Comment field contains structured data with quantity and price in the instrument's native currency (not PLN):

| Pattern | Example | Extracted |
|---------|---------|-----------|
| Simple buy | `OPEN BUY 20 @ 16.50` | qty=20, price=16.50 |
| Partial fill buy | `OPEN BUY 3/20 @ 17.00` | qty=3, price=17.00 |
| Sell (close) | `CLOSE BUY 20/60 @ 22.31` | qty=20, price=22.31 |
| Regex | `(OPEN\|CLOSE) BUY (\d+)(?:/\d+)? @ ([\d.]+)` | groups: qty, price |

The Amount column (D) is always in PLN. The Comment field has the price in native currency.

### Column-to-Domain Mapping

| Domain Field | Source |
|-------------|--------|
| brokerInstrumentName | Column B (Instrument full name) |
| type (BUY/SELL) | Column A ("Stock purchase" = BUY, "Stock sell" = SELL) |
| quantity | Parsed from Column F Comment |
| unitPrice | Parsed from Column F Comment |
| commission | Not available |
| currency | Derived from ticker suffix (via ticker resolution) |

## Ticker Resolution Strategy

Cash Operations only has instrument full names (e.g., "Microsoft"), not tickers. Tickers must be resolved via:

1. **Primary**: Cross-reference instrument name with Closed Positions sheet (Column A = name, Column C = ticker like "MSFT.US")
2. **Secondary**: Parse dividend Comment field (e.g., `"MSFT.US USD 0.9100/ SHR"`)
3. **Fallback**: Manual mapping via import confirmation flow

| Resolution Method | Coverage | Example |
|-------------------|----------|---------|
| Closed Positions cross-ref | ~85% | "Microsoft" → MSFT.US |
| Dividend comment parsing | ~5% | "MSFT.US USD 0.9100/ SHR" |
| Manual mapping (user) | ~10% | "Amazon" → user provides AMZN.US |

### Ticker Format

Tickers use `SYMBOL.MARKET` format:
- `.US` — US stocks (HIMS.US, CRWD.US, MSFT.US)
- `.PL` — Polish stocks (MRB.PL, PZU.PL, CDR.PL)
- `.UK` — UK-listed (EGLN.UK, CSPX.UK)
- `.DE` — German-listed (NVD.DE, VBTC.DE)
- `.NL` — Dutch-listed (ASML.NL, INPST.NL)
- `.DK` — Danish-listed (NOVOB.DK)
- `.FR` — French-listed (AM.FR)
- `.IT` — Italian-listed (LDO.IT)

### Instrument Name Collisions

Some instruments share similar names across categories (e.g., "Bitcoin" as ETN VBTC.DE vs "BITCOIN" as CFD). The parser must use the operation type to distinguish:
- `Stock purchase` / `Stock sell` → real assets (STOCK, ETF, ETC, ETN)
- `Close trade` / `Swap` → CFDs (skip)

### Unresolvable Tickers

Instruments that have never been sold (no entry in Closed Positions) and have no dividend history cannot be automatically resolved to a ticker. These require manual mapping during import confirmation.

## Data Volume (Real Export)

From real export (2006-01-01 to 2026-03-14):
- **374 stock purchases**, **362 stock sells** across ~98 instruments
- **Categories found**: STOCK, ETF, ETC, ETN (importable), CFD (skip)

## Challenges

1. **XLSX format**: Need Apache POI dependency (vs simple CSV for mBank)
2. **Instrument name vs ticker**: Requires cross-referencing with Closed Positions sheet
3. **Amount in PLN only**: Native currency price only available via Comment parsing
4. **No explicit quantity column**: Must parse from Comment field
5. **Commission not available**: Cash Operations does not include commission data
6. **Multi-currency**: PLN account with instruments in USD, EUR, GBP, DKK, etc.
