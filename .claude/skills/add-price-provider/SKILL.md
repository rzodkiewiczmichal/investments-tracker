---
name: add-price-provider
description: Add or replace a market price provider or exchange-rate provider (new market such as DE, new data source, or a replacement for a broken provider). Use when prices are missing for a market, a provider API changes or breaks, or the user wants a new price source.
---

# Add / replace a price provider

Conventions:
- `.claude/rules/external-integrations.md`: ACL, provider quirks, the router.
- `.claude/rules/infrastructure.md`: cache-aside and bean wiring.

Decisions: ADR-030 (exchange rates), ADR-031 (price providers), ADR-032 (caching).

## 1. Research and decide (with the user)
- Probe the candidate API with `curl` against real symbols from the catalog: GPW, UK, US and DE samples.
- Check:
  - authentication and free-tier limits (zero third-party budget, NFR-092),
  - whether it supports batch requests,
  - currency and units (GBX vs GBP),
  - its no-data markers,
  - terms of use and anti-bot protection.
- Present a short comparison to the user. A new or replaced provider means an ADR-031 amendment, or a new ADR via the `new-requirement` skill.

## 2. Implement
1. Write the client in `infrastructure/external/<provider>/`, taking `RestClient` from a `<Provider>ApiConfig` in `infrastructure/config`. Base URL and paths go in `application.yml` under `app.<provider>`, with env overrides; secrets come from env vars only.
2. Put the ACL inside the client: domain `TICKER.MARKET` ↔ provider symbol, and provider units → domain `Price` in the instrument currency.
3. If the provider is optional or keyed, make it degrade gracefully when unconfigured (the Finnhub pattern).
4. Wire it into `PriceProviderRouter` for its `Market` value(s). Add a new `Market` value only after discussing it with the user, because it's a domain change.
5. Leave caching alone: `CachingCurrentPriceAdapter` already wraps the router. Never make the provider implement the domain port directly.

## 3. Test
- Unit test with a stubbed HTTP response, covering: success, no data, wrong units, HTML or error body, and timeouts.
- Add a `*LiveTest` (`./gradlew liveTest`, never run in CI) that hits the real API for one or two symbols.
- Run `./gradlew spotlessApply && ./gradlew clean build`, then check manually with `./dev.sh start` that portfolio prices show up.

## 4. Docs
Update ADR-031 or the new ADR, `docs/adr/README.md`, the provider section of `.claude/rules/external-integrations.md`, and `scripts/check-price.sh` in the `add-instruments` skill.
