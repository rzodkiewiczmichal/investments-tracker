package com.investments.tracker.cucumber;

import com.investments.tracker.infrastructure.InvestmentTrackerApplication;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;

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
 * <p>
 * Note: We manually start the container in a static block instead of using @Testcontainers/@Container
 * because Cucumber doesn't use JUnit 5's extension lifecycle. The container must be running
 * before Spring's @DynamicPropertySource accesses it.
 * </p>
 *
 * @see <a href="../../docs/adr/ADR-012-test-architecture.md">ADR-012: Test Architecture</a>
 * @see <a href="../../docs/adr/ADR-013-mock-vs-real-dependencies.md">ADR-013: Mock vs Real Dependencies</a>
 */
@CucumberContextConfiguration
@SpringBootTest(
        classes = InvestmentTrackerApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("bdd")
public class CucumberSpringConfiguration {

    /**
     * PostgreSQL container for Cucumber tests.
     * <p>
     * Started manually in static block to ensure it's running before Spring context loads.
     * Shared across all Cucumber scenarios for performance.
     * Database is cleaned between scenarios in step definitions.
     * </p>
     */
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("cucumber_testdb")
            .withUsername("cucumber")
            .withPassword("cucumber")
            .withReuse(true);

    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379)
            .withReuse(true);

    static {
        postgres.start();
        redis.start();
    }

    /**
     * Configure Spring Boot to use Testcontainers PostgreSQL for Cucumber tests.
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }
}
