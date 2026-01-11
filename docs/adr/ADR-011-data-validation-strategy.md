# ADR-011: Data Validation Strategy

## Status
Accepted

## Context

The Investment Tracker REST API must validate incoming data at multiple architectural layers to ensure:

1. **Data Integrity**: Prevent invalid data from entering the system
2. **User Experience**: Provide clear, actionable error messages (NFR-053)
3. **Security**: Reject malicious or malformed input at API boundary
4. **Domain Invariants**: Enforce business rules at domain level
5. **Fail Fast**: Detect validation errors early in request processing
6. **Consistency**: Same validation approach across all endpoints

### Requirements

From functional requirements:
- **FR-044**: Clear error message when required field is missing
- **FR-045**: "Quantity must be greater than zero" for invalid quantity
- **FR-046**: "Average cost must be greater than zero" for invalid price
- **NFR-053**: User-friendly error messages without technical jargon

From ADR-010 (Error Handling):
- Validation errors return 400 Bad Request
- Field-level details in `ErrorResponseDTO.details` array
- Consistent error format with OTLP trace ID

### Validation Sources

**Controller Layer** (Request DTOs):
- Missing required fields
- Invalid data types
- Format violations (e.g., invalid UUID)
- Range violations (negative numbers)

**Application Layer** (Business Context):
- Account doesn't exist
- Position already exists for instrument + account
- Instrument type not supported

**Domain Layer** (Invariants):
- Quantity must be positive (domain rule)
- Average cost must be positive (domain rule)
- InstrumentSymbol cannot be empty or whitespace

## Decision

### Three-Layer Validation Strategy

We implement validation at three distinct architectural layers:

```
┌─────────────────────────────────────────────────┐
│  1. Controller Layer (Bean Validation)         │
│     - Format, type, required, range            │
│     - Returns: 400 Bad Request                 │
└─────────────────────────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────────┐
│  2. Application Layer (Business Context)       │
│     - Entity existence, business rules         │
│     - Returns: 404 Not Found, 409 Conflict     │
└─────────────────────────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────────┐
│  3. Domain Layer (Invariants)                  │
│     - Domain model consistency                 │
│     - Throws: Domain exceptions                │
└─────────────────────────────────────────────────┘
```

---

### Layer 1: Controller Validation (Bean Validation)

**Decision**: Use Jakarta Bean Validation 3.0 with `@Valid` annotation

**Location**: Request DTO classes in `application/dto/`

**Validation Annotations**:
```java
public record AddPositionCommand(
    @NotBlank(message = "Instrument name is required")
    String instrumentName,

    @NotBlank(message = "Instrument symbol is required")
    String instrumentSymbol,

    @NotNull(message = "Instrument type is required")
    InstrumentType instrumentType,

    @NotNull(message = "Account ID is required")
    UUID accountId,

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be greater than zero")
    @DecimalMin(value = "0.00000001", message = "Quantity must be at least 0.00000001")
    BigDecimal quantity,

    @NotNull(message = "Average cost is required")
    @Positive(message = "Average cost must be greater than zero")
    @DecimalMin(value = "0.0001", message = "Average cost must be at least 0.0001")
    BigDecimal averageCost
) {}
```

**Controller Usage**:
```java
@RestController
@RequestMapping("/api/v1/positions")
public class PositionController {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PositionDetailDTO createPosition(@Valid @RequestBody AddPositionCommand command) {
        // Bean Validation triggered automatically by @Valid
        // If validation fails, MethodArgumentNotValidException thrown
        return manualEntryService.addPosition(command);
    }
}
```

**Error Handling**:
- `MethodArgumentNotValidException` caught by GlobalExceptionHandler
- Returns 400 Bad Request with field-level details
- See ADR-010 for error response format

**Rationale**:
- **Standard Java**: Jakarta Bean Validation is industry standard
- **Declarative**: Validation rules visible on DTO definition
- **Framework Integration**: Spring Boot validates automatically with @Valid
- **Fail Fast**: Invalid requests rejected before reaching application logic
- **Field-Level Errors**: BindingResult provides detailed field errors

---

### Layer 2: Application Service Validation

**Decision**: Application services validate business context before delegating to domain

**Location**: Application service classes in `application/service/`

**Validation Responsibilities**:
1. **Entity Existence**: Verify referenced entities exist
2. **Business Rule Violations**: Check uniqueness, conflicts
3. **State Preconditions**: Verify system state allows operation

**Example Implementation**:
```java
@Service
@Transactional
public class ManualEntryService {

    private final PositionRepository positionRepository;
    private final AccountRepository accountRepository;
    private final InstrumentRepository instrumentRepository;

    public PositionDetailDTO addPosition(AddPositionCommand command) {
        // Layer 2 Validation: Business Context

        // 1. Verify account exists
        Account account = accountRepository.findById(command.accountId())
            .orElseThrow(() -> new AccountNotFoundException(
                "Account with ID " + command.accountId() + " not found"
            ));

        // 2. Check if position already exists
        InstrumentSymbol symbol = new InstrumentSymbol(command.instrumentSymbol());
        if (positionRepository.existsForInstrumentAndAccount(symbol, account.getId())) {
            throw new PositionAlreadyExistsException(
                "Position already exists for instrument " + command.instrumentSymbol() +
                " in account " + account.getName()
            );
        }

        // 3. Get or create instrument
        Instrument instrument = instrumentRepository.findBySymbol(symbol)
            .orElseGet(() -> createInstrument(command));

        // Layer 3: Domain validation happens in constructors
        Position position = Position.create(
            symbol,
            Money.pln(command.averageCost()),
            command.quantity(),
            account,
            instrument
        );

        // Save and return
        Position saved = positionRepository.save(position);
        return PositionMapper.toDetailDTO(saved);
    }
}
```

**Exception Mapping**:
| Exception | HTTP Status | Handled By |
|-----------|-------------|------------|
| `AccountNotFoundException` | 404 Not Found | GlobalExceptionHandler |
| `PositionAlreadyExistsException` | 409 Conflict | GlobalExceptionHandler |
| `InstrumentNotFoundException` | 404 Not Found | GlobalExceptionHandler |

**Rationale**:
- **Hexagonal Architecture**: Application layer coordinates between ports
- **Business Context**: Only application layer has full repository access
- **Clear Separation**: Business validation separate from format validation
- **Meaningful Errors**: Context-aware messages (includes entity names)

---

### Layer 3: Domain Validation (Invariants)

**Decision**: Domain entities enforce their own invariants in constructors and setters

**Location**: Domain entity classes in `domain/model/`

**Validation Approach**:
- Validate in constructors (fail fast)
- Validate in factory methods
- Throw domain-specific exceptions

**Example - Position Entity**:
```java
public class Position {

    private InstrumentSymbol symbol;  // Must not be null
    private BigDecimal totalQuantity;  // Must be > 0
    private Money avgCostBasis;        // Must be > 0
    private List<AccountHolding> holdings;  // Must not be empty

    // Private constructor - use factory method
    private Position() {}

    // Factory method with validation
    public static Position create(
        InstrumentSymbol symbol,
        Money avgCostBasis,
        BigDecimal quantity,
        Account account,
        Instrument instrument
    ) {
        // Layer 3 Validation: Domain Invariants

        if (symbol == null) {
            throw new IllegalArgumentException("Instrument symbol cannot be null");
        }
        if (avgCostBasis == null || avgCostBasis.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidPriceException("Average cost must be greater than zero");
        }
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidQuantityException("Quantity must be greater than zero");
        }

        Position position = new Position();
        position.symbol = symbol;
        position.avgCostBasis = avgCostBasis;
        position.totalQuantity = quantity;

        // Create initial holding
        AccountHolding holding = AccountHolding.create(
            symbol, account.getId(), quantity, avgCostBasis
        );
        position.holdings = new ArrayList<>();
        position.holdings.add(holding);

        return position;
    }

    // Domain methods also validate
    public void addHolding(AccountHolding holding) {
        if (holding == null) {
            throw new IllegalArgumentException("Holding cannot be null");
        }
        if (!holding.getInstrumentSymbol().equals(this.symbol)) {
            throw new IllegalArgumentException("Holding symbol must match position symbol");
        }

        holdings.add(holding);
        recalculateAggregates();  // Maintains invariants
    }
}
```

**Example - Value Object Validation**:
```java
public record InstrumentSymbol(String value) {

    // Compact constructor with validation
    public InstrumentSymbol {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Instrument symbol cannot be empty");
        }
        if (value.length() > 50) {
            throw new IllegalArgumentException("Instrument symbol cannot exceed 50 characters");
        }
        // Normalize to uppercase
        value = value.trim().toUpperCase();
    }
}
```

**Exception Types**:
- `InvalidQuantityException` - Domain exception for quantity violations
- `InvalidPriceException` - Domain exception for price violations
- `IllegalArgumentException` - For precondition violations

**Rationale**:
- **Always Valid**: Domain objects cannot exist in invalid state
- **Encapsulation**: Domain logic stays in domain layer
- **Self-Documenting**: Validation rules visible in domain code
- **Framework Independence**: No dependency on Bean Validation or Spring

---

### Validation Messages (FR-044 to FR-046)

**Decision**: User-friendly messages that match functional requirements

**Required Messages**:

| Validation | Message | FR Reference |
|------------|---------|--------------|
| Missing instrument name | "Instrument name is required" | FR-044 |
| Missing instrument symbol | "Instrument symbol is required" | FR-044 |
| Missing quantity | "Quantity is required" | FR-044 |
| Missing average cost | "Average cost is required" | FR-044 |
| Zero or negative quantity | "Quantity must be greater than zero" | FR-045 |
| Zero or negative cost | "Average cost must be greater than zero" | FR-046 |
| Invalid quantity format | "Quantity must be a valid decimal number" | FR-044 |
| Invalid cost format | "Average cost must be a valid decimal number" | FR-044 |

**Message Guidelines**:
1. **Clear and Specific**: State what's wrong and what's expected
2. **No Technical Jargon**: Avoid terms like "validation failed", "constraint violation"
3. **Actionable**: User knows how to fix the error
4. **Consistent Terminology**: Use same field names as UI labels
5. **Positive Framing**: "must be greater than zero" not "cannot be zero or negative"

---

### Custom Validators (Optional - Not for v0.1)

**Decision**: Use standard Bean Validation for v0.1, add custom validators when needed

**Example Custom Validator** (for future reference):
```java
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = InstrumentSymbolValidator.class)
public @interface ValidInstrumentSymbol {
    String message() default "Invalid instrument symbol format";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

public class InstrumentSymbolValidator implements ConstraintValidator<ValidInstrumentSymbol, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;  // @NotNull handles null check
        }

        // Example: Validate ticker format (1-10 uppercase letters) or ISIN (12 alphanumeric)
        return value.matches("^[A-Z]{1,10}$") || value.matches("^[A-Z]{2}[A-Z0-9]{10}$");
    }
}
```

**Usage**:
```java
public record AddPositionCommand(
    @NotBlank
    @ValidInstrumentSymbol
    String instrumentSymbol,
    // ...
) {}
```

**Rationale for Deferral**:
- v0.1 accepts any non-empty symbol
- Ticker/ISIN validation can be added in v0.2+
- Keep v0.1 simple and flexible

---

### Validation Groups (Not Used in v0.1)

**Decision**: No validation groups for v0.1 MVP

**Rationale**:
- Single validation scenario (create position)
- No partial updates in v0.1
- Groups add complexity without benefit
- Can add in v0.2+ if needed for update operations

**Example for Future** (if partial updates added):
```java
public interface CreateValidation {}
public interface UpdateValidation {}

public record UpdatePositionCommand(
    @NotNull(groups = UpdateValidation.class)
    UUID id,

    @Positive(groups = {CreateValidation.class, UpdateValidation.class})
    BigDecimal quantity
) {}
```

---

## Consequences

### Positive

1. **Defense in Depth**: Multiple validation layers catch different error types
2. **Clear Separation**: Each layer has distinct validation responsibility
3. **User-Friendly Errors**: Field-level details help users fix issues
4. **Framework Integration**: Bean Validation works seamlessly with Spring
5. **Domain Protection**: Domain entities always in valid state
6. **Fail Fast**: Invalid requests rejected at API boundary
7. **Consistent Messages**: Same error format across all endpoints
8. **Testable**: Each layer independently unit-testable
9. **Standard Java**: No proprietary validation framework

### Negative

1. **Validation Duplication**: Same check may exist at multiple layers (by design)
2. **Message Maintenance**: Validation messages scattered across layers
3. **Bean Validation Dependency**: Controller DTOs depend on Jakarta validation API
4. **Learning Curve**: Developers must understand three-layer strategy
5. **Potential Over-Validation**: Risk of validating same thing multiple times

### Mitigation Strategies

1. **Duplication is Intentional**: Each layer validates for different reason
   - Controller: Format and type safety
   - Application: Business context
   - Domain: Invariants

2. **Message Centralization**: Create constants class if messages used in multiple places
   ```java
   public class ValidationMessages {
       public static final String QUANTITY_REQUIRED = "Quantity is required";
       public static final String QUANTITY_POSITIVE = "Quantity must be greater than zero";
   }
   ```

3. **Documentation**: This ADR serves as guide for validation approach

4. **Code Reviews**: Ensure developers apply validation at correct layer

---

## Alternatives Considered

### Alternative 1: Single-Layer Validation (Controller Only)

Validate everything at controller layer with extensive Bean Validation annotations.

**Rejected**:
- Cannot validate business context (entity existence)
- Violates hexagonal architecture (domain depends on Bean Validation)
- Domain entities could exist in invalid state
- Business rules leak into controller layer

### Alternative 2: Database Constraints Only

Rely on PostgreSQL constraints and catch database exceptions.

**Rejected**:
- Poor user experience (database error messages not user-friendly)
- Fails late (after application processing)
- Performance overhead (database round-trip for validation)
- Couples validation to database implementation

### Alternative 3: JSR-303 Bean Validation Everywhere

Use Bean Validation in domain entities.

**Rejected**:
- Violates hexagonal architecture (domain depends on framework)
- Domain not framework-independent (NFR-064)
- Adds dependency to domain layer
- Domain validation should use domain language, not annotations

### Alternative 4: Validation Service Pattern

Create separate validation service classes.

**Rejected for v0.1**:
- Over-engineering for current complexity
- Validation logic better co-located with validated entities
- Can introduce in v2.0+ if validation becomes complex
- Current approach sufficient for MVP

---

## Implementation Checklist

Controller Layer:
- [ ] Add Jakarta Bean Validation dependency to build.gradle
- [ ] Annotate AddPositionCommand with @NotNull, @NotBlank, @Positive
- [ ] Use @Valid annotation on controller method parameters
- [ ] Test validation with invalid requests (integration tests)

Application Layer:
- [ ] Implement entity existence checks in ManualEntryService
- [ ] Throw PositionAlreadyExistsException for duplicates
- [ ] Throw AccountNotFoundException when account doesn't exist
- [ ] Write unit tests for application validation logic

Domain Layer:
- [ ] Add validation to Position.create() factory method
- [ ] Add validation to InstrumentSymbol value object constructor
- [ ] Define InvalidQuantityException and InvalidPriceException
- [ ] Write unit tests for domain invariant violations

Error Handling:
- [ ] Verify GlobalExceptionHandler handles MethodArgumentNotValidException
- [ ] Verify field-level details in error response
- [ ] Verify OTLP trace ID inclusion
- [ ] Write integration tests for validation error scenarios

---

## Testing Strategy

### Controller Validation Tests (Integration)
```java
@SpringBootTest
@AutoConfigureMockMvc
class PositionControllerValidationTest {

    @Test
    void shouldReturn400WhenQuantityIsNegative() {
        AddPositionCommand command = new AddPositionCommand(
            "Apple Inc.",
            "AAPL",
            InstrumentType.STOCK,
            accountId,
            new BigDecimal("-10"),  // Invalid: negative
            new BigDecimal("500")
        );

        mockMvc.perform(post("/api/v1/positions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(command)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("Validation failed"))
            .andExpect(jsonPath("$.details[0].field").value("quantity"))
            .andExpect(jsonPath("$.details[0].message").value("Quantity must be greater than zero"))
            .andExpect(jsonPath("$.details[0].rejectedValue").value(-10))
            .andExpect(jsonPath("$.traceId").exists());
    }

    @Test
    void shouldReturn400WhenRequiredFieldMissing() {
        AddPositionCommand command = new AddPositionCommand(
            null,  // Invalid: missing
            "AAPL",
            InstrumentType.STOCK,
            accountId,
            new BigDecimal("100"),
            new BigDecimal("500")
        );

        mockMvc.perform(post("/api/v1/positions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(command)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.details[0].field").value("instrumentName"))
            .andExpect(jsonPath("$.details[0].message").value("Instrument name is required"));
    }
}
```

### Application Validation Tests (Unit)
```java
class ManualEntryServiceTest {

    @Test
    void shouldThrowAccountNotFoundExceptionWhenAccountDoesNotExist() {
        when(accountRepository.findById(unknownAccountId))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.addPosition(command))
            .isInstanceOf(AccountNotFoundException.class)
            .hasMessageContaining("Account with ID " + unknownAccountId + " not found");
    }

    @Test
    void shouldThrowPositionAlreadyExistsExceptionWhenDuplicate() {
        when(accountRepository.findById(accountId))
            .thenReturn(Optional.of(account));
        when(positionRepository.existsForInstrumentAndAccount(symbol, accountId))
            .thenReturn(true);

        assertThatThrownBy(() -> service.addPosition(command))
            .isInstanceOf(PositionAlreadyExistsException.class)
            .hasMessageContaining("Position already exists");
    }
}
```

### Domain Validation Tests (Unit)
```java
class PositionTest {

    @Test
    void shouldThrowInvalidQuantityExceptionWhenQuantityIsZero() {
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

    @Test
    void shouldThrowInvalidPriceExceptionWhenCostIsNegative() {
        assertThatThrownBy(() -> Position.create(
                symbol,
                Money.pln("-500"),  // Invalid
                new BigDecimal("100"),
                account,
                instrument
            ))
            .isInstanceOf(InvalidPriceException.class)
            .hasMessage("Average cost must be greater than zero");
    }
}
```

---

## Related Decisions

- [ADR-009: REST API Structure](ADR-009-rest-api-structure.md) - Controller endpoints that trigger validation
- [ADR-010: Error Handling Strategy](ADR-010-error-handling-strategy.md) - Maps validation exceptions to HTTP responses
- [ADR-003: Domain vs Application Services](ADR-003-domain-vs-application-services.md) - Service layer validation responsibilities
- [ADR-002: Value Objects and Entities](ADR-002-value-objects-and-entities.md) - Domain entity invariants

---

## References

- Jakarta Bean Validation 3.0 Specification
- Spring Framework @Valid Documentation
- Domain-Driven Design by Eric Evans (Chapter on Aggregates and Invariants)
- FR-044: Clear error message when required field missing
- FR-045: "Quantity must be greater than zero" validation
- FR-046: "Average cost must be greater than zero" validation
- NFR-053: User-friendly error messages
- NFR-064: Framework-independent domain

---

## Version History

- 2026-01-11: Initial version for v0.1 MVP
- Future: Add custom validators when ticker/ISIN format validation needed
- Future: Add validation groups if partial update operations added
