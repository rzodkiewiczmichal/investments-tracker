package com.investments.tracker.testutils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.investments.tracker.domain.model.value.InstrumentSymbol;
import com.investments.tracker.domain.model.value.Price;
import com.investments.tracker.domain.repository.PriceCache;

/**
 * In-memory stub implementation of PriceCache for integration and BDD tests.
 *
 * <p>Marked as {@code @Primary} to override any Redis-backed PriceCache adapter in the Spring test
 * context. Returns empty prices by default (no prices cached). Tests can pre-populate prices via
 * {@link #putPrice}.
 */
@Component
@Primary
public class StubPriceCache implements PriceCache {

    private final Map<InstrumentSymbol, Price> prices = new HashMap<>();

    @Override
    public Optional<Price> getPrice(InstrumentSymbol symbol) {
        return Optional.ofNullable(prices.get(symbol));
    }

    @Override
    public Map<InstrumentSymbol, Price> getPrices(Iterable<InstrumentSymbol> symbols) {
        Map<InstrumentSymbol, Price> result = new HashMap<>();
        for (InstrumentSymbol symbol : symbols) {
            Price price = prices.get(symbol);
            if (price != null) {
                result.put(symbol, price);
            }
        }
        return result;
    }

    @Override
    public void putPrice(InstrumentSymbol symbol, Price price) {
        prices.put(symbol, price);
    }

    public void clear() {
        prices.clear();
    }
}
