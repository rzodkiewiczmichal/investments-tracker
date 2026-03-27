package com.investments.tracker.infrastructure.external.finnhub;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.Collection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.investments.tracker.domain.model.Instrument;
import com.investments.tracker.domain.model.value.Currency;
import com.investments.tracker.domain.model.value.InstrumentSymbol;
import com.investments.tracker.domain.model.value.InstrumentType;

@DisplayName("FinnhubCatalogClient")
class FinnhubCatalogClientTest {

    private static final String SYMBOLS_PATH = "/api/v1/stock/symbol";
    private static final String API_KEY = "test-api-key";

    private MockRestServiceServer mockServer;
    private FinnhubCatalogClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://finnhub.io");
        mockServer = MockRestServiceServer.bindTo(builder).build();
        client = new FinnhubCatalogClient(builder.build(), SYMBOLS_PATH, API_KEY);
    }

    @Test
    @DisplayName("should parse valid response with supported types")
    void shouldParseValidResponse() {
        mockServer
                .expect(
                        requestTo(
                                "https://finnhub.io/api/v1/stock/symbol?exchange=US&token=test-api-key"))
                .andRespond(
                        withSuccess(
                                """
                                [
                                    {"symbol":"AAPL","description":"APPLE INC","currency":"USD","type":"Common Stock"},
                                    {"symbol":"SPY","description":"SPDR S&P 500 ETF TRUST","currency":"USD","type":"ETP"}
                                ]
                                """,
                                MediaType.APPLICATION_JSON));

        Collection<Instrument> instruments = client.fetchUsStockCatalog();

        assertThat(instruments).hasSize(2);
        assertThat(instruments)
                .extracting(i -> i.symbol().value())
                .containsExactlyInAnyOrder("AAPL.US", "SPY.US");
        mockServer.verify();
    }

    @Test
    @DisplayName("should map Common Stock to STOCK type")
    void shouldMapCommonStockToStock() {
        mockServer
                .expect(
                        requestTo(
                                "https://finnhub.io/api/v1/stock/symbol?exchange=US&token=test-api-key"))
                .andRespond(
                        withSuccess(
                                """
                                [{"symbol":"AAPL","description":"APPLE INC","currency":"USD","type":"Common Stock"}]
                                """,
                                MediaType.APPLICATION_JSON));

        Collection<Instrument> instruments = client.fetchUsStockCatalog();

        Instrument apple = instruments.iterator().next();
        assertThat(apple.type()).isEqualTo(InstrumentType.STOCK);
        assertThat(apple.currency()).isEqualTo(Currency.USD);
        mockServer.verify();
    }

    @Test
    @DisplayName("should map ETP to ETF type")
    void shouldMapEtpToEtf() {
        mockServer
                .expect(
                        requestTo(
                                "https://finnhub.io/api/v1/stock/symbol?exchange=US&token=test-api-key"))
                .andRespond(
                        withSuccess(
                                """
                                [{"symbol":"SPY","description":"SPDR S&P 500","currency":"USD","type":"ETP"}]
                                """,
                                MediaType.APPLICATION_JSON));

        Collection<Instrument> instruments = client.fetchUsStockCatalog();

        assertThat(instruments.iterator().next().type()).isEqualTo(InstrumentType.ETF);
        mockServer.verify();
    }

    @Test
    @DisplayName("should map ADR and REIT to STOCK type")
    void shouldMapAdrAndReitToStock() {
        mockServer
                .expect(
                        requestTo(
                                "https://finnhub.io/api/v1/stock/symbol?exchange=US&token=test-api-key"))
                .andRespond(
                        withSuccess(
                                """
                                [
                                    {"symbol":"BABA","description":"ALIBABA GROUP","currency":"USD","type":"ADR"},
                                    {"symbol":"AMT","description":"AMERICAN TOWER","currency":"USD","type":"REIT"}
                                ]
                                """,
                                MediaType.APPLICATION_JSON));

        Collection<Instrument> instruments = client.fetchUsStockCatalog();

        assertThat(instruments).hasSize(2);
        assertThat(instruments).allMatch(i -> i.type() == InstrumentType.STOCK);
        mockServer.verify();
    }

    @Test
    @DisplayName("should filter out unsupported types")
    void shouldFilterOutUnsupportedTypes() {
        mockServer
                .expect(
                        requestTo(
                                "https://finnhub.io/api/v1/stock/symbol?exchange=US&token=test-api-key"))
                .andRespond(
                        withSuccess(
                                """
                                [
                                    {"symbol":"AAPL","description":"APPLE INC","currency":"USD","type":"Common Stock"},
                                    {"symbol":"SKIP1","description":"SOME UNIT","currency":"USD","type":"Unit"},
                                    {"symbol":"SKIP2","description":"SOME WARRANT","currency":"USD","type":"Warrant"},
                                    {"symbol":"SKIP3","description":"SOME FUND","currency":"USD","type":"Closed-End Fund"}
                                ]
                                """,
                                MediaType.APPLICATION_JSON));

        Collection<Instrument> instruments = client.fetchUsStockCatalog();

        assertThat(instruments).hasSize(1);
        assertThat(instruments.iterator().next().symbol().value()).isEqualTo("AAPL.US");
        mockServer.verify();
    }

    @Test
    @DisplayName("should filter out non-USD currencies")
    void shouldFilterOutNonUsdCurrencies() {
        mockServer
                .expect(
                        requestTo(
                                "https://finnhub.io/api/v1/stock/symbol?exchange=US&token=test-api-key"))
                .andRespond(
                        withSuccess(
                                """
                                [
                                    {"symbol":"AAPL","description":"APPLE INC","currency":"USD","type":"Common Stock"},
                                    {"symbol":"EUR1","description":"EURO THING","currency":"EUR","type":"Common Stock"}
                                ]
                                """,
                                MediaType.APPLICATION_JSON));

        Collection<Instrument> instruments = client.fetchUsStockCatalog();

        assertThat(instruments).hasSize(1);
        assertThat(instruments.iterator().next().symbol().value()).isEqualTo("AAPL.US");
        mockServer.verify();
    }

    @Test
    @DisplayName("should skip symbols that fail InstrumentSymbol validation")
    void shouldSkipInvalidSymbols() {
        mockServer
                .expect(
                        requestTo(
                                "https://finnhub.io/api/v1/stock/symbol?exchange=US&token=test-api-key"))
                .andRespond(
                        withSuccess(
                                """
                                [
                                    {"symbol":"AAPL","description":"APPLE INC","currency":"USD","type":"Common Stock"},
                                    {"symbol":"INVALID-SYM!","description":"BAD","currency":"USD","type":"Common Stock"}
                                ]
                                """,
                                MediaType.APPLICATION_JSON));

        Collection<Instrument> instruments = client.fetchUsStockCatalog();

        assertThat(instruments).hasSize(1);
        assertThat(instruments.iterator().next().symbol().value()).isEqualTo("AAPL.US");
        mockServer.verify();
    }

    @Test
    @DisplayName("should flatten dotted share-class symbols like BRK.B to BRKB.US")
    void shouldFlattenDottedShareClassSymbols() {
        mockServer
                .expect(
                        requestTo(
                                "https://finnhub.io/api/v1/stock/symbol?exchange=US&token=test-api-key"))
                .andRespond(
                        withSuccess(
                                """
                                [{"symbol":"BRK.B","description":"BERKSHIRE HATHAWAY INC-CL B","currency":"USD","type":"Common Stock"}]
                                """,
                                MediaType.APPLICATION_JSON));

        Collection<Instrument> instruments = client.fetchUsStockCatalog();

        assertThat(instruments).hasSize(1);
        assertThat(instruments.iterator().next().symbol())
                .isEqualTo(InstrumentSymbol.of("BRKB.US"));
        mockServer.verify();
    }

    @Test
    @DisplayName("should return empty collection for empty response")
    void shouldReturnEmptyForEmptyResponse() {
        mockServer
                .expect(
                        requestTo(
                                "https://finnhub.io/api/v1/stock/symbol?exchange=US&token=test-api-key"))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        Collection<Instrument> instruments = client.fetchUsStockCatalog();

        assertThat(instruments).isEmpty();
        mockServer.verify();
    }

    @Test
    @DisplayName("should throw on server error")
    void shouldThrowOnServerError() {
        mockServer
                .expect(
                        requestTo(
                                "https://finnhub.io/api/v1/stock/symbol?exchange=US&token=test-api-key"))
                .andRespond(withServerError());

        assertThatThrownBy(() -> client.fetchUsStockCatalog())
                .isInstanceOf(RestClientException.class);
        mockServer.verify();
    }
}
