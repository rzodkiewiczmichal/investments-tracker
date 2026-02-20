package com.investments.tracker.application.usecase;

import java.util.Collection;

import com.investments.tracker.domain.model.Instrument;
import com.investments.tracker.domain.model.value.InstrumentSymbol;

/** Use case for querying instruments. */
public interface InstrumentQueryUseCase {

    /**
     * Gets an instrument by its symbol.
     *
     * @param symbol the instrument symbol
     * @return the instrument
     * @throws com.investments.tracker.application.exception.ResourceNotFoundException if not found
     */
    Instrument getInstrument(InstrumentSymbol symbol);

    /**
     * Lists all instruments.
     *
     * @return collection of all instruments
     */
    Collection<Instrument> listInstruments();

    /**
     * Searches instruments by symbol or name (case-insensitive partial match).
     *
     * @param query the search query
     * @return matching instruments (limited to 20 results)
     */
    Collection<Instrument> searchInstruments(String query);
}
