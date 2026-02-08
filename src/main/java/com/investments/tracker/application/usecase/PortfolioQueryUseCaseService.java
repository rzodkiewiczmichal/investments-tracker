package com.investments.tracker.application.usecase;

import com.investments.tracker.domain.model.Instrument;
import com.investments.tracker.domain.model.Portfolio;
import com.investments.tracker.domain.model.Position;
import com.investments.tracker.domain.model.value.InstrumentSymbol;
import com.investments.tracker.domain.model.value.Price;
import com.investments.tracker.domain.repository.InstrumentRepository;
import com.investments.tracker.domain.repository.PositionRepository;
import com.investments.tracker.domain.service.PortfolioCalculationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of portfolio query use case.
 */
@Service
@Transactional(readOnly = true)
public class PortfolioQueryUseCaseService implements PortfolioQueryUseCase {

    private final PositionRepository positionRepository;
    private final InstrumentRepository instrumentRepository;
    private final PortfolioCalculationService portfolioCalculationService;

    public PortfolioQueryUseCaseService(
            PositionRepository positionRepository,
            InstrumentRepository instrumentRepository,
            PortfolioCalculationService portfolioCalculationService) {
        this.positionRepository = positionRepository;
        this.instrumentRepository = instrumentRepository;
        this.portfolioCalculationService = portfolioCalculationService;
    }

    @Override
    public Portfolio getPortfolio() {
        List<Position> positions = new ArrayList<>(positionRepository.findAll());
        Map<InstrumentSymbol, Price> currentPrices = buildPriceMap();
        return portfolioCalculationService.createPortfolio(positions, currentPrices);
    }

    private Map<InstrumentSymbol, Price> buildPriceMap() {
        return instrumentRepository.findAll().stream()
                .filter(i -> i.currentPrice() != null)
                .collect(Collectors.toMap(Instrument::symbol, Instrument::currentPrice));
    }
}
