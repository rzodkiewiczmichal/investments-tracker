package com.investments.tracker.application.usecase;

import com.investments.tracker.domain.model.AccountHolding;
import com.investments.tracker.domain.model.Portfolio;
import com.investments.tracker.domain.model.Position;
import com.investments.tracker.domain.model.value.AccountId;
import com.investments.tracker.domain.model.value.CostBasis;
import com.investments.tracker.domain.model.value.InstrumentSymbol;
import com.investments.tracker.domain.model.value.Money;
import com.investments.tracker.domain.model.value.Price;
import com.investments.tracker.domain.model.value.Quantity;
import com.investments.tracker.domain.repository.PositionRepository;
import com.investments.tracker.domain.service.PortfolioCalculationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@DisplayName("PortfolioQueryUseCaseService")
@ExtendWith(MockitoExtension.class)
class PortfolioQueryUseCaseServiceTest {

    @Mock
    private PositionRepository positionRepository;

    private PortfolioCalculationService portfolioCalculationService;
    private PortfolioQueryUseCaseService portfolioQueryUseCaseService;

    @BeforeEach
    void setUp() {
        portfolioCalculationService = new PortfolioCalculationService();
        portfolioQueryUseCaseService = new PortfolioQueryUseCaseService(
                positionRepository, portfolioCalculationService);
    }

    @Nested
    @DisplayName("getPortfolio")
    class GetPortfolio {

        @Test
        @DisplayName("should return empty portfolio when no positions")
        void shouldReturnEmptyPortfolioWhenNoPositions() {
            // Given
            when(positionRepository.findAll()).thenReturn(List.of());

            // When
            Portfolio portfolio = portfolioQueryUseCaseService.getPortfolio();

            // Then
            assertThat(portfolio.positions()).isEmpty();
            assertThat(portfolio.metrics().totalPositions()).isZero();
            assertThat(portfolio.metrics().totalCurrentValue().money().amount())
                    .isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("should return portfolio with positions")
        void shouldReturnPortfolioWithPositions() {
            // Given
            Position position = createPosition("AAPL", 100, "150.00", "175.00");
            when(positionRepository.findAll()).thenReturn(List.of(position));

            // When
            Portfolio portfolio = portfolioQueryUseCaseService.getPortfolio();

            // Then
            assertThat(portfolio.positions()).hasSize(1);
            assertThat(portfolio.metrics().totalPositions()).isEqualTo(1);
            assertThat(portfolio.metrics().totalCurrentValue().money().amount()).isPositive();
            assertThat(portfolio.metrics().totalInvestedAmount().money().amount()).isPositive();
        }
    }

    private Position createPosition(String symbol, int qty, String costBasis, String price) {
        return new Position(
                InstrumentSymbol.of(symbol),
                List.of(new AccountHolding(
                        new AccountId(1L),
                        Quantity.of(qty),
                        CostBasis.of(Money.pln(costBasis)))),
                new Price(Money.pln(price)));
    }
}
