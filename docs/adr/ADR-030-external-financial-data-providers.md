# ADR-030: External Financial Data Providers

## Status
Accepted

## Context

The Investment Tracker requires two types of external financial data:

1. **Currency Exchange Rates**: To convert non-PLN cost basis and current prices to PLN at query time (FR-089)
2. **Instrument Prices**: To fetch current market prices for stocks and ETFs (FR-051, future version)

### Requirements

- Free tier sufficient for personal use (20-30 instruments, multiple daily updates)
- REST API with JSON responses
- PLN exchange rates (EUR/PLN, GBP/PLN, USD/PLN)
- Stock and ETF prices from global markets (NYSE, NASDAQ, LSE, GPW)
- Reliable and legal (no unofficial scraping)

### Research Findings

A comprehensive evaluation of 13+ APIs was conducted (see `temp/financial-apis-research-report.md`). Key findings:

- No single free API provides both comprehensive stock prices AND currency exchange rates with sufficient daily limits
- Best approach is combining two specialized APIs
- Official government source exists for PLN exchange rates (NBP)

## Decision

### Two-Provider Architecture

We adopt a two-provider strategy, using specialized APIs for each concern:

| Concern | Provider | Free Tier | Coverage |
|---------|----------|-----------|----------|
| **Currency Exchange Rates** | NBP API (Narodowy Bank Polski) | Unlimited, no auth | Official PLN rates |
| **Stock/ETF Prices** | Twelve Data | 800 calls/day | Global markets (US, EU, GPW) |

### Provider 1: NBP API (Currency Exchange Rates)

**URL**: https://api.nbp.pl
**Authentication**: None required (public API)
**Rate Limit**: Unlimited (reasonable use expected)
**Update Frequency**: Daily on working days (~11:45-12:15 CET)

**Why NBP**:
1. **Official source** - Polish National Bank, used by banks and financial institutions
2. **Free and unlimited** - no registration, no API key, no rate limits
3. **Perfect for PLN** - all rates expressed relative to PLN
4. **Government-backed** - highest reliability and authority
5. **Simple API** - clean REST endpoints, JSON responses

**Key Endpoints**:
```
GET /api/exchangerates/rates/a/{currency}/     # Single currency rate
GET /api/exchangerates/tables/a/               # All Table A rates (35 currencies)
```

**Supported currencies (Table A)**: EUR, USD, GBP, CHF, JPY, and 30+ others

**Limitations**:
- Currency only (no stock data)
- Daily updates only (no intraday)
- No weekend/holiday updates (uses last working day rate)

### Provider 2: Twelve Data (Stock/ETF Prices)

**URL**: https://api.twelvedata.com
**Authentication**: API key (free registration, email only)
**Rate Limit**: 800 requests/day, 8 requests/minute
**Data Delay**: 15-20 minutes for US markets

**Why Twelve Data**:
1. **Generous free tier** - 800 calls/day sufficient for personal use
2. **Global coverage** - 60+ exchanges including GPW (Warsaw)
3. **Batch requests** - query multiple symbols in one call
4. **Simple REST API** - clean JSON responses
5. **Legal and official** - proper API with terms of service

**Key Endpoints**:
```
GET /quote?symbol=AAPL&apikey=KEY              # Single quote
GET /quote?symbol=AAPL,MSFT,GOOGL&apikey=KEY   # Batch quotes
```

**Usage Estimate** (personal portfolio):
- 25 instruments x 4 updates/day = 100 calls (with batch: ~10 calls)
- Well within 800/day limit

**Limitations**:
- 15-20 minute delay for US stocks (acceptable for portfolio tracking)
- 800 calls/day (sufficient but monitor usage)
- Free tier excludes real-time WebSocket streaming

### Domain Integration

Both providers are accessed through domain port interfaces:

```java
// Currency rates - implemented by NBP adapter
public interface ExchangeRateProvider {
    ExchangeRate getExchangeRateToPln(Currency source);
    Map<Currency, ExchangeRate> getExchangeRatesToPln(Iterable<Currency> currencies);
}

// Stock prices - implemented by Twelve Data adapter (future)
public interface InstrumentPriceProvider {
    Price getCurrentPrice(InstrumentSymbol symbol);
    Map<InstrumentSymbol, Price> getCurrentPrices(List<InstrumentSymbol> symbols);
}
```

### Caching Strategy

- **NBP rates**: Cache for 24 hours (rates update once daily)
- **Twelve Data prices**: Cache for 15 minutes (matches data delay)
- Caching reduces API call count and improves response times

### Error Handling

- Track Twelve Data daily call count to avoid exceeding limits
- Implement exponential backoff on failures
- Cache last successful response as fallback for temporary outages
- NBP API very reliable (government-backed), minimal error handling needed

## Consequences

### Positive

1. **Zero cost** - both APIs completely free for personal use
2. **Official PLN rates** - most authoritative source for Polish currency
3. **Global market coverage** - US, European, and Polish stocks/ETFs
4. **Separation of concerns** - independent providers for independent functions
5. **Reliability** - if one fails, the other still works
6. **Hexagonal architecture** - providers behind port interfaces, easy to swap

### Negative

1. **Two integrations to maintain** - more code than single provider
2. **NBP daily rates only** - no intraday currency updates
3. **Twelve Data delayed data** - 15-20 min delay for US stocks
4. **Twelve Data daily limit** - 800 calls/day requires monitoring

### Mitigation

1. **Provider abstraction** - port interfaces make swapping easy
2. **Daily rates sufficient** - portfolio tracking doesn't need real-time currency
3. **Delayed data acceptable** - not a trading platform
4. **Caching + batching** - reduces actual API calls significantly

## Alternatives Considered

### Alternative 1: Alpha Vantage (Single Provider)

Both stocks and forex from one API. **Rejected**: Only 25 calls/day total - too restrictive for 20+ instrument portfolio.

### Alternative 2: Yahoo Finance (Unofficial)

Free, good coverage, no auth needed. **Rejected**: Unofficial API, no SLA, can break anytime, may violate ToS. Not suitable for reliable portfolio tracking.

### Alternative 3: ExchangeRate-API (Currency Only)

1,500 calls/month, good PLN support. **Not adopted**: NBP API is better (unlimited, official, no auth).

### Alternative 4: Finnhub

60 calls/minute, real-time US data. **Rejected**: PLN currency pair support unclear, primarily US-focused.

### Alternative 5: Paid Solution

Twelve Data Pro ($9/month). **Deferred**: Free tier sufficient for personal use. Can upgrade if needed.

## Related Decisions

- [ADR-006: Money Representation](ADR-006-money-representation.md) - Currency storage and conversion
- [ADR-005: Database Schema](ADR-005-database-schema.md) - Multi-currency columns

## Implementation Notes

### Phase 1 (Current): ExchangeRateProvider

Port interface `ExchangeRateProvider` created in domain layer. NBP adapter implementation to follow.

### Phase 2 (Future): InstrumentPriceProvider

Port interface for stock/ETF prices. Twelve Data adapter implementation planned for price management version.

### API Key Management

- Twelve Data API key stored in application properties (not committed to git)
- NBP API requires no credentials
- Use Spring's `@Value` or environment variables for API key injection
