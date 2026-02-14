package com.investments.tracker.infrastructure.web.controller;

import com.investments.tracker.application.dto.mapper.PositionMapper;
import com.investments.tracker.application.dto.request.AddPositionRequest;
import com.investments.tracker.application.dto.request.UpdatePositionRequest;
import com.investments.tracker.application.dto.response.PositionDetailResponse;
import com.investments.tracker.application.dto.response.PositionListResponse;
import com.investments.tracker.application.dto.response.PositionSummaryDTO;
import com.investments.tracker.application.exception.ResourceNotFoundException;
import com.investments.tracker.application.usecase.AccountQueryUseCase;
import com.investments.tracker.application.usecase.InstrumentQueryUseCase;
import com.investments.tracker.application.usecase.PositionCommandUseCase;
import com.investments.tracker.application.usecase.PositionQueryUseCase;
import com.investments.tracker.domain.model.AccountHolding;
import com.investments.tracker.domain.model.Instrument;
import com.investments.tracker.domain.model.Position;
import com.investments.tracker.domain.model.value.AccountId;
import com.investments.tracker.domain.model.value.CostBasis;
import com.investments.tracker.domain.model.value.Currency;
import com.investments.tracker.domain.model.value.ExchangeRate;
import com.investments.tracker.domain.model.value.InstrumentName;
import com.investments.tracker.domain.model.value.InstrumentSymbol;
import com.investments.tracker.domain.model.value.InstrumentType;
import com.investments.tracker.domain.model.value.Money;
import com.investments.tracker.domain.model.value.Quantity;
import com.investments.tracker.domain.repository.ExchangeRateProvider;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * REST controller for position operations.
 */
@RestController
@RequestMapping("/api/v1/positions")
public class PositionController {

    private final PositionQueryUseCase positionQueryUseCase;
    private final PositionCommandUseCase positionCommandUseCase;
    private final AccountQueryUseCase accountQueryUseCase;
    private final InstrumentQueryUseCase instrumentQueryUseCase;
    private final PositionMapper positionMapper;
    private final ExchangeRateProvider exchangeRateProvider;

    public PositionController(
            PositionQueryUseCase positionQueryUseCase,
            PositionCommandUseCase positionCommandUseCase,
            AccountQueryUseCase accountQueryUseCase,
            InstrumentQueryUseCase instrumentQueryUseCase,
            PositionMapper positionMapper,
            ExchangeRateProvider exchangeRateProvider) {
        this.positionQueryUseCase = positionQueryUseCase;
        this.positionCommandUseCase = positionCommandUseCase;
        this.accountQueryUseCase = accountQueryUseCase;
        this.instrumentQueryUseCase = instrumentQueryUseCase;
        this.positionMapper = positionMapper;
        this.exchangeRateProvider = exchangeRateProvider;
    }

    /**
     * Lists all positions sorted by current value descending (FR-014).
     *
     * @return position list response
     */
    @GetMapping
    public PositionListResponse listPositions() {
        Collection<Position> positions = positionQueryUseCase.listPositions();
        Map<InstrumentSymbol, Instrument> instrumentMap = instrumentQueryUseCase.listInstruments()
                .stream()
                .collect(Collectors.toMap(Instrument::symbol, Function.identity()));

        Set<Currency> currencies = positions.stream()
                .flatMap(p -> p.holdings().stream())
                .map(h -> h.costBasis().currency())
                .collect(Collectors.toSet());
        Map<Currency, ExchangeRate> exchangeRates = exchangeRateProvider.getExchangeRatesToPln(currencies);

        List<PositionSummaryDTO> summaries = positions.stream()
                .map(position -> {
                    Instrument instrument = instrumentMap.get(position.symbol());
                    if (instrument == null) {
                        throw new ResourceNotFoundException(
                                "Instrument", "symbol", position.symbol().value());
                    }
                    return positionMapper.toSummaryDTO(position, instrument, exchangeRates);
                })
                .sorted(Comparator.comparing(
                        (PositionSummaryDTO dto) -> dto.currentValue() != null ? dto.currentValue().amount() : null,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();

        return new PositionListResponse(summaries, summaries.size());
    }

    /**
     * Gets a specific position by symbol.
     *
     * @param symbol the instrument symbol
     * @return position detail response
     */
    @GetMapping("/{symbol}")
    public PositionDetailResponse getPosition(@PathVariable String symbol) {
        InstrumentSymbol instrumentSymbol = new InstrumentSymbol(symbol);
        Position position = positionQueryUseCase.getPosition(instrumentSymbol);
        Instrument instrument = instrumentQueryUseCase.getInstrument(instrumentSymbol);
        Map<AccountId, String> accountNames = getAccountNames(position);
        Map<Currency, ExchangeRate> exchangeRates = getExchangeRates(position);
        return positionMapper.toDetailResponse(position, instrument, accountNames, exchangeRates);
    }

    /**
     * Adds a new position or adds shares to an existing position.
     *
     * @param request the add position request
     * @return position detail response
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PositionDetailResponse addPosition(@Valid @RequestBody AddPositionRequest request) {
        InstrumentSymbol instrumentSymbol = new InstrumentSymbol(request.instrumentSymbol());
        Currency currency = Currency.valueOf(request.currency());
        Position position = positionCommandUseCase.addPosition(
                instrumentSymbol,
                new InstrumentName(request.instrumentName()),
                InstrumentType.valueOf(request.instrumentType()),
                currency,
                new AccountId(request.accountId()),
                new Quantity(request.quantity()),
                CostBasis.of(new Money(request.averageCost(), currency)));
        Instrument instrument = instrumentQueryUseCase.getInstrument(instrumentSymbol);
        Map<AccountId, String> accountNames = getAccountNames(position);
        Map<Currency, ExchangeRate> exchangeRates = getExchangeRates(position);
        return positionMapper.toDetailResponse(position, instrument, accountNames, exchangeRates);
    }

    /**
     * Updates a position by adding more shares.
     *
     * @param symbol the instrument symbol
     * @param request the update position request
     * @return position detail response
     */
    @PutMapping("/{symbol}")
    public PositionDetailResponse updatePosition(
            @PathVariable String symbol,
            @Valid @RequestBody UpdatePositionRequest request) {
        InstrumentSymbol instrumentSymbol = new InstrumentSymbol(symbol);
        Instrument instrument = instrumentQueryUseCase.getInstrument(instrumentSymbol);
        Position position = positionCommandUseCase.updatePosition(
                instrumentSymbol,
                new AccountId(request.accountId()),
                new Quantity(request.quantity()),
                CostBasis.of(new Money(request.averageCost(), instrument.currency())));
        Map<AccountId, String> accountNames = getAccountNames(position);
        Map<Currency, ExchangeRate> exchangeRates = getExchangeRates(position);
        return positionMapper.toDetailResponse(position, instrument, accountNames, exchangeRates);
    }

    /**
     * Gets exchange rates to PLN for all currencies in a position's holdings.
     */
    private Map<Currency, ExchangeRate> getExchangeRates(Position position) {
        Set<Currency> currencies = position.holdings().stream()
                .map(h -> h.costBasis().currency())
                .collect(Collectors.toSet());
        return exchangeRateProvider.getExchangeRatesToPln(currencies);
    }

    /**
     * Gets account names for all accounts in a position.
     *
     * @param position the position
     * @return map of account IDs to account names
     */
    private Map<AccountId, String> getAccountNames(Position position) {
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
