package com.investments.tracker.infrastructure.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import org.springframework.web.client.RestClientException;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.investments.tracker.domain.model.value.InstrumentSymbol;
import com.investments.tracker.domain.model.value.Money;
import com.investments.tracker.domain.model.value.Price;
import com.investments.tracker.infrastructure.external.stooq.StooqPriceClient;

@Testcontainers
@DisplayName("CachingCurrentPriceAdapter integration test")
class CachingCurrentPriceAdapterIntegrationTest {

    @Container
    static GenericContainer<?> redis =
            new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

    private CachingCurrentPriceAdapter adapter;
    private RedisCurrentPriceAdapter redisAdapter;
    private RedisTemplate<String, String> redisTemplate;
    private StooqPriceClient stooqClient;

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

        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();

        redisAdapter = new RedisCurrentPriceAdapter(redisTemplate);
        stooqClient = mock(StooqPriceClient.class);
        adapter = new CachingCurrentPriceAdapter(redisAdapter, stooqClient);
    }

    @Test
    @DisplayName("should fetch from Stooq on cache miss and cache result")
    void shouldFetchFromStooqOnCacheMissAndCacheResult() {
        InstrumentSymbol symbol = InstrumentSymbol.of("PKO");
        Price stooqPrice = new Price(Money.pln(new BigDecimal("92")));
        when(stooqClient.fetchPrice(symbol)).thenReturn(Optional.of(stooqPrice));

        Optional<Price> result = adapter.getPrice(symbol);

        assertThat(result).isPresent();
        assertThat(result.get().money().amount()).isEqualByComparingTo(new BigDecimal("92"));
        verify(stooqClient).fetchPrice(symbol);

        // Verify cached in Redis
        Object storedAmount = redisTemplate.opsForHash().get("price:current:PKO", "amount");
        assertThat(storedAmount).isEqualTo("92.0000");
    }

    @Test
    @DisplayName("should return cached price without Stooq call")
    void shouldReturnCachedPriceWithoutStooqCall() {
        InstrumentSymbol symbol = InstrumentSymbol.of("PZU");
        redisAdapter.putPrice(symbol, new Price(Money.pln(new BigDecimal("45.80"))));

        Optional<Price> result = adapter.getPrice(symbol);

        assertThat(result).isPresent();
        assertThat(result.get().money().amount()).isEqualByComparingTo(new BigDecimal("45.80"));
        verify(stooqClient, never()).fetchPrice(symbol);
    }

    @Test
    @DisplayName("should set 24h TTL on cached price")
    void shouldSetTtlOnCachedPrice() {
        InstrumentSymbol symbol = InstrumentSymbol.of("KGHM");
        Price stooqPrice = new Price(Money.pln(new BigDecimal("141.20")));
        when(stooqClient.fetchPrice(symbol)).thenReturn(Optional.of(stooqPrice));

        adapter.getPrice(symbol);

        Long ttl = redisTemplate.getExpire("price:current:KGHM");
        assertThat(ttl).isNotNull().isPositive();
        assertThat(ttl).isGreaterThan(86000L);
    }

    @Test
    @DisplayName("should return empty when Stooq fails on cache miss")
    void shouldReturnEmptyWhenStooqFailsOnCacheMiss() {
        InstrumentSymbol symbol = InstrumentSymbol.of("PKO");
        when(stooqClient.fetchPrice(symbol))
                .thenThrow(new RestClientException("Connection refused"));

        Optional<Price> result = adapter.getPrice(symbol);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("should return empty when Stooq has no data for symbol")
    void shouldReturnEmptyWhenStooqHasNoData() {
        InstrumentSymbol symbol = InstrumentSymbol.of("UNKNOWN");
        when(stooqClient.fetchPrice(symbol)).thenReturn(Optional.empty());

        Optional<Price> result = adapter.getPrice(symbol);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("should handle batch with partial cache hits")
    void shouldHandleBatchWithPartialCacheHits() {
        InstrumentSymbol pko = InstrumentSymbol.of("PKO");
        InstrumentSymbol pzu = InstrumentSymbol.of("PZU");
        InstrumentSymbol kghm = InstrumentSymbol.of("KGHM");

        // PKO cached, PZU and KGHM not
        redisAdapter.putPrice(pko, new Price(Money.pln(new BigDecimal("92"))));

        Price pzuPrice = new Price(Money.pln(new BigDecimal("45.80")));
        Price kghmPrice = new Price(Money.pln(new BigDecimal("141.20")));
        when(stooqClient.fetchPrices(List.of(pzu, kghm)))
                .thenReturn(Map.of(pzu, pzuPrice, kghm, kghmPrice));

        Map<InstrumentSymbol, Price> result = adapter.getPrices(List.of(pko, pzu, kghm));

        assertThat(result).hasSize(3);
        assertThat(result.get(pko).money().amount()).isEqualByComparingTo(new BigDecimal("92"));
        assertThat(result.get(pzu).money().amount()).isEqualByComparingTo(new BigDecimal("45.80"));
        assertThat(result.get(kghm).money().amount())
                .isEqualByComparingTo(new BigDecimal("141.20"));

        // Only PZU and KGHM should have been fetched from Stooq
        verify(stooqClient).fetchPrices(List.of(pzu, kghm));
    }

    @Test
    @DisplayName("should return only cached prices when Stooq fails for missing symbols")
    void shouldReturnOnlyCachedWhenStooqFailsForMissing() {
        InstrumentSymbol pko = InstrumentSymbol.of("PKO");
        InstrumentSymbol pzu = InstrumentSymbol.of("PZU");

        redisAdapter.putPrice(pko, new Price(Money.pln(new BigDecimal("92"))));
        when(stooqClient.fetchPrices(List.of(pzu)))
                .thenThrow(new RestClientException("Stooq down"));

        Map<InstrumentSymbol, Price> result = adapter.getPrices(List.of(pko, pzu));

        assertThat(result).hasSize(1);
        assertThat(result).containsKey(pko);
        assertThat(result).doesNotContainKey(pzu);
    }

    @Test
    @DisplayName("should skip Stooq call when all prices are cached")
    void shouldSkipStooqWhenAllCached() {
        InstrumentSymbol pko = InstrumentSymbol.of("PKO");
        InstrumentSymbol pzu = InstrumentSymbol.of("PZU");

        redisAdapter.putPrice(pko, new Price(Money.pln(new BigDecimal("92"))));
        redisAdapter.putPrice(pzu, new Price(Money.pln(new BigDecimal("45.80"))));

        Map<InstrumentSymbol, Price> result = adapter.getPrices(List.of(pko, pzu));

        assertThat(result).hasSize(2);
        verify(stooqClient, never()).fetchPrices(List.of());
    }

    @Test
    @DisplayName("should return immutable map from batch method")
    void shouldReturnImmutableMapFromBatchMethod() {
        InstrumentSymbol pko = InstrumentSymbol.of("PKO");
        redisAdapter.putPrice(pko, new Price(Money.pln(new BigDecimal("92"))));

        Map<InstrumentSymbol, Price> result = adapter.getPrices(List.of(pko));

        org.assertj.core.api.Assertions.assertThatThrownBy(
                        () -> result.put(InstrumentSymbol.of("X"), new Price(Money.pln("1"))))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
