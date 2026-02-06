package com.investments.tracker.infrastructure.web.controller;

import com.investments.tracker.application.dto.mapper.PositionMapper;
import com.investments.tracker.application.dto.request.AddPositionRequest;
import com.investments.tracker.application.dto.request.UpdatePositionRequest;
import com.investments.tracker.application.dto.response.PositionDetailResponse;
import com.investments.tracker.application.dto.response.PositionListResponse;
import com.investments.tracker.application.dto.response.PositionSummaryDTO;
import com.investments.tracker.application.exception.ResourceNotFoundException;
import com.investments.tracker.application.usecase.AccountQueryUseCase;
import com.investments.tracker.application.usecase.PositionCommandUseCase;
import com.investments.tracker.application.usecase.PositionQueryUseCase;
import com.investments.tracker.domain.model.AccountHolding;
import com.investments.tracker.domain.model.Position;
import com.investments.tracker.domain.model.value.AccountId;
import com.investments.tracker.domain.model.value.CostBasis;
import com.investments.tracker.domain.model.value.InstrumentSymbol;
import com.investments.tracker.domain.model.value.Money;
import com.investments.tracker.domain.model.value.Price;
import com.investments.tracker.domain.model.value.Quantity;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for position operations.
 */
@RestController
@RequestMapping("/api/v1/positions")
public class PositionController {

    private final PositionQueryUseCase positionQueryUseCase;
    private final PositionCommandUseCase positionCommandUseCase;
    private final AccountQueryUseCase accountQueryUseCase;
    private final PositionMapper positionMapper;

    public PositionController(
            PositionQueryUseCase positionQueryUseCase,
            PositionCommandUseCase positionCommandUseCase,
            AccountQueryUseCase accountQueryUseCase,
            PositionMapper positionMapper) {
        this.positionQueryUseCase = positionQueryUseCase;
        this.positionCommandUseCase = positionCommandUseCase;
        this.accountQueryUseCase = accountQueryUseCase;
        this.positionMapper = positionMapper;
    }

    /**
     * Lists all positions.
     *
     * @return position list response
     */
    @GetMapping
    public PositionListResponse listPositions() {
        Collection<Position> positions = positionQueryUseCase.listPositions();
        List<PositionSummaryDTO> summaries = positions.stream()
                .map(positionMapper::toSummaryDTO)
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
        Position position = positionQueryUseCase.getPosition(new InstrumentSymbol(symbol));
        Map<AccountId, String> accountNames = getAccountNames(position);
        return positionMapper.toDetailResponse(position, accountNames);
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
        Position position = positionCommandUseCase.addPosition(
                new InstrumentSymbol(request.instrumentSymbol()),
                new AccountId(request.accountId()),
                new Quantity(request.quantity()),
                CostBasis.of(Money.pln(request.costBasis())),
                new Price(Money.pln(request.currentPrice())));
        Map<AccountId, String> accountNames = getAccountNames(position);
        return positionMapper.toDetailResponse(position, accountNames);
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
        Position position = positionCommandUseCase.updatePosition(
                new InstrumentSymbol(symbol),
                new AccountId(request.accountId()),
                new Quantity(request.quantity()),
                CostBasis.of(Money.pln(request.costBasis())));
        Map<AccountId, String> accountNames = getAccountNames(position);
        return positionMapper.toDetailResponse(position, accountNames);
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
