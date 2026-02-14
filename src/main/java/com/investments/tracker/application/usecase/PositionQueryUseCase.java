package com.investments.tracker.application.usecase;

import java.util.Collection;

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
}
