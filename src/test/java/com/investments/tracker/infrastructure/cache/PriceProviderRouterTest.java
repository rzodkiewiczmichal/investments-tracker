package com.investments.tracker.infrastructure.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.investments.tracker.domain.model.Instrument;
import com.investments.tracker.domain.model.value.Currency;
import com.investments.tracker.domain.model.value.InstrumentName;
import com.investments.tracker.domain.model.value.InstrumentSymbol;
import com.investments.tracker.domain.model.value.InstrumentType;
import com.investments.tracker.domain.model.value.Market;
import com.investments.tracker.domain.model.value.Money;
import com.investments.tracker.domain.model.value.Price;
import com.investments.tracker.domain.repository.InstrumentRepository;
import com.investments.tracker.infrastructure.external.finnhub.FinnhubPriceClient;
import com.investments.tracker.infrastructure.external.stooq.StooqPriceClient;

@DisplayName("PriceProviderRouter")
@ExtendWith(MockitoExtension.class)
class PriceProviderRouterTest {

    @Mock private StooqPriceClient stooqClient;
    @Mock private FinnhubPriceClient finnhubClient;
    @Mock private InstrumentRepository instrumentRepository;

    @InjectMocks private PriceProviderRouter router;

    @Test
    @DisplayName("should route GPW instrument to Stooq")
    void shouldRouteGpwInstrumentToStooq() {
        InstrumentSymbol symbol = InstrumentSymbol.of("PKO");
        Price price = new Price(Money.pln("42.50"));
        when(instrumentRepository.findBySymbol(symbol))
                .thenReturn(Optional.of(gpwInstrument("PKO")));
        when(stooqClient.fetchPrice(symbol)).thenReturn(Optional.of(price));

        Optional<Price> result = router.fetchPrice(symbol);

        assertThat(result).contains(price);
        verifyNoInteractions(finnhubClient);
    }

    @Test
    @DisplayName("should route US instrument to Finnhub")
    void shouldRouteUsInstrumentToFinnhub() {
        InstrumentSymbol symbol = InstrumentSymbol.of("AAPL");
        Price price = new Price(new Money(new java.math.BigDecimal("185.50"), Currency.USD));
        when(instrumentRepository.findBySymbol(symbol))
                .thenReturn(Optional.of(usInstrument("AAPL")));
        when(finnhubClient.fetchPrice(symbol)).thenReturn(Optional.of(price));

        Optional<Price> result = router.fetchPrice(symbol);

        assertThat(result).contains(price);
        verifyNoInteractions(stooqClient);
    }

    @Test
    @DisplayName("should return empty for unknown instrument")
    void shouldReturnEmptyForUnknownInstrument() {
        InstrumentSymbol symbol = InstrumentSymbol.of("UNKNOWN");
        when(instrumentRepository.findBySymbol(symbol)).thenReturn(Optional.empty());

        Optional<Price> result = router.fetchPrice(symbol);

        assertThat(result).isEmpty();
        verifyNoInteractions(stooqClient);
        verifyNoInteractions(finnhubClient);
    }

    @Test
    @DisplayName("should group mixed instruments by market")
    void shouldGroupMixedInstrumentsByMarket() {
        InstrumentSymbol pko = InstrumentSymbol.of("PKO");
        InstrumentSymbol cdr = InstrumentSymbol.of("CDR");
        InstrumentSymbol aapl = InstrumentSymbol.of("AAPL");
        InstrumentSymbol voo = InstrumentSymbol.of("VOO");

        when(instrumentRepository.findBySymbol(pko)).thenReturn(Optional.of(gpwInstrument("PKO")));
        when(instrumentRepository.findBySymbol(cdr)).thenReturn(Optional.of(gpwInstrument("CDR")));
        when(instrumentRepository.findBySymbol(aapl)).thenReturn(Optional.of(usInstrument("AAPL")));
        when(instrumentRepository.findBySymbol(voo)).thenReturn(Optional.of(usInstrument("VOO")));

        Price pkoPrice = new Price(Money.pln("42.50"));
        Price cdrPrice = new Price(Money.pln("120.00"));
        Price aaplPrice = new Price(new Money(new java.math.BigDecimal("185.50"), Currency.USD));
        Price vooPrice = new Price(new Money(new java.math.BigDecimal("480.20"), Currency.USD));

        when(stooqClient.fetchPrices(List.of(pko, cdr)))
                .thenReturn(Map.of(pko, pkoPrice, cdr, cdrPrice));
        when(finnhubClient.fetchPrices(List.of(aapl, voo)))
                .thenReturn(Map.of(aapl, aaplPrice, voo, vooPrice));

        Collection<InstrumentSymbol> symbols = List.of(pko, cdr, aapl, voo);
        Map<InstrumentSymbol, Price> result = router.fetchPrices(symbols);

        assertThat(result).hasSize(4);
        assertThat(result.get(pko)).isEqualTo(pkoPrice);
        assertThat(result.get(cdr)).isEqualTo(cdrPrice);
        assertThat(result.get(aapl)).isEqualTo(aaplPrice);
        assertThat(result.get(voo)).isEqualTo(vooPrice);
    }

    @Test
    @DisplayName("should return empty map for empty symbol list")
    void shouldReturnEmptyMapForEmptyList() {
        Map<InstrumentSymbol, Price> result = router.fetchPrices(List.of());

        assertThat(result).isEmpty();
        verifyNoInteractions(stooqClient);
        verifyNoInteractions(finnhubClient);
        verifyNoInteractions(instrumentRepository);
    }

    @Test
    @DisplayName("should merge results from both providers")
    void shouldMergeResultsFromBothProviders() {
        InstrumentSymbol pko = InstrumentSymbol.of("PKO");
        InstrumentSymbol aapl = InstrumentSymbol.of("AAPL");

        when(instrumentRepository.findBySymbol(pko)).thenReturn(Optional.of(gpwInstrument("PKO")));
        when(instrumentRepository.findBySymbol(aapl)).thenReturn(Optional.of(usInstrument("AAPL")));

        Price pkoPrice = new Price(Money.pln("42.50"));
        Price aaplPrice = new Price(new Money(new java.math.BigDecimal("185.50"), Currency.USD));

        when(stooqClient.fetchPrices(List.of(pko))).thenReturn(Map.of(pko, pkoPrice));
        when(finnhubClient.fetchPrices(List.of(aapl))).thenReturn(Map.of(aapl, aaplPrice));

        Map<InstrumentSymbol, Price> result = router.fetchPrices(List.of(pko, aapl));

        assertThat(result).hasSize(2);
        assertThat(result).containsEntry(pko, pkoPrice);
        assertThat(result).containsEntry(aapl, aaplPrice);
    }

    @Test
    @DisplayName("should skip unknown instruments in batch fetch")
    void shouldSkipUnknownInstrumentsInBatchFetch() {
        InstrumentSymbol pko = InstrumentSymbol.of("PKO");
        InstrumentSymbol unknown = InstrumentSymbol.of("UNKNOWN");

        when(instrumentRepository.findBySymbol(pko)).thenReturn(Optional.of(gpwInstrument("PKO")));
        when(instrumentRepository.findBySymbol(unknown)).thenReturn(Optional.empty());

        Price pkoPrice = new Price(Money.pln("42.50"));
        when(stooqClient.fetchPrices(List.of(pko))).thenReturn(Map.of(pko, pkoPrice));

        Map<InstrumentSymbol, Price> result = router.fetchPrices(List.of(pko, unknown));

        assertThat(result).hasSize(1);
        assertThat(result).containsEntry(pko, pkoPrice);
        verifyNoInteractions(finnhubClient);
    }

    private Instrument gpwInstrument(String symbol) {
        return new Instrument(
                new InstrumentSymbol(symbol),
                new InstrumentName(symbol),
                InstrumentType.STOCK,
                Currency.PLN,
                Market.GPW);
    }

    private Instrument usInstrument(String symbol) {
        return new Instrument(
                new InstrumentSymbol(symbol),
                new InstrumentName(symbol),
                InstrumentType.STOCK,
                Currency.USD,
                Market.US);
    }
}
