# ADR-032: External Data Caching Strategy

## Status
Accepted (revised 2026-02-14, changed from PostgreSQL-as-cache to Redis)

## Context

The Investment Tracker consumes three external APIs for financial data (ADR-030, ADR-031):

| Provider | Data | Update Frequency |
|----------|------|-----------------|
| NBP API | Currency exchange rates (EUR/PLN, GBP/PLN, USD/PLN) | Once daily ~12:00 CET on business days |
| Stooq.pl | GPW stock/ETF prices | End-of-day after GPW close (17:00 CET) |
| Finnhub | US stock/ETF prices | End-of-day after US close (16:00 ET / 22:00 CET) |

The application reads this data at query time (e.g., when displaying portfolio). Without caching, every portfolio view would trigger external API calls. This is wasteful and risky:

1. **Day precision is sufficient** — the user does not need intraday prices or real-time exchange rates. End-of-day values are perfectly adequate for a portfolio tracker.
2. **API abuse** — calling external APIs on every page refresh wastes quota (Stooq has undocumented low daily limits) and is disrespectful to free providers.
3. **Latency** — external HTTP calls add 200-1000ms per request. Portfolio views should be fast.
4. **Availability** — if an external API is temporarily down, the application should still show data (possibly stale).
5. **Weekends/holidays** — NBP returns 404, Stooq and Finnhub return stale data. The app must handle this gracefully.

### Schema Purity Concern

The original version of this ADR stored cached prices directly in the `instruments` table (`current_price_amount`, `current_price_currency`, `price_updated_at`). This mixes two distinct concerns:

- **Reference data** (symbol, name, type, currency) — stable, user-managed, part of the domain model.
- **Cached external data** (current price, exchange rates) — volatile, externally-sourced, changes daily.

These have different lifecycles and different owners. Mixing them in the same table and the same domain entity (`Instrument.currentPrice`) violates separation of concerns.

### Learning Goals

This is a training project. Using Redis as a dedicated caching layer provides hands-on experience with polyglot persistence, cache-aside patterns, Spring Data Redis, and Testcontainers multi-container setups.

## Decision

### Core Principle: Fetch Once Daily, Serve from Redis

All external financial data (current prices, exchange rates) is cached in **Redis**. The application reads from Redis on every query. External APIs are called **at most once per calendar day per data point**.

PostgreSQL stores only domain data (accounts, positions, instruments as reference data). Redis stores only volatile cached data (prices, exchange rates).

### Cache-Aside Pattern

```
User requests portfolio
    │
    ├─ Read exchange rates from Redis (via ExchangeRateProvider port)
    │   └─ If key exists and not expired → use it
    │   └─ If missing → fetch from NBP API → write to Redis (TTL 24h) → use it
    │
    ├─ Read instrument prices from Redis (via PriceCache port)
    │   └─ If key exists and not expired → use it
    │   └─ If missing → fetch from Stooq/Finnhub → write to Redis (TTL 24h) → use it
    │
    ├─ Read instrument reference data from PostgreSQL (InstrumentRepository)
    │
    └─ Calculate and return portfolio
```

Data is fetched **lazily on first read of the day**, not on a scheduled timer.

### Redis Data Structures

#### Instrument Prices

Redis Hash per instrument symbol:

- **Key**: `price:current:{symbol}` (e.g., `price:current:PKO`, `price:current:AAPL`)
- **Fields**: `amount` (decimal string), `currency` (e.g., "PLN", "USD"), `updatedAt` (ISO timestamp)
- **TTL**: 24 hours

#### Exchange Rates

Redis Hash per currency pair:

- **Key**: `rate:current:{from}:{to}` (e.g., `rate:current:EUR:PLN`, `rate:current:USD:PLN`)
- **Fields**: `rate` (decimal string), `effectiveDate` (ISO date), `updatedAt` (ISO timestamp)
- **TTL**: 24 hours

### Domain Model Change

The `Instrument` entity becomes pure reference data — no cached price fields:

```java
public record Instrument(
        InstrumentSymbol symbol,
        InstrumentName name,
        InstrumentType type,
        Currency currency) { }
```

Prices are fetched separately at query time via a new domain port:

```java
public interface PriceCache {
    Optional<Price> getPrice(InstrumentSymbol symbol);
    Map<InstrumentSymbol, Price> getPrices(Iterable<InstrumentSymbol> symbols);
    void putPrice(InstrumentSymbol symbol, Price price);
}
```

The existing `ExchangeRateProvider` port remains unchanged. Its Redis-backed implementation caches rates internally using the decorator pattern.

### Read Path

**Cache hit (typical):**
1. `PortfolioQueryUseCaseService` calls `PriceCache.getPrices(symbols)` → Redis HGETALL.
2. All prices exist in Redis → return immediately. Sub-millisecond latency.
3. Calls `ExchangeRateProvider.getExchangeRatesToPln(currencies)` → Redis HGETALL internally.
4. Calculate and return portfolio.

**Cache miss (first load of the day):**
1. `PriceCache.getPrices(symbols)` returns partial or empty map.
2. Application layer fetches missing prices from `InstrumentPriceProvider` (Stooq/Finnhub).
3. Writes fetched prices to `PriceCache` (Redis HSET with TTL 24h).
4. Calculates and returns portfolio.

### Fallback When Redis Is Down

If Redis is unavailable, the application falls back to fetching directly from external APIs on every request (no caching). This is acceptable for a single-user project: slightly slower but functional.

### Freshness Rules

| Data | Considered Fresh If | Fetch Window |
|------|-------------------|--------------|
| NBP exchange rates | `effectiveDate = today` (or last business day on weekends) | After 12:15 CET on business days |
| Stooq GPW prices | `updatedAt` is after today's 17:05 CET | After 17:05 CET on trading days |
| Finnhub US prices | `updatedAt` is after today's 22:05 CET | After 22:05 CET on trading days |

### Staleness Handling

When fresh data is unavailable (weekend, holiday, API down), the application serves the **most recent cached value** from Redis:

- The data is usable — it represents the last known market state.
- The UI may optionally indicate the data date (e.g., "Prices as of 2026-02-07").
- No error is thrown for stale data — it is expected behavior on non-trading days.

### Fetch Deduplication

Multiple concurrent portfolio requests must not trigger multiple API calls for the same data. The fetch operation is idempotent: if data already exists in Redis when the API response arrives, the write overwrites with the same value (idempotent HSET).

### No Background Scheduler

Data is fetched on demand (lazy), not on a cron schedule. Rationale:

1. **Simplicity** — no scheduler infrastructure, no missed-job handling, no startup ordering.
2. **No waste** — if the user doesn't open the app on a given day, no API calls are made.
3. **Sufficient for personal use** — the first portfolio load of the day may be ~1-2 seconds slower; subsequent loads are instant from Redis.

### No Price History

Price history storage (e.g., via TimescaleDB or a dedicated history table) is **explicitly out of scope**. It belongs to v1.0 (FR-056). Redis stores only the current value per instrument — no historical data.

### Future: Domain Events

When domain events are introduced (via message queue such as RabbitMQ), the write path will publish `PriceUpdated` and `ExchangeRateUpdated` events. Downstream consumers can use these events for notifications, analytics, or populating future price history stores. Redis caching will remain as the fast-read layer regardless.

### Per-Provider Details

#### NBP API (Exchange Rates)

- **Fetch trigger**: First portfolio query of the day after 12:15 CET.
- **Weekend/holiday**: NBP returns 404. Use last cached rate from Redis. Do not retry until next business day.
- **Storage**: Write to Redis via `ExchangeRateProvider` cache decorator (`rate:current:{from}:{to}`).
- **Freshness**: `effectiveDate` from API response matches today → fresh.
- **Polish holidays**: Same as weekends — NBP publishes no data. App uses Friday's rate through the weekend.

#### Stooq.pl (GPW Prices)

- **Fetch trigger**: First portfolio query of the day after 17:05 CET (GPW close + buffer).
- **Before market close**: If queried before 17:05 CET, serve yesterday's cached price (don't fetch partial intraday data).
- **Weekend**: GPW closed. Serve Friday's price. Do not call Stooq.
- **Batch**: Fetch all GPW instruments in a single request (comma-separated symbols).
- **Storage**: Write to Redis via `PriceCache.putPrices()` (`price:current:{symbol}`).

#### Finnhub (US Prices)

- **Fetch trigger**: First portfolio query of the day after 22:05 CET (US close + buffer).
- **Before market close**: If queried before 22:05 CET, serve yesterday's cached price.
- **Weekend**: US markets closed. Serve Friday's price. Do not call Finnhub.
- **Sequential**: Finnhub has no batch endpoint. Fetch symbols one by one within 60 req/min limit.
- **Storage**: Write to Redis via `PriceCache.putPrice()` (`price:current:{symbol}`).

### API Call Budget (Worst Case Daily)

| Provider | Calls/Day | Within Limits? |
|----------|----------|----------------|
| NBP | 3 (one per currency: EUR, USD, GBP) | Unlimited — no concern |
| Stooq | 1 (batch all GPW symbols) | Well within undocumented quota |
| Finnhub | ~50 (one per US instrument) | 60/min limit — takes <1 minute |
| **Total** | ~54 | All providers comfortable |

### Database Schema Change

Remove cached price columns from the `instruments` table:

- DROP COLUMN `current_price_amount`
- DROP COLUMN `current_price_currency`
- DROP COLUMN `price_updated_at`
- DROP CONSTRAINT `instruments_price_positive`
- DROP INDEX `idx_instruments_price_updated`

The `instruments` table becomes pure reference data: `symbol`, `name`, `instrument_type`, `currency`, `created_at`, `updated_at`, `version`.

### Infrastructure

- **Redis**: Added to Docker Compose (`redis:7-alpine`, port 6379)
- **Spring Data Redis**: `spring-boot-starter-data-redis` with Lettuce client
- **Testcontainers**: Redis container alongside PostgreSQL in integration and Cucumber tests

## Consequences

### Positive

1. **Schema purity** — `instruments` table is pure reference data; volatile cache lives in Redis
2. **Domain model purity** — `Instrument` entity has no cached fields; prices fetched via dedicated port
3. **Fast reads** — Redis sub-millisecond reads for portfolio queries
4. **Native TTL** — Redis handles cache expiration automatically; no manual staleness checks
5. **Minimal API usage** — at most ~54 calls/day across all providers, far below any limits
6. **Resilient** — app works even when external APIs are down (serves cached data from Redis)
7. **Learning value** — hands-on experience with polyglot persistence, Spring Data Redis, cache-aside patterns

### Negative

1. **Additional infrastructure** — Redis must be running alongside PostgreSQL (extra Docker container)
2. **First load of the day is slower** — triggers external API calls (~1-2s extra latency)
3. **No pre-market prices** — before market close, app shows previous day's data
4. **Redis dependency** — if Redis is down, falls back to direct API calls (slower)

### Mitigation

1. **Simple setup** — Redis in Docker Compose is a single service definition
2. **Perceived performance** — UI can show cached data immediately and refresh in background
3. **Explicit freshness** — UI can display "Prices as of {date}" when data is from a previous day
4. **Graceful fallback** — application functions without Redis, just without caching

## Related Decisions

- [ADR-030: Currency Exchange Rate Provider](ADR-030-currency-exchange-rate-provider.md) — NBP API details
- [ADR-031: Instrument Price Providers](ADR-031-instrument-price-providers.md) — Stooq and Finnhub details
- [ADR-005: Database Schema](ADR-005-database-schema.md) — PostgreSQL schema (reference data only)
