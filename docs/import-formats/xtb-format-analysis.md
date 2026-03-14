# XTB (xStation) Export Format Analysis

## File Characteristics

| Property | Value |
|----------|-------|
| **Format** | XLSX (Excel) |
| **Sheets** | "Closed Positions", "Cash Operations" |
| **Encoding** | N/A (binary Excel format) |
| **Delimiter** | N/A (structured cells) |

## Sheet: Closed Positions

### Header Structure

| Row | Content |
|-----|---------|
| 1 | `Account` / account number |
| 2 | `Closed Positions` / empty |
| 3 | `Date from (UTC)` / date |
| 4 | `Date to (UTC)` / date |
| 5 | Column headers |
| 6+ | Data rows |
| Last | `Profit/loss` summary row |

**Metadata rows to skip:** 4 (rows 1-4)
**Header row:** 5
**Data starts at:** row 6
**Summary row at end:** 1 row (`Profit/loss`)

### Columns (25 total)

| # | Column | Type | Example | Notes |
|---|--------|------|---------|-------|
| A | Instrument | String | "Hims & Hers Health" | Full instrument name |
| B | Category | String | "STOCK", "CFD", "ETC", "ETF", "ETN" | Asset category |
| C | Ticker | String | "HIMS.US", "MRB.PL", "EGLN.UK" | Ticker with market suffix |
| D | Type | String | "BUY" or "SELL" | Transaction side |
| E | Volume | Decimal | 20.0, 0.01 | Quantity (fractional for CFDs) |
| F | Open Price | Decimal | 16.5 | Price in instrument's native currency |
| G | Open Time (UTC) | DateTime | 2026-02-12 14:30:02.887 | Millisecond precision |
| H | Close Price | Decimal | 22.31 | Price at close |
| I | Close Time (UTC) | DateTime | 2026-03-09 15:29:29.363 | Millisecond precision |
| J | Product | String | "My Trades" | Always "My Trades" in sample |
| K | Profit/Loss | Decimal | 460.61, -686.59 | P&L in PLN (account currency) |
| L | Gross Profit | Decimal | 460.61 | Gross P&L |
| M | Purchase Value | Decimal/Empty | 1176.05 | In PLN. Empty for CFDs |
| N | Sale Value | Decimal/Empty | 1636.66 | In PLN. Empty for CFDs |
| O | Stop Loss | String/Empty | "" | Usually empty |
| P | Take Profit | String/Empty | "" | Usually empty |
| Q | Commission | String/Empty | "" | Always empty in sample |
| R | Margin | Decimal/Empty | 744.87 | Only for CFDs |
| S | Swap | Decimal | 0.0, -0.44 | Overnight swap costs |
| T | Rollover | Decimal | 0.0 | Rollover costs |
| U | Open Conversion Rate | Decimal/Empty | 3.5638 | FX rate at open (empty for PLN instruments) |
| V | Close Conversion Rate | Decimal/Empty | 3.668 | FX rate at close (empty for PLN instruments) |
| W | Close Origin | String | "xStation5" | Platform |
| X | Position ID | Integer | 2427493785 | Unique position identifier |
| Y | Comment | String | "" | Usually empty |

### Categories Found

| Category | Count | Examples | Relevant for Import? |
|----------|-------|---------|---------------------|
| STOCK | Many | HIMS.US, MRB.PL, CRWD.US | Yes |
| ETF | Some | ETFBM40TR.PL, ETFBS80TR.PL | Yes |
| ETC | Few | EGLN.UK (Physical Gold) | Yes |
| ETN | Few | VBTC.DE | Yes |
| CFD | Many | OIL, GOLD, EURPLN, US100 | No - derivatives, not real positions |

### Ticker Format

Tickers use a `SYMBOL.MARKET` format:
- `.US` - US stocks (HIMS.US, CRWD.US, MSFT.US)
- `.PL` - Polish stocks (MRB.PL, PZU.PL, CDR.PL)
- `.UK` - UK-listed (EGLN.UK, IPOL.UK, CSPX.UK)
- `.DE` - German-listed (NVD.DE, RHM.DE, IBCJ.DE)
- `.NL` - Dutch-listed (ASML.NL, INPST.NL)
- `.DK` - Danish-listed (NOVOB.DK)
- `.FR` - French-listed (AM.FR)
- `.IT` - Italian-listed (LDO.IT)
- No suffix - CFDs (OIL, GOLD, EURPLN, US100, W20)

### Currency Handling

XTB positions are **multi-currency**:
- PLN instruments (.PL): Open/Close Conversion Rate is 0.0 or empty
- Foreign instruments (.US, .UK, etc.): Conversion rates provided
- Purchase/Sale Values appear to be in PLN (account currency)
- Open/Close Prices are in the instrument's native currency (USD, GBP, EUR, etc.)

### Key Design Decisions for Parser

1. **File format**: XLSX requires Apache POI library (not simple CSV parsing)
2. **Filter by Category**: Only import STOCK, ETF, ETC, ETN. Skip CFD category entirely
3. **Closed positions = sell transactions**: This sheet shows completed round-trips. For current portfolio import, we need to look at **open positions** or use cash operations
4. **This is NOT a transaction log**: Unlike mBank which exports individual buy/sell transactions, XTB exports **closed position pairs** (open + close together). Open positions are not in this export.

## Sheet: Cash Operations

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

| Type | Meaning | Import Relevant? |
|------|---------|-----------------|
| Stock purchase | Buy transaction | Yes |
| Stock sell | Sell transaction | Yes |
| Dividend | Dividend payment | Future (v0.3+) |
| Withholding tax | Tax on dividends | Future (v0.3+) |
| Deposit | Cash deposit | No |
| Close trade | CFD close | No |
| Free funds interest | Interest on cash | No |
| Free funds interest tax | Tax on interest | No |

### Comment Field Parsing

The Comment field in Cash Operations contains useful structured data:
- Stock purchase: `"OPEN BUY 1 @ 1416.00"` or `"OPEN BUY 1/9 @ 56.60"` (partial fill: 1 of 9)
- Stock sell: `"CLOSE BUY 20/60 @ 22.31"` (closing 20 of 60 shares at 22.31)
- Dividend: `"MSFT.US USD 0.9100/ SHR"` (ticker, currency, amount per share)

**Important**: The Comment field contains the **ticker** (e.g., "MSFT.US") for dividends, and the **price in native currency** for stock operations. The Amount column is always in PLN.

## Important: No Separate Open Positions Export

XTB (xStation5) does **not** provide a separate "Open Positions" export. Both report types (different date ranges, different downloads) produce the same two-sheet structure: "Closed Positions" and "Cash Operations".

To determine currently held positions, one must calculate net quantities from Cash Operations: `sum(Stock purchase quantities) - sum(Stock sell quantities)` per instrument.

### Verified with Real Data

From real export (2006-01-01 to 2026-03-14):
- **18 currently open positions** derived from 374 purchases and 362 sells across 98 instruments
- **3 instruments have no ticker resolution** from file alone (never had a closed trade): Amazon, NASDAQ 100, Torpol
- These 3 require manual mapping during import confirmation

## Recommended Import Strategy

### Primary approach: Cash Operations sheet

Use the "Cash Operations" sheet filtering for "Stock purchase" and "Stock sell" types:
- Maps to our existing `RawTransaction` model (individual buy/sell entries)
- Consistent with mBank's transaction-log approach
- Amount is in PLN but Comment has the native currency price

### Ticker Resolution Strategy

1. **Primary**: Cross-reference instrument name with Closed Positions sheet (has ticker in Column C)
2. **Secondary**: Parse dividend Comment field (contains ticker, e.g., `"MSFT.US USD 0.9100/ SHR"`)
3. **Fallback**: Manual mapping via import confirmation flow (for instruments with no closed trades or dividends)

| Resolution Method | Coverage | Example |
|-------------------|----------|---------|
| Closed Positions cross-ref | ~85% of instruments | "Microsoft" → MSFT.US |
| Dividend comment parsing | Additional ~5% | "MSFT.US USD 0.9100/ SHR" |
| Manual mapping (user) | Remaining ~10% | "Amazon" → user provides AMZN.US |

### Challenges

1. **XLSX format**: Need Apache POI dependency (vs simple CSV for mBank)
2. **Instrument name vs ticker**: Cash Operations uses full names ("Microsoft"), not tickers
3. **Cross-referencing**: Must join Cash Operations with Closed Positions for ticker resolution
4. **Amount in PLN only**: Native currency price only available in Comment field parsing
5. **No explicit quantity in Cash Operations**: Must derive from Comment parsing (`"OPEN BUY 20/60 @ 22.31"` = 20 units at 22.31)
6. **Commission not available**: Cash Operations does not include commission data

### Column-to-Domain Mapping (Cash Operations approach)

| Domain Field | Source |
|-------------|--------|
| brokerInstrumentName | Column B (Instrument full name) |
| type (BUY/SELL) | Column A ("Stock purchase" = BUY, "Stock sell" = SELL) |
| quantity | Parsed from Column F Comment (e.g., "OPEN BUY **20**/60 @ 22.31") |
| unitPrice | Parsed from Column F Comment (e.g., "OPEN BUY 20/60 @ **22.31**") |
| commission | Not available in Cash Operations |
| currency | Derived from ticker suffix (via Closed Positions cross-ref) or manual mapping |

### Comment Field Parsing Patterns

| Pattern | Example | Extracted |
|---------|---------|-----------|
| Simple buy | `OPEN BUY 20 @ 16.50` | qty=20, price=16.50 |
| Partial fill buy | `OPEN BUY 3/20 @ 17.00` | qty=3, price=17.00 |
| Sell (close) | `CLOSE BUY 20/60 @ 22.31` | qty=20, price=22.31 |
| Regex | `(OPEN\|CLOSE) BUY (\d+)(?:/\d+)? @ ([\d.]+)` | groups: qty, price |

### Alternative: Closed Positions sheet

Could use Closed Positions for historical P&L analysis, but it doesn't suit our current import model (individual transactions). It shows completed round-trips, not open positions.
