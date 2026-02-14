package com.investments.tracker.application.usecase;

import com.investments.tracker.application.exception.ResourceNotFoundException;
import com.investments.tracker.domain.model.AccountHolding;
import com.investments.tracker.domain.model.Position;
import com.investments.tracker.domain.model.value.AccountId;
import com.investments.tracker.domain.model.value.CostBasis;
import com.investments.tracker.domain.model.value.InstrumentSymbol;
import com.investments.tracker.domain.model.value.Money;
import com.investments.tracker.domain.model.value.Quantity;
import com.investments.tracker.domain.repository.CurrentPriceProvider;
import com.investments.tracker.domain.repository.ExchangeRateProvider;
import com.investments.tracker.domain.repository.PositionRepository;
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

@DisplayName("PositionQueryUseCaseService")
@ExtendWith(MockitoExtension.class)
class PositionQueryUseCaseServiceTest {

    @Mock
    private PositionRepository positionRepository;

    @Mock
    private InstrumentQueryUseCase instrumentQueryUseCase;

    @Mock
    private AccountQueryUseCase accountQueryUseCase;

    @Mock
    private CurrentPriceProvider currentPriceProvider;

    @Mock
    private ExchangeRateProvider exchangeRateProvider;

    private PositionQueryUseCaseService positionQueryUseCaseService;

    @BeforeEach
    void setUp() {
        positionQueryUseCaseService = new PositionQueryUseCaseService(
                positionRepository, instrumentQueryUseCase, accountQueryUseCase,
                currentPriceProvider, exchangeRateProvider);
    }

    @Nested
    @DisplayName("listPositions")
    class ListPositions {

        @Test
        @DisplayName("should return empty collection when no positions")
        void shouldReturnEmptyCollectionWhenNoPositions() {
            // Given
            when(positionRepository.findAll()).thenReturn(List.of());

            // When
            Collection<Position> positions = positionQueryUseCaseService.listPositions();

            // Then
            assertThat(positions).isEmpty();
        }

        @Test
        @DisplayName("should return all positions")
        void shouldReturnAllPositions() {
            // Given
            Position position1 = createPosition("AAPL", 10, "150.00");
            Position position2 = createPosition("MSFT", 100, "300.00");
            when(positionRepository.findAll()).thenReturn(List.of(position1, position2));

            // When
            Collection<Position> positions = positionQueryUseCaseService.listPositions();

            // Then
            assertThat(positions).hasSize(2);
            assertThat(positions).containsExactlyInAnyOrder(position1, position2);
        }
    }

    @Nested
    @DisplayName("getPosition")
    class GetPosition {

        @Test
        @DisplayName("should return position when found")
        void shouldReturnPositionWhenFound() {
            // Given
            Position position = createPosition("AAPL", 100, "150.00");
            when(positionRepository.findBySymbol(any())).thenReturn(Optional.of(position));

            // When
            Position result = positionQueryUseCaseService.getPosition(InstrumentSymbol.of("AAPL"));

            // Then
            assertThat(result.symbol().value()).isEqualTo("AAPL");
            assertThat(result.calculateTotalQuantity().value()).isEqualByComparingTo("100");
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when position not found")
        void shouldThrowResourceNotFoundExceptionWhenPositionNotFound() {
            // Given
            when(positionRepository.findBySymbol(any())).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> positionQueryUseCaseService.getPosition(InstrumentSymbol.of("UNKNOWN")))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Position")
                    .hasMessageContaining("UNKNOWN");
        }
    }

    private Position createPosition(String symbol, int qty, String costBasis) {
        return new Position(
                InstrumentSymbol.of(symbol),
                List.of(new AccountHolding(
                        new AccountId(1L),
                        Quantity.of(qty),
                        CostBasis.of(Money.pln(costBasis)))));
    }
}
