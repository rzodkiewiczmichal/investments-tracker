# ADR-010: Error Handling Strategy

## Status
Accepted

## Context

A REST API must provide clear, consistent, and actionable error responses to frontend developers. The Investment Tracker API needs to:

1. **Map Domain Exceptions**: Translate domain exceptions to appropriate HTTP status codes
2. **User-Friendly Messages**: Provide clear error messages without exposing implementation details
3. **Support Debugging**: Include trace IDs for OTLP observability integration
4. **Validation Errors**: Handle Bean Validation errors with field-level details
5. **Security**: Never expose stack traces or sensitive data in responses
6. **Consistency**: Same error format across all endpoints

### Key Requirements

- **NFR-053**: User-friendly error messages (no technical jargon or stack traces)
- **NFR-054**: OTLP tracing integration (trace ID in responses)
- **FR-044 to FR-046**: Specific validation error messages for manual entry

### Exception Sources

From ADR-003 and functional requirements:
- **Domain Exceptions**: InvalidQuantityException, PositionNotFoundException, InvalidPriceException
- **Application Exceptions**: ValidationException, PositionAlreadyExistsException
- **Infrastructure Exceptions**: DatabaseConnectionException, PriceFetchException
- **Framework Exceptions**: MethodArgumentNotValidException (Bean Validation)

## Decision

### Global Exception Handler

**Decision**: Use Spring's `@RestControllerAdvice` to centralize exception handling

**Location**: `com.investments.tracker.infrastructure.web.exception.GlobalExceptionHandler`

**Rationale**:
- **Single Responsibility**: All error handling logic in one place
- **DRY Principle**: No repetitive try-catch in controllers
- **Consistency**: Guarantees same error format everywhere
- **Separation of Concerns**: Controllers focus on request/response, not error handling

---

### Error Response Format

**Decision**: Consistent JSON structure for all errors

**Schema**: `ErrorResponseDTO`
```json
{
  "timestamp": "2026-01-11T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "details": [
    {
      "field": "quantity",
      "message": "Quantity must be greater than zero",
      "rejectedValue": -10
    }
  ],
  "path": "/api/v1/positions",
  "traceId": "4bf92f3570d1d8c4517b702d7d6e8319"
}
```

**Required Fields**:
- `timestamp`: ISO 8601 UTC timestamp when error occurred
- `status`: HTTP status code (integer)
- `error`: HTTP status text (e.g., "Bad Request")
- `message`: User-friendly error message
- `path`: Request path that caused the error
- `traceId`: OTLP trace ID for debugging

**Optional Fields**:
- `details`: Array of validation errors (only for 400 Bad Request with validation failures)

**Rationale**:
- **Self-Documenting**: Clearly indicates what went wrong
- **Actionable**: Frontend can display `message` to user
- **Traceable**: `traceId` links to backend logs/traces
- **Debug-Friendly**: `path` and `timestamp` help reproduce issues
- **Standard Format**: Similar to Spring Boot's default error response

---

### HTTP Status Code Mapping

| Domain Exception | HTTP Status | User Message | Details |
|------------------|-------------|--------------|---------|
| `InvalidQuantityException` | 400 Bad Request | "Quantity must be greater than zero" | Validation failed |
| `InvalidPriceException` | 400 Bad Request | "Average cost must be greater than zero" | Validation failed |
| `MethodArgumentNotValidException` | 400 Bad Request | "Validation failed" | Includes field-level details |
| `PositionNotFoundException` | 404 Not Found | "Position with ID {id} not found" | Resource not found |
| `AccountNotFoundException` | 404 Not Found | "Account with ID {id} not found" | Resource not found |
| `InstrumentNotFoundException` | 404 Not Found | "Instrument with symbol {symbol} not found" | Resource not found |
| `PositionAlreadyExistsException` | 409 Conflict | "Position already exists for this instrument in this account" | Conflict with existing resource |
| `DatabaseException` | 500 Internal Server Error | "An unexpected error occurred while processing your request" | Hide technical details |
| `PriceFetchException` | 500 Internal Server Error | "Unable to fetch current price" | External service failure |
| `Exception` (uncaught) | 500 Internal Server Error | "An unexpected error occurred" | Catch-all handler |

**Principles**:
- **4xx Client Errors**: Client can fix by changing request
- **5xx Server Errors**: Server-side issue, client cannot fix
- **404 for Missing Resources**: Not found is distinct from validation failure
- **409 for Business Rule Violations**: Duplicate position is conflict
- **Generic 500 Messages**: Never expose technical details in user messages

---

### Validation Error Response

**400 Bad Request** with validation details:

```json
{
  "timestamp": "2026-01-11T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "details": [
    {
      "field": "instrumentName",
      "message": "Instrument name is required",
      "rejectedValue": null
    },
    {
      "field": "quantity",
      "message": "Quantity must be greater than zero",
      "rejectedValue": -10
    },
    {
      "field": "averageCost",
      "message": "Average cost must be greater than zero",
      "rejectedValue": 0
    }
  ],
  "path": "/api/v1/positions",
  "traceId": "4bf92f3570d1d8c4517b702d7d6e8319"
}
```

**Validation Error Messages** (FR-044 to FR-046):
- Missing field: "{Field} is required"
- Invalid quantity: "Quantity must be greater than zero"
- Invalid cost: "Average cost must be greater than zero"
- Invalid format: "{Field} must be {expected format}"

**Rationale**:
- **Field-Level Details**: Frontend can highlight specific input fields
- **Multiple Errors**: All validation errors returned at once
- **User-Friendly**: Messages suitable for display to end users
- **Rejected Value**: Frontend can show what value was rejected

---

### 404 Not Found Response

```json
{
  "timestamp": "2026-01-11T10:30:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Position with ID 550e8400-e29b-41d4-a716-446655440099 not found",
  "path": "/api/v1/positions/550e8400-e29b-41d4-a716-446655440099",
  "traceId": "4bf92f3570d1d8c4517b702d7d6e8319"
}
```

**Rationale**:
- **Specific**: Includes the ID that wasn't found
- **Clear**: "Not found" is unambiguous
- **No Details Array**: Single error, no validation details

---

### 409 Conflict Response

```json
{
  "timestamp": "2026-01-11T10:30:00Z",
  "status": 409,
  "error": "Conflict",
  "message": "Position already exists for instrument AAPL in account Main Broker Account",
  "path": "/api/v1/positions",
  "traceId": "4bf92f3570d1d8c4517b702d7d6e8319"
}
```

**Rationale**:
- **Business Rule Violation**: Not a validation error, but a conflict
- **Actionable**: User can choose different account or update existing position
- **Context**: Includes instrument and account for clarity

---

### 500 Internal Server Error Response

```json
{
  "timestamp": "2026-01-11T10:30:00Z",
  "status": 500,
  "error": "Internal Server Error",
  "message": "An unexpected error occurred while processing your request",
  "path": "/api/v1/portfolio",
  "traceId": "4bf92f3570d1d8c4517b702d7d6e8319"
}
```

**Rationale**:
- **Generic Message**: Don't expose internal details (database errors, NPE, etc.)
- **Trace ID Critical**: Backend logs have full details linked to trace ID
- **User-Friendly**: Non-technical message
- **Security**: Stack traces never included

---

### OTLP Trace ID Integration

**Decision**: Include OTLP trace ID in all error responses

**Implementation**:
```java
String traceId = tracer.currentSpan().context().traceId();
```

**Rationale**:
- **NFR-054**: OTLP tracing integration required
- **Debugging**: Link frontend errors to backend logs/traces
- **Observability**: End-to-end request tracking
- **Support**: Customer support can reference trace ID

**Trace ID Format**: 32-character hexadecimal string
```
"traceId": "4bf92f3570d1d8c4517b702d7d6e8319"
```

---

### Security Considerations

**Never Expose**:
- Stack traces
- SQL queries
- Internal class/package names
- Database connection strings
- Environment variables
- File system paths

**Log Internally**:
- Full exception stack trace
- Request details (headers, body)
- User ID (when authentication added in v2.0)
- Trace ID for correlation

**Balance**:
- **User-Friendly**: Clear message for frontend display
- **Debug-Friendly**: Trace ID links to detailed backend logs
- **Secure**: No sensitive information leakage

---

### Exception Handler Implementation

**GlobalExceptionHandler.java**:

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private final Tracer tracer;

    // 400 Bad Request - Bean Validation
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDTO handleValidationExceptions(
        MethodArgumentNotValidException ex,
        HttpServletRequest request
    ) {
        List<ValidationErrorDTO> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> new ValidationErrorDTO(
                error.getField(),
                error.getDefaultMessage(),
                error.getRejectedValue()
            ))
            .toList();

        log.warn("Validation failed for request to {}: {}",
            request.getRequestURI(), errors);

        return ErrorResponseDTO.builder()
            .timestamp(LocalDateTime.now(ZoneOffset.UTC))
            .status(HttpStatus.BAD_REQUEST.value())
            .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
            .message("Validation failed")
            .details(errors)
            .path(request.getRequestURI())
            .traceId(getTraceId())
            .build();
    }

    // 400 Bad Request - Domain Validation
    @ExceptionHandler({InvalidQuantityException.class, InvalidPriceException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDTO handleDomainValidationExceptions(
        DomainException ex,
        HttpServletRequest request
    ) {
        log.warn("Domain validation failed for request to {}: {}",
            request.getRequestURI(), ex.getMessage());

        return ErrorResponseDTO.builder()
            .timestamp(LocalDateTime.now(ZoneOffset.UTC))
            .status(HttpStatus.BAD_REQUEST.value())
            .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
            .message(ex.getMessage())
            .path(request.getRequestURI())
            .traceId(getTraceId())
            .build();
    }

    // 404 Not Found
    @ExceptionHandler({PositionNotFoundException.class, AccountNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponseDTO handleNotFoundExceptions(
        DomainException ex,
        HttpServletRequest request
    ) {
        log.info("Resource not found for request to {}: {}",
            request.getRequestURI(), ex.getMessage());

        return ErrorResponseDTO.builder()
            .timestamp(LocalDateTime.now(ZoneOffset.UTC))
            .status(HttpStatus.NOT_FOUND.value())
            .error(HttpStatus.NOT_FOUND.getReasonPhrase())
            .message(ex.getMessage())
            .path(request.getRequestURI())
            .traceId(getTraceId())
            .build();
    }

    // 409 Conflict
    @ExceptionHandler(PositionAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponseDTO handleConflictExceptions(
        DomainException ex,
        HttpServletRequest request
    ) {
        log.warn("Conflict for request to {}: {}",
            request.getRequestURI(), ex.getMessage());

        return ErrorResponseDTO.builder()
            .timestamp(LocalDateTime.now(ZoneOffset.UTC))
            .status(HttpStatus.CONFLICT.value())
            .error(HttpStatus.CONFLICT.getReasonPhrase())
            .message(ex.getMessage())
            .path(request.getRequestURI())
            .traceId(getTraceId())
            .build();
    }

    // 500 Internal Server Error - Catch All
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponseDTO handleAllExceptions(
        Exception ex,
        HttpServletRequest request
    ) {
        log.error("Unexpected error for request to {}: {}",
            request.getRequestURI(), ex.getMessage(), ex);

        return ErrorResponseDTO.builder()
            .timestamp(LocalDateTime.now(ZoneOffset.UTC))
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
            .message("An unexpected error occurred while processing your request")
            .path(request.getRequestURI())
            .traceId(getTraceId())
            .build();
    }

    private String getTraceId() {
        Span currentSpan = tracer.currentSpan();
        return currentSpan != null ? currentSpan.context().traceId() : "unknown";
    }
}
```

---

## Consequences

### Positive

1. **Consistent Errors**: Same format across all endpoints
2. **User-Friendly**: Clear messages suitable for end users
3. **Debug-Friendly**: Trace IDs link to backend logs
4. **Security**: No sensitive data exposed
5. **Frontend Integration**: Simple to parse and display
6. **Validation Details**: Field-level errors for forms
7. **Observability**: OTLP integration built-in
8. **Maintainable**: Centralized handler easy to update

### Negative

1. **Generic 500 Messages**: Users don't know root cause (by design)
2. **Trace ID Dependency**: Requires OTLP setup to be useful
3. **Log Volume**: All errors logged, could be noisy
4. **Message Localization**: English only in v0.1 (can add i18n later)

### Mitigation Strategies

1. **Monitoring**: Alert on 500 errors to catch issues quickly
2. **Trace ID Documentation**: Teach users to provide trace ID to support
3. **Log Levels**: Use appropriate levels (WARN for 400, ERROR for 500)
4. **i18n Future**: Design allows adding localization in v2.0

---

## Alternatives Considered

### Alternative 1: Problem Details (RFC 7807)

**Format**:
```json
{
  "type": "https://example.com/probs/validation-failed",
  "title": "Validation Failed",
  "status": 400,
  "detail": "Quantity must be greater than zero",
  "instance": "/api/v1/positions"
}
```

**Rejected**:
- More verbose than needed for v0.1
- `type` URI adds complexity
- Custom format simpler and sufficient
- Can adopt RFC 7807 in v2.0 if needed

### Alternative 2: Spring Boot Default Error Response

**Rejected**:
- No OTLP trace ID
- No validation details array
- `timestamp` format inconsistent
- Harder to customize
- Want full control over format

### Alternative 3: Exception-per-Status-Code

Create separate exception classes for each HTTP status code.

**Rejected**:
- Tightly couples domain to HTTP
- Violates hexagonal architecture
- Domain should not know about HTTP status codes
- GlobalExceptionHandler provides better separation

### Alternative 4: GraphQL-Style Errors Array

**Rejected**:
- Over-engineering for REST API
- GraphQL pattern doesn't map well to REST
- Single error message sufficient for most cases
- Validation errors use `details` array

---

## Related Decisions

- [ADR-003: Domain vs Application Services](ADR-003-domain-vs-application-services.md) - Exception mapping from domain
- [ADR-009: REST API Structure](ADR-009-rest-api-structure.md) - HTTP status codes per endpoint
- [ADR-011: Data Validation Strategy](ADR-011-data-validation-strategy.md) - Validation error sources

---

## Implementation Checklist

- [ ] Create `ErrorResponseDTO` class in `application/dto/`
- [ ] Create `ValidationErrorDTO` class in `application/dto/`
- [ ] Create `GlobalExceptionHandler` class in `infrastructure/web/exception/`
- [ ] Define domain exceptions in `domain/exception/`
- [ ] Configure OTLP tracer injection
- [ ] Write integration tests for error scenarios
- [ ] Document error responses in OpenAPI spec

---

## Testing Strategy

### Unit Tests
```java
@Test
void shouldHandleValidationException() {
    MethodArgumentNotValidException ex = createValidationException();
    ErrorResponseDTO response = handler.handleValidationExceptions(ex, request);

    assertThat(response.getStatus()).isEqualTo(400);
    assertThat(response.getMessage()).isEqualTo("Validation failed");
    assertThat(response.getDetails()).hasSize(2);
    assertThat(response.getTraceId()).isNotNull();
}
```

### Integration Tests
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

---

## References

- RFC 7807: Problem Details for HTTP APIs
- Spring Framework @RestControllerAdvice documentation
- OWASP Top 10: Security Logging and Monitoring Failures
- NFR-053: User-friendly error messages
- NFR-054: OTLP tracing integration
- FR-044 to FR-046: Validation error messages
