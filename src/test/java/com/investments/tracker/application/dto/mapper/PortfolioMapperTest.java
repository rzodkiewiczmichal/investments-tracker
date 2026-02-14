package com.investments.tracker.application.dto.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.investments.tracker.application.dto.response.PortfolioSummaryResponse;
import com.investments.tracker.domain.model.AccountHolding;
import com.investments.tracker.domain.model.Portfolio;
import com.investments.tracker.domain.model.PortfolioMetrics;
import com.investments.tracker.domain.model.Position;
import com.investments.tracker.domain.model.value.AccountId;
import com.investments.tracker.domain.model.value.CostBasis;
import com.investments.tracker.domain.model.value.CurrentValue;
import com.investments.tracker.domain.model.value.InstrumentSymbol;
import com.investments.tracker.domain.model.value.InvestedAmount;
import com.investments.tracker.domain.model.value.Money;
import com.investments.tracker.domain.model.value.ProfitAndLoss;
import com.investments.tracker.domain.model.value.Quantity;

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
            Portfolio portfolio = new Portfolio(List.of(), PortfolioMetrics.empty());

            // When
            PortfolioSummaryResponse response = mapper.toSummaryResponse(portfolio);

            // Then
            assertThat(response.positionsCount()).isZero();
            assertThat(response.totalCurrentValue()).isNull();
            assertThat(response.totalInvestedAmount().amount())
                    .isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(response.totalProfitLoss().amount()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(response.totalReturnPercentage()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(response.message()).isEqualTo("No positions yet");
        }

        @Test
        @DisplayName("should map portfolio with positions and prices")
        void shouldMapPortfolioWithPositionsAndPrices() {
            // Given
            Position position = createPosition("AAPL", 100, "150.00");
            PortfolioMetrics metrics =
                    new PortfolioMetrics(
                            new InvestedAmount(Money.pln("15000")),
                            new CurrentValue(Money.pln("17500")),
                            ProfitAndLoss.calculate(
                                    new CurrentValue(Money.pln("17500")),
                                    new InvestedAmount(Money.pln("15000"))),
                            1);
            Portfolio portfolio = new Portfolio(List.of(position), metrics);

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

        @Test
        @DisplayName("should map portfolio with null current value when prices unknown")
        void shouldMapPortfolioWithNullCurrentValueWhenPricesUnknown() {
            // Given
            Position position = createPosition("AAPL", 100, "150.00");
            PortfolioMetrics metrics =
                    new PortfolioMetrics(new InvestedAmount(Money.pln("15000")), null, null, 1);
            Portfolio portfolio = new Portfolio(List.of(position), metrics);

            // When
            PortfolioSummaryResponse response = mapper.toSummaryResponse(portfolio);

            // Then
            assertThat(response.positionsCount()).isEqualTo(1);
            assertThat(response.totalCurrentValue()).isNull();
            assertThat(response.totalInvestedAmount().amount()).isPositive();
            assertThat(response.totalProfitLoss().amount()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(response.totalReturnPercentage()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(response.message()).isNull();
        }
    }

    private Position createPosition(String symbol, int qty, String costBasis) {
        return new Position(
                InstrumentSymbol.of(symbol),
                List.of(
                        new AccountHolding(
                                new AccountId(1L),
                                Quantity.of(qty),
                                CostBasis.of(Money.pln(costBasis)))));
    }
}
