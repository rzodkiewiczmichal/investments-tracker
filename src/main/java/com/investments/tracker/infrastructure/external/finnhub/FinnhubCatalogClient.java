package com.investments.tracker.infrastructure.external.finnhub;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.investments.tracker.domain.model.Instrument;
import com.investments.tracker.domain.model.value.Currency;
import com.investments.tracker.domain.model.value.InstrumentName;
import com.investments.tracker.domain.model.value.InstrumentSymbol;
import com.investments.tracker.domain.model.value.InstrumentType;
import com.investments.tracker.domain.model.value.Market;
import com.investments.tracker.domain.repository.InstrumentCatalogProvider;

/** Finnhub API client that fetches the US instrument catalog. */
@Component
public class FinnhubCatalogClient implements InstrumentCatalogProvider {

    private static final Logger log = LoggerFactory.getLogger(FinnhubCatalogClient.class);

    private static final Map<String, InstrumentType> TYPE_MAPPING =
            Map.of(
                    "Common Stock", InstrumentType.STOCK,
                    "ADR", InstrumentType.STOCK,
                    "REIT", InstrumentType.STOCK,
                    "ETP", InstrumentType.ETF);

    private final RestClient restClient;
    private final String symbolsPath;
    private final String apiKey;

    public FinnhubCatalogClient(
            RestClient finnhubRestClient,
            @Value("${app.finnhub.symbols-path}") String symbolsPath,
            @Value("${app.finnhub.api-key}") String apiKey) {
        this.restClient = finnhubRestClient;
        this.symbolsPath = symbolsPath;
        this.apiKey = apiKey;
    }

    @Override
    public Collection<Instrument> fetchUsStockCatalog() {
        log.info("Fetching US stock symbols from Finnhub");

        FinnhubSymbolResponse[] responses =
                restClient
                        .get()
                        .uri(
                                uriBuilder ->
                                        uriBuilder
                                                .path(symbolsPath)
                                                .queryParam("exchange", "US")
                                                .queryParam("token", apiKey)
                                                .build())
                        .retrieve()
                        .body(FinnhubSymbolResponse[].class);

        if (responses == null) {
            log.warn("Finnhub returned null response");
            return List.of();
        }

        List<Instrument> instruments =
                Arrays.stream(responses)
                        .filter(r -> "USD".equals(r.currency()))
                        .filter(r -> TYPE_MAPPING.containsKey(r.type()))
                        .filter(r -> isValidSymbol(r.symbol()))
                        .map(this::toDomainInstrument)
                        .toList();

        log.info(
                "Fetched {} instruments from Finnhub ({} total symbols received)",
                instruments.size(),
                responses.length);

        return instruments;
    }

    private Instrument toDomainInstrument(FinnhubSymbolResponse response) {
        return new Instrument(
                new InstrumentSymbol(response.symbol()),
                new InstrumentName(response.description()),
                TYPE_MAPPING.get(response.type()),
                Currency.USD,
                Market.US);
    }

    private boolean isValidSymbol(String symbol) {
        try {
            new InstrumentSymbol(symbol);
            return true;
        } catch (Exception e) {
            log.debug("Skipping invalid symbol from Finnhub: {}", symbol);
            return false;
        }
    }
}
