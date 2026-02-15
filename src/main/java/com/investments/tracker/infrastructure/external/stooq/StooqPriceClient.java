package com.investments.tracker.infrastructure.external.stooq;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.investments.tracker.domain.model.value.InstrumentSymbol;
import com.investments.tracker.domain.model.value.Money;
import com.investments.tracker.domain.model.value.Price;

/**
 * HTTP client for the Stooq.pl CSV price endpoint.
 *
 * <p>Fetches end-of-day and intraday prices for GPW (Warsaw Stock Exchange) instruments. All GPW
 * instruments are priced in PLN.
 *
 * @see <a href="https://stooq.pl">Stooq.pl</a>
 */
@Component
public class StooqPriceClient {

    private static final Logger log = LoggerFactory.getLogger(StooqPriceClient.class);

    private final RestClient restClient;
    private final String csvPath;

    public StooqPriceClient(
            RestClient stooqRestClient, @Value("${app.stooq.csv-path}") String csvPath) {
        this.restClient = stooqRestClient;
        this.csvPath = csvPath;
    }

    /**
     * Fetches current prices for multiple instruments in a single HTTP request.
     *
     * @param symbols the instrument symbols to fetch
     * @return map of symbol to price (symbols with no data are omitted)
     * @throws RestClientException if a network or server error occurs
     */
    public Map<InstrumentSymbol, Price> fetchPrices(List<InstrumentSymbol> symbols) {
        if (symbols.isEmpty()) {
            return Map.of();
        }

        String symbolsParam =
                symbols.stream().map(s -> s.value().toLowerCase()).collect(Collectors.joining(","));

        log.debug("Fetching prices from Stooq for: {}", symbolsParam);

        String csv =
                restClient
                        .get()
                        .uri(
                                uriBuilder ->
                                        uriBuilder
                                                .path(csvPath)
                                                .queryParam("s", symbolsParam)
                                                .queryParam("f", "sd2t2ohlcv")
                                                .queryParam("h", "")
                                                .queryParam("e", "csv")
                                                .build())
                        .retrieve()
                        .body(String.class);

        return parseCsvResponse(csv);
    }

    /**
     * Fetches the current price for a single instrument.
     *
     * @param symbol the instrument symbol
     * @return the price, or empty if not available
     * @throws RestClientException if a network or server error occurs
     */
    public Optional<Price> fetchPrice(InstrumentSymbol symbol) {
        Map<InstrumentSymbol, Price> prices = fetchPrices(List.of(symbol));
        return Optional.ofNullable(prices.get(symbol));
    }

    private Map<InstrumentSymbol, Price> parseCsvResponse(String csv) {
        if (csv == null || csv.isBlank()) {
            log.warn("Stooq returned empty response");
            return Map.of();
        }

        String[] lines = csv.strip().split("\n");
        if (lines.length < 2) {
            log.warn("Stooq returned CSV with no data rows");
            return Map.of();
        }

        Map<InstrumentSymbol, Price> result = new HashMap<>();

        // Skip header row (line 0)
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) {
                continue;
            }
            try {
                StooqCsvRow.parse(line)
                        .ifPresent(
                                row -> {
                                    InstrumentSymbol symbol = InstrumentSymbol.of(row.symbol());
                                    Price price = new Price(Money.pln(row.close()));
                                    result.put(symbol, price);
                                    log.debug("Stooq price for {}: {}", row.symbol(), row.close());
                                });
            } catch (Exception e) {
                log.warn("Failed to parse Stooq CSV row: '{}' — {}", line, e.getMessage());
            }
        }

        return Map.copyOf(result);
    }
}
