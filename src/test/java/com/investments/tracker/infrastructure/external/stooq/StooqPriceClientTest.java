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

        Optional<Price> price = client.fetchPrice(InstrumentSymbol.of("PKO.PL"));

        assertThat(price).isPresent();
        assertThat(price.get().money().amount()).isEqualByComparingTo(new BigDecimal("92"));
        assertThat(price.get().currency()).isEqualTo(Currency.PLN);
        mockServer.verify();
    }

    @Test
    @DisplayName("should parse valid multi-symbol batch CSV response")
    void shouldParseMultiSymbolBatchCsvResponse() {
        mockServer
                .expect(requestTo("https://stooq.pl/q/l/?s=pko%20pzu%20kghm&f=sd2t2ohlcv&h=&e=csv"))
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
                                InstrumentSymbol.of("PKO.PL"),
                                InstrumentSymbol.of("PZU.PL"),
                                InstrumentSymbol.of("KGHM.PL")));

        assertThat(prices).hasSize(3);
        assertThat(prices.get(InstrumentSymbol.of("PKO.PL")).money().amount())
                .isEqualByComparingTo(new BigDecimal("92"));
        assertThat(prices.get(InstrumentSymbol.of("PZU.PL")).money().amount())
                .isEqualByComparingTo(new BigDecimal("45.8"));
        assertThat(prices.get(InstrumentSymbol.of("KGHM.PL")).money().amount())
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

        Optional<Price> price = client.fetchPrice(InstrumentSymbol.of("XYZ.PL"));

        assertThat(price).isEmpty();
        mockServer.verify();
    }

    @Test
    @DisplayName("should skip N/D rows for unavailable data")
    void shouldSkipNdRows() {
        mockServer
                .expect(requestTo("https://stooq.pl/q/l/?s=pko%20bad1&f=sd2t2ohlcv&h=&e=csv"))
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
                        List.of(InstrumentSymbol.of("PKO.PL"), InstrumentSymbol.of("BAD1.PL")));

        assertThat(prices).hasSize(1);
        assertThat(prices).containsKey(InstrumentSymbol.of("PKO.PL"));
        assertThat(prices).doesNotContainKey(InstrumentSymbol.of("BAD1.PL"));
        mockServer.verify();
    }

    @Test
    @DisplayName("should skip B/D rows for unavailable data")
    void shouldSkipBdRows() {
        mockServer
                .expect(
                        requestTo(
                                "https://stooq.pl/q/l/?s=pko%20atrem%20elektroti&f=sd2t2ohlcv&h=&e=csv"))
                .andRespond(
                        withSuccess(
                                """
                                Symbol,Date,Time,Open,High,Low,Close,Volume
                                PKO,20260212,170401,92,92.94,91.66,92,2646515
                                ATREM,B/D,B/D,B/D,B/D,B/D,B/D,B/D
                                ELEKTROTI,B/D,B/D,B/D,B/D,B/D,B/D,B/D
                                """,
                                MediaType.TEXT_PLAIN));

        Map<InstrumentSymbol, Price> prices =
                client.fetchPrices(
                        List.of(
                                InstrumentSymbol.of("PKO.PL"),
                                InstrumentSymbol.of("ATREM.PL"),
                                InstrumentSymbol.of("ELEKTROTI.PL")));

        assertThat(prices).hasSize(1);
        assertThat(prices).containsKey(InstrumentSymbol.of("PKO.PL"));
        assertThat(prices).doesNotContainKey(InstrumentSymbol.of("ATREM.PL"));
        assertThat(prices).doesNotContainKey(InstrumentSymbol.of("ELEKTROTI.PL"));
        mockServer.verify();
    }

    @Test
    @DisplayName("should return empty map for empty response body")
    void shouldReturnEmptyForEmptyResponse() {
        mockServer
                .expect(requestTo("https://stooq.pl/q/l/?s=pko&f=sd2t2ohlcv&h=&e=csv"))
                .andRespond(withSuccess("", MediaType.TEXT_PLAIN));

        Optional<Price> price = client.fetchPrice(InstrumentSymbol.of("PKO.PL"));

        assertThat(price).isEmpty();
        mockServer.verify();
    }

    @Test
    @DisplayName("should throw on server error")
    void shouldThrowOnServerError() {
        mockServer
                .expect(requestTo("https://stooq.pl/q/l/?s=pko&f=sd2t2ohlcv&h=&e=csv"))
                .andRespond(withServerError());

        assertThatThrownBy(() -> client.fetchPrice(InstrumentSymbol.of("PKO.PL")))
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
    @DisplayName("should append .pl suffix for ETF symbols in request")
    void shouldAppendPlSuffixForEtfSymbols() {
        mockServer
                .expect(requestTo("https://stooq.pl/q/l/?s=etfbtbsp.pl&f=sd2t2ohlcv&h=&e=csv"))
                .andRespond(
                        withSuccess(
                                """
                                Symbol,Date,Time,Open,High,Low,Close,Volume
                                ETFBTBSP.PL,20260220,170200,230.85,230.9,230.5,230.5,8885
                                """,
                                MediaType.TEXT_PLAIN));

        Optional<Price> price = client.fetchPrice(InstrumentSymbol.of("ETFBTBSP.PL"));

        assertThat(price).isPresent();
        assertThat(price.get().money().amount()).isEqualByComparingTo(new BigDecimal("230.5"));
        mockServer.verify();
    }

    @Test
    @DisplayName("should handle mixed stock and ETF batch request")
    void shouldHandleMixedStockAndEtfBatchRequest() {
        mockServer
                .expect(
                        requestTo(
                                "https://stooq.pl/q/l/?s=pko%20etfsp500.pl&f=sd2t2ohlcv&h=&e=csv"))
                .andRespond(
                        withSuccess(
                                """
                                Symbol,Date,Time,Open,High,Low,Close,Volume
                                PKO,20260220,170401,90.5,91.4,89.76,91.4,1466051
                                ETFSP500.PL,20260220,170200,253.4,254,252.55,254,4406
                                """,
                                MediaType.TEXT_PLAIN));

        Map<InstrumentSymbol, Price> prices =
                client.fetchPrices(
                        List.of(InstrumentSymbol.of("PKO.PL"), InstrumentSymbol.of("ETFSP500.PL")));

        assertThat(prices).hasSize(2);
        assertThat(prices.get(InstrumentSymbol.of("PKO.PL")).money().amount())
                .isEqualByComparingTo(new BigDecimal("91.4"));
        assertThat(prices.get(InstrumentSymbol.of("ETFSP500.PL")).money().amount())
                .isEqualByComparingTo(new BigDecimal("254"));
        mockServer.verify();
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

        Optional<Price> price = client.fetchPrice(InstrumentSymbol.of("PZU.PL"));

        assertThat(price).isPresent();
        assertThat(price.get().money().amount()).isEqualByComparingTo(new BigDecimal("45.85"));
        mockServer.verify();
    }

    @Test
    @DisplayName("should fetch UK instrument price with .uk suffix and correct currency")
    void shouldFetchUkInstrumentPrice() {
        mockServer
                .expect(requestTo("https://stooq.pl/q/l/?s=cspx.uk&f=sd2t2ohlcv&h=&e=csv"))
                .andRespond(
                        withSuccess(
                                """
                                Symbol,Date,Time,Open,High,Low,Close,Volume
                                CSPX.UK,20260317,170000,721.96,723.5,720.1,721.96,12345
                                """,
                                MediaType.TEXT_PLAIN));

        Optional<Price> price = client.fetchPrice(InstrumentSymbol.of("CSPX.UK"), Currency.USD);

        assertThat(price).isPresent();
        assertThat(price.get().money().amount()).isEqualByComparingTo(new BigDecimal("721.96"));
        assertThat(price.get().currency()).isEqualTo(Currency.USD);
        mockServer.verify();
    }

    @Test
    @DisplayName("should convert GBX pence to GBP for UK instrument with GBP currency")
    void shouldFetchUkInstrumentWithGbpCurrency() {
        mockServer
                .expect(requestTo("https://stooq.pl/q/l/?s=emim.uk&f=sd2t2ohlcv&h=&e=csv"))
                .andRespond(
                        withSuccess(
                                """
                                Symbol,Date,Time,Open,High,Low,Close,Volume
                                EMIM.UK,20260317,170000,3593,3610,3580,3593,8000
                                """,
                                MediaType.TEXT_PLAIN));

        Optional<Price> price = client.fetchPrice(InstrumentSymbol.of("EMIM.UK"), Currency.GBP);

        assertThat(price).isPresent();
        // Stooq returns GBX (pence), should be divided by 100 to get GBP
        assertThat(price.get().money().amount()).isEqualByComparingTo(new BigDecimal("35.9300"));
        assertThat(price.get().currency()).isEqualTo(Currency.GBP);
        mockServer.verify();
    }

    @Test
    @DisplayName("should return empty for UK instrument with N/D data")
    void shouldReturnEmptyForUkInstrumentWithNoData() {
        mockServer
                .expect(requestTo("https://stooq.pl/q/l/?s=unknown.uk&f=sd2t2ohlcv&h=&e=csv"))
                .andRespond(
                        withSuccess(
                                """
                                Symbol,Date,Time,Open,High,Low,Close,Volume
                                UNKNOWN.UK,N/D,N/D,N/D,N/D,N/D,N/D,N/D
                                """,
                                MediaType.TEXT_PLAIN));

        Optional<Price> price = client.fetchPrice(InstrumentSymbol.of("UNKNOWN.UK"), Currency.USD);

        assertThat(price).isEmpty();
        mockServer.verify();
    }
}
