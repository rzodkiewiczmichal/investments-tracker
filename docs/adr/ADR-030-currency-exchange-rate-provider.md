# ADR-030: Currency Exchange Rate Provider (NBP API)

## Status
Accepted (revised 2026-02-13, narrowed scope from original 2026-02-08 version)

## Context

The Investment Tracker stores cost basis and prices in their original currencies (EUR, USD, GBP, PLN) and converts to PLN at query time (FR-089). This requires reliable, up-to-date currency exchange rates for PLN.

### Requirements

- Free tier sufficient for personal use
- PLN exchange rates (EUR/PLN, GBP/PLN, USD/PLN)
- Daily update frequency acceptable
- Reliable for a personal hobby project (occasional downtime acceptable)

### Research Findings (February 2026)

A comprehensive evaluation of currency rate APIs was conducted. Key findings:

- NBP API (Polish National Bank) is the clear winner: free, unlimited, no auth, official PLN rates
- Commercial alternatives (ExchangeRate-API, Fixer.io, Open Exchange Rates) offer no advantage over NBP for PLN

## Decision

We use the **NBP API** (Narodowy Bank Polski) as the sole currency exchange rate provider.

**URL**: https://api.nbp.pl
**Authentication**: None required (public API)
**Rate Limit**: Unlimited (reasonable use expected)
**Update Frequency**: Daily on working days (~11:45-12:15 CET)

### Why NBP

1. **Official source** - Narodowy Bank Polski, used by banks and financial institutions
2. **Free and unlimited** - no registration, no API key, no rate limits
3. **Perfect for PLN** - all rates expressed relative to PLN (native base currency)
4. **Government-backed** - highest reliability, stable API for 10+ years
5. **Simple REST API** - clean endpoints, JSON responses

### Key Endpoints

```
GET /api/exchangerates/rates/a/{currency}/          # Single currency rate (e.g., /a/eur/)
GET /api/exchangerates/rates/a/{currency}/today/    # Today's rate
GET /api/exchangerates/tables/a/                    # All Table A rates (35 currencies)
```

### Response Example

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

### Limitations

- Returns 404 on weekends and Polish holidays (no rates published)
- Daily updates only (no intraday)
- Separate call per currency (no batch endpoint for specific currencies)
- Polish field names in some responses (e.g., "dolar amerykanski")

### Weekend/Holiday Handling

- Cache last business day rate in database
- On 404, serve cached rate flagged as stale
- Fetch schedule: once daily at 13:00 CET (after NBP publishes ~12:00)

### Domain Integration

The NBP adapter implements the domain port interface:

```java
public interface ExchangeRateProvider {
    ExchangeRate getExchangeRateToPln(Currency source);
    Map<Currency, ExchangeRate> getExchangeRatesToPln(Iterable<Currency> currencies);
}
```

### Caching Strategy

| Provider | Cache Duration | Rationale |
|----------|---------------|-----------|
| NBP rates | 24 hours | Rates update once daily on business days |

### API Key Management

No credentials needed - NBP API is fully public.

## Consequences

### Positive

1. **Zero cost** - completely free for personal use
2. **Official PLN rates** - most authoritative source for Polish currency conversion
3. **No auth complexity** - no API keys to manage or rotate
4. **Hexagonal architecture** - provider behind port interface, easy to swap
5. **Government stability** - lowest risk of API disappearing or changing

### Negative

1. **PLN-only base currency** - rates are always X/PLN; if non-PLN base currency needed in the future, cross-rate calculation required
2. **No intraday rates** - daily snapshots only
3. **Weekend gaps** - requires caching strategy for non-business days

### Mitigation

1. **Provider abstraction** - port interface makes swapping providers trivial
2. **Caching** - reduces dependency on provider availability and covers weekends/holidays

## Alternatives Considered

### ExchangeRate-API, Fixer.io, Open Exchange Rates

Commercial currency rate APIs. **Not adopted**: NBP is free, unlimited, and provides official PLN rates. These commercial services add cost and API key management with no benefit for PLN-centric use case.

### Alpha Vantage (Forex)

Provides forex rates alongside stocks. **Rejected**: Only 25 calls/day total, shared with any stock price calls.

## Related Decisions

- [ADR-006: Money Representation](ADR-006-money-representation.md) - Currency storage and conversion
- [ADR-005: Database Schema](ADR-005-database-schema.md) - Multi-currency columns
- [ADR-031: Instrument Price Providers](ADR-031-instrument-price-providers.md) - Stock/ETF price fetching (separate concern)
