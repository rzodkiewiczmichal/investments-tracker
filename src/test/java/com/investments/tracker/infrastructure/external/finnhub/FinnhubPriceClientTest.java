package com.investments.tracker.infrastructure.external.finnhub;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.investments.tracker.domain.model.value.Currency;
import com.investments.tracker.domain.model.value.InstrumentSymbol;
import com.investments.tracker.domain.model.value.Price;

@DisplayName("FinnhubPriceClient")
class FinnhubPriceClientTest {

    private RestClient restClient;
    private RestClient.RequestHeadersUriSpec<?> requestSpec;
    private RestClient.ResponseSpec responseSpec;
    private FinnhubPriceClient client;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        restClient = mock(RestClient.class);
        requestSpec = mock(RestClient.RequestHeadersUriSpec.class);
        responseSpec = mock(RestClient.ResponseSpec.class);

        when(restClient.get()).thenReturn((RestClient.RequestHeadersUriSpec) requestSpec);
        when(requestSpec.uri(any(Function.class))).thenReturn(requestSpec);
        when(requestSpec.retrieve()).thenReturn(responseSpec);

        client = new FinnhubPriceClient(restClient);
    }

    @Test
    @DisplayName("should fetch price for valid symbol")
    void shouldFetchPriceForValidSymbol() {
        FinnhubQuoteResponse response =
                new FinnhubQuoteResponse(
                        new BigDecimal("185.50"), null, null, null, null, null, null, null);
        when(responseSpec.body(FinnhubQuoteResponse.class)).thenReturn(response);

        Optional<Price> result = client.fetchPrice(InstrumentSymbol.of("AAPL"));

        assertThat(result).isPresent();
        assertThat(result.get().money().amount()).isEqualByComparingTo("185.50");
        assertThat(result.get().money().currency()).isEqualTo(Currency.USD);
    }

    @Test
    @DisplayName("should return empty for zero price (delisted/unknown symbol)")
    void shouldReturnEmptyForZeroPrice() {
        FinnhubQuoteResponse response =
                new FinnhubQuoteResponse(BigDecimal.ZERO, null, null, null, null, null, null, null);
        when(responseSpec.body(FinnhubQuoteResponse.class)).thenReturn(response);

        Optional<Price> result = client.fetchPrice(InstrumentSymbol.of("XYZXYZ"));

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("should return empty for null response")
    void shouldReturnEmptyForNullResponse() {
        when(responseSpec.body(FinnhubQuoteResponse.class)).thenReturn(null);

        Optional<Price> result = client.fetchPrice(InstrumentSymbol.of("AAPL"));

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("should return empty on API exception")
    void shouldReturnEmptyOnApiException() {
        when(responseSpec.body(FinnhubQuoteResponse.class))
                .thenThrow(new RestClientException("Connection refused"));

        Optional<Price> result = client.fetchPrice(InstrumentSymbol.of("AAPL"));

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("should fetch multiple prices sequentially")
    void shouldFetchMultiplePricesSequentially() {
        FinnhubQuoteResponse appleResponse =
                new FinnhubQuoteResponse(
                        new BigDecimal("185.50"), null, null, null, null, null, null, null);
        FinnhubQuoteResponse vooResponse =
                new FinnhubQuoteResponse(
                        new BigDecimal("480.20"), null, null, null, null, null, null, null);

        when(responseSpec.body(FinnhubQuoteResponse.class))
                .thenReturn(appleResponse)
                .thenReturn(vooResponse);

        Map<InstrumentSymbol, Price> result =
                client.fetchPrices(
                        List.of(InstrumentSymbol.of("AAPL"), InstrumentSymbol.of("VOO")));

        assertThat(result).hasSize(2);
        assertThat(result.get(InstrumentSymbol.of("AAPL")).money().amount())
                .isEqualByComparingTo("185.50");
        assertThat(result.get(InstrumentSymbol.of("VOO")).money().amount())
                .isEqualByComparingTo("480.20");
    }

    @Test
    @DisplayName("should return empty map for empty symbol list")
    void shouldReturnEmptyMapForEmptyList() {
        Map<InstrumentSymbol, Price> result = client.fetchPrices(List.of());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("should return empty when API not configured")
    void shouldReturnEmptyWhenApiNotConfigured() {
        FinnhubPriceClient disabledClient = new FinnhubPriceClient();

        Optional<Price> result = disabledClient.fetchPrice(InstrumentSymbol.of("AAPL"));

        assertThat(result).isEmpty();
    }
}
