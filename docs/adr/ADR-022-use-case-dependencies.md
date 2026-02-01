# ADR-022: Application Core Operates on Domain Model Only

## Status
Accepted

## Context

Hexagonal architecture separates application core (use cases) from infrastructure adapters. A key principle is that the application core should express operations in domain language, independent of any particular adapter's data format.

The application core includes:
- **Use cases (input ports)**: Define what the application can do
- **Output ports**: Define what the application needs from external systems

Both must remain adapter-agnostic to preserve flexibility and testability.

## Decision

### Rule

**Use cases and output ports operate exclusively on domain model types.**

This means:
- **Input**: Accept domain value objects, entities, or primitives
- **Output**: Return domain objects (entities, value objects, aggregates)
- **No DTOs**: Request/Response DTOs belong to adapters, not application core

### Use Case Example

```java
public interface PositionCommandUseCase {
    Position addPosition(InstrumentSymbol symbol, AccountId accountId,
                         Quantity quantity, CostBasis costBasis, Price currentPrice);
}
```

### Output Port Example

```java
public interface PositionRepository {
    Position save(Position position);
    Optional<Position> findBySymbol(InstrumentSymbol symbol);
    Collection<Position> findAll();
}
```

### Data Flow

```
Adapter (infrastructure) → Use Case (application) → Domain
       ↓                          ↓                    ↓
   DTO → domain              pure domain           pure domain
       ↓                          ↓                    ↓
   domain → DTO  ←───────── domain result ←────────────┘
```

Adapters are responsible for:
- Parsing external formats (JSON, CSV, etc.) into domain objects
- Mapping domain objects to external formats (HTTP responses, events, etc.)

### Allowed Dependencies

| Application Core Component | Allowed Dependencies |
|---------------------------|---------------------|
| Use case interfaces | Domain model only |
| Use case implementations | Domain model, domain services, output ports, application exceptions |
| Output port interfaces | Domain model only |

### Prohibited Dependencies

Application core (use cases, output ports) must NOT depend on:
- DTOs (Request, Response, Command)
- Mappers
- Framework-specific types (except @Service, @Transactional for implementations)
- Infrastructure adapters

## Consequences

### Positive

1. **Adapter independence**: Use cases work with any adapter (REST, CLI, events, schedulers)
2. **Domain language**: Interfaces express business operations, not technical formats
3. **Testability**: Tests use domain objects directly without adapter concerns
4. **Single responsibility**: Mapping is adapter's job, orchestration is use case's job

### Negative

1. **Adapter complexity**: Adapters handle mapping (but this is their responsibility)
2. **More parameters**: Use case methods may have multiple parameters

### ArchUnit Enforcement

```java
@ArchTest
static final ArchRule use_cases_should_not_depend_on_dtos =
    noClasses()
        .that().resideInAPackage("..application.usecase..")
        .should().dependOnClassesThat()
        .resideInAnyPackage("..dto..");

@ArchTest
static final ArchRule output_ports_should_not_depend_on_dtos =
    noClasses()
        .that().resideInAPackage("..domain.repository..")
        .or().resideInAPackage("..domain.port..")
        .should().dependOnClassesThat()
        .resideInAnyPackage("..dto..");
```

## Related Decisions

- [ADR-003: Domain vs Application Services](ADR-003-domain-vs-application-services.md)
- [ADR-004: Package Structure](ADR-004-package-structure.md)

## References

- Hexagonal Architecture by Alistair Cockburn
- Clean Architecture by Robert C. Martin
- NFR-063: Hexagonal architecture
- NFR-064: Framework-independent domain model
