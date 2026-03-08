package com.investments.tracker.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.investments.tracker.domain.service.ImportCalculationService;
import com.investments.tracker.domain.service.PortfolioCalculationService;
import com.investments.tracker.domain.service.PositionCalculationService;

/**
 * Spring configuration for domain services.
 *
 * <p>Domain services are pure domain objects without Spring annotations. This configuration exposes
 * them as Spring beans for dependency injection in the application layer.
 */
@Configuration
public class DomainServiceConfig {

    @Bean
    public PositionCalculationService positionCalculationService() {
        return new PositionCalculationService();
    }

    @Bean
    public ImportCalculationService importCalculationService() {
        return new ImportCalculationService();
    }

    @Bean
    public PortfolioCalculationService portfolioCalculationService(
            PositionCalculationService positionCalculationService) {
        return new PortfolioCalculationService(positionCalculationService);
    }
}
