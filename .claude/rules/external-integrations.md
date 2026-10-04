---
paths:
  - "src/main/java/**/infrastructure/external/**"
  - "src/main/java/**/infrastructure/cache/**"
  - "src/test/java/**/infrastructure/external/**"
---
# External price & FX providers (ADR-030, ADR-031)

**Anti-corruption layer:** never pass domain symbols (`TICKER.MARKET`) straight to an external API. Translate them in the adapter.
- Finnhub: strip `.US` before the call and append `.US` when syncing.
- Stooq: `CDR.PL` → `cdr`, `CSPX.UK` → `cspx.uk`.
- Dotted tickers are flattened: `BRK.B` → `BRKB.US`.

**PriceProviderRouter** groups symbols by `Market` and dispatches them: GPW and UK go to Stooq, US goes to Finnhub. DE has no provider yet.

**Stooq** (CSV, no auth):
- In a batch request, separate symbols with spaces, not commas.
- GPW ETFs need the `.pl` suffix (`etfsp500.pl`).
- Fetch UK instruments one at a time; a batch returns N/D for them.
- UK prices come back in GBX: divide by 100 to get GBP.
- No-data markers are `N/D` and `B/D`.

**Finnhub** (US):
- Uses `RestClient` and the `FINNHUB_API_KEY` env var. Free tier allows 60 req/min.
- If the key is missing, the client degrades gracefully (`@Autowired(required = false)` plus a no-arg fallback).
- Catalog sync: `POST /api/v1/instruments/sync`.

**NBP**: PLN rates for EUR, GBP and USD. Free, no auth.

Every new provider needs a `*LiveTest`. Live tests never run in CI.
