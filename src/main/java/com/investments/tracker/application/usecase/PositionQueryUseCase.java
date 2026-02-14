package com.investments.tracker.application.usecase;

import java.util.Collection;
import java.util.List;

import com.investments.tracker.domain.model.Position;
import com.investments.tracker.domain.model.value.InstrumentSymbol;

/** Use case for querying positions. */
public interface PositionQueryUseCase {

    /**
     * Lists all positions.
     *
     * @return collection of positions
     */
    Collection<Position> listPositions();

    /**
     * Gets a specific position by symbol.
     *
     * @param symbol the instrument symbol
     * @return the position
     * @throws com.investments.tracker.application.exception.ResourceNotFoundException if position
     *     not found
     */
    Position getPosition(InstrumentSymbol symbol);

    /**
     * Lists all positions enriched with market data (instrument, price, exchange rates).
     *
     * @return list of positions with market data
     */
    List<PositionWithMarketData> listPositionsWithMarketData();

    /**
     * Gets a specific position enriched with market data and account names.
     *
     * @param symbol the instrument symbol
     * @return position detail data
     * @throws com.investments.tracker.application.exception.ResourceNotFoundException if position
     *     not found
     */
    PositionDetailData getPositionDetail(InstrumentSymbol symbol);
}
