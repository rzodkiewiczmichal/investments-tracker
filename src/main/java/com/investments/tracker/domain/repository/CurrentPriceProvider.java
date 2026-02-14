package com.investments.tracker.domain.repository;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import com.investments.tracker.domain.model.value.InstrumentSymbol;
import com.investments.tracker.domain.model.value.Price;

/**
 * Port for providing current market prices of instruments.
 *
 * <p>This is a driven port that will be implemented by infrastructure adapters. The domain only
 * reads prices through this interface; writing prices is an infrastructure concern handled by the
 * adapters directly.
 *
 * @see <a href="../../docs/adr/ADR-032-external-data-caching-strategy.md">ADR-032: External Data
 *     Caching Strategy</a>
 */
public interface CurrentPriceProvider {

    /**
     * Gets the current price for an instrument.
     *
     * @param symbol the instrument symbol
     * @return the current price, or empty if not available
     */
    Optional<Price> getPrice(InstrumentSymbol symbol);

    /**
     * Gets current prices for multiple instruments in a single call. Only returns entries for
     * symbols that have a known price.
     *
     * @param symbols the instrument symbols to look up
     * @return map of symbol to current price (missing symbols are omitted)
     */
    Map<InstrumentSymbol, Price> getPrices(Collection<InstrumentSymbol> symbols);
}
