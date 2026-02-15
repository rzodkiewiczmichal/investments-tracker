package com.investments.tracker.infrastructure.external.stooq;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.investments.tracker.domain.model.value.Currency;
import com.investments.tracker.domain.model.value.InstrumentSymbol;
import com.investments.tracker.domain.model.value.Price;

@DisplayName("StooqPriceClient")
class StooqPriceClientTest {

    private static final String CSV_PATH = "/q/l/";

    private MockRestServiceServer mockServer;
    private StooqPriceClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://stooq.pl");
        mockServer = MockRestServiceServer.bindTo(builder).build();
        client = new StooqPriceClient(builder.build(), CSV_PATH);
    }

    @Test
    @DisplayName("should parse valid single-symbol CSV response")
    void shouldParseSingleSymbolCsvResponse() {
        mockServer
                .expect(requestTo("https://stooq.pl/q/l/?s=pko&f=sd2t2ohlcv&h=&e=csv"))
                .andRespond(
                        withSuccess(
                                """
                                Symbol,Date,Time,Open,High,Low,Close,Volume
                                PKO,20260212,170401,92,92.94,91.66,92,2646515
                                """,
                                MediaType.TEXT_PLAIN));

        Optional<Price> price = client.fetchPrice(InstrumentSymbol.of("PKO"));

        assertThat(price).isPresent();
        assertThat(price.get().money().amount()).isEqualByComparingTo(new BigDecimal("92"));
        assertThat(price.get().currency()).isEqualTo(Currency.PLN);
        mockServer.verify();
    }

    @Test
    @DisplayName("should parse valid multi-symbol batch CSV response")
    void shouldParseMultiSymbolBatchCsvResponse() {
        mockServer
                .expect(requestTo("https://stooq.pl/q/l/?s=pko,pzu,kghm&f=sd2t2ohlcv&h=&e=csv"))
                .andRespond(
                        withSuccess(
                                """
                                Symbol,Date,Time,Open,High,Low,Close,Volume
                                PKO,20260212,170401,92,92.94,91.66,92,2646515
                                PZU,20260212,170401,45.5,46.1,45.2,45.8,1234567
                                KGHM,20260212,170401,140.3,142.5,139.8,141.2,890123
                                """,
                                MediaType.TEXT_PLAIN));

        Map<InstrumentSymbol, Price> prices =
                client.fetchPrices(
                        List.of(
                                InstrumentSymbol.of("PKO"),
                                InstrumentSymbol.of("PZU"),
                                InstrumentSymbol.of("KGHM")));

        assertThat(prices).hasSize(3);
        assertThat(prices.get(InstrumentSymbol.of("PKO")).money().amount())
                .isEqualByComparingTo(new BigDecimal("92"));
        assertThat(prices.get(InstrumentSymbol.of("PZU")).money().amount())
                .isEqualByComparingTo(new BigDecimal("45.8"));
        assertThat(prices.get(InstrumentSymbol.of("KGHM")).money().amount())
                .isEqualByComparingTo(new BigDecimal("141.2"));
        mockServer.verify();
    }

    @Test
    @DisplayName("should return empty map for CSV with only header row")
    void shouldReturnEmptyForHeaderOnly() {
        mockServer
                .expect(requestTo("https://stooq.pl/q/l/?s=xyz&f=sd2t2ohlcv&h=&e=csv"))
                .andRespond(
                        withSuccess(
                                "Symbol,Date,Time,Open,High,Low,Close,Volume\n",
                                MediaType.TEXT_PLAIN));

        Optional<Price> price = client.fetchPrice(InstrumentSymbol.of("XYZ"));

        assertThat(price).isEmpty();
        mockServer.verify();
    }

    @Test
    @DisplayName("should skip N/D rows for unavailable data")
    void shouldSkipNdRows() {
        mockServer
                .expect(requestTo("https://stooq.pl/q/l/?s=pko,bad1&f=sd2t2ohlcv&h=&e=csv"))
                .andRespond(
                        withSuccess(
                                """
                                Symbol,Date,Time,Open,High,Low,Close,Volume
                                PKO,20260212,170401,92,92.94,91.66,92,2646515
                                BAD1,N/D,N/D,N/D,N/D,N/D,N/D,N/D
                                """,
                                MediaType.TEXT_PLAIN));

        Map<InstrumentSymbol, Price> prices =
                client.fetchPrices(
                        List.of(InstrumentSymbol.of("PKO"), InstrumentSymbol.of("BAD1")));

        assertThat(prices).hasSize(1);
        assertThat(prices).containsKey(InstrumentSymbol.of("PKO"));
        assertThat(prices).doesNotContainKey(InstrumentSymbol.of("BAD1"));
        mockServer.verify();
    }

    @Test
    @DisplayName("should return empty map for empty response body")
    void shouldReturnEmptyForEmptyResponse() {
        mockServer
                .expect(requestTo("https://stooq.pl/q/l/?s=pko&f=sd2t2ohlcv&h=&e=csv"))
                .andRespond(withSuccess("", MediaType.TEXT_PLAIN));

        Optional<Price> price = client.fetchPrice(InstrumentSymbol.of("PKO"));

        assertThat(price).isEmpty();
        mockServer.verify();
    }

    @Test
    @DisplayName("should throw on server error")
    void shouldThrowOnServerError() {
        mockServer
                .expect(requestTo("https://stooq.pl/q/l/?s=pko&f=sd2t2ohlcv&h=&e=csv"))
                .andRespond(withServerError());

        assertThatThrownBy(() -> client.fetchPrice(InstrumentSymbol.of("PKO")))
                .isInstanceOf(RestClientException.class);
        mockServer.verify();
    }

    @Test
    @DisplayName("should return empty map for empty symbol list")
    void shouldReturnEmptyForEmptySymbolList() {
        Map<InstrumentSymbol, Price> prices = client.fetchPrices(List.of());

        assertThat(prices).isEmpty();
    }

    @Test
    @DisplayName("should handle decimal prices correctly")
    void shouldHandleDecimalPricesCorrectly() {
        mockServer
                .expect(requestTo("https://stooq.pl/q/l/?s=pzu&f=sd2t2ohlcv&h=&e=csv"))
                .andRespond(
                        withSuccess(
                                """
                                Symbol,Date,Time,Open,High,Low,Close,Volume
                                PZU,20260212,170401,45.50,46.10,45.20,45.85,1234567
                                """,
                                MediaType.TEXT_PLAIN));

        Optional<Price> price = client.fetchPrice(InstrumentSymbol.of("PZU"));

        assertThat(price).isPresent();
        assertThat(price.get().money().amount()).isEqualByComparingTo(new BigDecimal("45.85"));
        mockServer.verify();
    }
}
