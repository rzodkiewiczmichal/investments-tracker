# ADR-012: Test Architecture

## Status
Accepted

## Context

The Investment Tracker requires a comprehensive testing strategy that ensures code quality, enforces architectural boundaries, and supports behavior-driven development. The testing strategy must:

1. **Enforce Architecture**: Prevent violations of hexagonal architecture boundaries (NFR-064)
2. **Support TDD/BDD**: Enable test-driven development with Cucumber (NFR-072)
3. **Achieve Coverage**: Target 70%+ code coverage for v0.1 MVP (NFR-072)
4. **Test Isolation**: Clear separation between unit, integration, and architectural tests
5. **Fast Feedback**: Unit tests run quickly, integration tests use real infrastructure
6. **Maintainability**: Test code mirrors production code structure

### Requirements

- **NFR-072**: TDD with BDD using Cucumber, 70%+ code coverage
- **NFR-064**: Framework-independent domain (enforced via ArchUnit)
- **NFR-036**: PostgreSQL database (requires Testcontainers for integration tests)

### Technology Stack

- **JUnit 5**: Test framework (Jupiter)
- **ArchUnit**: Architecture testing
- **Cucumber**: BDD testing
- **Testcontainers**: Docker-based integration testing
- **Mockito**: Mocking framework
- **AssertJ**: Fluent assertions

## Decision

### Test Organization Strategy

**Decision**: Mirror production code structure in test directory

```
src/
├── main/java/com/investments/tracker/
│   ├── domain/
│   ├── application/
│   └── infrastructure/
└── test/java/com/investments/tracker/
    ├── architecture/           # ArchUnit Tests
    │   ├── HexagonalArchitectureTest.java
    │   ├── DomainLayerRulesTest.java
    │   ├── DependencyRulesTest.java
    │   └── NamingConventionTest.java
    ├── domain/                 # Domain Unit Tests
    │   ├── model/
    │   └── service/
    ├── application/            # Application Layer Tests
    │   ├── port/
    │   └── service/
    ├── infrastructure/         # Integration Tests
    │   ├── adapter/
    │   └── IntegrationTestBase.java
    └── cucumber/               # BDD Tests
        ├── CucumberSpringConfiguration.java
        ├── RunCucumberTest.java
        └── steps/
```

**Rationale**:
- **Discoverability**: Easy to find tests for specific classes
- **Consistency**: Same package structure as production code
- **IDE Support**: IDEs automatically link test and production classes
- **Separation**: Clear boundary between test types

---

### Test Types and Responsibilities

#### 1. Architecture Tests (ArchUnit)

**Location**: `src/test/java/com/investments/tracker/architecture/`

**Purpose**: Enforce hexagonal architecture boundaries and prevent accidental dependencies

**Test Classes**:
- `HexagonalArchitectureTest` - Layer dependency rules
- `DomainLayerRulesTest` - Domain purity (no Spring, JPA, etc.)
- `DependencyRulesTest` - Strict dependency enforcement
- `NamingConventionTest` - Naming conventions (JpaEntity, Repository, etc.)

**Execution**: Every build (fast, no external dependencies)

**Example**:
```java
@Test
void domainShouldNotDependOnSpring() {
    noClasses()
        .that().resideInAPackage("..domain..")
        .should().dependOnClassesThat().resideInAnyPackage("org.springframework..")
        .allowEmptyShould(true)  // Pass on empty packages
        .check(classes);
}
```

**Rationale**:
- **Prevention**: Catches architectural violations during build
- **Documentation**: Tests serve as executable architecture documentation
- **Fast**: No runtime overhead, pure static analysis

---

#### 2. Domain Unit Tests

**Location**: `src/test/java/com/investments/tracker/domain/`

**Purpose**: Test domain logic in isolation (no Spring, no database)

**Test Scope**:
- Value object validation (Money, InstrumentSymbol, Quantity)
- Entity invariants (Position, Account, Instrument)
- Domain service logic (PortfolioCalculationService)
- Aggregate boundary enforcement

**Dependencies**: None (pure Java, no mocks needed for value objects)

**Example**:
```java
@Test
void shouldThrowExceptionWhenQuantityIsZero() {
    assertThatThrownBy(() -> Position.create(
            symbol,
            Money.pln("500"),
            BigDecimal.ZERO,  // Invalid
            account,
            instrument
        ))
        .isInstanceOf(InvalidQuantityException.class)
        .hasMessage("Quantity must be greater than zero");
}
```

**Rationale**:
- **Fast**: No external dependencies, runs in milliseconds
- **Pure**: Tests business logic without infrastructure concerns
- **Focus**: Tests one domain concept at a time

---

#### 3. Application Layer Tests

**Location**: `src/test/java/com/investments/tracker/application/`

**Purpose**: Test use case orchestration with mocked ports

**Test Scope**:
- Use case logic (ManualEntryService, PortfolioViewingService)
- Port interactions (repository calls, external service calls)
- DTO mapping
- Transaction boundaries (verify @Transactional behavior)

**Dependencies**: Mockito for port implementations

**Example**:
```java
@Test
void shouldThrowAccountNotFoundExceptionWhenAccountDoesNotExist() {
    when(accountRepository.findById(unknownAccountId))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.addPosition(command))
        .isInstanceOf(AccountNotFoundException.class)
        .hasMessageContaining("Account with ID " + unknownAccountId + " not found");
}
```

**Rationale**:
- **Isolation**: Tests use case logic without database
- **Fast**: Mocks avoid slow I/O operations
- **Control**: Precise control over port behavior (success, failure, edge cases)

---

#### 4. Infrastructure Integration Tests

**Location**: `src/test/java/com/investments/tracker/infrastructure/`

**Purpose**: Test adapters with real external dependencies

**Test Scope**:
- REST controllers (full request/response cycle)
- JPA repositories (real PostgreSQL via Testcontainers)
- Database migrations (Flyway)
- OTLP tracing integration
- Error handling (GlobalExceptionHandler)

**Dependencies**:
- Testcontainers (PostgreSQL)
- Spring Boot Test (@SpringBootTest)
- MockMvc (for REST controllers)
- Real database

**Base Class**:
```java
@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
public abstract class IntegrationTestBase {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
}
```

**Example**:
```java
@Test
void shouldReturn404WhenPositionNotFound() {
    mockMvc.perform(get("/api/v1/positions/{id}", unknownId))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.message").value(containsString("not found")))
        .andExpect(jsonPath("$.traceId").exists());
}
```

**Rationale**:
- **Real Dependencies**: Tests actual database behavior (transactions, constraints)
- **End-to-End**: Full request through all layers to database
- **Confidence**: Catches integration issues that mocks miss

---

#### 5. Cucumber BDD Tests

**Location**: `src/test/java/com/investments/tracker/cucumber/`

**Purpose**: Business-readable acceptance tests

**Test Scope**:
- User scenarios (FR-001 to FR-046)
- End-to-end workflows (add position → view portfolio)
- Multi-step interactions

**Dependencies**:
- Cucumber-Java
- Cucumber-Spring
- Testcontainers (real database)

**File Structure**:
```
src/test/
├── java/com/investments/tracker/cucumber/
│   ├── RunCucumberTest.java           # JUnit runner
│   ├── CucumberSpringConfiguration.java  # Spring context
│   └── steps/
│       ├── PortfolioSteps.java
│       ├── PositionSteps.java
│       └── AccountSteps.java
└── resources/features/
    ├── portfolio-viewing.feature
    ├── position-management.feature
    └── manual-entry.feature
```

**Example Feature**:
```gherkin
Feature: Portfolio Viewing
  As an investor
  I want to view my portfolio summary
  So that I can track my investment performance

  Scenario: View empty portfolio
    Given I have no positions
    When I request the portfolio summary
    Then the total current value should be 0 PLN
    And the positions count should be 0

  Scenario: View portfolio with positions
    Given I have the following positions:
      | Instrument | Quantity | Average Cost |
      | AAPL       | 100      | 600.00       |
      | CSPX.L     | 50       | 800.00       |
    When I request the portfolio summary
    Then the total invested amount should be 100000 PLN
    And the positions count should be 2
```

**Rationale**:
- **Business Language**: Features readable by non-technical stakeholders
- **Living Documentation**: Features document system behavior
- **Acceptance Criteria**: Maps directly to functional requirements

---

### Test Coverage Strategy

**Decision**: Target 70%+ code coverage, with exceptions

**Coverage by Layer**:

| Layer | Target Coverage | Rationale |
|-------|----------------|-----------|
| Domain | 90%+ | Business logic must be thoroughly tested |
| Application | 80%+ | Use case orchestration critical |
| Infrastructure | 60%+ | Integration tests slower, focus on critical paths |
| Architecture | 100% | All rules must pass (but rules sparse) |

**Exclusions**:
- DTOs (data classes with no logic)
- Configuration classes (Spring Boot auto-configuration)
- Main application class (InvestmentTrackerApplication)
- JPA entity classes (pure data mappers)

**Measurement**: JaCoCo plugin configured in Gradle

**Enforcement**: Build fails if coverage < 70% (can be adjusted per module)

```kotlin
// build.gradle.kts
tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = "0.70".toBigDecimal()  // 70%
            }
        }
    }
}
```

**Rationale**:
- **Focus**: High coverage where business logic lives
- **Pragmatic**: Lower coverage for infrastructure (expensive to test)
- **Quality Gate**: Prevents coverage regression

---

### Test Execution Strategy

**Decision**: Separate fast tests from slow tests

**Test Suites**:

1. **Fast Suite** (Unit + Architecture)
   - Runs in < 10 seconds
   - No external dependencies
   - Runs on every commit (pre-commit hook)
   - Command: `./gradlew test --tests '*Test' -x integrationTest`

2. **Integration Suite** (Infrastructure)
   - Runs in < 60 seconds
   - Uses Testcontainers
   - Runs on push to remote
   - Command: `./gradlew integrationTest`

3. **BDD Suite** (Cucumber)
   - Runs in < 120 seconds
   - Full end-to-end scenarios
   - Runs nightly and before release
   - Command: `./gradlew cucumberTest`

4. **Full Suite** (All)
   - Runs in < 3 minutes
   - All tests
   - Runs in CI/CD pipeline
   - Command: `./gradlew clean build`

**Gradle Configuration**:
```kotlin
tasks.register<Test>("integrationTest") {
    useJUnitPlatform()
    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath
    shouldRunAfter(tasks.test)

    filter {
        includeTestsMatching("*IntegrationTest")
        includeTestsMatching("*IT")
    }
}

tasks.register<Test>("cucumberTest") {
    useJUnitPlatform()
    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath
    shouldRunAfter(tasks.test)

    filter {
        includeTestsMatching("*CucumberTest")
    }
}
```

**Rationale**:
- **Developer Productivity**: Fast tests provide immediate feedback
- **CI/CD Optimization**: Run fast tests first, fail fast
- **Resource Management**: Integration tests use Docker, run less frequently

---

### Base Test Classes

**Decision**: Provide abstract base classes for common test setup

#### UnitTestBase (Optional - for common utilities)

```java
public abstract class UnitTestBase {

    protected Money plnMoney(String amount) {
        return Money.pln(new BigDecimal(amount));
    }

    protected InstrumentSymbol symbol(String value) {
        return new InstrumentSymbol(value);
    }
}
```

#### IntegrationTestBase (PostgreSQL via Testcontainers)

```java
@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class IntegrationTestBase {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;
}
```

#### CucumberSpringConfiguration (Cucumber + Spring Boot)

```java
@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class CucumberSpringConfiguration {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
}
```

**Rationale**:
- **DRY Principle**: Common setup in one place
- **Consistency**: All integration tests use same PostgreSQL version
- **Maintainability**: Change Testcontainers config once, applies everywhere

---

## Consequences

### Positive

1. **Architectural Enforcement**: ArchUnit prevents violations at build time
2. **Fast Feedback**: Unit tests run in < 10 seconds
3. **Real Integration Testing**: Testcontainers provides actual PostgreSQL
4. **Business Alignment**: Cucumber features map to functional requirements
5. **High Coverage**: 70%+ target ensures quality
6. **Separation of Concerns**: Clear test organization by type
7. **Developer Experience**: Mirrors production structure, easy navigation
8. **CI/CD Ready**: Separate test suites for different stages

### Negative

1. **Test Infrastructure Complexity**: Testcontainers requires Docker
2. **Execution Time**: Integration tests slower than mocks
3. **Resource Usage**: Multiple PostgreSQL containers during parallel tests
4. **Learning Curve**: Developers must understand test types and when to use each
5. **Maintenance**: More test code to maintain (mirrors production structure)

### Mitigation Strategies

1. **Docker Requirement**: Document in README, provide Docker Compose setup
2. **Execution Time**: Run integration tests only on push, not every save
3. **Resource Usage**: Use test containers' resource limits, cleanup after tests
4. **Documentation**: Create testing guide (this ADR + examples)
5. **Test Templates**: Provide templates for each test type

---

## Alternatives Considered

### Alternative 1: All Tests Use Mocks (No Testcontainers)

**Rejected**:
- Mocks don't catch database constraint violations
- Missing SQL syntax errors (only found at runtime)
- False confidence (tests pass but production fails)
- Complex setup for JPA mocking (Mockito + EntityManager)

### Alternative 2: In-Memory H2 Database for Integration Tests

**Rejected**:
- H2 SQL dialect differs from PostgreSQL
- Missing PostgreSQL-specific features (JSONB, arrays)
- False positives (works in H2, fails in PostgreSQL)
- Flyway migrations may behave differently

### Alternative 3: Single Test Directory (No Separation)

**Rejected**:
- Hard to distinguish unit from integration tests
- Cannot run subsets of tests (all or nothing)
- IDE test runners execute all tests (slow feedback)
- Harder to enforce test type conventions

### Alternative 4: No ArchUnit (Manual Code Reviews)

**Rejected**:
- Human error (reviewers miss violations)
- No automated enforcement
- Regressions possible (architecture degrades over time)
- Inconsistent standards across team

---

## Implementation Checklist

Test Infrastructure:
- [x] Create ArchUnit tests (HexagonalArchitectureTest, DomainLayerRulesTest, DependencyRulesTest)
- [ ] Create NamingConventionTest for ArchUnit
- [ ] Create IntegrationTestBase abstract class
- [ ] Create CucumberSpringConfiguration
- [ ] Configure JaCoCo code coverage
- [ ] Create Gradle test tasks (integrationTest, cucumberTest)

Test Organization:
- [ ] Create test package structure mirroring production
- [ ] Create example unit test for domain layer
- [ ] Create example application layer test with mocks
- [ ] Create example integration test with Testcontainers
- [ ] Create example Cucumber feature and step definitions

Documentation:
- [ ] Create TESTING.md guide for developers
- [ ] Document test types and when to use each
- [ ] Provide test templates for each type
- [ ] Document Testcontainers setup

---

## Testing Strategy Summary

| Test Type | Speed | Dependencies | Purpose | Example |
|-----------|-------|--------------|---------|---------|
| **Architecture** | Very Fast | None | Enforce boundaries | ArchUnit rules |
| **Domain Unit** | Very Fast | None | Test business logic | Position invariants |
| **Application Unit** | Fast | Mockito | Test use cases | Service orchestration |
| **Integration** | Slow | Testcontainers | Test adapters | REST controllers |
| **BDD (Cucumber)** | Slow | Testcontainers | Acceptance tests | User scenarios |

---

## Related Decisions

- [ADR-004: Package Structure](ADR-004-package-structure.md) - Test package mirrors production
- [ADR-008: Dependency Management](ADR-008-dependency-management.md) - Test dependencies
- [ADR-013: Mock vs Real Dependencies Strategy](ADR-013-mock-vs-real-dependencies.md) - When to use mocks

---

## References

- NFR-072: TDD with BDD using Cucumber, 70%+ code coverage
- NFR-064: Framework-independent domain layer
- ArchUnit Documentation: https://www.archunit.org/userguide/html/000_Index.html
- Testcontainers Documentation: https://www.testcontainers.org/
- Cucumber Documentation: https://cucumber.io/docs/cucumber/
- JUnit 5 Documentation: https://junit.org/junit5/docs/current/user-guide/
