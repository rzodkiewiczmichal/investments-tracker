package com.investments.tracker.infrastructure.external.finnhub;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.investments.tracker.domain.model.value.Currency;
import com.investments.tracker.domain.model.value.InstrumentSymbol;
import com.investments.tracker.domain.model.value.Money;
import com.investments.tracker.domain.model.value.Price;

/**
 * HTTP client for the Finnhub quote API.
 *
 * <p>Fetches real-time prices for US stocks and ETFs (NYSE, NASDAQ). Each symbol requires a
 * separate HTTP call — Finnhub has no batch quote endpoint.
 *
 * <p>When the Finnhub API key is not configured, the {@code finnhubRestClient} bean will not exist
 * and this client will return empty results for all queries.
 *
 * @see <a href="https://finnhub.io/docs/api/quote">Finnhub Quote API</a>
 * @see <a href="../../docs/adr/ADR-031-instrument-price-providers.md">ADR-031</a>
 */
@Component
public class FinnhubPriceClient {

    private static final Logger log = LoggerFactory.getLogger(FinnhubPriceClient.class);

    @Nullable private final RestClient restClient;

    @Autowired(required = false)
    public FinnhubPriceClient(@Qualifier("finnhubRestClient") RestClient finnhubRestClient) {
        this.restClient = finnhubRestClient;
    }

    /** No-arg constructor used when the Finnhub API key is not configured. */
    public FinnhubPriceClient() {
        this.restClient = null;
        log.info("Finnhub API key not configured — US price fetching disabled");
    }

    /**
     * Fetches the current price for a single US instrument.
     *
     * @param symbol the instrument symbol (e.g., AAPL, VOO)
     * @return the current price in USD, or empty if unavailable
     */
    public Optional<Price> fetchPrice(InstrumentSymbol symbol) {
        if (restClient == null) {
            return Optional.empty();
        }
        try {
            FinnhubQuoteResponse response =
                    restClient
                            .get()
                            .uri(
                                    uriBuilder ->
                                            uriBuilder
                                                    .path("/api/v1/quote")
                                                    .queryParam("symbol", symbol.value())
                                                    .build())
                            .retrieve()
                            .body(FinnhubQuoteResponse.class);

            if (response == null || response.isInvalid()) {
                log.debug("Finnhub returned no valid price for {}", symbol.value());
                return Optional.empty();
            }

            Price price = new Price(new Money(response.c(), Currency.USD));
            log.debug("Finnhub price for {}: {}", symbol.value(), response.c());
            return Optional.of(price);
        } catch (RestClientException e) {
            log.warn(
                    "Failed to fetch price from Finnhub for {}: {}",
                    symbol.value(),
                    e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Fetches current prices for multiple US instruments. Calls are made sequentially — Finnhub has
     * no batch quote endpoint.
     *
     * @param symbols the instrument symbols to fetch
     * @return map of symbol to price (symbols with no data are omitted)
     */
    public Map<InstrumentSymbol, Price> fetchPrices(List<InstrumentSymbol> symbols) {
        if (symbols.isEmpty()) {
            return Map.of();
        }

        Map<InstrumentSymbol, Price> result = new HashMap<>();
        for (InstrumentSymbol symbol : symbols) {
            fetchPrice(symbol).ifPresent(price -> result.put(symbol, price));
        }
        return Map.copyOf(result);
    }
}
