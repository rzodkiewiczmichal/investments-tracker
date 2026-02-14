package com.investments.tracker.testutils;

import com.investments.tracker.domain.model.value.InstrumentSymbol;
import com.investments.tracker.domain.model.value.Price;
import com.investments.tracker.domain.repository.CurrentPriceProvider;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * In-memory stub implementation of CurrentPriceProvider for integration and BDD tests.
 * <p>
 * Marked as {@code @Primary} to override any Redis-backed adapter
 * in the Spring test context. Returns empty prices by default (no prices available).
 * Tests can pre-populate prices via {@link #putPrice}.
 * </p>
 */
@Component
@Primary
public class StubCurrentPriceProvider implements CurrentPriceProvider {

    private final Map<InstrumentSymbol, Price> prices = new HashMap<>();

    @Override
    public Optional<Price> getPrice(InstrumentSymbol symbol) {
        return Optional.ofNullable(prices.get(symbol));
    }

    @Override
    public Map<InstrumentSymbol, Price> getPrices(Collection<InstrumentSymbol> symbols) {
        Map<InstrumentSymbol, Price> result = new HashMap<>();
        for (InstrumentSymbol symbol : symbols) {
            Price price = prices.get(symbol);
            if (price != null) {
                result.put(symbol, price);
            }
        }
        return Map.copyOf(result);
    }

    /**
     * Stores a price for test setup. Not part of the domain port.
     */
    public void putPrice(InstrumentSymbol symbol, Price price) {
        prices.put(symbol, price);
    }

    /**
     * Clears all stored prices. Called between test scenarios.
     */
    public void clear() {
        prices.clear();
    }
}
