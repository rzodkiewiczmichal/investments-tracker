package com.investments.tracker.domain.repository;

import com.investments.tracker.domain.model.Position;
import com.investments.tracker.domain.model.value.InstrumentSymbol;

import java.util.Collection;
import java.util.Optional;

/**
 * Repository port for Position aggregate persistence.
 * <p>
 * This is a driven port that will be implemented by infrastructure adapters.
 * </p>
 */
public interface PositionRepository {

    /**
     * Finds a position by its instrument symbol.
     *
     * @param symbol the instrument symbol
     * @return optional position
     */
    Optional<Position> findBySymbol(InstrumentSymbol symbol);

    /**
     * Finds all positions.
     *
     * @return collection of all positions
     */
    Collection<Position> findAll();

    /**
     * Saves a position (insert or update).
     *
     * @param position the position to save
     * @return the saved position
     */
    Position save(Position position);

    /**
     * Deletes a position by its symbol.
     *
     * @param symbol the instrument symbol
     */
    void deleteBySymbol(InstrumentSymbol symbol);

    /**
     * Checks if a position exists for the given symbol.
     *
     * @param symbol the instrument symbol
     * @return true if exists
     */
    boolean existsBySymbol(InstrumentSymbol symbol);

    /**
     * Counts all positions.
     *
     * @return the count
     */
    long count();
}
