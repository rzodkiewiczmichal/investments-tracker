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
import org.springframework.web.client.RestClientException;

import com.investments.tracker.domain.model.value.InstrumentSymbol;
import com.investments.tracker.domain.model.value.Price;
import com.investments.tracker.domain.repository.CurrentPriceProvider;
import com.investments.tracker.infrastructure.external.stooq.StooqPriceClient;

/**
 * Cache-aside implementation of {@link CurrentPriceProvider}.
 *
 * <p>Checks Redis first via {@link RedisCurrentPriceAdapter}; on cache miss, fetches from Stooq.pl
 * via {@link StooqPriceClient} and stores the result in Redis with a 24-hour TTL.
 *
 * @see <a href="../../docs/adr/ADR-031-instrument-price-providers.md">ADR-031</a>
 * @see <a href="../../docs/adr/ADR-032-external-data-caching-strategy.md">ADR-032</a>
 */
@Component
public class CachingCurrentPriceAdapter implements CurrentPriceProvider {

    private static final Logger log = LoggerFactory.getLogger(CachingCurrentPriceAdapter.class);

    private final RedisCurrentPriceAdapter redisAdapter;
    private final StooqPriceClient stooqClient;

    public CachingCurrentPriceAdapter(
            RedisCurrentPriceAdapter redisAdapter, StooqPriceClient stooqClient) {
        this.redisAdapter = redisAdapter;
        this.stooqClient = stooqClient;
    }

    @Override
    public Optional<Price> getPrice(InstrumentSymbol symbol) {
        Optional<Price> cached = redisAdapter.getPrice(symbol);
        if (cached.isPresent()) {
            return cached;
        }

        try {
            Optional<Price> fetched = stooqClient.fetchPrice(symbol);
            fetched.ifPresent(price -> redisAdapter.putPrice(symbol, price));
            return fetched;
        } catch (RestClientException e) {
            log.warn("Failed to fetch price from Stooq for {}: {}", symbol.value(), e.getMessage());
            return Optional.empty();
        }
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

        // Fetch missing from Stooq
        Map<InstrumentSymbol, Price> fetched;
        try {
            fetched = stooqClient.fetchPrices(missing);
        } catch (RestClientException e) {
            log.warn("Failed to fetch prices from Stooq: {}", e.getMessage());
            return cached;
        }

        // Cache fetched prices in Redis
        fetched.forEach(redisAdapter::putPrice);

        // Merge cached + fetched
        Map<InstrumentSymbol, Price> result = new HashMap<>(cached);
        result.putAll(fetched);
        return Map.copyOf(result);
    }
}
