package com.investments.tracker.domain.repository;

import java.util.Collection;
import java.util.Optional;

import com.investments.tracker.domain.model.Instrument;
import com.investments.tracker.domain.model.value.InstrumentSymbol;

/**
 * Repository port for Instrument reference data persistence.
 *
 * <p>This is a driven port that will be implemented by infrastructure adapters.
 */
public interface InstrumentRepository {

    /**
     * Finds an instrument by its symbol.
     *
     * @param symbol the instrument symbol
     * @return optional instrument
     */
    Optional<Instrument> findBySymbol(InstrumentSymbol symbol);

    /**
     * Finds all instruments.
     *
     * @return collection of all instruments
     */
    Collection<Instrument> findAll();

    /**
     * Saves an instrument (insert or update).
     *
     * @param instrument the instrument to save
     * @return the saved instrument
     */
    Instrument save(Instrument instrument);

    /**
     * Deletes an instrument by its symbol.
     *
     * @param symbol the instrument symbol
     */
    void deleteBySymbol(InstrumentSymbol symbol);

    /**
     * Checks if an instrument exists with the given symbol.
     *
     * @param symbol the instrument symbol
     * @return true if exists
     */
    boolean existsBySymbol(InstrumentSymbol symbol);

    /**
     * Searches instruments whose symbol or name contains the query string (case-insensitive).
     *
     * @param query the search query
     * @return matching instruments (limited to 20 results)
     */
    Collection<Instrument> searchBySymbolOrName(String query);

    /**
     * Counts all instruments.
     *
     * @return the count
     */
    long count();
}
