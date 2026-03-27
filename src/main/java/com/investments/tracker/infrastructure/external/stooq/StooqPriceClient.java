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

import com.investments.tracker.domain.model.value.Currency;
import com.investments.tracker.domain.model.value.InstrumentSymbol;
import com.investments.tracker.domain.model.value.Money;
import com.investments.tracker.domain.model.value.Price;

/**
 * HTTP client for the Stooq.pl CSV price endpoint.
 *
 * <p>Fetches end-of-day and intraday prices for GPW and LSE instruments. GPW instruments support
 * batch fetching; UK instruments must be fetched individually (Stooq returns N/D for UK batch
 * requests).
 *
 * <p>Stooq symbol conventions:
 *
 * <ul>
 *   <li>GPW stocks: bare lowercase ({@code pko})
 *   <li>GPW ETFs: lowercase with {@code .pl} suffix ({@code etfsp500.pl})
 *   <li>UK instruments: lowercase with {@code .uk} suffix ({@code cspx.uk})
 * </ul>
 *
 * @see <a href="https://stooq.pl">Stooq.pl</a>
 */
@Component
public class StooqPriceClient {

    private static final Logger log = LoggerFactory.getLogger(StooqPriceClient.class);
    private static final String ETF_PREFIX = "ETF";
    private static final String STOOQ_GPW_SUFFIX = ".pl";
    private static final String STOOQ_UK_SUFFIX = ".uk";

    private final RestClient restClient;
    private final String csvPath;

    public StooqPriceClient(
            RestClient stooqRestClient, @Value("${app.stooq.csv-path}") String csvPath) {
        this.restClient = stooqRestClient;
        this.csvPath = csvPath;
    }

    /**
     * Fetches current prices for multiple GPW instruments in a single HTTP request.
     *
     * @param symbols the GPW instrument symbols to fetch
     * @return map of symbol to price in PLN (symbols with no data are omitted)
     * @throws RestClientException if a network or server error occurs
     */
    public Map<InstrumentSymbol, Price> fetchPrices(List<InstrumentSymbol> symbols) {
        if (symbols.isEmpty()) {
            return Map.of();
        }

        String symbolsParam =
                symbols.stream().map(this::toStooqSymbol).collect(Collectors.joining(" "));

        log.debug("Fetching prices from Stooq for: {}", symbolsParam);

        String csv = fetchCsv(symbolsParam);
        return parseCsvResponse(csv, Currency.PLN);
    }

    /**
     * Fetches the current price for a single GPW instrument.
     *
     * @param symbol the instrument symbol
     * @return the price in PLN, or empty if not available
     * @throws RestClientException if a network or server error occurs
     */
    public Optional<Price> fetchPrice(InstrumentSymbol symbol) {
        Map<InstrumentSymbol, Price> prices = fetchPrices(List.of(symbol));
        return Optional.ofNullable(prices.get(symbol));
    }

    /**
     * Fetches the current price for a single instrument with the given currency.
     *
     * <p>Used for non-GPW markets (e.g. UK) where each instrument may have a different currency and
     * batch requests are not supported.
     *
     * @param symbol the instrument symbol
     * @param currency the instrument's trading currency
     * @return the price, or empty if not available
     * @throws RestClientException if a network or server error occurs
     */
    public Optional<Price> fetchPrice(InstrumentSymbol symbol, Currency currency) {
        String stooqSymbol = toStooqSymbol(symbol);
        log.debug("Fetching price from Stooq for: {}", stooqSymbol);

        String csv = fetchCsv(stooqSymbol);
        Map<InstrumentSymbol, Price> prices = parseCsvResponse(csv, currency);
        return Optional.ofNullable(prices.get(symbol));
    }

    private String fetchCsv(String symbolsParam) {
        return restClient
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
    }

    private Map<InstrumentSymbol, Price> parseCsvResponse(String csv, Currency currency) {
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
                                    InstrumentSymbol symbol =
                                            InstrumentSymbol.of(fromStooqSymbol(row.symbol()));
                                    Price price = new Price(new Money(row.close(), currency));
                                    result.put(symbol, price);
                                    log.debug(
                                            "Stooq price for {}: {}", symbol.value(), row.close());
                                });
            } catch (Exception e) {
                log.warn("Failed to parse Stooq CSV row: '{}' -- {}", line, e.getMessage());
            }
        }

        return Map.copyOf(result);
    }

    private String toStooqSymbol(InstrumentSymbol symbol) {
        String value = symbol.value();

        // UK instruments: CSPX.UK -> cspx.uk
        if (value.endsWith(".UK")) {
            return value.substring(0, value.length() - 3).toLowerCase() + STOOQ_UK_SUFFIX;
        }

        // GPW instruments: strip .PL suffix
        if (value.endsWith(".PL")) {
            value = value.substring(0, value.length() - 3);
        }
        String lower = value.toLowerCase();
        // Stooq requires .pl suffix for GPW ETFs
        return lower.startsWith(ETF_PREFIX.toLowerCase()) ? lower + STOOQ_GPW_SUFFIX : lower;
    }

    static String fromStooqSymbol(String stooqSymbol) {
        // UK instruments: Stooq returns CSPX.UK -> already in domain format
        if (stooqSymbol.endsWith(".UK")) {
            return stooqSymbol;
        }

        // GPW: Stooq returns ETFs with .PL suffix, regular stocks without
        String base =
                stooqSymbol.endsWith(STOOQ_GPW_SUFFIX.toUpperCase())
                        ? stooqSymbol.substring(0, stooqSymbol.length() - STOOQ_GPW_SUFFIX.length())
                        : stooqSymbol;
        return base + ".PL";
    }
}
