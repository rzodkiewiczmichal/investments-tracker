package com.investments.tracker.application.usecase;

import com.investments.tracker.application.exception.ResourceNotFoundException;
import com.investments.tracker.domain.model.Instrument;
import com.investments.tracker.domain.model.value.InstrumentName;
import com.investments.tracker.domain.model.value.InstrumentSymbol;
import com.investments.tracker.domain.model.value.InstrumentType;
import com.investments.tracker.domain.model.value.Money;
import com.investments.tracker.domain.model.value.Price;
import com.investments.tracker.domain.repository.InstrumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@DisplayName("InstrumentQueryUseCaseService")
@ExtendWith(MockitoExtension.class)
class InstrumentQueryUseCaseServiceTest {

    @Mock
    private InstrumentRepository instrumentRepository;

    private InstrumentQueryUseCaseService instrumentQueryUseCaseService;

    @BeforeEach
    void setUp() {
        instrumentQueryUseCaseService = new InstrumentQueryUseCaseService(instrumentRepository);
    }

    @Nested
    @DisplayName("getInstrument")
    class GetInstrument {

        @Test
        @DisplayName("should return instrument when found")
        void shouldReturnInstrumentWhenFound() {
            // Given
            Instrument instrument = createInstrument("AAPL", "Apple Inc.", "STOCK", "175.00");
            when(instrumentRepository.findBySymbol(any())).thenReturn(Optional.of(instrument));

            // When
            Instrument result = instrumentQueryUseCaseService.getInstrument(InstrumentSymbol.of("AAPL"));

            // Then
            assertThat(result.symbol().value()).isEqualTo("AAPL");
            assertThat(result.name().value()).isEqualTo("Apple Inc.");
            assertThat(result.type()).isEqualTo(InstrumentType.STOCK);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when instrument not found")
        void shouldThrowResourceNotFoundExceptionWhenInstrumentNotFound() {
            // Given
            when(instrumentRepository.findBySymbol(any())).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> instrumentQueryUseCaseService.getInstrument(InstrumentSymbol.of("UNKNOWN")))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Instrument")
                    .hasMessageContaining("UNKNOWN");
        }

        @Test
        @DisplayName("should throw NullPointerException when symbol is null")
        void shouldThrowNullPointerExceptionWhenSymbolIsNull() {
            // When/Then
            assertThatThrownBy(() -> instrumentQueryUseCaseService.getInstrument(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("symbol");
        }
    }

    @Nested
    @DisplayName("listInstruments")
    class ListInstruments {

        @Test
        @DisplayName("should return empty collection when no instruments")
        void shouldReturnEmptyCollectionWhenNoInstruments() {
            // Given
            when(instrumentRepository.findAll()).thenReturn(List.of());

            // When
            Collection<Instrument> instruments = instrumentQueryUseCaseService.listInstruments();

            // Then
            assertThat(instruments).isEmpty();
        }

        @Test
        @DisplayName("should return all instruments")
        void shouldReturnAllInstruments() {
            // Given
            Instrument instrument1 = createInstrument("AAPL", "Apple Inc.", "STOCK", "175.00");
            Instrument instrument2 = createInstrument("MSFT", "Microsoft Corp.", "STOCK", "350.00");
            when(instrumentRepository.findAll()).thenReturn(List.of(instrument1, instrument2));

            // When
            Collection<Instrument> instruments = instrumentQueryUseCaseService.listInstruments();

            // Then
            assertThat(instruments).hasSize(2);
            assertThat(instruments).containsExactlyInAnyOrder(instrument1, instrument2);
        }
    }

    private Instrument createInstrument(String symbol, String name, String type, String price) {
        return new Instrument(
                InstrumentSymbol.of(symbol),
                new InstrumentName(name),
                InstrumentType.valueOf(type),
                new Price(Money.pln(price)));
    }
}
