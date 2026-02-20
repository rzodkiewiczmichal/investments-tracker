package com.investments.tracker.infrastructure.web.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.investments.tracker.application.dto.mapper.PositionMapper;
import com.investments.tracker.application.dto.request.AddPositionRequest;
import com.investments.tracker.application.dto.request.UpdatePositionRequest;
import com.investments.tracker.application.dto.response.PositionDetailResponse;
import com.investments.tracker.application.dto.response.PositionListResponse;
import com.investments.tracker.application.usecase.InstrumentQueryUseCase;
import com.investments.tracker.application.usecase.PositionCommandUseCase;
import com.investments.tracker.application.usecase.PositionDetailData;
import com.investments.tracker.application.usecase.PositionQueryUseCase;
import com.investments.tracker.application.usecase.PositionWithMarketData;
import com.investments.tracker.domain.model.Instrument;
import com.investments.tracker.domain.model.value.AccountId;
import com.investments.tracker.domain.model.value.CostBasis;
import com.investments.tracker.domain.model.value.InstrumentSymbol;
import com.investments.tracker.domain.model.value.Money;
import com.investments.tracker.domain.model.value.Quantity;

/** REST controller for position operations. */
@RestController
@RequestMapping("/api/v1/positions")
public class PositionController {

    private final PositionQueryUseCase positionQueryUseCase;
    private final PositionCommandUseCase positionCommandUseCase;
    private final InstrumentQueryUseCase instrumentQueryUseCase;
    private final PositionMapper positionMapper;

    public PositionController(
            PositionQueryUseCase positionQueryUseCase,
            PositionCommandUseCase positionCommandUseCase,
            InstrumentQueryUseCase instrumentQueryUseCase,
            PositionMapper positionMapper) {
        this.positionQueryUseCase = positionQueryUseCase;
        this.positionCommandUseCase = positionCommandUseCase;
        this.instrumentQueryUseCase = instrumentQueryUseCase;
        this.positionMapper = positionMapper;
    }

    /**
     * Lists all positions sorted by current value descending (FR-014).
     *
     * @return position list response
     */
    @GetMapping
    public PositionListResponse listPositions() {
        List<PositionWithMarketData> data = positionQueryUseCase.listPositionsWithMarketData();
        return positionMapper.toListResponse(data);
    }

    /**
     * Gets a specific position by symbol.
     *
     * @param symbol the instrument symbol
     * @return position detail response
     */
    @GetMapping("/{symbol}")
    public PositionDetailResponse getPosition(@PathVariable String symbol) {
        PositionDetailData data =
                positionQueryUseCase.getPositionDetail(new InstrumentSymbol(symbol));
        return positionMapper.toDetailResponse(data);
    }

    /**
     * Adds a new position or adds shares to an existing position. The instrument must exist in the
     * catalog (ADR-033).
     *
     * @param request the add position request
     * @return position detail response
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PositionDetailResponse addPosition(@Valid @RequestBody AddPositionRequest request) {
        InstrumentSymbol instrumentSymbol = new InstrumentSymbol(request.instrumentSymbol());
        Instrument instrument = instrumentQueryUseCase.getInstrument(instrumentSymbol);
        positionCommandUseCase.addPosition(
                instrumentSymbol,
                new AccountId(request.accountId()),
                new Quantity(request.quantity()),
                CostBasis.of(new Money(request.averageCost(), instrument.currency())));
        PositionDetailData data = positionQueryUseCase.getPositionDetail(instrumentSymbol);
        return positionMapper.toDetailResponse(data);
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
            @PathVariable String symbol, @Valid @RequestBody UpdatePositionRequest request) {
        InstrumentSymbol instrumentSymbol = new InstrumentSymbol(symbol);
        Instrument instrument = instrumentQueryUseCase.getInstrument(instrumentSymbol);
        positionCommandUseCase.updatePosition(
                instrumentSymbol,
                new AccountId(request.accountId()),
                new Quantity(request.quantity()),
                CostBasis.of(new Money(request.averageCost(), instrument.currency())));
        PositionDetailData data = positionQueryUseCase.getPositionDetail(instrumentSymbol);
        return positionMapper.toDetailResponse(data);
    }

    /**
     * Deletes a position. The catalog instrument is preserved (master data).
     *
     * @param symbol the instrument symbol
     */
    @DeleteMapping("/{symbol}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePosition(@PathVariable String symbol) {
        positionCommandUseCase.deletePosition(new InstrumentSymbol(symbol));
    }
}
