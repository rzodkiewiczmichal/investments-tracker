package com.investments.tracker.cucumber;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Cucumber-Spring integration configuration.
 * <p>
 * Connects Cucumber BDD tests to Spring Boot application context with real infrastructure:
 * - PostgreSQL database via Testcontainers
 * - Full Spring Boot application startup
 * - All adapters and services initialized
 * </p>
 * <p>
 * All Cucumber step definitions will have access to Spring beans via @Autowired.
 * </p>
 *
 * @see <a href="../../docs/adr/ADR-012-test-architecture.md">ADR-012: Test Architecture</a>
 * @see <a href="../../docs/adr/ADR-013-mock-vs-real-dependencies.md">ADR-013: Mock vs Real Dependencies</a>
 */
@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("cucumber")
public class CucumberSpringConfiguration {

    /**
     * PostgreSQL container for Cucumber tests.
     * <p>
     * Shared across all Cucumber scenarios for performance.
     * Database is cleaned between scenarios in step definitions.
     * </p>
     */
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("cucumber_testdb")
            .withUsername("cucumber")
            .withPassword("cucumber")
            .withReuse(true);

    /**
     * Configure Spring Boot to use Testcontainers PostgreSQL for Cucumber tests.
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.flyway.enabled", () -> "true");
    }
}
