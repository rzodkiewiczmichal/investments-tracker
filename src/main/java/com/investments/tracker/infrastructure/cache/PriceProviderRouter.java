package com.investments.tracker.infrastructure.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import com.investments.tracker.domain.model.Instrument;
import com.investments.tracker.domain.model.value.InstrumentSymbol;
import com.investments.tracker.domain.model.value.Market;
import com.investments.tracker.domain.model.value.Price;
import com.investments.tracker.domain.repository.InstrumentRepository;
import com.investments.tracker.infrastructure.external.finnhub.FinnhubPriceClient;
import com.investments.tracker.infrastructure.external.stooq.StooqPriceClient;

/**
 * Routes price fetching to the correct provider based on instrument market.
 *
 * <p>Groups requested symbols by their {@link Market} and dispatches to the appropriate client:
 * {@link StooqPriceClient} for GPW and UK instruments, {@link FinnhubPriceClient} for US
 * instruments.
 *
 * @see <a href="../../docs/adr/ADR-031-instrument-price-providers.md">ADR-031</a>
 */
@Component
public class PriceProviderRouter {

    private static final Logger log = LoggerFactory.getLogger(PriceProviderRouter.class);

    private final StooqPriceClient stooqClient;
    private final FinnhubPriceClient finnhubClient;
    private final InstrumentRepository instrumentRepository;

    public PriceProviderRouter(
            StooqPriceClient stooqClient,
            FinnhubPriceClient finnhubClient,
            InstrumentRepository instrumentRepository) {
        this.stooqClient = stooqClient;
        this.finnhubClient = finnhubClient;
        this.instrumentRepository = instrumentRepository;
    }

    /**
     * Fetches the current price for a single instrument, routing to the correct provider.
     *
     * @param symbol the instrument symbol
     * @return the current price, or empty if unavailable or instrument unknown
     */
    public Optional<Price> fetchPrice(InstrumentSymbol symbol) {
        Optional<Instrument> instrument = instrumentRepository.findBySymbol(symbol);
        if (instrument.isEmpty()) {
            log.warn("Unknown instrument symbol for price routing: {}", symbol.value());
            return Optional.empty();
        }

        Instrument inst = instrument.get();
        try {
            return switch (inst.market()) {
                case GPW -> stooqClient.fetchPrice(symbol);
                case US -> finnhubClient.fetchPrice(symbol);
                case UK -> stooqClient.fetchPrice(symbol, inst.currency());
                case DE -> {
                    log.debug("No price provider for DE market yet");
                    yield Optional.empty();
                }
            };
        } catch (RestClientException e) {
            log.warn("Failed to fetch price for {}: {}", symbol.value(), e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Fetches prices for multiple instruments, grouping by market and dispatching to the correct
     * provider.
     *
     * @param symbols the instrument symbols to fetch
     * @return map of symbol to price (symbols with no data are omitted)
     */
    public Map<InstrumentSymbol, Price> fetchPrices(Collection<InstrumentSymbol> symbols) {
        if (symbols.isEmpty()) {
            return Map.of();
        }

        List<InstrumentSymbol> gpwSymbols = new ArrayList<>();
        List<InstrumentSymbol> usSymbols = new ArrayList<>();
        List<Instrument> ukInstruments = new ArrayList<>();

        for (InstrumentSymbol symbol : symbols) {
            instrumentRepository
                    .findBySymbol(symbol)
                    .ifPresentOrElse(
                            instrument -> {
                                switch (instrument.market()) {
                                    case GPW -> gpwSymbols.add(symbol);
                                    case US -> usSymbols.add(symbol);
                                    case UK -> ukInstruments.add(instrument);
                                    case DE ->
                                            log.debug(
                                                    "Skipping {} -- no price provider for DE market",
                                                    symbol.value());
                                }
                            },
                            () ->
                                    log.warn(
                                            "Unknown instrument symbol for price routing: {}",
                                            symbol.value()));
        }

        Map<InstrumentSymbol, Price> result = new HashMap<>();

        if (!gpwSymbols.isEmpty()) {
            try {
                result.putAll(stooqClient.fetchPrices(gpwSymbols));
            } catch (RestClientException e) {
                log.warn("Failed to fetch GPW prices from Stooq: {}", e.getMessage());
            }
        }
        if (!usSymbols.isEmpty()) {
            try {
                result.putAll(finnhubClient.fetchPrices(usSymbols));
            } catch (RestClientException e) {
                log.warn("Failed to fetch US prices from Finnhub: {}", e.getMessage());
            }
        }
        // UK instruments must be fetched individually (Stooq batch returns N/D for UK)
        for (Instrument ukInstrument : ukInstruments) {
            try {
                stooqClient
                        .fetchPrice(ukInstrument.symbol(), ukInstrument.currency())
                        .ifPresent(price -> result.put(ukInstrument.symbol(), price));
            } catch (RestClientException e) {
                log.warn(
                        "Failed to fetch UK price for {}: {}",
                        ukInstrument.symbol().value(),
                        e.getMessage());
            }
        }

        return Map.copyOf(result);
    }
}
