# Investment Tracker - Project Context for Claude

## Project Overview
**Name:** Investment Tracker
**Purpose:** Application for private usage to track investments done in multiple different broker accounts

## Project Goals

### More Important Goal
Practice development of clean modern architecture clean code Java application proofing:
- Domain Driven Design skills
- Cucumber testing skills
- Backend development skills

### Minor Goal
Have working application for my usage

## Development Workflow

### Claude's Role
Claude will be used for all phases of software lifecycle:
- Defining requirements
- Creating epics, tasks etc
- Discovering domains
- Designing architecture
- Implementing the application

Claude Code will not be just agentic code help - will use all handy MCP servers for variety of automations (e.g., GitHub MCP server is configured)

## Branch Naming Convention

All work must happen on a feature branch, never directly on `main`. Branch names must include both the epic and issue number:

**Format:** `rzodkiewiczmichal/<epic#>-<issue#>-<short-description>`

**Examples:**
- `rzodkiewiczmichal/55-68-analyze-xtb-format`
- `rzodkiewiczmichal/54-67-mbank-cucumber-scenarios`

When a task spans multiple issues under the same epic, use the primary issue number.

## Project Location
GitHub repository cloned in this directory

## Documentation Locations

### Requirements
- **Functional Requirements:** `requirements/functional/functional-requirements.md` (57 FRs)
- **Non-Functional Requirements:** `requirements/non-functional/non-functional-requirements.md` (48 NFRs)
- **Ubiquitous Language:** `requirements/functional/ubiquitous-language.md`
- **User Personas:** `requirements/functional/user-personas.md`

### Planning
- **Version Roadmap:** `planning/versions-roadmap.md`
- **Requirements by Version:** `planning/requirements-by-version.md` (traceability matrix)
- **Scenarios to Requirements:** `planning/scenarios-to-requirements.md`

### Architecture Decision Records (ADRs)
Located in `docs/adr/`. Read the ADR README (`docs/adr/README.md`) for an index, or read individual ADRs as needed when working on related areas.

### Diagrams
- **Domain Model:** `docs/diagrams/domain-model.md`
- **Database Schema:** `docs/diagrams/database-schema.md`

### v0.1 MVP Requirements Scope
**Functional (20):** FR-001 to FR-004, FR-011, FR-012, FR-014, FR-041, FR-042, FR-044 to FR-046, FR-081 to FR-084, FR-089, FR-091 to FR-096
**Non-Functional (37):** See `planning/requirements-by-version.md` for complete list

## Project Tracking

### GitHub Issues
The project uses GitHub Issues for comprehensive task tracking and project management.

**Issue Structure:**
- **Epics:** High-level features/phases (e.g., "Design Phase Epic", "v0.1 MVP")
- **Sub-Issues:** Specific tasks within epics (e.g., "Domain Model Design", "Infrastructure Setup")
- **Labels:** Categorize issues (epic, v0.1, design, infrastructure, testing, etc.)

**GitHub MCP Server Integration:**
Claude has direct access to GitHub via MCP server and can:
- Create issues and sub-issues
- Update issue status (open/closed)
- Add comments to issues
- Link related issues
- Apply labels
- Mark issues as completed with `state_reason: completed`

**Workflow:**
1. Create issues for new features/tasks
2. Track progress via issue comments
3. Close issues when work is complete (with completion comment)
4. Link related issues (e.g., "Closes #18", "Part of #13")

**Post-Push Issue Closure (Mandatory):**
After work is pushed (PR merged or commits pushed to main), Claude must:
1. Search open GitHub issues for any that are addressed by the pushed work
2. Close matched issues with a completion comment summarizing what was delivered and which PR(s) resolved it
3. If no matching issue exists for the pushed work, ask the user whether to create and immediately close one (for traceability) or skip

**Repository:** `rzodkiewiczmichal/investments-tracker`

## Output Guidelines

Claude must use one of three output types based on the nature of the response:

### 1. Normal Artifact
**Definition:** Files intended to stay in the project permanently, be committed and pushed to git.

**Examples:**
- Formal requirements documents (functional-requirements.md, non-functional-requirements.md)
- Domain documentation (ubiquitous-language.md, user-personas.md)
- Planning documents (VERSION-ROADMAP.md)
- Feature files (*.feature)
- Source code files
- Architecture documentation

**Location:** Appropriate directory in project structure

### 2. Temp Artifact
**Definition:** Files in `/temp` directory used when output is long enough to be unreadable in terminal. Intended to be read by user and deleted afterwards.

**Examples:**
- Analysis documents (like PROPOSED-STRUCTURE.md)
- Long comparisons or evaluations
- Draft proposals for discussion
- Verbose reports or summaries
- Exploration documents

**Location:** `/temp` directory (must be created if it doesn't exist)

### 3. Terminal Response
**Definition:** Direct response in terminal without creating a file. For short questions and answers.

**Examples:**
- Brief answers to questions
- Confirmations
- Short explanations
- Status updates
- Simple guidance

**Location:** No file created

### Decision Criteria

Claude should judge which type to use based on:
- **Length:** Short (terminal), Medium-Long (temp), Permanent value (normal)
- **Purpose:** Exploration/draft (temp), Formal documentation (normal), Quick answer (terminal)
- **Lifespan:** Temporary (temp), Permanent (normal), Immediate (terminal)
- **Commit-worthiness:** Should this be in git history? (yes = normal, no = temp or terminal)

## Documentation Quality Rules

### Single Source of Truth Principle

**Rule:** Each piece of information should have exactly ONE authoritative location in the project.

**Rationale:** Redundancy creates maintenance burden - when information changes, all duplicates must be updated, leading to inconsistencies.

**Guidelines:**

1. **Before creating content:**
   - Check if similar information exists elsewhere
   - If it exists, reference it rather than duplicating it

2. **When information is needed in multiple places:**
   - Choose ONE authoritative source
   - Other documents should cross-reference: "See [document-name.md] for [specific information]"
   - Example: ✅ "See requirements-by-version.md for complete requirement-to-version mapping"
   - Anti-example: ❌ Copying 20 requirement IDs into multiple documents

3. **Acceptable exceptions:**
   - Summary statistics in multiple places (e.g., "Total: 57 requirements")
   - Different perspectives of same data (e.g., static table vs. progressive timeline)
   - Essential context needed for document to be self-contained

4. **When refactoring documents:**
   - Identify the primary purpose of each document
   - Assign each type of information to the most appropriate document
   - Replace duplications with cross-references

5. **Red flags indicating redundancy:**
   - Copying lists of IDs between documents
   - Repeating detailed descriptions verbatim
   - Same table appearing in multiple places
   - Having to update information in more than one file

**Examples:**

✅ **Good separation:**
- requirements-by-version.md: Flat lookup table (ID → Version)
- versions-roadmap.md: Strategic narrative that references requirements-by-version.md

❌ **Bad redundancy:**
- Both documents contain identical lists of requirement IDs for each version

## Domain Model Coding Rules

**Reference:** See ADR-018 for full rationale.

### Core Principles
1. **All domain types are Java records** - both value objects AND entities
2. **No primitives** - use value types (AccountName, not String)
3. **No infrastructure references** - no mention of database, JPA, persistence
4. **Immutable only** - no update/set methods, state changes create new instances

### Implementation Rules

| Rule | Wrong | Correct |
|------|-------|---------|
| Use records | `public class Account { }` | `public record Account(...) { }` |
| Value types | `String name` | `AccountName name` |
| No DB refs | "database-generated ID" | "account identifier" |
| No factory methods | `Account.create()`, `Account.reconstitute()` | Use canonical constructor |
| No setters | `setId()`, `updateName()` | `withName()` returning new instance |

### State Changes Pattern
```java
// Wrong: mutation
account.updateName(newName);

// Correct: return new instance
Account updated = new Account(account.id(), newName, account.brokerName());
```

### Validation Pattern
```java
public record AccountName(String value) {
    public AccountName {
        Objects.requireNonNull(value, "value cannot be null");
        if (value.isBlank()) {
            throw new DomainException("Account name cannot be blank");
        }
        value = value.trim();  // Normalize in constructor
    }
}
```

## Java Coding Style Rules

### Nullability Annotations
Use `@NonNull` and `@Nullable` annotations in record constructors to document intent:
```java
// Correct: explicit nullability
public record Instrument(
        @NonNull InstrumentSymbol symbol,
        @NonNull InstrumentName name,
        @NonNull InstrumentType type,
        @Nullable Price currentPrice) { }
```

### No Redundant Comments
Do not add comments that state the obvious about nullability or other things visible from annotations:
```java
// Wrong
public Instrument {
    // currentPrice can be null (price not yet available)
}

// Correct: @Nullable annotation speaks for itself
```

### Method Naming
Use simple, direct method names. Avoid redundant suffixes:
```java
// Wrong
public Optional<Price> getCurrentPriceOptional() { }

// Correct
public Optional<Price> getCurrentPrice() { }
```

### No Version References in Code
Do not reference version numbers (v0.1, v0.2) in code comments. Version information is tracked elsewhere:
```java
// Wrong
/**
 * Portfolio is a singleton in v0.1 (single user).
 */

// Correct
/**
 * Portfolio represents the user's complete investment view.
 */
```

### One Record Per File
Nested records should be moved to separate files:
```java
// Wrong: nested record
public record Portfolio(...) {
    public record PortfolioMetrics(...) { }
}

// Correct: separate file
// File: Portfolio.java
public record Portfolio(...) { }

// File: PortfolioMetrics.java
public record PortfolioMetrics(...) { }
```

### Separate Types for Distinct Concepts
Do not create one type that serves multiple distinct concepts. Create separate types:
```java
// Wrong: one type for two concepts
public record InstrumentSymbol(String value) {
    // Handles both ISIN and Ticker
}

// Correct: separate types
public record Ticker(String value) { }
public record Isin(String value) { }
```

### No Convenience Constructors
Records should have only the canonical constructor. Do not add secondary constructors for convenience:
```java
// Wrong: secondary constructor for convenience
public record Instrument(..., @Nullable Price currentPrice) {
    public Instrument(InstrumentSymbol symbol, InstrumentName name, InstrumentType type) {
        this(symbol, name, type, null);
    }
}

// Correct: only canonical constructor, caller passes null explicitly
new Instrument(symbol, name, type, null)
```

### No with* Methods
Do not add `with*` methods for state changes. Use constructor directly:
```java
// Wrong: with* method
public Account withName(AccountName newName) {
    return new Account(this.id, newName, this.brokerName);
}

// Correct: caller uses constructor
Account updated = new Account(existing.id(), newName, existing.brokerName());
```

### Identity-Based Equality for Entities
Entities (Account, Position, Instrument) must override `equals()` and `hashCode()` based on identity field only:
```java
// Correct: identity-based equality for Position entity
@Override
public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Position other)) return false;
    return symbol.equals(other.symbol);  // Only compare identity
}

@Override
public int hashCode() {
    return symbol.hashCode();
}
```

### Domain Exceptions
Use `DomainException` (or its subclasses) for domain rule violations, not `IllegalArgumentException` or `IllegalStateException`:
```java
// Wrong
if (holdings.isEmpty()) {
    throw new IllegalArgumentException("Position must have at least one holding");
}

// Correct
if (holdings.isEmpty()) {
    throw new DomainException("Position must have at least one holding");
}
```

### Exception Factory Methods
Use factory methods on domain exception classes for common validation scenarios:
```java
// Correct: factory methods for common cases
public class InvalidQuantityException extends DomainException {
    public static InvalidQuantityException negative(String value) {
        return new InvalidQuantityException("Quantity cannot be negative: " + value);
    }

    public static InvalidQuantityException zero() {
        return new InvalidQuantityException("Quantity cannot be zero");
    }

    public static InvalidQuantityException exceedsScale(int actualScale, int maxScale) {
        return new InvalidQuantityException(
                "Quantity cannot exceed " + maxScale + " decimal places, got: " + actualScale);
    }
}
```

### Null Check Messages
Include the parameter name in null check messages for debugging clarity:
```java
// Wrong: generic message
Objects.requireNonNull(symbol, "Symbol cannot be null");

// Correct: includes parameter name
Objects.requireNonNull(symbol, "symbol cannot be null");
```

### Return Optional Instead of Null
Never return null from methods. Use `Optional` for values that may be absent:
```java
// Wrong
public InvestedAmount getTotalInvestedAmount() {
    return metrics.totalInvestedAmount();  // may return null
}

// Correct
public Optional<InvestedAmount> getTotalInvestedAmount() {
    return Optional.ofNullable(metrics.totalInvestedAmount());
}
```

### No Redundant Record Accessor Overrides
Do not override record accessors if they just return the same value:
```java
// Wrong: redundant override
public record Portfolio(List<Position> positions, PortfolioMetrics metrics) {
    public List<Position> getPositions() {
        return positions;
    }
}

// Correct: use built-in accessor
portfolio.positions()  // not portfolio.getPositions()
```

### Services Should Not Duplicate Entity Methods
Domain services should only contain methods that add value. Do not create service methods that simply delegate to entity methods:
```java
// Wrong: service just delegates to entity
public class PositionCalculationService {
    public InvestedAmount calculateInvestedAmount(Position position) {
        return position.calculateInvestedAmount();  // Just delegates!
    }
}

// Correct: service adds value (uses different price)
public class PositionCalculationService {
    public CurrentValue calculateCurrentValue(Position position, Price currentPrice) {
        Quantity totalQuantity = position.calculateTotalQuantity();
        return CurrentValue.calculate(totalQuantity, currentPrice);
    }
}
```

### Avoid Duplicate Calculations
When multiple methods need the same computed value, use private helper methods with pre-computed parameters:
```java
// Wrong: calculateTotalQuantity() called multiple times
public InvestedAmount calculateInvestedAmount() {
    Quantity totalQuantity = calculateTotalQuantity();
    CostBasis avgCostBasis = calculateWeightedAverageCostBasis(); // calls calculateTotalQuantity() again!
    return InvestedAmount.calculate(totalQuantity, avgCostBasis);
}

// Correct: pass pre-computed value
public InvestedAmount calculateInvestedAmount() {
    Quantity totalQuantity = calculateTotalQuantity();
    CostBasis avgCostBasis = calculateWeightedAverageCostBasis(totalQuantity);
    return InvestedAmount.calculate(totalQuantity, avgCostBasis);
}

private CostBasis calculateWeightedAverageCostBasis(Quantity totalQuantity) {
    // Use pre-computed totalQuantity
}
```

## Application Layer Naming Conventions

### Use Case Naming
Application layer services follow the UseCase naming pattern:
- **Interface:** `*UseCase` (e.g., `PortfolioQueryUseCase`)
- **Implementation:** `*UseCaseService` (e.g., `PortfolioQueryUseCaseService`)

```java
// Correct: interface returns domain model (ADR-022)
public interface PortfolioQueryUseCase {
    Portfolio getPortfolio();
}

// Correct: implementation
@Service
public class PortfolioQueryUseCaseService implements PortfolioQueryUseCase {
    // ...
}
```

## Infrastructure Layer Rules

### Domain Services Need Spring Configuration
Domain services (e.g., `PositionCalculationService`, `PortfolioCalculationService`) are pure domain objects without `@Service` annotation. A `@Configuration` class in the infrastructure layer must register them as Spring beans:
```java
@Configuration
public class DomainServiceConfig {
    @Bean
    public PortfolioCalculationService portfolioCalculationService() {
        return new PortfolioCalculationService();
    }
}
```

### JPA `@CollectionTable` Names Must Match Flyway Migrations
JPA default table naming will not match Flyway-created tables. Always specify the table name explicitly in `@CollectionTable` to match the migration:
```java
// Wrong: defaults to "position_holdings"
@ElementCollection
@CollectionTable(joinColumns = @JoinColumn(name = "instrument_symbol"))

// Correct: matches Flyway migration table name
@ElementCollection
@CollectionTable(name = "account_holdings", joinColumns = @JoinColumn(name = "instrument_symbol"))
```

## Cucumber Testing Rules

### Step Definitions Are Globally Unique
Cucumber uses a global step registry. Two step classes cannot define the same `@Then`/`@Given`/`@When` pattern. Use prefixes to disambiguate:
```java
// Wrong: same pattern in PositionSteps and PortfolioSteps
@Then("I should see P&L of +{int} PLN")

// Correct: prefix to make unique
@Then("I should see position P&L of +{int} PLN")  // PositionSteps
@Then("I should see P&L of +{int} PLN")            // PortfolioSteps (portfolio context)
```

### Integration Tests Require Docker
Testcontainers is used for the database. `./gradlew test` runs unit tests without Docker. `./gradlew build` includes integration tests that need Docker running.

### Cucumber Tests Should Set Up Data via JDBC
Don't rely on the REST API for test data setup in Given steps. Use `JdbcTemplate` directly to insert accounts, instruments, and positions. This avoids circular dependencies on the API being correct and keeps test setup fast and reliable.

## Code Quality and Build Verification

### Mandatory Build Verification

**Rule:** Always verify that code compiles and tests pass after making changes.

**Rationale:** Broken builds waste time and block progress. Claude must verify changes work before presenting them to the user.

**Guidelines:**

1. **After code changes:**
   - Run `./gradlew build` to verify compilation and tests
   - Fix any compilation errors immediately
   - Fix any test failures immediately
   - Never present broken code to the user

2. **When creating tests:**
   - Ensure tests compile
   - Ensure tests pass (or pass on empty packages if appropriate)
   - Use `.allowEmptyShould(true)` for ArchUnit tests that should pass on empty packages

3. **Before marking work complete:**
   - Run full build: `./gradlew clean build`
   - Verify all tests pass
   - Check for any warnings or deprecations

4. **Build commands:**
   ```bash
   # Full build with tests (always run spotless first)
   ./gradlew spotlessApply && ./gradlew clean build

   # Compile only (faster check)
   ./gradlew compileJava compileTestJava

   # Run tests only
   ./gradlew test
   ```

5. **What to check:**
   - ✅ Code compiles (no syntax errors)
   - ✅ All tests pass (or appropriately pass on empty packages)
   - ✅ No new warnings introduced
   - ✅ Architecture tests enforce boundaries

**Consequences of not verifying:**
- User wastes time debugging Claude's broken code
- Erodes trust in Claude's output
- Slows development velocity

## Post-Implementation Code Review (Mandatory)

After every implementation is finished (all tasks completed, build passing), automatically perform the following:

1. Run `/ddd-java` and `/effective-java` skills to do a code review of all changed files.
2. Apply fixes to all found issues.
3. If any fix requires a decision (ambiguous intent, multiple valid approaches, trade-offs), **ask the user** — do not guess or assume.
4. Generate `post-implementation.md` in the project directory with:
   - List of all issues found
   - Fixes applied
   - Any open questions or decisions deferred to the user
5. **Never commit `post-implementation.md`** — it is a local review artifact only. Do not `git add` it or include it in any commit.

## Design and Planning Rules

### No Hardcoded Data
Never hardcode domain data in frontend or backend code (broker names, account names, instrument lists, etc.). If data comes from a finite set of implementations (e.g., registered parsers), let the user type it freely and validate server-side. Dropdown menus require a persistent, user-managed data source — not a hardcoded list or an endpoint that wraps a hardcoded list. When in doubt, use a plain text input with server-side validation.

### Validate Against Real Data Before Planning
When designing data import, parsing, or transformation logic, always analyze ALL available sample/real data files first (check `.context/attachments/` and test resources). Cross-reference assumptions against actual data before proposing a solution. Never design parsers or matching logic based on assumed formats — verify with real files.

### User Confirmation for Critical Data Mapping
Never implement automatic fuzzy matching, text-based guessing, or heuristic resolution for critical data like instrument symbols, account identifiers, or financial amounts. When exact matching fails, always return unmatched items to the user for explicit manual resolution. The user decides — the system only suggests.

### Think Through State Conflicts
When planning features that modify data (import, update, delete), proactively identify conflicts with existing data states. Ask yourself: "What if this data already exists from a different source?" Present edge cases and conflict scenarios to the user as part of the proposal, not as an afterthought when asked.

### Proactive DDD Layer Validation
When placing new types (records, interfaces, classes), proactively verify the layer assignment against DDD/hexagonal architecture rules. Specifically:
- Domain records used by aggregates belong in `domain/model/`
- Port interfaces (driven ports) belong in `application/port/out/`
- DTOs that cross layer boundaries need justification for their placement
If uncertain about a type's placement, flag it to the user rather than defaulting to a possibly wrong layer.

## Refactoring and Rename Rules

### Atomic Renames
When renaming a class, interface, or method, grep the entire codebase for all references and update them in the same step. Never leave a rename partially complete. After renaming, always grep to confirm zero remaining references to the old name before running the build.

### ArchUnit Naming Awareness
Before creating any new class in a convention-governed package, review the ArchUnit tests in `src/test/java/.../architecture/` to check naming constraints. Key conventions:
- `domain.service..` — top-level classes must end with `Service`
- `domain.repository..` — interfaces must end with `Repository`, `Provider`, or `Cache`
- `infrastructure.web.controller..` — classes must end with `Controller`
- `application.dto.mapper..` — classes must end with `Mapper`
Inner/nested classes are excluded from these rules.

## Communication Style Preferences

### Terse User Messages
The user communicates tersely. Short messages like "what was wrong?", "create them", "push this" should be interpreted in context of the current work. Don't ask for clarification on messages that are clearly referring to the immediately preceding discussion. Act on the obvious intent.

### Architecture Review Awareness
The user is a DDD practitioner who actively reviews plans for architectural correctness. When presenting plans or code, anticipate DDD challenges: layer violations, naming conventions, aggregate boundaries, port/adapter placement. Don't wait for the user to invoke `/ddd-java` — apply DDD principles proactively during design and flag any questionable decisions.

---
*Last updated: 2026-03-08*