package com.investments.tracker.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.investments.tracker.domain.model.Instrument;
import com.investments.tracker.domain.model.value.CatalogSyncResult;
import com.investments.tracker.domain.model.value.Currency;
import com.investments.tracker.domain.model.value.InstrumentName;
import com.investments.tracker.domain.model.value.InstrumentSymbol;
import com.investments.tracker.domain.model.value.InstrumentType;
import com.investments.tracker.domain.repository.InstrumentCatalogProvider;
import com.investments.tracker.domain.repository.InstrumentRepository;

@DisplayName("InstrumentSyncUseCaseService")
@ExtendWith(MockitoExtension.class)
class InstrumentSyncUseCaseServiceTest {

    @Mock private InstrumentCatalogProvider catalogProvider;
    @Mock private InstrumentRepository instrumentRepository;
    @Captor private ArgumentCaptor<Collection<Instrument>> saveAllCaptor;

    private InstrumentSyncUseCaseService service;

    @BeforeEach
    void setUp() {
        service = new InstrumentSyncUseCaseService(catalogProvider, instrumentRepository);
    }

    @Test
    @DisplayName("should create all instruments when catalog is empty")
    void shouldCreateAllWhenEmpty() {
        Instrument aapl = usdInstrument("AAPL", "APPLE INC");
        Instrument msft = usdInstrument("MSFT", "MICROSOFT CORP");
        when(catalogProvider.fetchUsStockCatalog()).thenReturn(List.of(aapl, msft));
        when(instrumentRepository.findAll()).thenReturn(List.of());
        when(instrumentRepository.saveAll(anyCollection())).thenReturn(List.of(aapl, msft));

        CatalogSyncResult result = service.syncUsInstruments();

        assertThat(result.created()).isEqualTo(2);
        assertThat(result.updated()).isZero();
        assertThat(result.skipped()).isZero();
        verify(instrumentRepository).saveAll(saveAllCaptor.capture());
        assertThat(saveAllCaptor.getValue()).containsExactlyInAnyOrder(aapl, msft);
    }

    @Test
    @DisplayName("should update existing USD instruments")
    void shouldUpdateExistingUsd() {
        Instrument existingAapl = usdInstrument("AAPL", "APPLE INC OLD");
        Instrument fetchedAapl = usdInstrument("AAPL", "APPLE INC");
        when(catalogProvider.fetchUsStockCatalog()).thenReturn(List.of(fetchedAapl));
        when(instrumentRepository.findAll()).thenReturn(List.of(existingAapl));
        when(instrumentRepository.saveAll(anyCollection())).thenReturn(List.of(fetchedAapl));

        CatalogSyncResult result = service.syncUsInstruments();

        assertThat(result.created()).isZero();
        assertThat(result.updated()).isEqualTo(1);
        assertThat(result.skipped()).isZero();
    }

    @Test
    @DisplayName("should skip symbols that exist with non-USD currency")
    void shouldSkipNonUsdCollision() {
        Instrument existingPko = plnInstrument("PKO", "PKO BANK POLSKI");
        Instrument fetchedPko = usdInstrument("PKO", "PARK OHIO HOLDINGS CORP");
        when(catalogProvider.fetchUsStockCatalog()).thenReturn(List.of(fetchedPko));
        when(instrumentRepository.findAll()).thenReturn(List.of(existingPko));
        when(instrumentRepository.saveAll(anyCollection())).thenReturn(List.of());

        CatalogSyncResult result = service.syncUsInstruments();

        assertThat(result.created()).isZero();
        assertThat(result.updated()).isZero();
        assertThat(result.skipped()).isEqualTo(1);
        verify(instrumentRepository).saveAll(saveAllCaptor.capture());
        assertThat(saveAllCaptor.getValue()).isEmpty();
    }

    @Test
    @DisplayName("should handle mixed create, update, and skip")
    void shouldHandleMixed() {
        Instrument existingPko = plnInstrument("PKO", "PKO BANK POLSKI");
        Instrument existingAapl = usdInstrument("AAPL", "APPLE INC OLD");

        Instrument fetchedAapl = usdInstrument("AAPL", "APPLE INC");
        Instrument fetchedPko = usdInstrument("PKO", "PARK OHIO");
        Instrument fetchedMsft = usdInstrument("MSFT", "MICROSOFT CORP");

        when(catalogProvider.fetchUsStockCatalog())
                .thenReturn(List.of(fetchedAapl, fetchedPko, fetchedMsft));
        when(instrumentRepository.findAll()).thenReturn(List.of(existingPko, existingAapl));
        when(instrumentRepository.saveAll(anyCollection()))
                .thenReturn(List.of(fetchedAapl, fetchedMsft));

        CatalogSyncResult result = service.syncUsInstruments();

        assertThat(result.created()).isEqualTo(1);
        assertThat(result.updated()).isEqualTo(1);
        assertThat(result.skipped()).isEqualTo(1);
        verify(instrumentRepository).saveAll(saveAllCaptor.capture());
        assertThat(saveAllCaptor.getValue())
                .extracting(i -> i.symbol().value())
                .containsExactlyInAnyOrder("AAPL", "MSFT");
    }

    @Test
    @DisplayName("should handle empty catalog from provider")
    void shouldHandleEmptyCatalog() {
        when(catalogProvider.fetchUsStockCatalog()).thenReturn(List.of());
        when(instrumentRepository.findAll()).thenReturn(List.of());
        when(instrumentRepository.saveAll(anyCollection())).thenReturn(List.of());

        CatalogSyncResult result = service.syncUsInstruments();

        assertThat(result.created()).isZero();
        assertThat(result.updated()).isZero();
        assertThat(result.skipped()).isZero();
    }

    private Instrument usdInstrument(String symbol, String name) {
        return new Instrument(
                InstrumentSymbol.of(symbol),
                new InstrumentName(name),
                InstrumentType.STOCK,
                Currency.USD);
    }

    private Instrument plnInstrument(String symbol, String name) {
        return new Instrument(
                InstrumentSymbol.of(symbol),
                new InstrumentName(name),
                InstrumentType.STOCK,
                Currency.PLN);
    }
}
