package com.investments.tracker.application.usecase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.investments.tracker.domain.model.Instrument;
import com.investments.tracker.domain.model.Portfolio;
import com.investments.tracker.domain.model.Position;
import com.investments.tracker.domain.model.value.Currency;
import com.investments.tracker.domain.model.value.ExchangeRate;
import com.investments.tracker.domain.model.value.InstrumentSymbol;
import com.investments.tracker.domain.model.value.Price;
import com.investments.tracker.domain.repository.CurrentPriceProvider;
import com.investments.tracker.domain.repository.ExchangeRateProvider;
import com.investments.tracker.domain.repository.InstrumentRepository;
import com.investments.tracker.domain.repository.PositionRepository;
import com.investments.tracker.domain.service.PortfolioCalculationService;

/** Implementation of portfolio query use case. */
@Service
@Transactional(readOnly = true)
public class PortfolioQueryUseCaseService implements PortfolioQueryUseCase {

    private final PositionRepository positionRepository;
    private final InstrumentRepository instrumentRepository;
    private final CurrentPriceProvider currentPriceProvider;
    private final ExchangeRateProvider exchangeRateProvider;
    private final PortfolioCalculationService portfolioCalculationService;

    public PortfolioQueryUseCaseService(
            PositionRepository positionRepository,
            InstrumentRepository instrumentRepository,
            CurrentPriceProvider currentPriceProvider,
            ExchangeRateProvider exchangeRateProvider,
            PortfolioCalculationService portfolioCalculationService) {
        this.positionRepository = positionRepository;
        this.instrumentRepository = instrumentRepository;
        this.currentPriceProvider = currentPriceProvider;
        this.exchangeRateProvider = exchangeRateProvider;
        this.portfolioCalculationService = portfolioCalculationService;
    }

    @Override
    public Portfolio getPortfolio() {
        List<Position> positions = new ArrayList<>(positionRepository.findAll());
        Map<InstrumentSymbol, Price> currentPrices = buildPriceMap();
        Map<Currency, ExchangeRate> exchangeRates = buildExchangeRateMap(currentPrices);
        return portfolioCalculationService.createPortfolio(positions, currentPrices, exchangeRates);
    }

    private Map<InstrumentSymbol, Price> buildPriceMap() {
        List<InstrumentSymbol> symbols =
                instrumentRepository.findAll().stream().map(Instrument::symbol).toList();
        return currentPriceProvider.getPrices(symbols);
    }

    private Map<Currency, ExchangeRate> buildExchangeRateMap(
            Map<InstrumentSymbol, Price> pricesBySymbol) {

        Set<Currency> currencies =
                pricesBySymbol.values().stream().map(Price::currency).collect(Collectors.toSet());

        currencies.add(Currency.PLN);

        return exchangeRateProvider.getExchangeRatesToPln(currencies);
    }
}
