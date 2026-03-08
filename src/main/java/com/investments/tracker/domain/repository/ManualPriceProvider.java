package com.investments.tracker.domain.repository;

import com.investments.tracker.domain.model.value.InstrumentSymbol;
import com.investments.tracker.domain.model.value.Price;

/**
 * Port for storing manually-provided instrument prices.
 *
 * <p>Used during import when instruments lack automatic price providers. The implementation stores
 * prices in the same cache as automatic providers.
 */
public interface ManualPriceProvider {

    /**
     * Stores a price for the given instrument.
     *
     * @param symbol the instrument symbol
     * @param price the price to store
     */
    void putPrice(InstrumentSymbol symbol, Price price);
}
