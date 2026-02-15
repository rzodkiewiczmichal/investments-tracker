package com.investments.tracker.infrastructure.cache;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.investments.tracker.domain.model.value.Currency;
import com.investments.tracker.domain.model.value.ExchangeRate;
import com.investments.tracker.domain.repository.ExchangeRateProvider;
import com.investments.tracker.infrastructure.external.nbp.NbpExchangeRateClient;

/**
 * Redis-cached implementation of {@link ExchangeRateProvider}.
 *
 * <p>Checks Redis first; on cache miss, fetches from NBP API via {@link NbpExchangeRateClient} and
 * stores the result in Redis with a 24-hour TTL.
 *
 * <p>Redis key format: {@code rate:pln:{CURRENCY}} (e.g., {@code rate:pln:EUR}). Hash fields:
 * {@code from}, {@code to}, {@code rate}.
 *
 * @see <a href="../../docs/adr/ADR-030-currency-exchange-rate-provider.md">ADR-030</a>
 */
@Component
public class CachingExchangeRateAdapter implements ExchangeRateProvider {

    private static final Logger log = LoggerFactory.getLogger(CachingExchangeRateAdapter.class);
    private static final String KEY_PREFIX = "rate:pln:";
    private static final String FIELD_FROM = "from";
    private static final String FIELD_TO = "to";
    private static final String FIELD_RATE = "rate";
    private static final Duration TTL = Duration.ofHours(24);

    private final RedisTemplate<String, String> redisTemplate;
    private final NbpExchangeRateClient nbpClient;

    public CachingExchangeRateAdapter(
            RedisTemplate<String, String> redisTemplate, NbpExchangeRateClient nbpClient) {
        this.redisTemplate = redisTemplate;
        this.nbpClient = nbpClient;
    }

    @Override
    public ExchangeRate getExchangeRateToPln(Currency source) {
        if (source == Currency.PLN) {
            return ExchangeRate.identity(Currency.PLN);
        }

        ExchangeRate cached = readFromRedis(source);
        if (cached != null) {
            return cached;
        }

        ExchangeRate fetched = nbpClient.fetchRate(source);
        writeToRedis(fetched);
        return fetched;
    }

    @Override
    public Map<Currency, ExchangeRate> getExchangeRatesToPln(Collection<Currency> currencies) {
        Map<Currency, ExchangeRate> result = new HashMap<>();
        for (Currency currency : currencies) {
            result.put(currency, getExchangeRateToPln(currency));
        }
        return Map.copyOf(result);
    }

    private ExchangeRate readFromRedis(Currency source) {
        String key = toKey(source);
        try {
            Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
            if (entries.isEmpty()) {
                return null;
            }
            String from = (String) entries.get(FIELD_FROM);
            String to = (String) entries.get(FIELD_TO);
            String rate = (String) entries.get(FIELD_RATE);
            if (from == null || to == null || rate == null) {
                return null;
            }
            return new ExchangeRate(
                    Currency.valueOf(from), Currency.valueOf(to), new BigDecimal(rate));
        } catch (RedisConnectionFailureException | RedisSystemException e) {
            log.warn(
                    "Failed to read exchange rate from Redis for {}: {}",
                    source.getCode(),
                    e.getMessage());
            return null;
        }
    }

    private void writeToRedis(ExchangeRate exchangeRate) {
        String key = toKey(exchangeRate.from());
        try {
            Map<String, String> fields =
                    Map.of(
                            FIELD_FROM, exchangeRate.from().getCode(),
                            FIELD_TO, exchangeRate.to().getCode(),
                            FIELD_RATE, exchangeRate.rate().toPlainString());
            redisTemplate.opsForHash().putAll(key, fields);
            redisTemplate.expire(key, TTL);
        } catch (RedisConnectionFailureException | RedisSystemException e) {
            log.warn(
                    "Failed to write exchange rate to Redis for {}: {}",
                    exchangeRate.from().getCode(),
                    e.getMessage());
        }
    }

    private String toKey(Currency currency) {
        return KEY_PREFIX + currency.getCode();
    }
}
