package com.investments.tracker.application.usecase;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.investments.tracker.application.exception.ResourceNotFoundException;
import com.investments.tracker.domain.model.AccountHolding;
import com.investments.tracker.domain.model.Instrument;
import com.investments.tracker.domain.model.Position;
import com.investments.tracker.domain.model.value.AccountId;
import com.investments.tracker.domain.model.value.Currency;
import com.investments.tracker.domain.model.value.ExchangeRate;
import com.investments.tracker.domain.model.value.InstrumentSymbol;
import com.investments.tracker.domain.model.value.Price;
import com.investments.tracker.domain.repository.CurrentPriceProvider;
import com.investments.tracker.domain.repository.ExchangeRateProvider;
import com.investments.tracker.domain.repository.PositionRepository;

/** Implementation of position query use case. */
@Service
@Transactional(readOnly = true)
public class PositionQueryUseCaseService implements PositionQueryUseCase {

    private final PositionRepository positionRepository;
    private final InstrumentQueryUseCase instrumentQueryUseCase;
    private final AccountQueryUseCase accountQueryUseCase;
    private final CurrentPriceProvider currentPriceProvider;
    private final ExchangeRateProvider exchangeRateProvider;

    public PositionQueryUseCaseService(
            PositionRepository positionRepository,
            InstrumentQueryUseCase instrumentQueryUseCase,
            AccountQueryUseCase accountQueryUseCase,
            CurrentPriceProvider currentPriceProvider,
            ExchangeRateProvider exchangeRateProvider) {
        this.positionRepository = positionRepository;
        this.instrumentQueryUseCase = instrumentQueryUseCase;
        this.accountQueryUseCase = accountQueryUseCase;
        this.currentPriceProvider = currentPriceProvider;
        this.exchangeRateProvider = exchangeRateProvider;
    }

    @Override
    public Collection<Position> listPositions() {
        return positionRepository.findAll();
    }

    @Override
    public Position getPosition(InstrumentSymbol symbol) {
        Objects.requireNonNull(symbol, "symbol cannot be null");

        return positionRepository
                .findBySymbol(symbol)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Position", "symbol", symbol.value()));
    }

    @Override
    public List<PositionWithMarketData> listPositionsWithMarketData() {
        Collection<Position> positions = positionRepository.findAll();

        Map<InstrumentSymbol, Instrument> instrumentMap =
                instrumentQueryUseCase.listInstruments().stream()
                        .collect(Collectors.toMap(Instrument::symbol, Function.identity()));

        Map<InstrumentSymbol, Price> prices =
                currentPriceProvider.getPrices(positions.stream().map(Position::symbol).toList());

        Set<Currency> currencies =
                positions.stream()
                        .flatMap(p -> p.holdings().stream())
                        .map(h -> h.costBasis().currency())
                        .collect(Collectors.toCollection(java.util.HashSet::new));
        // Also include instrument currencies for price-to-PLN conversion
        positions.stream()
                .map(p -> instrumentMap.get(p.symbol()))
                .filter(Objects::nonNull)
                .map(Instrument::currency)
                .forEach(currencies::add);
        Map<Currency, ExchangeRate> exchangeRates =
                exchangeRateProvider.getExchangeRatesToPln(currencies);

        return positions.stream()
                .map(
                        position -> {
                            Instrument instrument = instrumentMap.get(position.symbol());
                            if (instrument == null) {
                                throw new ResourceNotFoundException(
                                        "Instrument", "symbol", position.symbol().value());
                            }
                            Price price = prices.get(position.symbol());
                            return new PositionWithMarketData(
                                    position, instrument, price, exchangeRates);
                        })
                .toList();
    }

    @Override
    public PositionDetailData getPositionDetail(InstrumentSymbol symbol) {
        Objects.requireNonNull(symbol, "symbol cannot be null");

        Position position =
                positionRepository
                        .findBySymbol(symbol)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Position", "symbol", symbol.value()));
        Instrument instrument = instrumentQueryUseCase.getInstrument(symbol);
        Price currentPrice = currentPriceProvider.getPrice(symbol).orElse(null);
        Map<AccountId, String> accountNames = resolveAccountNames(position);
        Map<Currency, ExchangeRate> exchangeRates = resolveExchangeRates(position, instrument);

        return new PositionDetailData(
                position, instrument, currentPrice, accountNames, exchangeRates);
    }

    private Map<Currency, ExchangeRate> resolveExchangeRates(
            Position position, Instrument instrument) {
        Set<Currency> currencies =
                position.holdings().stream()
                        .map(h -> h.costBasis().currency())
                        .collect(Collectors.toCollection(java.util.HashSet::new));
        // Include instrument currency for price-to-PLN conversion
        currencies.add(instrument.currency());
        return exchangeRateProvider.getExchangeRatesToPln(currencies);
    }

    private Map<AccountId, String> resolveAccountNames(Position position) {
        Map<AccountId, String> accountNames = new HashMap<>();
        for (AccountHolding holding : position.holdings()) {
            AccountId accountId = holding.accountId();
            if (!accountNames.containsKey(accountId)) {
                try {
                    String name = accountQueryUseCase.getAccount(accountId).name().value();
                    accountNames.put(accountId, name);
                } catch (ResourceNotFoundException e) {
                    accountNames.put(accountId, "Unknown Account");
                }
            }
        }
        return accountNames;
    }
}
