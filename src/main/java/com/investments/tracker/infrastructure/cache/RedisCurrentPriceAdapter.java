package com.investments.tracker.infrastructure.cache;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.investments.tracker.domain.model.value.Currency;
import com.investments.tracker.domain.model.value.InstrumentSymbol;
import com.investments.tracker.domain.model.value.Money;
import com.investments.tracker.domain.model.value.Price;
import com.investments.tracker.domain.repository.CurrentPriceProvider;

/**
 * Redis-backed implementation of the {@link CurrentPriceProvider} domain port.
 *
 * <p>Stores instrument prices as Redis Hash keys with 24-hour TTL. Key format: {@code
 * price:current:{symbol}} Hash fields: {@code amount}, {@code currency}
 *
 * @see <a href="../../docs/adr/ADR-032-external-data-caching-strategy.md">ADR-032: External Data
 *     Caching Strategy</a>
 */
@Component
public class RedisCurrentPriceAdapter implements CurrentPriceProvider {

    private static final Logger log = LoggerFactory.getLogger(RedisCurrentPriceAdapter.class);
    private static final String KEY_PREFIX = "price:current:";
    private static final String FIELD_AMOUNT = "amount";
    private static final String FIELD_CURRENCY = "currency";
    private static final Duration TTL = Duration.ofHours(24);

    private final RedisTemplate<String, String> redisTemplate;

    public RedisCurrentPriceAdapter(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Optional<Price> getPrice(InstrumentSymbol symbol) {
        String key = toKey(symbol);
        try {
            Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
            if (entries.isEmpty()) {
                return Optional.empty();
            }
            String amount = (String) entries.get(FIELD_AMOUNT);
            String currency = (String) entries.get(FIELD_CURRENCY);
            if (amount == null || currency == null) {
                return Optional.empty();
            }
            return Optional.of(toPrice(amount, currency));
        } catch (RedisConnectionFailureException | RedisSystemException e) {
            log.warn("Failed to read price from Redis for {}: {}", symbol.value(), e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<InstrumentSymbol, Price> getPrices(Collection<InstrumentSymbol> symbols) {
        if (symbols.isEmpty()) {
            return Map.of();
        }
        List<InstrumentSymbol> symbolList = new ArrayList<>(symbols);
        try {
            List<Object> pipelineResults =
                    redisTemplate.executePipelined(
                            (RedisCallback<Object>)
                                    connection -> {
                                        for (InstrumentSymbol symbol : symbolList) {
                                            connection
                                                    .hashCommands()
                                                    .hGetAll(
                                                            toKey(symbol)
                                                                    .getBytes(
                                                                            StandardCharsets
                                                                                    .UTF_8));
                                        }
                                        return null;
                                    });

            Map<InstrumentSymbol, Price> result = new HashMap<>();
            for (int i = 0; i < symbolList.size(); i++) {
                Map<Object, Object> entries = (Map<Object, Object>) pipelineResults.get(i);
                if (entries != null && !entries.isEmpty()) {
                    String amount = (String) entries.get(FIELD_AMOUNT);
                    String currency = (String) entries.get(FIELD_CURRENCY);
                    if (amount != null && currency != null) {
                        result.put(symbolList.get(i), toPrice(amount, currency));
                    }
                }
            }
            return Map.copyOf(result);
        } catch (RedisConnectionFailureException | RedisSystemException e) {
            log.warn("Failed to read prices from Redis: {}", e.getMessage());
            return Map.of();
        }
    }

    /**
     * Stores a price in Redis for the given instrument. This is not part of the {@link
     * CurrentPriceProvider} domain port — it is used by infrastructure-level data providers to
     * populate the cache.
     *
     * @param symbol the instrument symbol
     * @param price the price to store
     */
    public void putPrice(InstrumentSymbol symbol, Price price) {
        String key = toKey(symbol);
        try {
            byte[] rawKey = key.getBytes(StandardCharsets.UTF_8);
            byte[] rawAmountField = FIELD_AMOUNT.getBytes(StandardCharsets.UTF_8);
            byte[] rawCurrencyField = FIELD_CURRENCY.getBytes(StandardCharsets.UTF_8);
            byte[] rawAmount =
                    price.money().amount().toPlainString().getBytes(StandardCharsets.UTF_8);
            byte[] rawCurrency = price.currency().getCode().getBytes(StandardCharsets.UTF_8);

            redisTemplate.execute(
                    (RedisCallback<Object>)
                            connection -> {
                                Map<byte[], byte[]> rawFields =
                                        Map.of(
                                                rawAmountField, rawAmount,
                                                rawCurrencyField, rawCurrency);
                                connection.hashCommands().hMSet(rawKey, rawFields);
                                connection.keyCommands().expire(rawKey, TTL.getSeconds());
                                return null;
                            });
        } catch (RedisConnectionFailureException | RedisSystemException e) {
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
