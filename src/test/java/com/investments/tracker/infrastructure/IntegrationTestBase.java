package com.investments.tracker.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base class for integration tests.
 * <p>
 * Provides:
 * - PostgreSQL database via Testcontainers
 * - Spring Boot application context
 * - MockMvc for REST API testing
 * - ObjectMapper for JSON serialization
 * </p>
 * <p>
 * All integration tests should extend this class to ensure consistent setup.
 * </p>
 * <p>
 * Usage:
 * <pre>
 * class PositionControllerIntegrationTest extends IntegrationTestBase {
 *
 *     &#64;Test
 *     void shouldCreatePosition() throws Exception {
 *         mockMvc.perform(post("/api/v1/positions")
 *                 .contentType(MediaType.APPLICATION_JSON)
 *                 .content(objectMapper.writeValueAsString(command)))
 *             .andExpect(status().isCreated());
 *     }
 * }
 * </pre>
 * </p>
 *
 * @see <a href="https://www.testcontainers.org/">Testcontainers Documentation</a>
 * @see <a href="../../docs/adr/ADR-012-test-architecture.md">ADR-012: Test Architecture</a>
 * @see <a href="../../docs/adr/ADR-013-mock-vs-real-dependencies.md">ADR-013: Mock vs Real Dependencies</a>
 */
@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class IntegrationTestBase {

    /**
     * PostgreSQL container shared across all integration tests.
     * <p>
     * Configuration:
     * - Version: postgres:16-alpine (matches production)
     * - Database: testdb
     * - Username: test
     * - Password: test
     * - Reuse: enabled (faster test execution)
     * </p>
     */
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);  // Reuse container across tests for speed

    /**
     * Configure Spring Boot to use Testcontainers PostgreSQL.
     * <p>
     * Dynamically sets:
     * - spring.datasource.url (from Testcontainers)
     * - spring.datasource.username
     * - spring.datasource.password
     * - spring.jpa.hibernate.ddl-auto=validate (use Flyway, not Hibernate)
     * - spring.flyway.enabled=true (run migrations)
     * </p>
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.flyway.enabled", () -> "true");
    }

    /**
     * MockMvc for testing REST controllers.
     * <p>
     * Use this to perform HTTP requests and assert responses without starting
     * a full HTTP server.
     * </p>
     * <p>
     * Example:
     * <pre>
     * mockMvc.perform(get("/api/v1/portfolio"))
     *     .andExpect(status().isOk())
     *     .andExpect(jsonPath("$.positionsCount").value(3));
     * </pre>
     * </p>
     */
    @Autowired
    protected MockMvc mockMvc;

    /**
     * ObjectMapper for JSON serialization/deserialization.
     * <p>
     * Use this to convert objects to JSON strings for POST/PUT requests,
     * and to parse JSON responses.
     * </p>
     * <p>
     * Example:
     * <pre>
     * String json = objectMapper.writeValueAsString(command);
     * mockMvc.perform(post("/api/v1/positions")
     *         .contentType(MediaType.APPLICATION_JSON)
     *         .content(json))
     *     .andExpect(status().isCreated());
     * </pre>
     * </p>
     */
    @Autowired
    protected ObjectMapper objectMapper;
}
