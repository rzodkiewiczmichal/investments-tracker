# ADR-030: External Financial Data Providers

## Status
Accepted (revised 2026-02-12, replaces initial version from 2026-02-08)

## Context

The Investment Tracker requires two types of external financial data:

1. **Currency Exchange Rates**: To convert non-PLN cost basis and current prices to PLN at query time (FR-089)
2. **Instrument Prices**: To fetch current market prices for stocks and ETFs (FR-051, future version)

### Requirements

- Free tier sufficient for personal use (50-100 instruments, daily updates)
- PLN exchange rates (EUR/PLN, GBP/PLN, USD/PLN)
- Stock and ETF prices from Warsaw Stock Exchange (GPW) and US markets (NYSE, NASDAQ)
- Reliable for a personal hobby project (occasional downtime acceptable)

### Research Findings (February 2026)

A comprehensive evaluation of 10+ APIs was conducted for each concern. Key findings:

**Currency exchange rates:**
- NBP API (Polish National Bank) is the clear winner: free, unlimited, no auth, official PLN rates
- Commercial alternatives (ExchangeRate-API, Fixer.io, Open Exchange Rates) offer no advantage over NBP for PLN

**Stock/ETF prices - the GPW problem:**
- Most free API tiers only cover US markets. Warsaw Stock Exchange (GPW) is gated behind expensive paid tiers:
  - **Twelve Data**: GPW requires Ultra tier ($999/month) - free tier covers US only
  - **FMP (Financial Modeling Prep)**: GPW likely requires Ultimate tier ($149/month) - free tier is US-limited
  - **Polygon.io**: International data requires $99+/month
  - **Tiingo, IEX Cloud**: US-only on free tier
- **No single official free API covers both GPW and US markets**
- Stooq.pl (Polish financial data site) provides a well-known CSV endpoint used by the quant community for GPW data
- Finnhub provides free real-time US stock data with an official Java client

## Decision

### Three-Provider Architecture

We adopt a three-provider strategy, using the best free option for each market segment:

| Concern | Provider | Type | Free Tier | Coverage |
|---------|----------|------|-----------|----------|
| **Currency Exchange Rates** | NBP API | Official government API | Unlimited, no auth | PLN rates for 35+ currencies |
| **Polish Stocks/ETFs (GPW)** | Stooq.pl | Unofficial CSV endpoint | Unlimited, no auth | Full GPW coverage |
| **US Stocks/ETFs** | Finnhub | Official commercial API | Unlimited (60 req/min) | NYSE, NASDAQ, real-time |

### Provider 1: NBP API (Currency Exchange Rates)

**URL**: https://api.nbp.pl
**Authentication**: None required (public API)
**Rate Limit**: Unlimited (reasonable use expected)
**Update Frequency**: Daily on working days (~11:45-12:15 CET)

**Why NBP**:
1. **Official source** - Narodowy Bank Polski, used by banks and financial institutions
2. **Free and unlimited** - no registration, no API key, no rate limits
3. **Perfect for PLN** - all rates expressed relative to PLN (native base currency)
4. **Government-backed** - highest reliability, stable API for 10+ years
5. **Simple REST API** - clean endpoints, JSON responses

**Key Endpoints**:
```
GET /api/exchangerates/rates/a/{currency}/          # Single currency rate (e.g., /a/eur/)
GET /api/exchangerates/rates/a/{currency}/today/    # Today's rate
GET /api/exchangerates/tables/a/                    # All Table A rates (35 currencies)
```

**Response Example**:
```json
{
  "table": "A",
  "currency": "euro",
  "code": "EUR",
  "rates": [{
    "no": "030/A/NBP/2026",
    "effectiveDate": "2026-02-12",
    "mid": 4.3214
  }]
}
```

**Limitations**:
- Returns 404 on weekends and Polish holidays (no rates published)
- Daily updates only (no intraday)
- Separate call per currency (no batch endpoint for specific currencies)
- Polish field names in some responses (e.g., "dolar amerykanski")

**Weekend/Holiday Handling**:
- Cache last business day rate in database
- On 404, serve cached rate flagged as stale
- Fetch schedule: once daily at 13:00 CET (after NBP publishes ~12:00)

### Provider 2: Stooq.pl (Polish Stocks and ETFs)

**URL**: https://stooq.pl
**Authentication**: None required
**Rate Limit**: Undocumented, low daily quota (sufficient for ~100 daily queries)
**Data**: End-of-day and intraday prices

**Why Stooq**:
1. **Full GPW coverage** - Polish financial data site, GPW is their home turf
2. **Free, no auth** - no registration or API key needed
3. **Well-known CSV endpoint** - widely used by Polish quant community since 2001
4. **Simple integration** - HTTP GET returns CSV, trivial to parse

**Key Endpoint**:
```
GET /q/l/?s={symbols}&f=sd2t2ohlcv&h&e=csv
```

**Parameters**:
- `s` - comma-separated symbols (e.g., `pko,pzu,kghm,cdr`)
- `f` - fields: `s`=symbol, `d2`=date, `t2`=time, `o`=open, `h`=high, `l`=low, `c`=close, `v`=volume
- `h` - include header row
- `e=csv` - CSV output format

**Response Example**:
```csv
Symbol,Date,Time,Open,High,Low,Close,Volume
PKO,2026-02-12,17:04:01,92,92.94,91.66,92,2646515
PZU,2026-02-12,17:04:01,45.5,46.1,45.2,45.8,1234567
```

**Multiple symbols in one request** - comma-separated in `s` parameter.

**Limitations**:
- **Unofficial** - no SLA, no documentation, no support
- Undocumented rate limits (community reports low daily quota to prevent abuse)
- Could change without notice (though stable for 20+ years)
- CSV format only (no JSON)
- Polish-centric (symbol format may differ from international conventions)

### Provider 3: Finnhub (US Stocks and ETFs)

**URL**: https://finnhub.io
**Authentication**: API key (free registration)
**Rate Limit**: 60 API calls/minute (no daily cap)
**Data**: Real-time US stock prices on free tier

**Why Finnhub**:
1. **Official Java client** - `io.finnhub:finnhub-java-client`, maintained by Finnhub
2. **Real-time US data** - unique among free providers (most offer 15-min delay)
3. **Generous rate limits** - 60/min with no daily cap (vs Twelve Data's 800/day)
4. **Proper documented API** - OpenAPI spec, clear ToS, commercial provider
5. **Free tier explicitly covers US** - no exchange tier gating for NYSE/NASDAQ

**Key Endpoints**:
```
GET /api/v1/quote?symbol=AAPL&token=KEY         # Real-time quote
GET /api/v1/stock/symbol?exchange=US&token=KEY   # List all US symbols
```

**Java Client Example**:
```java
// Official Finnhub Java client
ApiClient client = new DefaultApi().getApiClient();
client.addDefaultHeader("X-Finnhub-Token", apiKey);
Quote quote = defaultApi.quote("AAPL");
BigDecimal currentPrice = quote.getC(); // Current price
```

**Limitations**:
- US markets only on free tier (international exchanges require paid plans)
- 60 calls/minute means ~100 instruments take ~2 minutes sequentially
- No batch endpoint (one call per symbol)
- WebSocket available for real-time streaming but REST sufficient for daily updates

### Domain Integration

All three providers are accessed through domain port interfaces:

```java
// Currency rates - implemented by NBP adapter
public interface ExchangeRateProvider {
    ExchangeRate getExchangeRateToPln(Currency source);
    Map<Currency, ExchangeRate> getExchangeRatesToPln(Iterable<Currency> currencies);
}

// Stock/ETF prices - implemented by Stooq adapter (GPW) and Finnhub adapter (US)
public interface InstrumentPriceProvider {
    Price getCurrentPrice(InstrumentSymbol symbol);
    Map<InstrumentSymbol, Price> getCurrentPrices(List<InstrumentSymbol> symbols);
}
```

The `InstrumentPriceProvider` has two adapters. Routing logic (which adapter handles which symbol) belongs in the infrastructure layer, transparent to the domain.

### Caching Strategy

| Provider | Cache Duration | Rationale |
|----------|---------------|-----------|
| NBP rates | 24 hours | Rates update once daily on business days |
| Stooq prices | Until next market close | GPW closes at 17:00 CET |
| Finnhub prices | 15 minutes | Real-time data, but no need for constant refresh |

### API Key Management

- **NBP API**: No credentials needed
- **Stooq.pl**: No credentials needed
- **Finnhub**: API key stored in environment variable / application properties (not committed to git)

## Consequences

### Positive

1. **Zero cost** - all three providers completely free for personal use
2. **Full market coverage** - GPW via Stooq, US via Finnhub, currencies via NBP
3. **Official PLN rates** - most authoritative source for Polish currency
4. **Official Java client** - Finnhub provides maintained Java SDK
5. **Hexagonal architecture** - providers behind port interfaces, easy to swap
6. **Independence** - each provider serves a distinct concern, failures are isolated

### Negative

1. **Three integrations to maintain** - more code than single provider
2. **Two unofficial providers** - Stooq has no SLA (NBP is official, Finnhub is official)
3. **Different data formats** - JSON (NBP, Finnhub) and CSV (Stooq)
4. **Symbol mapping needed** - GPW symbols on Stooq may differ from other providers

### Mitigation

1. **Provider abstraction** - port interfaces make swapping providers trivial
2. **Stooq stability** - operating since 2001, widely used, unlikely to disappear
3. **Caching** - reduces dependency on provider availability
4. **Simple CSV parsing** - Stooq's format is straightforward

## Alternatives Considered

### Alternative 1: Twelve Data (Single Stock Provider)

Initially chosen in first version of this ADR. **Rejected after deeper analysis**: Free tier only covers US, Crypto, and Forex (3 markets). Warsaw Stock Exchange requires Ultra tier at $999/month. The original assumption of "60+ exchanges including GPW" on free tier was incorrect.

### Alternative 2: Financial Modeling Prep (Single Stock Provider)

Confirmed GPW support claimed online. **Rejected**: Pricing structure shows free tier is heavily exchange-limited. GPW likely requires Ultimate tier ($149/month). Could not verify without API key.

### Alternative 3: Yahoo Finance (Single Stock Provider)

Free, good coverage including partial GPW (.WA suffix). **Not adopted as primary**: Unofficial API with history of breaking changes (2017 cookie/crumb requirement, 2023 endpoint changes). Acceptable risk for hobby project but Stooq is more reliable for GPW and Finnhub is more reliable for US.

### Alternative 4: EOD Historical Data (Paid)

Best GPW coverage, proper API. **Deferred**: $20/month. Could adopt if free providers fail. Best paid fallback option.

### Alternative 5: Alpha Vantage

Both stocks and forex. **Rejected**: Only 25 calls/day total - insufficient for 50+ instrument portfolio.

## Related Decisions

- [ADR-006: Money Representation](ADR-006-money-representation.md) - Currency storage and conversion
- [ADR-005: Database Schema](ADR-005-database-schema.md) - Multi-currency columns

## Implementation Notes

### Phase 1 (Current): ExchangeRateProvider

Port interface `ExchangeRateProvider` created in domain layer. NBP adapter implementation to follow.

### Phase 2 (Future): InstrumentPriceProvider

Port interface for stock/ETF prices. Two adapters planned:
- `StooqPriceAdapter` - for GPW instruments (CSV parsing)
- `FinnhubPriceAdapter` - for US instruments (official Java client)

Routing logic determines which adapter handles each symbol based on exchange/market metadata.

### Dependencies

```gradle
// Finnhub official Java client
implementation 'io.finnhub:finnhub-java-client:x.x.x'
```
