---
paths:
  - "src/main/java/**"
---
# Architecture & package placement

Hexagonal (ports & adapters) + DDD, enforced by ArchUnit (`src/test/java/**/architecture/`). Do not modify ArchUnit tests without explicit approval.

```
com.investments.tracker
├── domain                 pure Java, no Spring
│   ├── model/             entities/aggregates (Account, Position, Instrument, ImportSession, ...)
│   ├── model/value/       value objects (Money, Quantity, Price, CostBasis, Currency, Market, ...)
│   ├── repository/        driven ports owned by the domain (*Repository, *Provider)
│   ├── service/           pure domain services (no annotations, registered in DomainServiceConfig)
│   └── exception/         DomainException hierarchy
├── application            depends on domain only
│   ├── usecase/           *UseCase interfaces AND *UseCaseService implementations
│   ├── port/out/          application-level driven ports (TransactionHistoryParser, ParseResult)
│   ├── dto/{request,response,mapper}/
│   └── exception/         ResourceNotFoundException (404), ResourceAlreadyExistsException (409)
└── infrastructure         Spring, adapters
    ├── web/controller/    REST controllers, base path /api/v1
    ├── persistence/       JdbcEntity, JdbcRepository, PersistenceMapper, RepositoryAdapter
    ├── external/          stooq, finnhub, nbp, mbank, xtb, degiro
    ├── cache/             Redis cache-aside adapters, PriceProviderRouter
    └── config/            @Configuration
```
Empty `.gitkeep` packages (`application/service`, `application/port/in`, `domain/model/aggregates/*`, `events`, `valueobjects`) are unused placeholders. Do not put code there.

## Port placement
- Driven port that the **domain** needs (repositories, price and exchange-rate providers) → `domain/repository/`.
- Driven port that only the **application** needs (e.g. broker file parsing) → `application/port/out/`.
- Port names describe a domain capability and must not leak mechanism: `CurrentPriceProvider`, not `PriceCache`. (The legacy `PriceCache` and `RedisPriceCacheAdapter` are unused by production code.)
- Flag any uncertain placement to the user instead of guessing.

## ArchUnit naming (build fails otherwise; nested classes excluded)
- `domain.service..` → `*Service`
- `domain.repository..` interfaces → `*Repository` | `*Provider` | `*Cache`
- `infrastructure.web.controller..` → `*Controller`
- `application.dto.mapper..` → `*Mapper`

## Gotcha
The `.gitignore` pattern `/out/` must stay root-anchored so it never matches `application/port/out/`.
