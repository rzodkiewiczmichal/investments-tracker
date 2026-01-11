# ADR-009: REST API Structure

## Status
Accepted

## Context

The Investment Tracker application requires a REST API to enable communication between the Angular frontend and the Java Spring Boot backend. The API must:

1. **Support v0.1 MVP Features**: Portfolio viewing, position details, manual position entry
2. **Follow REST Principles**: Resource-based URLs, appropriate HTTP verbs, stateless
3. **Enable Frontend Development**: Clear contract for Angular team to build against
4. **Support Future Growth**: Versioning strategy for backwards compatibility
5. **Integrate with Architecture**: Align with hexagonal architecture (ports/adapters)
6. **Include Observability**: Support OTLP tracing through all requests

### Application Services Available

From ADR-003, the following application services define our use cases:
- `PortfolioViewingService` - view portfolio and list positions
- `ManualEntryService` - create and update positions
- `ImportService` - import positions from CSV (v0.2+)
- `ReconciliationService` - reconcile with broker statements (v0.7)

### v0.1 MVP Scope

**In Scope:**
- GET portfolio summary (FR-001 to FR-004)
- GET list of positions (FR-014)
- GET individual position details (FR-011, FR-012)
- POST new position (FR-041, FR-042, FR-044 to FR-046)
- GET accounts list

**Out of Scope:**
- Import endpoints (v0.2)
- Position updates/deletes
- Price management endpoints (v0.4)
- Reconciliation endpoints (v0.7)

## Decision

### API Versioning Strategy

**Decision**: URL-based versioning with `/api/v1/` prefix

**Base URL Structure**:
```
http://localhost:8080/api/v1/{resource}
```

**Rationale**:
- **Clear and Explicit**: Version immediately visible in URL
- **Angular-Friendly**: Easy to configure base URL in Angular HttpClient
- **Proxy-Friendly**: Can route different versions to different backend instances
- **Standard Practice**: Widely adopted in REST APIs

**Alternative Rejected**: Header-based versioning (`Accept: application/vnd.investments.v1+json`)
- More complex for frontend developers
- Harder to test in browser
- URL versioning sufficient for MVP

---

### Resource Naming Conventions

**Decision**: Use plural nouns for all resources

**Resource Names**:
- `/portfolio` - Single portfolio (special case - user has one portfolio)
- `/positions` - Collection of positions
- `/accounts` - Collection of accounts

**Rationale**:
- **RESTful Standard**: Plural nouns represent collections
- **Consistency**: All resources follow same pattern
- **Intuitive**: `GET /positions` clearly returns multiple items

---

### Endpoint Design for v0.1

#### 1. Portfolio Endpoints

**GET /api/v1/portfolio**
- **Purpose**: View aggregated portfolio metrics across all accounts
- **Use Case**: `PortfolioViewingService.viewPortfolio()`
- **Request**: None
- **Response**: `PortfolioSummaryDTO`
- **Status Codes**:
  - 200 OK: Portfolio retrieved successfully
  - 500 Internal Server Error: Database or calculation error
- **Empty Portfolio Handling**: Returns zeros (FR-004)

**Example Response**:
```json
{
  "totalCurrentValue": {
    "amount": 150000.00,
    "currency": "PLN"
  },
  "totalInvestedAmount": {
    "amount": 120000.00,
    "currency": "PLN"
  },
  "totalProfitLoss": {
    "amount": 30000.00,
    "currency": "PLN"
  },
  "totalReturnPercentage": 25.00,
  "positionsCount": 3,
  "lastUpdatedAt": "2026-01-11T10:30:00Z"
}
```

---

#### 2. Position Endpoints

**GET /api/v1/positions**
- **Purpose**: List all positions sorted by current value descending (FR-014)
- **Use Case**: `PortfolioViewingService.listPositions()`
- **Request**: Query parameters (optional):
  - `sortBy`: "currentValue" (default), "returnPercentage", "profitLoss", "quantity"
  - `order`: "DESC" (default), "ASC"
- **Response**: Array of `PositionSummaryDTO`
- **Status Codes**:
  - 200 OK: Positions retrieved successfully (empty array if no positions)
  - 400 Bad Request: Invalid query parameter
  - 500 Internal Server Error: Database error

**Example Response**:
```json
{
  "positions": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440001",
      "instrumentName": "Apple Inc.",
      "instrumentSymbol": "AAPL",
      "instrumentType": "STOCK",
      "quantity": 100.00000000,
      "averageCost": {
        "amount": 600.0000,
        "currency": "PLN"
      },
      "currentValue": {
        "amount": 65000.00,
        "currency": "PLN"
      },
      "investedAmount": {
        "amount": 60000.00,
        "currency": "PLN"
      },
      "profitLoss": {
        "amount": 5000.00,
        "currency": "PLN"
      },
      "returnPercentage": 8.33
    }
  ],
  "totalCount": 1
}
```

---

**GET /api/v1/positions/{id}**
- **Purpose**: View individual position details (FR-011, FR-012)
- **Use Case**: New method `PositionViewingService.getPosition(UUID id)`
- **Request**: Path parameter `id` (UUID)
- **Response**: `PositionDetailDTO`
- **Status Codes**:
  - 200 OK: Position found
  - 404 Not Found: Position with given ID does not exist
  - 500 Internal Server Error: Database error

**Example Response**:
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440001",
  "instrumentName": "Apple Inc.",
  "instrumentSymbol": "AAPL",
  "instrumentType": "STOCK",
  "accountId": "550e8400-e29b-41d4-a716-446655440000",
  "accountName": "Main Broker Account",
  "quantity": 100.00000000,
  "averageCost": {
    "amount": 600.0000,
    "currency": "PLN"
  },
  "currentPrice": {
    "amount": 650.0000,
    "currency": "PLN"
  },
  "currentValue": {
    "amount": 65000.00,
    "currency": "PLN"
  },
  "investedAmount": {
    "amount": 60000.00,
    "currency": "PLN"
  },
  "profitLoss": {
    "amount": 5000.00,
    "currency": "PLN"
  },
  "returnPercentage": 8.33,
  "createdAt": "2026-01-01T00:00:00Z",
  "updatedAt": "2026-01-11T10:30:00Z"
}
```

---

**POST /api/v1/positions**
- **Purpose**: Create a new position manually (FR-041, FR-042)
- **Use Case**: `ManualEntryService.addPosition(AddPositionCommand)`
- **Request**: `AddPositionCommand` in request body
- **Response**: `PositionDetailDTO` (created position)
- **Status Codes**:
  - 201 Created: Position created successfully
  - 400 Bad Request: Validation error (missing field, invalid value)
  - 409 Conflict: Position already exists for this instrument in this account
  - 500 Internal Server Error: Database error

**Example Request**:
```json
{
  "instrumentName": "Apple Inc.",
  "instrumentSymbol": "AAPL",
  "instrumentType": "STOCK",
  "accountId": "550e8400-e29b-41d4-a716-446655440000",
  "quantity": 100.00000000,
  "averageCost": 600.0000
}
```

**Example Response**: Same as GET /api/v1/positions/{id}

---

#### 3. Account Endpoints

**GET /api/v1/accounts**
- **Purpose**: List accounts available for position assignment
- **Use Case**: `AccountService.listAccounts()`
- **Request**: None
- **Response**: Array of `AccountDTO`
- **Status Codes**:
  - 200 OK: Accounts retrieved successfully
  - 500 Internal Server Error: Database error

**Example Response**:
```json
{
  "accounts": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "name": "Main Broker Account",
      "broker": "ING",
      "accountType": "NORMAL",
      "createdAt": "2026-01-01T00:00:00Z"
    }
  ],
  "totalCount": 1
}
```

---

### HTTP Verb Usage

| Operation | HTTP Verb | Endpoint Pattern | Example |
|-----------|-----------|------------------|---------|
| Read collection | GET | /resource | GET /api/v1/positions |
| Read single | GET | /resource/{id} | GET /api/v1/positions/123 |
| Create | POST | /resource | POST /api/v1/positions |
| Update (v0.2+) | PUT | /resource/{id} | PUT /api/v1/positions/123 |
| Delete (v0.2+) | DELETE | /resource/{id} | DELETE /api/v1/positions/123 |

**v0.1 Scope**: Only GET and POST

---

### Content Negotiation

**Decision**: JSON only for v0.1

**Request Headers**:
```
Content-Type: application/json
Accept: application/json
```

**Response Headers**:
```
Content-Type: application/json; charset=utf-8
```

**Rationale**:
- Angular's HttpClient uses JSON by default
- Simplifies implementation
- Can add XML/other formats in future versions if needed

---

### Request/Response DTO Structure

#### Common Patterns

**Money Representation**:
```json
{
  "amount": 1234.5678,
  "currency": "PLN"
}
```
- Follows ADR-006 (DECIMAL(19,4) precision)
- Explicit currency field (always "PLN" in v0.1)
- Separates amount from currency for type safety

**Timestamps**:
```json
"createdAt": "2026-01-11T10:30:00Z"
```
- ISO 8601 format with UTC timezone indicator (Z)
- Consistent across all DTOs

**UUIDs**:
```json
"id": "550e8400-e29b-41d4-a716-446655440001"
```
- String format (hyphenated)
- 36 characters including hyphens

**Enums**:
```json
"instrumentType": "STOCK"
```
- Uppercase string values
- Validated on server side

---

### Pagination (Future - Not v0.1)

**Design for Future**:
When needed (v0.2+), use query parameters:
```
GET /api/v1/positions?page=1&size=20
```

Response includes pagination metadata:
```json
{
  "positions": [...],
  "page": {
    "number": 1,
    "size": 20,
    "totalElements": 157,
    "totalPages": 8
  }
}
```

**v0.1 Note**: Not needed for MVP (max 5 positions expected)

---

### CORS Configuration

**Decision**: Allow CORS for Angular frontend on localhost:4200

**Configuration**:
```
Access-Control-Allow-Origin: http://localhost:4200
Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS
Access-Control-Allow-Headers: Content-Type, Authorization
Access-Control-Max-Age: 3600
```

**Rationale**:
- Angular frontend runs on different port (4200)
- Backend API on port 8080
- CORS required for cross-origin requests

---

## Consequences

### Positive

1. **Clear API Contract**: Spec-first approach provides contract before implementation
2. **RESTful Design**: Standard patterns easy to understand and consume
3. **Version Isolation**: URL versioning enables breaking changes in v2
4. **Frontend Independence**: Angular team can develop against spec
5. **Stateless**: No session management simplifies scaling
6. **Testable**: Each endpoint independently testable
7. **Observability Ready**: All requests flow through controllers for OTLP tracing

### Negative

1. **Version Proliferation**: Multiple versions to maintain over time
2. **URL Length**: Version prefix adds characters to every URL
3. **No Hypermedia**: Not fully RESTful (no HATEOAS) - acceptable for v0.1
4. **Manual Sync**: Spec-first requires keeping spec and code in sync

### Mitigation Strategies

1. **Version Deprecation Policy**: Define lifecycle (v1 supported for 12 months after v2 release)
2. **OpenAPI Validation**: Use tooling to validate spec consistency
3. **Integration Tests**: Test against spec to ensure implementation matches
4. **Documentation**: Keep OpenAPI spec as single source of truth

---

## Alternatives Considered

### Alternative 1: GraphQL API

**Rejected**:
- Over-engineering for MVP
- Angular has excellent REST support
- Team unfamiliar with GraphQL
- REST sufficient for v0.1 requirements
- Can add GraphQL in v2.0+ if needed

### Alternative 2: Header-Based Versioning

**Rejected**:
- More complex for frontend developers
- Harder to test in browser/Postman
- URL versioning more explicit and standard

### Alternative 3: No Versioning (Breaking Changes Allowed)

**Rejected**:
- MVP will evolve rapidly
- Frontend and backend developed separately
- Versioning enables parallel development
- Low cost to implement upfront

### Alternative 4: gRPC

**Rejected**:
- Angular prefers REST/JSON
- HTTP/2 not required for v0.1 scale
- REST more familiar to team
- OpenAPI tooling ecosystem mature

---

## Related Decisions

- [ADR-003: Domain vs Application Services](ADR-003-domain-vs-application-services.md) - Services that endpoints delegate to
- [ADR-004: Package Structure](ADR-004-package-structure.md) - Controller location (infrastructure/web/controller/)
- [ADR-006: Money Representation](ADR-006-money-representation.md) - Money DTO structure
- [ADR-010: Error Handling Strategy](ADR-010-error-handling-strategy.md) - HTTP status code mapping
- [ADR-011: Data Validation Strategy](ADR-011-data-validation-strategy.md) - Request validation

---

## Implementation Notes

### Controller Pattern

```java
@RestController
@RequestMapping("/api/v1/positions")
@CrossOrigin(origins = "http://localhost:4200")
public class PositionController {

    private final ManualEntryService manualEntryService;
    private final PortfolioViewingService portfolioViewingService;

    @GetMapping
    public ResponseEntity<PositionsResponse> listPositions(
        @RequestParam(defaultValue = "currentValue") String sortBy,
        @RequestParam(defaultValue = "DESC") String order
    ) {
        List<PositionSummaryDTO> positions = portfolioViewingService.listPositions();
        return ResponseEntity.ok(new PositionsResponse(positions));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PositionDetailDTO> getPosition(@PathVariable UUID id) {
        PositionDetailDTO position = portfolioViewingService.getPosition(id);
        return ResponseEntity.ok(position);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PositionDetailDTO createPosition(@Valid @RequestBody AddPositionCommand command) {
        return manualEntryService.addPosition(command);
    }
}
```

### OpenAPI Specification Location

**File**: `docs/api/openapi.yaml`

**Purpose**: Complete API contract with all endpoints, schemas, examples

**Tooling**: Can generate from spec or validate code against spec

---

## References

- RESTful API Design Best Practices
- OpenAPI 3.0 Specification
- RFC 7231: HTTP/1.1 Semantics and Content
- FR-001 to FR-046: Functional requirements
- NFR-064: Framework-independent domain
- Angular HttpClient documentation
