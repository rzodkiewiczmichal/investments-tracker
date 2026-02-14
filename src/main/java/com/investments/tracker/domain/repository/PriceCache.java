package com.investments.tracker.domain.repository;

import com.investments.tracker.domain.model.value.InstrumentSymbol;
import com.investments.tracker.domain.model.value.Price;

import java.util.Map;
import java.util.Optional;

/**
 * Port for caching current instrument prices.
 * <p>
 * Abstracts the cache layer (e.g., Redis) from the domain.
 * Prices are cached with a TTL and fetched lazily from external
 * market data providers on cache miss.
 * </p>
 *
 * @see <a href="../../docs/adr/ADR-032-external-data-caching-strategy.md">ADR-032: External Data Caching Strategy</a>
 */
public interface PriceCache {

    /**
     * Gets the cached current price for an instrument.
     *
     * @param symbol the instrument symbol
     * @return the cached price, or empty if not cached
     */
    Optional<Price> getPrice(InstrumentSymbol symbol);

    /**
     * Gets cached current prices for multiple instruments in a single call.
     * Only returns entries for symbols that have a cached price.
     *
     * @param symbols the instrument symbols to look up
     * @return map of symbol to cached price (missing symbols are omitted)
     */
    Map<InstrumentSymbol, Price> getPrices(Iterable<InstrumentSymbol> symbols);

    /**
     * Stores a price in the cache for the given instrument.
     *
     * @param symbol the instrument symbol
     * @param price the price to cache
     */
    void putPrice(InstrumentSymbol symbol, Price price);
}
