package com.investments.tracker.infrastructure;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for Investment Tracker application.
 * <p>
 * Spring Boot application configured with hexagonal architecture:
 * - Domain layer: Pure business logic, no framework dependencies
 * - Application layer: Use cases (ports) and application services
 * - Infrastructure layer: Framework adapters (REST, JPA, configuration)
 * </p>
 * <p>
 * Component scanning is configured to scan:
 * - com.investments.tracker.application - Application services
 * - com.investments.tracker.infrastructure - Infrastructure adapters
 * </p>
 */
@SpringBootApplication(scanBasePackages = "com.investments.tracker")
public class InvestmentTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(InvestmentTrackerApplication.class, args);
    }
}
