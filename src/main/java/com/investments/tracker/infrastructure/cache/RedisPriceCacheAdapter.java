package com.investments.tracker.infrastructure.cache;

import com.investments.tracker.domain.model.value.Currency;
import com.investments.tracker.domain.model.value.InstrumentSymbol;
import com.investments.tracker.domain.model.value.Money;
import com.investments.tracker.domain.model.value.Price;
import com.investments.tracker.domain.repository.PriceCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Redis-backed implementation of the PriceCache domain port.
 * <p>
 * Stores instrument prices as Redis Hash keys with 24-hour TTL.
 * Key format: {@code price:current:{symbol}}
 * Hash fields: {@code amount}, {@code currency}
 * </p>
 *
 * @see <a href="../../docs/adr/ADR-032-external-data-caching-strategy.md">ADR-032: External Data Caching Strategy</a>
 */
@Component
public class RedisPriceCacheAdapter implements PriceCache {

    private static final Logger log = LoggerFactory.getLogger(RedisPriceCacheAdapter.class);
    private static final String KEY_PREFIX = "price:current:";
    private static final String FIELD_AMOUNT = "amount";
    private static final String FIELD_CURRENCY = "currency";
    private static final Duration TTL = Duration.ofHours(24);

    private final RedisTemplate<String, String> redisTemplate;

    public RedisPriceCacheAdapter(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Optional<Price> getPrice(InstrumentSymbol symbol) {
        String key = toKey(symbol);
        try {
            String amount = (String) redisTemplate.opsForHash().get(key, FIELD_AMOUNT);
            String currency = (String) redisTemplate.opsForHash().get(key, FIELD_CURRENCY);
            if (amount == null || currency == null) {
                return Optional.empty();
            }
            return Optional.of(toPrice(amount, currency));
        } catch (Exception e) {
            log.warn("Failed to read price from Redis for {}: {}", symbol.value(), e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public Map<InstrumentSymbol, Price> getPrices(Iterable<InstrumentSymbol> symbols) {
        Map<InstrumentSymbol, Price> result = new HashMap<>();
        for (InstrumentSymbol symbol : symbols) {
            getPrice(symbol).ifPresent(price -> result.put(symbol, price));
        }
        return result;
    }

    @Override
    public void putPrice(InstrumentSymbol symbol, Price price) {
        String key = toKey(symbol);
        try {
            Map<String, String> fields = Map.of(
                    FIELD_AMOUNT, price.money().amount().toPlainString(),
                    FIELD_CURRENCY, price.currency().getCode()
            );
            redisTemplate.opsForHash().putAll(key, fields);
            redisTemplate.expire(key, TTL);
        } catch (Exception e) {
            log.warn("Failed to write price to Redis for {}: {}", symbol.value(), e.getMessage());
        }
    }

    private String toKey(InstrumentSymbol symbol) {
        return KEY_PREFIX + symbol.value();
    }

    private Price toPrice(String amount, String currency) {
        return new Price(new Money(new BigDecimal(amount), Currency.valueOf(currency)));
    }
}
