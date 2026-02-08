package com.investments.tracker.infrastructure.config;

import com.investments.tracker.domain.service.PortfolioCalculationService;
import com.investments.tracker.domain.service.PositionCalculationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration for domain services.
 * <p>
 * Domain services are pure domain objects without Spring annotations.
 * This configuration exposes them as Spring beans for dependency injection
 * in the application layer.
 * </p>
 */
@Configuration
public class DomainServiceConfig {

    @Bean
    public PositionCalculationService positionCalculationService() {
        return new PositionCalculationService();
    }

    @Bean
    public PortfolioCalculationService portfolioCalculationService(
            PositionCalculationService positionCalculationService) {
        return new PortfolioCalculationService(positionCalculationService);
    }
}
