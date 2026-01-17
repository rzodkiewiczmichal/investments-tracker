# ADR-016: Database Migration Strategy

## Status
Accepted

## Context

Database schema changes need to be version controlled, repeatable across environments, and auditable. Requirements include:
- NFR-036: PostgreSQL 16+ database
- NFR-053: Migrations versioned and automated
- Spring Boot integration with automatic execution
- Separation of schema migrations from test data

## Decision

### Migration Tool: Flyway

Use Flyway for database migrations with Spring Boot integration.

**Rationale:**
- First-class Spring Boot integration
- SQL-based (no custom DSL)
- Audit trail via `flyway_schema_history` table
- Checksum validation prevents modification of applied migrations
- Industry standard

**Rejected Alternatives:**
- Liquibase: More complex, overkill for MVP
- Hibernate `hbm2ddl`: Data loss risk, never for production
- JPA `ddl-auto=update`: No version control or rollback
- Manual scripts: Error-prone, no audit trail

### File Structure and Naming

**Location:** `src/main/resources/db/migration/`

**Naming Convention:** `V{version}__{description}.sql`

Examples:
- `V1__create_accounts_table.sql`
- `V2__create_positions_table.sql`
- `V3_1__add_index_on_symbol.sql` (minor version)
- `R__portfolio_summary_view.sql` (repeatable)

**Rules:**
- Prefix: `V` for versioned, `R` for repeatable
- Version: Integer or dotted (1, 2, 3.1)
- Separator: Double underscore `__`
- Description: Lowercase with underscores
- Extension: `.sql`

### Migration Types

**Versioned Migrations (V prefix):**
- Schema changes (CREATE/ALTER/DROP TABLE)
- Data migrations (INSERT/UPDATE/DELETE)
- Indexes and constraints
- Run exactly once per environment
- Applied in order
- Checksum validated

**Repeatable Migrations (R prefix):**
- Views (CREATE OR REPLACE VIEW)
- Stored procedures/functions
- Triggers
- Run on every checksum change
- Applied after all versioned migrations
- Must be idempotent

### Migration Content Guidelines

**Header Comment:**
```sql
-- V3__add_instrument_type.sql
-- Description: Add instrument_type column to support stocks and ETFs
-- Author: <name>
-- Date: 2026-01-11
```

**Best Practices:**
- Use `CREATE TABLE IF NOT EXISTS` for idempotency (helps manual testing)
- Include CHECK constraints for domain rules (`quantity > 0`)
- Use FOREIGN KEY with appropriate ON DELETE action
- Match NUMERIC precision with ADR-006 (Money: 20,4; Quantity: 20,8)
- Create indexes for foreign keys and frequently queried columns
- Add COMMENT ON TABLE/COLUMN for documentation

**Example Migration:**
```sql
-- V1__create_accounts_table.sql
-- Description: Create accounts table
-- Author: Team
-- Date: 2026-01-11

CREATE TABLE accounts (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    broker VARCHAR(255) NOT NULL,
    account_type VARCHAR(50) NOT NULL CHECK (account_type IN ('IKE', 'IKZE', 'NORMAL')),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_accounts_name ON accounts(name);

COMMENT ON TABLE accounts IS 'Broker accounts for investment positions';
```

### Spring Boot Configuration

**Dependencies (build.gradle.kts):**
```kotlin
dependencies {
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    runtimeOnly("org.postgresql:postgresql")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
}
```

**Configuration (application.yml):**
```yaml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
    baseline-version: 0
    validate-on-migrate: true
    out-of-order: false
    clean-disabled: true  # NEVER allow in production

  datasource:
    url: jdbc:postgresql://localhost:5432/investments_tracker
    username: tracker_user
    password: tracker_password

  jpa:
    hibernate:
      ddl-auto: validate  # NEVER 'update' or 'create' - Flyway manages schema
    show-sql: false
```

**Environment-Specific:**
- **Local:** `clean-disabled: false`, `show-sql: true`
- **Production:** `clean-disabled: true`, `validate-on-migrate: true`

### Rollback Strategy

Flyway Community Edition doesn't support automatic rollback.

**Options:**
1. **Manual Rollback Migration** (recommended):
   - Create new migration to undo changes (e.g., `V4__remove_column_foo.sql`)
   - Explicit, auditable, version controlled

2. **Database Backup:**
   - Take backup before risky migrations
   - Restore if migration fails
   - Required for production deployments

3. **Blue-Green Deployment:**
   - Deploy to new environment
   - Switch traffic
   - Rollback = switch back

### Test Data Strategy

**Separate test data from schema migrations:**
- Schema: `src/main/resources/db/migration/`
- Test data: `src/main/resources/db/testdata/`

**Load test data in local profile only:**
```java
@Configuration
@Profile("local")
public class TestDataLoader implements ApplicationRunner {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        executeSqlScript(new ClassPathResource("db/testdata/accounts.sql"));
        executeSqlScript(new ClassPathResource("db/testdata/positions.sql"));
    }
}
```

### Integration Testing

Flyway migrations run automatically in integration tests using Testcontainers:

```java
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
public abstract class IntegrationTestBase {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }
}
```

### Version Control Workflow

**Rules:**
- Migrations are **immutable** once merged to main branch
- Never modify applied migrations (checksum validation will fail)
- If bug found after merge: create new migration to fix it
- Coordinate version numbers to avoid conflicts between branches

**Merge Conflict Resolution:**
- If two branches create same version (e.g., both V5): rename one to next version
- Document version renumbering in migration header

## Consequences

### Positive

- Version-controlled schema changes alongside code
- Repeatable deployments across environments
- Audit trail via `flyway_schema_history`
- Automatic execution on application startup
- Checksum validation prevents accidental modifications
- Integration tests verify migrations work
- Spring Boot zero-config integration

### Negative

- No automatic rollback (Community Edition)
- Migrations immutable after merge (can't fix typos easily)
- Version number conflicts between branches
- Production risk for schema changes

### Mitigation

- Document rollback steps in migration headers
- Code review catches errors before merge
- Mandatory backups before production migrations
- Staging environment for testing

## Related Decisions

- [ADR-006: Database Precision Configuration](ADR-006-database-precision-configuration.md)
- [ADR-014: Docker Compose Configuration](ADR-014-docker-compose-configuration.md)
- [ADR-017: Transaction Boundaries](ADR-017-transaction-boundaries.md)

## References

- Flyway Documentation: https://flywaydb.org/documentation/
- Spring Boot Flyway Integration: https://docs.spring.io/spring-boot/docs/current/reference/html/howto.html#howto.data-initialization.migration-tool.flyway
- NFR-036, NFR-053
