package com.investments.tracker.infrastructure.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.investments.tracker.domain.exception.ExchangeRateNotFoundException;
import com.investments.tracker.domain.model.value.Currency;
import com.investments.tracker.domain.model.value.ExchangeRate;
import com.investments.tracker.infrastructure.external.nbp.NbpExchangeRateClient;

@Testcontainers
@DisplayName("CachingExchangeRateAdapter integration test")
class CachingExchangeRateAdapterIntegrationTest {

    @Container
    static GenericContainer<?> redis =
            new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

    private CachingExchangeRateAdapter adapter;
    private RedisTemplate<String, String> redisTemplate;
    private NbpExchangeRateClient nbpClient;

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

        nbpClient = mock(NbpExchangeRateClient.class);
        adapter = new CachingExchangeRateAdapter(redisTemplate, nbpClient);
    }

    @Test
    @DisplayName("should return identity rate for PLN without Redis or HTTP")
    void shouldReturnIdentityRateForPln() {
        ExchangeRate rate = adapter.getExchangeRateToPln(Currency.PLN);

        assertThat(rate.from()).isEqualTo(Currency.PLN);
        assertThat(rate.to()).isEqualTo(Currency.PLN);
        assertThat(rate.rate()).isEqualByComparingTo(BigDecimal.ONE);
        verify(nbpClient, never()).fetchRate(Currency.PLN);
    }

    @Test
    @DisplayName("should fetch from NBP on cache miss and cache result")
    void shouldFetchFromNbpOnCacheMissAndCacheResult() {
        ExchangeRate nbpRate =
                new ExchangeRate(Currency.EUR, Currency.PLN, new BigDecimal("4.3214"));
        when(nbpClient.fetchRate(Currency.EUR)).thenReturn(nbpRate);

        ExchangeRate rate = adapter.getExchangeRateToPln(Currency.EUR);

        assertThat(rate.from()).isEqualTo(Currency.EUR);
        assertThat(rate.to()).isEqualTo(Currency.PLN);
        assertThat(rate.rate()).isEqualByComparingTo(new BigDecimal("4.3214"));
        verify(nbpClient).fetchRate(Currency.EUR);

        // Verify cached in Redis
        Object storedRate = redisTemplate.opsForHash().get("rate:pln:EUR", "rate");
        assertThat(storedRate).isEqualTo("4.3214");
    }

    @Test
    @DisplayName("should return cached rate without HTTP call")
    void shouldReturnCachedRateWithoutHttpCall() {
        // Pre-populate Redis
        redisTemplate
                .opsForHash()
                .putAll("rate:pln:USD", Map.of("from", "USD", "to", "PLN", "rate", "4.0512"));

        ExchangeRate rate = adapter.getExchangeRateToPln(Currency.USD);

        assertThat(rate.from()).isEqualTo(Currency.USD);
        assertThat(rate.to()).isEqualTo(Currency.PLN);
        assertThat(rate.rate()).isEqualByComparingTo(new BigDecimal("4.0512"));
        verify(nbpClient, never()).fetchRate(Currency.USD);
    }

    @Test
    @DisplayName("should set 24h TTL on cached rate")
    void shouldSetTtlOnCachedRate() {
        ExchangeRate nbpRate =
                new ExchangeRate(Currency.GBP, Currency.PLN, new BigDecimal("5.2500"));
        when(nbpClient.fetchRate(Currency.GBP)).thenReturn(nbpRate);

        adapter.getExchangeRateToPln(Currency.GBP);

        Long ttl = redisTemplate.getExpire("rate:pln:GBP");
        assertThat(ttl).isNotNull().isPositive();
        assertThat(ttl).isGreaterThan(86000L);
    }

    @Test
    @DisplayName("should store with correct Redis key and hash format")
    void shouldStoreWithCorrectKeyFormat() {
        ExchangeRate nbpRate =
                new ExchangeRate(Currency.EUR, Currency.PLN, new BigDecimal("4.3214"));
        when(nbpClient.fetchRate(Currency.EUR)).thenReturn(nbpRate);

        adapter.getExchangeRateToPln(Currency.EUR);

        Boolean exists = redisTemplate.hasKey("rate:pln:EUR");
        assertThat(exists).isTrue();
        assertThat(redisTemplate.opsForHash().get("rate:pln:EUR", "from")).isEqualTo("EUR");
        assertThat(redisTemplate.opsForHash().get("rate:pln:EUR", "to")).isEqualTo("PLN");
        assertThat(redisTemplate.opsForHash().get("rate:pln:EUR", "rate")).isEqualTo("4.3214");
    }

    @Test
    @DisplayName("should throw ExchangeRateNotFoundException when NBP fails")
    void shouldThrowWhenNbpFails() {
        when(nbpClient.fetchRate(Currency.EUR))
                .thenThrow(new ExchangeRateNotFoundException(Currency.EUR, Currency.PLN));

        assertThatThrownBy(() -> adapter.getExchangeRateToPln(Currency.EUR))
                .isInstanceOf(ExchangeRateNotFoundException.class);
    }

    @Test
    @DisplayName("should return rates for multiple currencies")
    void shouldReturnRatesForMultipleCurrencies() {
        when(nbpClient.fetchRate(Currency.EUR))
                .thenReturn(new ExchangeRate(Currency.EUR, Currency.PLN, new BigDecimal("4.3214")));
        when(nbpClient.fetchRate(Currency.USD))
                .thenReturn(new ExchangeRate(Currency.USD, Currency.PLN, new BigDecimal("4.0512")));

        Map<Currency, ExchangeRate> rates =
                adapter.getExchangeRatesToPln(Set.of(Currency.PLN, Currency.EUR, Currency.USD));

        assertThat(rates).hasSize(3);
        assertThat(rates.get(Currency.PLN).isIdentity()).isTrue();
        assertThat(rates.get(Currency.EUR).rate()).isEqualByComparingTo(new BigDecimal("4.3214"));
        assertThat(rates.get(Currency.USD).rate()).isEqualByComparingTo(new BigDecimal("4.0512"));
    }

    @Test
    @DisplayName("should return immutable map from batch method")
    void shouldReturnImmutableMapFromBatchMethod() {
        when(nbpClient.fetchRate(Currency.EUR))
                .thenReturn(new ExchangeRate(Currency.EUR, Currency.PLN, new BigDecimal("4.3214")));

        Map<Currency, ExchangeRate> rates = adapter.getExchangeRatesToPln(Set.of(Currency.EUR));

        assertThatThrownBy(
                        () ->
                                rates.put(
                                        Currency.USD,
                                        new ExchangeRate(
                                                Currency.USD, Currency.PLN, new BigDecimal("4.0"))))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
