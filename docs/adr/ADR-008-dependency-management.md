# ADR-008: Dependency Management and Build Tool Selection

## Status
Accepted

## Context

The Investment Tracker requires a build system, dependency management framework, and technology stack that supports:

1. **Hexagonal Architecture**: Clean separation between domain, application, and infrastructure layers
2. **Modern Java**: Latest LTS version with modern language features
3. **Production-Ready Framework**: Mature ecosystem with excellent Spring support
4. **Database Migration**: Version-controlled schema evolution (NFR-036)
5. **Comprehensive Testing**: Unit, integration, architecture, and BDD testing
6. **Observability**: Telemetry and monitoring for production deployment

### Requirements

**From Issue #16 Specification:**
- Java 25 LTS (latest long-term support release)
- PostgreSQL database (NFR-036)
- Flyway migration V1__create_schema.sql already exists

**From NFRs:**
- NFR-028: Complete audit trail
- NFR-035: ACID transaction compliance
- NFR-036: PostgreSQL database
- NFR-037: Testcontainers for integration testing

**Technology Choices to Make:**
1. Build tool: Gradle vs Maven
2. Build DSL: Kotlin DSL vs Groovy DSL (if Gradle) or XML (if Maven)
3. Framework: Spring Boot vs Quarkus vs Micronaut
4. Database migration: Flyway vs Liquibase
5. Testing frameworks: JUnit 5, Cucumber, Testcontainers, ArchUnit
6. Observability: OpenTelemetry/OTLP vs Micrometer

## Decision

### Build Tool: Gradle 8.11+ with Kotlin DSL

**Decision**: Use Gradle with Kotlin DSL for build configuration

**Rationale**:
1. **Type-Safe Configuration**: Kotlin DSL provides IDE autocomplete, refactoring support, and compile-time validation
2. **Better IDE Support**: IntelliJ IDEA (primary IDE for Java development) has excellent Gradle Kotlin DSL integration
3. **More Maintainable**: Kotlin DSL is more readable and less verbose than Groovy DSL or Maven XML
4. **Performance**: Gradle build cache and incremental compilation faster than Maven
5. **Flexibility**: Better support for custom build logic and multi-module projects (future)
6. **Modern Standard**: Kotlin DSL is the recommended approach for new Gradle projects

**Maven Rejected Because**:
- XML-based configuration is verbose and harder to read
- Limited support for custom build logic
- Slower build times (no build cache)
- Less flexible dependency management

### Java Version: Java 25 LTS

**Decision**: Use Java 25 LTS

**Rationale**:
1. **Latest LTS**: Long-term support guarantees stability and security updates
2. **Modern Language Features**: Pattern matching, records, sealed classes, virtual threads
3. **Performance**: Latest JVM improvements (ZGC, Shenandoah, etc.)
4. **Specified in Requirements**: Issue #16 explicitly requires Java 25

### Framework: Spring Boot 3.4.1

**Decision**: Use Spring Boot 3.4.1 as the application framework

**Rationale**:
1. **Production-Ready**: Mature ecosystem, battle-tested in production
2. **Excellent Ecosystem**: Spring Data JPA, Spring Web, Spring Actuator
3. **Minimal Domain Contamination**: Spring allows clean hexagonal architecture with proper package structure
4. **Convention over Configuration**: Reduces boilerplate code
5. **Strong JPA Support**: Excellent integration with Hibernate and PostgreSQL
6. **Actuator**: Built-in health checks and metrics endpoints
7. **Community**: Largest Java community, excellent documentation and support

**Quarkus/Micronaut Rejected Because**:
- Smaller ecosystems, less mature
- Hexagonal architecture is easier to enforce with Spring's flexibility
- Spring's widespread adoption means easier team onboarding

### Database Migration: Flyway 10.x

**Decision**: Use Flyway 10.x for database schema versioning

**Rationale**:
1. **Already Started**: V1__create_schema.sql migration file already exists
2. **SQL-Based**: Migration scripts are plain SQL (easier to review and understand)
3. **Simple**: Flyway is simpler than Liquibase for basic schema evolution
4. **Spring Integration**: Excellent Spring Boot auto-configuration
5. **Version Control**: Migration files are versioned alongside code

**Liquibase Rejected Because**:
- More complex (XML/YAML/JSON formats in addition to SQL)
- Overkill for current requirements
- Flyway's simplicity aligns better with v0.1 MVP scope

### Testing Frameworks

**Decision**: Use the following testing frameworks:

1. **JUnit 5**: Unit and integration testing framework
   - Latest version of JUnit, industry standard
   - Better parameterized tests and extensions
   - Excellent Spring Boot integration

2. **Cucumber 7.x**: BDD testing with Gherkin feature files
   - Living documentation aligned with requirements
   - Feature files serve as acceptance tests
   - Business-readable test scenarios

3. **Testcontainers 1.20.x**: Integration testing with real PostgreSQL
   - Real database testing (no mocks or in-memory DBs)
   - Ensures schema compatibility
   - Required by NFR-037

4. **ArchUnit 1.3.x**: Architecture testing
   - Enforce hexagonal architecture boundaries
   - Prevent domain contamination by infrastructure
   - Validate package dependencies match ADR-004

5. **AssertJ**: Fluent assertion library
   - More readable assertions than JUnit assertions
   - Better error messages

**Rationale**:
- Comprehensive testing strategy covering all layers
- Architecture validation prevents technical debt
- BDD tests align with requirements/feature files
- Real database testing ensures production compatibility

### Observability: OpenTelemetry/OTLP

**Decision**: Use OpenTelemetry with OTLP exporter

**Rationale**:
1. **Vendor-Neutral**: Not locked into specific monitoring vendor
2. **Industry Standard**: CNCF graduated project
3. **Future-Proof**: Supports multiple backends (Jaeger, Zipkin, Prometheus, etc.)
4. **Traces + Metrics + Logs**: Unified observability
5. **Spring Integration**: Good Spring Boot support via OpenTelemetry Java agent

**Micrometer Rejected Because**:
- Spring-specific, less portable
- OpenTelemetry is becoming the industry standard

## Consequences

### Positive

1. **Type Safety**: Kotlin DSL catches build configuration errors at compile time
2. **Modern Java**: Latest language features improve code quality and readability
3. **Production-Ready**: Spring Boot is battle-tested and widely adopted
4. **Clean Architecture**: ArchUnit enforces hexagonal architecture boundaries
5. **Living Documentation**: Cucumber feature files document behavior
6. **Real Database Testing**: Testcontainers ensures PostgreSQL compatibility
7. **Version-Controlled Schema**: Flyway migrations are versioned with code
8. **Future-Proof Observability**: OpenTelemetry supports multiple monitoring backends
9. **Excellent IDE Support**: IntelliJ IDEA has first-class Gradle Kotlin DSL support

### Negative

1. **Learning Curve**: Kotlin DSL requires learning Kotlin syntax (minimal for build files)
2. **Spring Complexity**: Spring Boot has a large API surface area
3. **JVM Memory**: Java 25 requires more memory than lightweight frameworks
4. **Testcontainers Docker Dependency**: Requires Docker for integration tests
5. **OpenTelemetry Maturity**: Some Spring Boot integrations are still evolving

### Mitigation Strategies

1. **Kotlin DSL Learning**: Build files are simple, minimal Kotlin knowledge needed
2. **Spring Complexity**: Focus on minimal Spring usage in v0.1 (JPA, Web, Actuator only)
3. **Memory**: Acceptable for local development and single-instance deployment
4. **Docker**: Standard development requirement, Docker Desktop available on macOS
5. **OpenTelemetry**: Can fall back to Micrometer if needed (Spring Boot default)

## Dependency Versions

### Core Framework
- **Java**: 25 (LTS)
- **Gradle**: 8.11.1
- **Spring Boot**: 3.4.1
- **Spring Dependency Management Plugin**: 1.1.7

### Database
- **PostgreSQL Driver**: (managed by Spring Boot BOM)
- **Flyway Core**: (managed by Spring Boot BOM)
- **Flyway PostgreSQL**: (managed by Spring Boot BOM)

### Testing
- **JUnit Jupiter**: (managed by Spring Boot BOM)
- **Cucumber Java**: 7.20.1
- **Cucumber JUnit Platform Engine**: 7.20.1
- **Cucumber Spring**: 7.20.1
- **Testcontainers**: 1.20.4 (core, postgresql, junit-jupiter)
- **ArchUnit JUnit 5**: 1.3.0
- **AssertJ**: (managed by Spring Boot BOM)

### Observability
- **OpenTelemetry BOM**: 1.44.1
- **OpenTelemetry API**: (managed by BOM)
- **OpenTelemetry SDK**: (managed by BOM)
- **OpenTelemetry OTLP Exporter**: (managed by BOM)

## Build Configuration

### Gradle Files Created
1. `build.gradle.kts` - Main build configuration
2. `settings.gradle.kts` - Project settings
3. `gradle/wrapper/gradle-wrapper.properties` - Wrapper configuration
4. `gradle/wrapper/gradle-wrapper.jar` - Wrapper JAR
5. `gradlew` - Unix wrapper script (executable)
6. `gradlew.bat` - Windows wrapper script

### Key Build Features
- **Java Toolchain**: Java 25 configured via toolchain (allows team to use different JDK versions)
- **Spring Dependency Management**: BOM imports for consistent dependency versions
- **JUnit Platform**: Configured for JUnit 5 + Cucumber integration
- **Test Logging**: Shows passed/skipped/failed tests
- **Boot JAR**: Executable JAR with embedded Tomcat
- **Standard JAR Disabled**: Only bootJar is built

## Alternatives Considered

### Alternative 1: Maven with XML

**Rejected**:
- XML is verbose and harder to read
- No IDE autocomplete or type safety
- Slower build performance
- Limited custom build logic support

### Alternative 2: Gradle with Groovy DSL

**Rejected**:
- No type safety or compile-time validation
- Harder to refactor and maintain
- Kotlin DSL is the recommended modern approach

### Alternative 3: Quarkus Framework

**Rejected**:
- Smaller ecosystem, less mature
- More opinionated, harder to implement clean hexagonal architecture
- Less community support and documentation

### Alternative 4: Micronaut Framework

**Rejected**:
- Similar to Quarkus - smaller ecosystem
- Less JPA support and tooling
- Spring's flexibility better aligns with hexagonal architecture

### Alternative 5: Liquibase for Migrations

**Rejected**:
- More complex than needed for v0.1
- XML/YAML/JSON adds abstraction over SQL
- Flyway's simplicity and existing V1 migration make it the better choice

## Related Decisions

- [ADR-004: Package Structure Design](ADR-004-package-structure.md) - Package organization
- [ADR-005: Database Schema Design](ADR-005-database-schema.md) - PostgreSQL schema design
- [ADR-007: Port Definitions](ADR-007-port-definitions.md) - Hexagonal architecture ports (to be created)

## Implementation Notes

### Build Commands

```bash
# Build project
./gradlew build

# Run application
./gradlew bootRun

# Run tests only
./gradlew test

# Clean and rebuild
./gradlew clean build

# Generate dependency report
./gradlew dependencies
```

### Adding Dependencies

When adding new dependencies:
1. Prefer Spring Boot managed versions (no version specified)
2. For non-Spring dependencies, specify exact version
3. Use BOM imports for multi-module dependency sets (e.g., OpenTelemetry)
4. Avoid transitive dependency conflicts by excluding unnecessary dependencies

### Gradle Wrapper

The Gradle wrapper ensures all developers use the same Gradle version:
- **Version**: 8.11.1
- **Distribution**: Binary (not full distribution for faster download)
- **Validation**: SHA-256 checksum validation enabled

## References

- [Gradle Kotlin DSL Documentation](https://docs.gradle.org/current/userguide/kotlin_dsl.html)
- [Spring Boot 3.4.1 Reference](https://docs.spring.io/spring-boot/docs/3.4.1/reference/html/)
- [Flyway Documentation](https://flywaydb.org/documentation/)
- [Testcontainers Documentation](https://www.testcontainers.org/)
- [ArchUnit User Guide](https://www.archunit.org/userguide/html/000_Index.html)
- [OpenTelemetry Java](https://opentelemetry.io/docs/instrumentation/java/)
- NFR-028: Complete audit trail
- NFR-035: ACID transaction compliance
- NFR-036: PostgreSQL database
- NFR-037: Testcontainers integration testing
- Issue #16: Hexagonal Architecture Setup

---

**Version**: 1.0
**Last Updated**: 2026-01-04
**Author**: System Design Team
