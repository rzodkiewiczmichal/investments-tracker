package com.investments.tracker.infrastructure.external.nbp;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import com.investments.tracker.domain.model.value.Currency;
import com.investments.tracker.domain.model.value.ExchangeRate;

/**
 * Live test hitting the real NBP API. Requires network access and valid SSL certificates.
 *
 * <p>Excluded from standard build — run manually via {@code ./gradlew liveTest}.
 */
@DisplayName("NbpExchangeRateClient live test (real NBP API)")
class NbpExchangeRateClientLiveTest {

    private static final String RATES_PATH = "/api/exchangerates/rates/a/{currency}/";

    private NbpExchangeRateClient client;

    @BeforeEach
    void setUp() {
        RestClient restClient = RestClient.builder().baseUrl("https://api.nbp.pl").build();
        client = new NbpExchangeRateClient(restClient, RATES_PATH);
    }

    @Test
    @DisplayName("should fetch current EUR/PLN rate")
    void shouldFetchEurPlnRate() {
        ExchangeRate rate = client.fetchRate(Currency.EUR);

        assertThat(rate.from()).isEqualTo(Currency.EUR);
        assertThat(rate.to()).isEqualTo(Currency.PLN);
        assertThat(rate.rate()).isBetween(new BigDecimal("1.0"), new BigDecimal("10.0"));
    }

    @Test
    @DisplayName("should fetch current USD/PLN rate")
    void shouldFetchUsdPlnRate() {
        ExchangeRate rate = client.fetchRate(Currency.USD);

        assertThat(rate.from()).isEqualTo(Currency.USD);
        assertThat(rate.to()).isEqualTo(Currency.PLN);
        assertThat(rate.rate()).isBetween(new BigDecimal("1.0"), new BigDecimal("10.0"));
    }

    @Test
    @DisplayName("should fetch current GBP/PLN rate")
    void shouldFetchGbpPlnRate() {
        ExchangeRate rate = client.fetchRate(Currency.GBP);

        assertThat(rate.from()).isEqualTo(Currency.GBP);
        assertThat(rate.to()).isEqualTo(Currency.PLN);
        assertThat(rate.rate()).isBetween(new BigDecimal("1.0"), new BigDecimal("10.0"));
    }
}
