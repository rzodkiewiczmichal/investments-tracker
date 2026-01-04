# ADR-004: Package Structure

## Status
Accepted

## Context

Hexagonal architecture requires clear physical separation between domain, application, and infrastructure layers. Package structure must:

1. **Enforce Architectural Boundaries**: Prevent domain from depending on infrastructure
2. **Support Testing**: Enable testing domain without infrastructure
3. **Guide Developers**: Make it obvious where new code belongs
4. **Enable ArchUnit Validation**: Allow automated architecture tests

Key requirements:
- Domain layer must be framework-independent (NFR-064)
- Hexagonal architecture with ports and adapters (NFR-063)
- Domain-Driven Design best practices (NFR-071)
- Support for multi-module separation (future: separate JARs for domain/application/infrastructure)

Organizational principles:
- **Layer-first**: Top-level packages by layer (domain, application, infrastructure)
- **Feature slices**: Within layers, organize by domain concept
- **Port/Adapter separation**: Clear distinction between interfaces and implementations

## Decision

### Root Package

```
com.investments.tracker
```

**Rationale**: Follows Java conventions, company domain reversed (investments before tracker)

### Complete Package Structure

```
com.investments.tracker/
│
├── domain/                                    # Core business logic (NO framework dependencies)
│   ├── model/                                 # Entities and Aggregates
│   │   ├── Position.java                      # Aggregate root
│   │   ├── AccountHolding.java                # Entity within Position aggregate
│   │   ├── Account.java                       # Aggregate root
│   │   ├── Portfolio.java                     # Aggregate root
│   │   ├── Instrument.java                    # Entity (reference data)
│   │   └── value/                             # Value Objects
│   │       ├── Money.java
│   │       ├── Quantity.java
│   │       ├── Price.java
│   │       ├── CostBasis.java
│   │       ├── InvestedAmount.java
│   │       ├── CurrentValue.java
│   │       ├── ProfitAndLoss.java
│   │       ├── XIRR.java
│   │       ├── InstrumentSymbol.java
│   │       ├── Percentage.java
│   │       └── Currency.java
│   ├── service/                               # Domain Services (pure business logic)
│   │   ├── PositionAggregationService.java
│   │   ├── CalculationService.java
│   │   └── XIRRCalculationService.java
│   ├── repository/                            # Repository Ports (interfaces)
│   │   ├── PositionRepository.java
│   │   ├── AccountRepository.java
│   │   ├── InstrumentRepository.java
│   │   └── PortfolioRepository.java
│   ├── port/                                  # Additional Ports
│   │   └── out/                               # Driven/Output Ports
│   │       ├── PriceFetchingPort.java
│   │       └── FileReaderPort.java
│   └── exception/                             # Domain Exceptions
│       ├── DomainException.java               # Base exception
│       ├── PositionNotFoundException.java
│       ├── InvalidQuantityException.java
│       ├── InvalidPriceException.java
│       └── PriceUnavailableException.java
│
├── application/                               # Use Cases and Orchestration (minimal Spring)
│   ├── service/                               # Application Services (orchestration)
│   │   ├── PortfolioViewingService.java
│   │   ├── PortfolioViewingServiceImpl.java
│   │   ├── ManualEntryService.java
│   │   ├── ManualEntryServiceImpl.java
│   │   ├── ImportService.java                 # v0.2+
│   │   ├── ImportServiceImpl.java             # v0.2+
│   │   ├── ReconciliationService.java         # v0.7+
│   │   └── ReconciliationServiceImpl.java     # v0.7+
│   ├── port/                                  # Optional: Use Case Ports
│   │   └── in/                                # Driving/Input Ports (use case interfaces)
│   │       ├── ViewPortfolioUseCase.java      # Optional: explicit input port
│   │       ├── AddPositionUseCase.java
│   │       └── ImportPositionsUseCase.java
│   ├── dto/                                   # Data Transfer Objects
│   │   ├── PortfolioSummaryDTO.java
│   │   ├── PositionSummaryDTO.java
│   │   ├── PositionDetailDTO.java
│   │   ├── AddPositionCommand.java
│   │   ├── UpdatePositionCommand.java
│   │   ├── ImportFileCommand.java
│   │   └── ReconciliationReportDTO.java
│   └── exception/                             # Application Exceptions
│       ├── ApplicationException.java
│       ├── ValidationException.java
│       └── ImportFailedException.java
│
├── infrastructure/                            # Adapters (framework-specific implementations)
│   ├── web/                                   # Web/REST Adapter (Driving)
│   │   ├── controller/
│   │   │   ├── PortfolioController.java
│   │   │   ├── PositionController.java
│   │   │   └── ImportController.java
│   │   ├── dto/                               # Web-specific DTOs (if needed)
│   │   └── exception/                         # Exception handlers
│   │       └── GlobalExceptionHandler.java    # @RestControllerAdvice
│   ├── persistence/                           # Persistence Adapter (Driven)
│   │   └── jpa/
│   │       ├── entity/                        # JPA Entities
│   │       │   ├── PositionJpaEntity.java
│   │       │   ├── AccountHoldingJpaEntity.java
│   │       │   ├── AccountJpaEntity.java
│   │       │   └── InstrumentJpaEntity.java
│   │       ├── repository/                    # Spring Data JPA Repositories
│   │       │   ├── PositionJpaRepository.java # extends JpaRepository
│   │       │   ├── AccountJpaRepository.java
│   │       │   └── InstrumentJpaRepository.java
│   │       └── adapter/                       # Repository Adapters (implements domain ports)
│   │           ├── PositionRepositoryAdapter.java
│   │           ├── AccountRepositoryAdapter.java
│   │           └── InstrumentRepositoryAdapter.java
│   ├── external/                              # External API Adapters (Driven)
│   │   └── yahoo/                             # Yahoo Finance integration (v0.2+)
│   │       ├── YahooFinanceAdapter.java       # implements PriceFetchingPort
│   │       ├── YahooFinanceClient.java
│   │       └── dto/
│   │           └── YahooQuoteResponse.java
│   ├── file/                                  # File I/O Adapters (v0.2+)
│   │   └── csv/
│   │       ├── CsvFileReaderAdapter.java      # implements FileReaderPort
│   │       └── CsvRowMapper.java
│   └── config/                                # Spring Configuration
│       ├── ApplicationConfig.java             # Bean definitions
│       ├── PersistenceConfig.java             # JPA configuration
│       ├── WebConfig.java                     # Web MVC configuration
│       └── ObservabilityConfig.java           # OTLP/Micrometer configuration
│
├── shared/                                    # Cross-cutting concerns (use sparingly)
│   └── util/
│       └── DateTimeUtils.java
│
└── InvestmentTrackerApplication.java          # Spring Boot main class
```

### Layer Dependencies

```
Infrastructure → Application → Domain
     ↓               ↓
  (adapters)      (use cases)     (business logic)

ALLOWED:
- Infrastructure can depend on Application and Domain
- Application can depend on Domain
- Domain depends on NOTHING (pure Java)

FORBIDDEN:
- Domain → Application (NEVER)
- Domain → Infrastructure (NEVER)
- Application → Infrastructure (AVOID, use ports)
```

### Naming Conventions

#### Domain Layer

- **Entities**: `Position`, `Account`, `Portfolio` (plain nouns)
- **Value Objects**: `Money`, `Quantity`, `Price` (plain nouns, use Java records)
- **Domain Services**: `PositionAggregationService`, `CalculationService` (ends with Service)
- **Repository Interfaces**: `PositionRepository`, `AccountRepository` (ends with Repository)
- **Ports**: `PriceFetchingPort`, `FileReaderPort` (ends with Port)
- **Exceptions**: `InvalidQuantityException`, `PositionNotFoundException` (ends with Exception)

#### Application Layer

- **Application Services**: `PortfolioViewingServiceImpl`, `ManualEntryServiceImpl` (ends with ServiceImpl)
- **DTOs**: `PortfolioSummaryDTO`, `AddPositionCommand` (ends with DTO or Command)
- **Use Case Interfaces**: `ViewPortfolioUseCase`, `AddPositionUseCase` (ends with UseCase - optional)

#### Infrastructure Layer

- **JPA Entities**: `PositionJpaEntity`, `AccountJpaEntity` (ends with JpaEntity)
- **JPA Repositories**: `PositionJpaRepository extends JpaRepository` (ends with JpaRepository)
- **Adapters**: `PositionRepositoryAdapter`, `YahooFinanceAdapter` (ends with Adapter)
- **Controllers**: `PortfolioController`, `PositionController` (ends with Controller)

### File Organization Guidelines

1. **One public class per file**: Matches filename
2. **Package-private helpers**: Can be in same file if closely related
3. **Test organization**: Mirror main structure in test/java
4. **Resources**: application.yml, db/migration/ in src/main/resources

### Module Organization (Future)

For v1.0+, consider separating into Gradle modules:

```
investments-tracker/
├── domain/                 # Pure Java module, no framework dependencies
├── application/            # Spring Boot application module
├── infrastructure/         # Infrastructure adapters
└── web/                    # Web layer (optional separate module)
```

**Benefits**: Enforces compile-time dependency checking (domain cannot see Spring)

## Consequences

### Positive

1. **Enforced Boundaries**: ArchUnit can verify domain has no Spring/JPA dependencies
2. **Clear Location**: Developers know where to put new code
3. **Independent Domain**: Domain module can be tested without infrastructure
4. **Scalable**: Structure supports future multi-module separation
5. **Standard**: Follows hexagonal architecture best practices
6. **IDE-Friendly**: Clear package structure in IDE project explorer
7. **Onboarding**: New developers can navigate codebase easily

### Negative

1. **Deep Nesting**: Many levels of packages (but improves organization)
2. **Verbose Paths**: Long import statements (mitigated by IDE auto-import)
3. **Initial Overhead**: More packages to create upfront
4. **Duplication Risk**: JPA entities vs domain entities (but necessary for hexagonal)

### ArchUnit Validation Rules

Create `ArchitectureTest.java` to enforce package structure:

```java
@AnalyzeClasses(packages = "com.investments.tracker")
class ArchitectureTest {

    @ArchTest
    static final ArchRule domain_should_not_depend_on_infrastructure =
        noClasses().that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAPackage("..infrastructure..");

    @ArchTest
    static final ArchRule domain_should_not_depend_on_application =
        noClasses().that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAPackage("..application..");

    @ArchTest
    static final ArchRule domain_should_not_use_spring =
        noClasses().that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAPackage("org.springframework..");

    @ArchTest
    static final ArchRule domain_should_not_use_jpa =
        noClasses().that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAPackage("jakarta.persistence..");

    @ArchTest
    static final ArchRule repositories_must_be_interfaces =
        classes().that().resideInAPackage("..domain.repository..")
            .should().beInterfaces();

    @ArchTest
    static final ArchRule jpa_entities_must_end_with_jpa_entity =
        classes().that().resideInAPackage("..infrastructure.persistence.jpa.entity..")
            .should().haveSimpleNameEndingWith("JpaEntity");

    @ArchTest
    static final ArchRule adapters_must_implement_ports =
        classes().that().haveSimpleNameEndingWith("Adapter")
            .should().implement(interfaces().that().haveSimpleNameEndingWith("Port")
                .or().haveSimpleNameEndingWith("Repository"));
}
```

### Package-by-Layer vs Package-by-Feature

**Decision**: Package-by-Layer at top level, package-by-feature within layers if needed

**Rationale**:
- Hexagonal architecture emphasizes layers
- Clear separation for architectural boundaries
- Within domain layer, could organize by aggregate (e.g., domain/position/, domain/portfolio/) in future
- For v0.1 MVP, flat structure within layers is sufficient

### Implementation Order

1. **Domain Layer First**: Start with value objects and entities (no framework dependencies)
2. **Domain Services**: Implement business logic
3. **Application Services**: Add orchestration layer
4. **Infrastructure Last**: Implement adapters after domain is stable

### Maven/Gradle Configuration

```gradle
// build.gradle structure (example)

dependencies {
    // Domain layer: Pure Java, NO Spring dependencies

    // Application layer: Spring Boot (minimal)
    implementation 'org.springframework.boot:spring-boot-starter'

    // Infrastructure layer: All framework dependencies
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.postgresql:postgresql'

    // Testing
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'com.tngtech.archunit:archunit-junit5'
}
```

### Alternatives Considered

**Alternative 1: Package-by-Feature (Vertical Slices)**
```
com.investments.tracker/
├── position/           # All position-related code
├── portfolio/          # All portfolio-related code
└── account/            # All account-related code
```
- **Rejected**: Harder to enforce layer boundaries
- Hexagonal architecture is layer-first by design
- Cross-cutting concerns (repositories, services) scattered

**Alternative 2: Flat Package Structure**
```
com.investments.tracker/
└── All classes in one package
```
- **Rejected**: No organization, doesn't scale
- Cannot enforce architectural boundaries
- Violates separation of concerns

**Alternative 3: Technology-First Packages**
```
com.investments.tracker/
├── controllers/
├── services/
├── repositories/
└── entities/
```
- **Rejected**: Doesn't align with hexagonal architecture
- No distinction between domain and infrastructure entities
- Mixes layers together

**Alternative 4: Domain as Separate Maven Module (v1.0+)**
```
investments-tracker-domain/        # Pure Java JAR
investments-tracker-application/    # Spring Boot JAR
investments-tracker-infrastructure/ # Infrastructure JAR
```
- **Deferred to v1.0**: Excellent for enforcing boundaries at compile time
- Adds build complexity for MVP
- Consider for future versions

## Related Decisions

- [ADR-001: Aggregate Boundaries](ADR-001-aggregate-boundaries.md) - Aggregates in domain/model/
- [ADR-002: Value Objects and Entities](ADR-002-value-objects-and-entities.md) - Value objects in domain/model/value/
- [ADR-003: Domain vs Application Services](ADR-003-domain-vs-application-services.md) - Services in domain/service/ and application/service/

## References

- Hexagonal Architecture by Alistair Cockburn
- Clean Architecture by Robert C. Martin
- Package by Component vs Package by Layer
- NFR-063: Hexagonal architecture
- NFR-064: Framework-independent domain
- NFR-071: DDD best practices
