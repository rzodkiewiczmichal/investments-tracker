# ADR-031: Instrument Price Providers (Stooq + Finnhub)

## Status
Accepted (2026-02-13, extracted from ADR-030)

## Context

The Investment Tracker needs current market prices for stocks and ETFs to calculate portfolio value, P&L, and current positions (FR-051, future version). The portfolio includes instruments from both the Warsaw Stock Exchange (GPW) and US markets (NYSE, NASDAQ).

### Requirements

- Free tier sufficient for personal use (50-100 instruments, daily updates)
- Stock and ETF prices from Warsaw Stock Exchange (GPW) and US markets (NYSE, NASDAQ)
- Reliable for a personal hobby project (occasional downtime acceptable)

### Research Findings (February 2026)

A comprehensive evaluation of 10+ stock price APIs was conducted. Key findings:

**The GPW problem:**
- Most free API tiers only cover US markets. Warsaw Stock Exchange (GPW) is gated behind expensive paid tiers:
  - **Twelve Data**: GPW requires Ultra tier ($999/month) - free tier covers US only
  - **FMP (Financial Modeling Prep)**: GPW likely requires Ultimate tier ($149/month) - free tier is US-limited
  - **Polygon.io**: International data requires $99+/month
  - **Tiingo, IEX Cloud**: US-only on free tier
- **No single official free API covers both GPW and US markets**
- Stooq.pl (Polish financial data site) provides a well-known CSV endpoint used by the quant community for GPW data
- Finnhub provides free real-time US stock data with an official Java client

## Decision

We adopt a **two-provider strategy**, using the best free option for each market segment:

| Market | Provider | Type | Free Tier | Coverage |
|--------|----------|------|-----------|----------|
| **Polish Stocks/ETFs (GPW)** | Stooq.pl | Unofficial CSV endpoint | Unlimited, no auth | Full GPW coverage |
| **US Stocks/ETFs** | Finnhub | Official commercial API | Unlimited (60 req/min) | NYSE, NASDAQ, real-time |

### Provider 1: Stooq.pl (Polish Stocks and ETFs)

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

### Provider 2: Finnhub (US Stocks and ETFs)

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

Both providers implement a single domain port interface:

```java
public interface InstrumentPriceProvider {
    Price getCurrentPrice(InstrumentSymbol symbol);
    Map<InstrumentSymbol, Price> getCurrentPrices(List<InstrumentSymbol> symbols);
}
```

Routing logic (which adapter handles which symbol) belongs in the infrastructure layer, transparent to the domain. The adapter selection is based on exchange/market metadata associated with each instrument.

### Caching Strategy

| Provider | Cache Duration | Rationale |
|----------|---------------|-----------|
| Stooq prices | Until next market close | GPW closes at 17:00 CET |
| Finnhub prices | 15 minutes | Real-time data, but no need for constant refresh |

### API Key Management

- **Stooq.pl**: No credentials needed
- **Finnhub**: API key stored in environment variable / application properties (not committed to git)

## Consequences

### Positive

1. **Zero cost** - both providers completely free for personal use
2. **Full market coverage** - GPW via Stooq, US via Finnhub
3. **Official Java client** - Finnhub provides maintained Java SDK
4. **Hexagonal architecture** - providers behind port interface, easy to swap
5. **Independence** - each provider serves a distinct market, failures are isolated
6. **Batch support** - Stooq supports multiple symbols per request

### Negative

1. **Two integrations to maintain** - more code than single provider
2. **One unofficial provider** - Stooq has no SLA (Finnhub is official)
3. **Different data formats** - JSON (Finnhub) and CSV (Stooq)
4. **Symbol mapping needed** - GPW symbols on Stooq may differ from other providers

### Mitigation

1. **Provider abstraction** - port interface makes swapping providers trivial
2. **Stooq stability** - operating since 2001, widely used, unlikely to disappear
3. **Caching** - reduces dependency on provider availability
4. **Simple CSV parsing** - Stooq's format is straightforward

## Alternatives Considered

### Twelve Data (Single Stock Provider)

Initially chosen in first version of ADR-030. **Rejected after deeper analysis**: Free tier only covers US, Crypto, and Forex (3 markets). Warsaw Stock Exchange requires Ultra tier at $999/month.

### Financial Modeling Prep (Single Stock Provider)

Confirmed GPW support claimed online. **Rejected**: Free tier is heavily exchange-limited. GPW likely requires Ultimate tier ($149/month).

### Yahoo Finance (Single Stock Provider)

Free, good coverage including partial GPW (.WA suffix). **Not adopted as primary**: Unofficial API with history of breaking changes (2017 cookie/crumb requirement, 2023 endpoint changes). Stooq is more reliable for GPW and Finnhub is more reliable for US.

### EOD Historical Data (Paid)

Best GPW coverage, proper API. **Deferred**: $20/month. Could adopt if free providers fail. Best paid fallback option.

### Alpha Vantage

Both stocks and forex. **Rejected**: Only 25 calls/day total - insufficient for 50+ instrument portfolio.

## Related Decisions

- [ADR-030: Currency Exchange Rate Provider](ADR-030-currency-exchange-rate-provider.md) - Currency conversion (separate concern)
- [ADR-006: Money Representation](ADR-006-money-representation.md) - Currency storage and conversion

## Implementation Notes

### Dependencies

```gradle
// Finnhub official Java client
implementation 'io.finnhub:finnhub-java-client:x.x.x'
```

### Planned Adapters

- `StooqPriceAdapter` - for GPW instruments (CSV parsing)
- `FinnhubPriceAdapter` - for US instruments (official Java client)

Routing logic determines which adapter handles each symbol based on exchange/market metadata.
