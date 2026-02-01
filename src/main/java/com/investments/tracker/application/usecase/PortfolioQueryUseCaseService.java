package com.investments.tracker.application.usecase;

import com.investments.tracker.domain.model.Portfolio;
import com.investments.tracker.domain.model.Position;
import com.investments.tracker.domain.repository.PositionRepository;
import com.investments.tracker.domain.service.PortfolioCalculationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of portfolio query use case.
 */
@Service
@Transactional(readOnly = true)
public class PortfolioQueryUseCaseService implements PortfolioQueryUseCase {

    private final PositionRepository positionRepository;
    private final PortfolioCalculationService portfolioCalculationService;

    public PortfolioQueryUseCaseService(
            PositionRepository positionRepository,
            PortfolioCalculationService portfolioCalculationService) {
        this.positionRepository = positionRepository;
        this.portfolioCalculationService = portfolioCalculationService;
    }

    @Override
    public Portfolio getPortfolio() {
        List<Position> positions = new ArrayList<>(positionRepository.findAll());
        return portfolioCalculationService.createPortfolio(positions);
    }
}
