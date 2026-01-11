# ADR-013: Mock vs Real Dependencies Strategy

## Status
Accepted

## Context

Testing in hexagonal architecture requires careful decisions about when to use mocked dependencies versus real implementations. The choice affects:

1. **Test Reliability**: Real dependencies catch more integration issues
2. **Test Speed**: Mocks are faster, real dependencies slower
3. **Test Isolation**: Mocks provide perfect isolation, real dependencies introduce coupling
4. **Maintenance**: Mocks require updates when interfaces change
5. **Confidence**: Real dependencies provide higher confidence in production behavior

### Trade-offs

**Mocks (Mockito)**:
- ✅ Fast execution (milliseconds)
- ✅ Complete control over behavior
- ✅ Easy to simulate edge cases and failures
- ✅ No external infrastructure required
- ❌ Can create false confidence (tests pass, production fails)
- ❌ Require maintenance when interfaces change
- ❌ Don't catch integration issues

**Real Dependencies (Testcontainers)**:
- ✅ High confidence in production behavior
- ✅ Catch integration issues (SQL errors, constraints, transactions)
- ✅ Test actual database features (JSONB, triggers, constraints)
- ✅ No mock maintenance needed
- ❌ Slow execution (seconds)
- ❌ Require Docker runtime
- ❌ More complex test setup

### Requirements

- **NFR-072**: 70%+ code coverage requires both fast unit tests and comprehensive integration tests
- **NFR-064**: Framework-independent domain (no Spring mocks in domain tests)
- **NFR-036**: PostgreSQL database (integration tests must use PostgreSQL, not H2)

## Decision

### Mock vs Real Decision Matrix

We use a **layered approach**: mocks for unit tests, real dependencies for integration tests.

| Test Type | Dependencies | Rationale |
|-----------|-------------|-----------|
| **Domain Unit Tests** | None (pure Java) | Domain logic has no external dependencies |
| **Application Unit Tests** | Mocks (Mockito) | Test use case logic without infrastructure |
| **Integration Tests** | Real (Testcontainers) | Test adapters with actual infrastructure |
| **Cucumber BDD Tests** | Real (Testcontainers) | End-to-end acceptance with real system |

---

### Layer 1: Domain Unit Tests (No Mocks)

**Decision**: Domain tests use no mocks or external dependencies

**Rationale**:
- Domain layer is pure Java (no frameworks)
- Value objects have no dependencies
- Entities only depend on value objects
- Domain services only depend on domain types

**Example**:
```java
@Test
void shouldCalculateProfitLossCorrectly() {
    // No mocks needed - pure domain logic
    Money avgCost = Money.pln("500");
    Money currentPrice = Money.pln("650");
    BigDecimal quantity = new BigDecimal("100");

    Money investedAmount = avgCost.multiply(quantity);  // 50,000 PLN
    Money currentValue = currentPrice.multiply(quantity);  // 65,000 PLN
    Money profitLoss = currentValue.subtract(investedAmount);  // 15,000 PLN

    assertThat(profitLoss.amount()).isEqualByComparingTo("15000.0000");
}
```

**When Domain Needs Collaborators**:
If domain service needs repository (e.g., PortfolioCalculationService needs PositionRepository):
- **Option A**: Pass data as parameters (preferred - keeps domain pure)
- **Option B**: Mock the repository (acceptable if unavoidable)

---

### Layer 2: Application Unit Tests (Mockito)

**Decision**: Application layer tests mock all ports (repositories, external services)

**Mocked Dependencies**:
- Repository ports (PositionRepository, AccountRepository, InstrumentRepository)
- External service ports (PriceService, NotificationService)
- Domain services (if complex)

**Not Mocked**:
- DTOs (data classes)
- Domain entities (created as real objects)
- Value objects (created as real objects)

**Example**:
```java
@ExtendWith(MockitoExtension.class)
class ManualEntryServiceTest {

    @Mock
    private PositionRepository positionRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private InstrumentRepository instrumentRepository;

    @InjectMocks
    private ManualEntryService service;

    @Test
    void shouldThrowAccountNotFoundExceptionWhenAccountDoesNotExist() {
        // Arrange: Mock repository behavior
        UUID unknownAccountId = UUID.randomUUID();
        when(accountRepository.findById(unknownAccountId))
            .thenReturn(Optional.empty());

        AddPositionCommand command = new AddPositionCommand(
            "Apple Inc.", "AAPL", InstrumentType.STOCK,
            unknownAccountId, new BigDecimal("100"), new BigDecimal("600")
        );

        // Act & Assert
        assertThatThrownBy(() -> service.addPosition(command))
            .isInstanceOf(AccountNotFoundException.class)
            .hasMessageContaining("Account with ID " + unknownAccountId + " not found");

        // Verify interaction
        verify(accountRepository).findById(unknownAccountId);
        verifyNoInteractions(positionRepository, instrumentRepository);
    }

    @Test
    void shouldCreatePositionSuccessfully() {
        // Arrange
        Account account = new Account(accountId, "Main Account", "ING", AccountType.NORMAL);
        Instrument instrument = new Instrument(
            new InstrumentSymbol("AAPL"),
            "Apple Inc.",
            InstrumentType.STOCK
        );

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(instrumentRepository.findBySymbol(any())).thenReturn(Optional.of(instrument));
        when(positionRepository.existsForInstrumentAndAccount(any(), any())).thenReturn(false);
        when(positionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        PositionDetailDTO result = service.addPosition(command);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.instrumentName()).isEqualTo("Apple Inc.");
        assertThat(result.quantity()).isEqualByComparingTo("100");

        // Verify interactions
        verify(accountRepository).findById(accountId);
        verify(instrumentRepository).findBySymbol(new InstrumentSymbol("AAPL"));
        verify(positionRepository).save(any(Position.class));
    }
}
```

**Mockito Best Practices**:
1. **Use @Mock and @InjectMocks**: Cleaner than manual mocking
2. **Verify Interactions**: Use `verify()` to ensure ports called correctly
3. **Mock Return Values**: Use `when().thenReturn()` for behavior
4. **Mock Exceptions**: Use `when().thenThrow()` for error scenarios
5. **Don't Over-Mock**: Only mock ports, not domain objects

---

### Layer 3: Integration Tests (Testcontainers)

**Decision**: Infrastructure tests use real PostgreSQL via Testcontainers

**Real Dependencies**:
- PostgreSQL database (Testcontainers)
- Spring Boot application context
- JPA repositories
- REST controllers (via MockMvc)
- Flyway migrations

**Not Real**:
- External services (PriceService) - mocked via @MockBean
- OTLP exporter - disabled in test profile
- Email/notification services - mocked

**Base Class**:
```java
@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class IntegrationTestBase {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test")
        .withReuse(true);  // Reuse container across tests

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.flyway.enabled", () -> "true");
    }

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @BeforeEach
    void cleanDatabase() {
        // Clean tables before each test (or use @Transactional + @Rollback)
    }
}
```

**Example**:
```java
class PositionControllerIntegrationTest extends IntegrationTestBase {

    @Test
    void shouldCreatePositionAndPersistToDatabase() throws Exception {
        // Arrange
        AddPositionCommand command = new AddPositionCommand(
            "Apple Inc.", "AAPL", InstrumentType.STOCK,
            accountId, new BigDecimal("100"), new BigDecimal("600")
        );

        // Act: POST request
        String response = mockMvc.perform(post("/api/v1/positions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(command)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.instrumentName").value("Apple Inc."))
            .andExpect(jsonPath("$.quantity").value(100))
            .andReturn()
            .getResponse()
            .getContentAsString();

        PositionDetailDTO created = objectMapper.readValue(response, PositionDetailDTO.class);

        // Assert: Verify persisted to database
        mockMvc.perform(get("/api/v1/positions/{id}", created.id()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.instrumentName").value("Apple Inc."));
    }

    @Test
    void shouldReturn404WhenPositionNotFound() throws Exception {
        UUID unknownId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/positions/{id}", unknownId))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.message").value(containsString("not found")))
            .andExpect(jsonPath("$.traceId").exists());
    }
}
```

**Testcontainers Best Practices**:
1. **Container Reuse**: Use `.withReuse(true)` for faster tests
2. **Same PostgreSQL Version**: Match production (postgres:16)
3. **Clean State**: Clear database between tests (@Transactional + @Rollback or manual cleanup)
4. **Network Alias**: Use `.withNetworkAliases()` if multiple containers
5. **Resource Limits**: Set memory limits to avoid OOM

---

### Layer 4: Cucumber BDD Tests (Real Dependencies)

**Decision**: Cucumber tests use real infrastructure for end-to-end acceptance

**Real Dependencies**:
- PostgreSQL (Testcontainers)
- Spring Boot application
- All adapters (REST controllers, repositories)
- Flyway migrations

**Mocked**:
- External services that cost money (Yahoo Finance API) - mock with WireMock
- Email services
- External notifications

**Configuration**:
```java
@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("cucumber")
public class CucumberSpringConfiguration {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
        .withDatabaseName("cucumber_testdb")
        .withUsername("cucumber")
        .withPassword("cucumber")
        .withReuse(true);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @LocalServerPort
    protected int port;
}
```

**Example Step Definition**:
```java
@SpringBootTest
public class PositionSteps {

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    private ResponseEntity<PortfolioSummaryDTO> portfolioResponse;

    @Given("I have no positions")
    public void i_have_no_positions() {
        positionRepository.deleteAll();  // Real database cleanup
    }

    @Given("I have the following positions:")
    public void i_have_the_following_positions(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps();
        for (Map<String, String> row : rows) {
            // Create real positions in database
            AddPositionCommand command = new AddPositionCommand(
                row.get("Instrument"),
                row.get("Instrument"),
                InstrumentType.STOCK,
                accountId,
                new BigDecimal(row.get("Quantity")),
                new BigDecimal(row.get("Average Cost"))
            );

            restTemplate.postForEntity("/api/v1/positions", command, PositionDetailDTO.class);
        }
    }

    @When("I request the portfolio summary")
    public void i_request_the_portfolio_summary() {
        portfolioResponse = restTemplate.getForEntity("/api/v1/portfolio", PortfolioSummaryDTO.class);
    }

    @Then("the positions count should be {int}")
    public void the_positions_count_should_be(Integer expectedCount) {
        assertThat(portfolioResponse.getBody().positionsCount()).isEqualTo(expectedCount);
    }
}
```

**Rationale**:
- **Real User Scenarios**: Tests exactly what users will experience
- **Confidence**: Catches issues mocks miss (transaction boundaries, constraints)
- **Living Documentation**: Features describe actual system behavior

---

### Exception: When to Mock in Integration Tests

**Decision**: Mock only external paid services or slow operations

**Mock with @MockBean**:
- Yahoo Finance API (costs money, has rate limits)
- Email sending (SendGrid, AWS SES)
- SMS notifications
- Payment gateways

**Example**:
```java
@SpringBootTest
class PositionControllerIntegrationTest extends IntegrationTestBase {

    @MockBean
    private PriceService priceService;  // Mock Yahoo Finance

    @Test
    void shouldFetchPriceFromYahooFinance() throws Exception {
        // Arrange: Mock external service
        when(priceService.getCurrentPrice(new InstrumentSymbol("AAPL")))
            .thenReturn(Money.pln("650"));

        // Act: Trigger price fetch via API
        mockMvc.perform(post("/api/v1/prices/refresh/{symbol}", "AAPL"))
            .andExpect(status().isOk());

        // Assert
        verify(priceService).getCurrentPrice(new InstrumentSymbol("AAPL"));
    }
}
```

**Rationale**:
- **Cost**: Avoid real API calls during tests
- **Speed**: Mocked responses instant
- **Reliability**: No network failures or rate limits
- **Control**: Simulate edge cases (API down, invalid response)

---

### Test Data Management Strategy

**Decision**: Use test data builders for complex objects

**Test Data Builders**:
```java
public class TestDataBuilder {

    public static Account defaultAccount() {
        return new Account(
            UUID.randomUUID(),
            "Test Account",
            "Test Broker",
            AccountType.NORMAL
        );
    }

    public static Instrument apple() {
        return new Instrument(
            new InstrumentSymbol("AAPL"),
            "Apple Inc.",
            InstrumentType.STOCK
        );
    }

    public static AddPositionCommand addPositionCommand() {
        return new AddPositionCommand(
            "Apple Inc.",
            "AAPL",
            InstrumentType.STOCK,
            UUID.randomUUID(),
            new BigDecimal("100"),
            new BigDecimal("600")
        );
    }

    public static class AddPositionCommandBuilder {
        private String instrumentName = "Apple Inc.";
        private String instrumentSymbol = "AAPL";
        private InstrumentType instrumentType = InstrumentType.STOCK;
        private UUID accountId = UUID.randomUUID();
        private BigDecimal quantity = new BigDecimal("100");
        private BigDecimal averageCost = new BigDecimal("600");

        public AddPositionCommandBuilder instrumentName(String name) {
            this.instrumentName = name;
            return this;
        }

        public AddPositionCommandBuilder quantity(String qty) {
            this.quantity = new BigDecimal(qty);
            return this;
        }

        public AddPositionCommand build() {
            return new AddPositionCommand(
                instrumentName, instrumentSymbol, instrumentType,
                accountId, quantity, averageCost
            );
        }
    }
}
```

**Usage**:
```java
@Test
void testWithCustomData() {
    AddPositionCommand command = TestDataBuilder.addPositionCommand()
        .instrumentName("Microsoft Corp.")
        .quantity("50")
        .build();

    // Use command in test
}
```

**Rationale**:
- **Readability**: Fluent builder API clear and concise
- **Maintainability**: Change defaults in one place
- **Flexibility**: Override only what's different per test

---

## Consequences

### Positive

1. **Clear Strategy**: Developers know when to use mocks vs real dependencies
2. **Fast Unit Tests**: Mocks enable millisecond execution
3. **High Confidence Integration**: Real database catches SQL issues
4. **No H2 Pitfalls**: PostgreSQL in tests matches production
5. **Cost Control**: Mock expensive external services
6. **Parallel Execution**: Testcontainers supports concurrent tests
7. **Realistic BDD**: Cucumber tests real system behavior

### Negative

1. **Docker Dependency**: Testcontainers requires Docker runtime
2. **Slower Integration Tests**: Real PostgreSQL adds seconds per test
3. **Mock Maintenance**: Mocks must be updated when interfaces change
4. **Complexity**: Developers must understand when to use each approach
5. **Resource Usage**: Multiple PostgreSQL containers consume memory

### Mitigation Strategies

1. **Docker Setup**: Document Docker installation in README
2. **Container Reuse**: Use `.withReuse(true)` to speed up tests
3. **Fast Unit Tests**: Run unit tests frequently, integration tests less often
4. **Test Categorization**: Separate fast and slow tests (see ADR-012)
5. **CI/CD Optimization**: Use GitHub Actions' Docker support

---

## Decision Rules Summary

Use this flowchart to decide:

```
Is this a domain unit test?
├─ YES → No mocks (pure Java)
└─ NO → Is this an application unit test?
    ├─ YES → Mock all ports (Mockito)
    └─ NO → Is this an integration test?
        ├─ YES → Real PostgreSQL (Testcontainers)
        │         Mock only external paid services
        └─ NO → Is this a Cucumber test?
            └─ YES → Real PostgreSQL (Testcontainers)
                      Mock only external paid services
```

**Quick Reference**:

| Test Layer | Use Mocks? | Use Real DB? |
|------------|-----------|--------------|
| Domain Unit | ❌ No (pure Java) | ❌ No |
| Application Unit | ✅ Yes (Mockito) | ❌ No |
| Integration | ⚠️ Partial (external services only) | ✅ Yes (Testcontainers) |
| Cucumber BDD | ⚠️ Partial (external services only) | ✅ Yes (Testcontainers) |

---

## Related Decisions

- [ADR-012: Test Architecture](ADR-012-test-architecture.md) - Overall testing strategy
- [ADR-008: Dependency Management](ADR-008-dependency-management.md) - Test dependencies (Mockito, Testcontainers)
- [ADR-004: Package Structure](ADR-004-package-structure.md) - Hexagonal architecture layers

---

## References

- Mockito Documentation: https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html
- Testcontainers Documentation: https://www.testcontainers.org/
- Spring Boot Testing: https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing
- Martin Fowler - Test Pyramid: https://martinfowler.com/articles/practical-test-pyramid.html
- NFR-072: TDD with BDD using Cucumber, 70%+ code coverage
