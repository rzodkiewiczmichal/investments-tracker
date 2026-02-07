package com.investments.tracker.application.dto.mapper;

import com.investments.tracker.application.dto.response.PortfolioSummaryResponse;
import com.investments.tracker.domain.model.AccountHolding;
import com.investments.tracker.domain.model.Portfolio;
import com.investments.tracker.domain.model.Position;
import com.investments.tracker.domain.model.value.AccountId;
import com.investments.tracker.domain.model.value.CostBasis;
import com.investments.tracker.domain.model.value.InstrumentSymbol;
import com.investments.tracker.domain.model.value.Money;
import com.investments.tracker.domain.model.value.Price;
import com.investments.tracker.domain.model.value.Quantity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PortfolioMapper")
class PortfolioMapperTest {

    private final PortfolioMapper mapper = new PortfolioMapper();

    @Nested
    @DisplayName("toSummaryResponse")
    class ToSummaryResponse {

        @Test
        @DisplayName("should map empty portfolio")
        void shouldMapEmptyPortfolio() {
            // Given
            Portfolio portfolio = Portfolio.fromPositions(List.of());

            // When
            PortfolioSummaryResponse response = mapper.toSummaryResponse(portfolio);

            // Then
            assertThat(response.positionsCount()).isZero();
            assertThat(response.totalCurrentValue().amount()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(response.totalInvestedAmount().amount()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(response.totalProfitLoss().amount()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(response.totalReturnPercentage()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(response.message()).isEqualTo("No positions yet");
        }

        @Test
        @DisplayName("should map portfolio with positions")
        void shouldMapPortfolioWithPositions() {
            // Given
            Position position = createPosition("AAPL", 100, "150.00", "175.00");
            Portfolio portfolio = Portfolio.fromPositions(List.of(position));

            // When
            PortfolioSummaryResponse response = mapper.toSummaryResponse(portfolio);

            // Then
            assertThat(response.positionsCount()).isEqualTo(1);
            assertThat(response.totalCurrentValue().amount()).isPositive();
            assertThat(response.totalCurrentValue().currency()).isEqualTo("PLN");
            assertThat(response.totalInvestedAmount().amount()).isPositive();
            assertThat(response.totalProfitLoss().amount()).isPositive();
            assertThat(response.message()).isNull();
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
