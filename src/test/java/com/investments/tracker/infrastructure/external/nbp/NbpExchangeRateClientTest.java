package com.investments.tracker.infrastructure.external.nbp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.investments.tracker.domain.exception.ExchangeRateNotFoundException;
import com.investments.tracker.domain.model.value.Currency;
import com.investments.tracker.domain.model.value.ExchangeRate;

@DisplayName("NbpExchangeRateClient")
class NbpExchangeRateClientTest {

    private static final String RATES_PATH = "/api/exchangerates/rates/a/{currency}/";

    private MockRestServiceServer mockServer;
    private NbpExchangeRateClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://api.nbp.pl");
        mockServer = MockRestServiceServer.bindTo(builder).build();
        client = new NbpExchangeRateClient(builder.build(), RATES_PATH);
    }

    @Test
    @DisplayName("should parse valid EUR rate response")
    void shouldParseValidEurRateResponse() {
        mockServer
                .expect(requestTo("https://api.nbp.pl/api/exchangerates/rates/a/eur/"))
                .andRespond(
                        withSuccess(
                                """
                        {
                            "table": "A",
                            "currency": "euro",
                            "code": "EUR",
                            "rates": [{
                                "no": "030/A/NBP/2026",
                                "effectiveDate": "2026-02-12",
                                "mid": 4.3214
                            }]
                        }
                        """,
                                MediaType.APPLICATION_JSON));

        ExchangeRate rate = client.fetchRate(Currency.EUR);

        assertThat(rate.from()).isEqualTo(Currency.EUR);
        assertThat(rate.to()).isEqualTo(Currency.PLN);
        assertThat(rate.rate()).isEqualByComparingTo(new BigDecimal("4.3214"));
        mockServer.verify();
    }

    @Test
    @DisplayName("should parse valid USD rate response")
    void shouldParseValidUsdRateResponse() {
        mockServer
                .expect(requestTo("https://api.nbp.pl/api/exchangerates/rates/a/usd/"))
                .andRespond(
                        withSuccess(
                                """
                        {
                            "table": "A",
                            "currency": "dolar amerykanski",
                            "code": "USD",
                            "rates": [{
                                "no": "030/A/NBP/2026",
                                "effectiveDate": "2026-02-12",
                                "mid": 4.0512
                            }]
                        }
                        """,
                                MediaType.APPLICATION_JSON));

        ExchangeRate rate = client.fetchRate(Currency.USD);

        assertThat(rate.from()).isEqualTo(Currency.USD);
        assertThat(rate.to()).isEqualTo(Currency.PLN);
        assertThat(rate.rate()).isEqualByComparingTo(new BigDecimal("4.0512"));
        mockServer.verify();
    }

    @Test
    @DisplayName("should return identity rate for PLN without HTTP call")
    void shouldReturnIdentityRateForPln() {
        ExchangeRate rate = client.fetchRate(Currency.PLN);

        assertThat(rate.from()).isEqualTo(Currency.PLN);
        assertThat(rate.to()).isEqualTo(Currency.PLN);
        assertThat(rate.rate()).isEqualByComparingTo(BigDecimal.ONE);
    }

    @Test
    @DisplayName("should throw ExchangeRateNotFoundException on 404")
    void shouldThrowOnNotFound() {
        mockServer
                .expect(requestTo("https://api.nbp.pl/api/exchangerates/rates/a/gbp/"))
                .andRespond(withResourceNotFound());

        assertThatThrownBy(() -> client.fetchRate(Currency.GBP))
                .isInstanceOf(ExchangeRateNotFoundException.class);
        mockServer.verify();
    }

    @Test
    @DisplayName("should throw on server error")
    void shouldThrowOnServerError() {
        mockServer
                .expect(requestTo("https://api.nbp.pl/api/exchangerates/rates/a/eur/"))
                .andRespond(withServerError());

        assertThatThrownBy(() -> client.fetchRate(Currency.EUR))
                .isInstanceOf(RestClientException.class);
        mockServer.verify();
    }
}
