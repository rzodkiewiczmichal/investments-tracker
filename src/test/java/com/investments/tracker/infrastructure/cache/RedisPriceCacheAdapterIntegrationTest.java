package com.investments.tracker.infrastructure.cache;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.investments.tracker.domain.model.value.Currency;
import com.investments.tracker.domain.model.value.InstrumentSymbol;
import com.investments.tracker.domain.model.value.Money;
import com.investments.tracker.domain.model.value.Price;

@Testcontainers
@DisplayName("RedisPriceCacheAdapter integration test")
class RedisPriceCacheAdapterIntegrationTest {

    @Container
    static GenericContainer<?> redis =
            new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

    private RedisPriceCacheAdapter adapter;
    private RedisTemplate<String, String> redisTemplate;

    @BeforeEach
    void setUp() {
        LettuceConnectionFactory connectionFactory =
                new LettuceConnectionFactory(redis.getHost(), redis.getMappedPort(6379));
        connectionFactory.afterPropertiesSet();

        redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new StringRedisSerializer());
        redisTemplate.afterPropertiesSet();

        // Clear all keys before each test
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();

        adapter = new RedisPriceCacheAdapter(redisTemplate);
    }

    @Test
    @DisplayName("should store and retrieve a price")
    void shouldStoreAndRetrievePrice() {
        InstrumentSymbol symbol = InstrumentSymbol.of("AAPL");
        Price price = new Price(new Money(new BigDecimal("175.50"), Currency.PLN));

        adapter.putPrice(symbol, price);

        Optional<Price> result = adapter.getPrice(symbol);
        assertThat(result).isPresent();
        assertThat(result.get().money().amount()).isEqualByComparingTo(new BigDecimal("175.5000"));
        assertThat(result.get().currency()).isEqualTo(Currency.PLN);
    }

    @Test
    @DisplayName("should return empty for non-existent price")
    void shouldReturnEmptyForNonExistentPrice() {
        Optional<Price> result = adapter.getPrice(InstrumentSymbol.of("UNKNOWN"));
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("should overwrite existing price")
    void shouldOverwriteExistingPrice() {
        InstrumentSymbol symbol = InstrumentSymbol.of("AAPL");
        adapter.putPrice(symbol, new Price(new Money(new BigDecimal("150.00"), Currency.PLN)));
        adapter.putPrice(symbol, new Price(new Money(new BigDecimal("175.00"), Currency.USD)));

        Optional<Price> result = adapter.getPrice(symbol);
        assertThat(result).isPresent();
        assertThat(result.get().money().amount()).isEqualByComparingTo(new BigDecimal("175.0000"));
        assertThat(result.get().currency()).isEqualTo(Currency.USD);
    }

    @Test
    @DisplayName("should retrieve multiple prices")
    void shouldRetrieveMultiplePrices() {
        InstrumentSymbol aapl = InstrumentSymbol.of("AAPL");
        InstrumentSymbol msft = InstrumentSymbol.of("MSFT");
        InstrumentSymbol unknown = InstrumentSymbol.of("UNKNOWN");

        adapter.putPrice(aapl, new Price(new Money(new BigDecimal("175.00"), Currency.PLN)));
        adapter.putPrice(msft, new Price(new Money(new BigDecimal("350.00"), Currency.PLN)));

        Map<InstrumentSymbol, Price> result = adapter.getPrices(List.of(aapl, msft, unknown));

        assertThat(result).hasSize(2);
        assertThat(result).containsKey(aapl);
        assertThat(result).containsKey(msft);
        assertThat(result).doesNotContainKey(unknown);
    }

    @Test
    @DisplayName("should set TTL on cached price")
    void shouldSetTtlOnCachedPrice() {
        InstrumentSymbol symbol = InstrumentSymbol.of("AAPL");
        adapter.putPrice(symbol, new Price(new Money(new BigDecimal("175.00"), Currency.PLN)));

        Long ttl = redisTemplate.getExpire("price:current:AAPL");
        assertThat(ttl).isNotNull().isPositive();
        // TTL should be close to 24 hours (86400 seconds), allow some margin
        assertThat(ttl).isGreaterThan(86000L);
    }

    @Test
    @DisplayName("should store price with correct Redis key format")
    void shouldStoreWithCorrectKeyFormat() {
        InstrumentSymbol symbol = InstrumentSymbol.of("AAPL");
        adapter.putPrice(symbol, new Price(new Money(new BigDecimal("175.00"), Currency.PLN)));

        Boolean exists = redisTemplate.hasKey("price:current:AAPL");
        assertThat(exists).isTrue();

        Object amount = redisTemplate.opsForHash().get("price:current:AAPL", "amount");
        Object currency = redisTemplate.opsForHash().get("price:current:AAPL", "currency");
        assertThat(amount).isEqualTo("175.0000");
        assertThat(currency).isEqualTo("PLN");
    }
}
