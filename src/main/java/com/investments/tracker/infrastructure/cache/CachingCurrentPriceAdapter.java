package com.investments.tracker.infrastructure.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.investments.tracker.domain.model.value.InstrumentSymbol;
import com.investments.tracker.domain.model.value.Price;
import com.investments.tracker.domain.repository.CurrentPriceProvider;
import com.investments.tracker.domain.repository.ManualPriceProvider;

/**
 * Cache-aside implementation of {@link CurrentPriceProvider} and {@link ManualPriceProvider}.
 *
 * <p>Checks Redis first via {@link RedisCurrentPriceAdapter}; on cache miss, fetches from the
 * appropriate price provider via {@link PriceProviderRouter} and stores the result in Redis with a
 * 24-hour TTL.
 *
 * @see <a href="../../docs/adr/ADR-031-instrument-price-providers.md">ADR-031</a>
 * @see <a href="../../docs/adr/ADR-032-external-data-caching-strategy.md">ADR-032</a>
 */
@Component
public class CachingCurrentPriceAdapter implements CurrentPriceProvider, ManualPriceProvider {

    private static final Logger log = LoggerFactory.getLogger(CachingCurrentPriceAdapter.class);

    private final RedisCurrentPriceAdapter redisAdapter;
    private final PriceProviderRouter priceProviderRouter;

    public CachingCurrentPriceAdapter(
            RedisCurrentPriceAdapter redisAdapter, PriceProviderRouter priceProviderRouter) {
        this.redisAdapter = redisAdapter;
        this.priceProviderRouter = priceProviderRouter;
    }

    @Override
    public Optional<Price> getPrice(InstrumentSymbol symbol) {
        Optional<Price> cached = redisAdapter.getPrice(symbol);
        if (cached.isPresent()) {
            return cached;
        }

        Optional<Price> fetched = priceProviderRouter.fetchPrice(symbol);
        fetched.ifPresent(price -> redisAdapter.putPrice(symbol, price));
        return fetched;
    }

    @Override
    public Map<InstrumentSymbol, Price> getPrices(Collection<InstrumentSymbol> symbols) {
        if (symbols.isEmpty()) {
            return Map.of();
        }

        // Read all from Redis first
        Map<InstrumentSymbol, Price> cached = redisAdapter.getPrices(symbols);
        if (cached.size() == symbols.size()) {
            return cached;
        }

        // Determine which symbols are missing
        List<InstrumentSymbol> missing = new ArrayList<>();
        for (InstrumentSymbol symbol : symbols) {
            if (!cached.containsKey(symbol)) {
                missing.add(symbol);
            }
        }

        // Fetch missing from appropriate providers
        Map<InstrumentSymbol, Price> fetched = priceProviderRouter.fetchPrices(missing);

        // Cache fetched prices in Redis
        fetched.forEach(redisAdapter::putPrice);

        // Merge cached + fetched
        Map<InstrumentSymbol, Price> result = new HashMap<>(cached);
        result.putAll(fetched);
        return Map.copyOf(result);
    }

    /**
     * Stores a price directly in the cache. Used for user-provided prices during import when no
     * automatic price provider covers the instrument.
     *
     * @param symbol the instrument symbol
     * @param price the price to store
     */
    @Override
    public void putPrice(InstrumentSymbol symbol, Price price) {
        redisAdapter.putPrice(symbol, price);
    }
}
