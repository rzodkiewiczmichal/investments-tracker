package com.investments.tracker.domain.service;

import com.investments.tracker.domain.model.AccountHolding;
import com.investments.tracker.domain.model.Portfolio;
import com.investments.tracker.domain.model.PortfolioMetrics;
import com.investments.tracker.domain.model.Position;
import com.investments.tracker.domain.model.value.AccountId;
import com.investments.tracker.domain.model.value.CostBasis;
import com.investments.tracker.domain.model.value.CurrentValue;
import com.investments.tracker.domain.model.value.InstrumentSymbol;
import com.investments.tracker.domain.model.value.InvestedAmount;
import com.investments.tracker.domain.model.value.Price;
import com.investments.tracker.domain.model.value.ProfitAndLoss;
import com.investments.tracker.domain.model.value.Quantity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PortfolioCalculationService")
class PortfolioCalculationServiceTest {

    private PortfolioCalculationService service;

    @BeforeEach
    void setUp() {
        service = new PortfolioCalculationService();
    }

    @Nested
    @DisplayName("portfolio creation")
    class PortfolioCreation {

        @Test
        @DisplayName("creates empty portfolio from empty list")
        void createsEmptyPortfolio() {
            Portfolio portfolio = service.createPortfolio(List.of());

            assertThat(portfolio.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("creates portfolio from positions")
        void createsPortfolioFromPositions() {
            List<Position> positions = List.of(
                    createPosition("AAPL", 100, "500", "550"),
                    createPosition("MSFT", 50, "300", "320"));

            Portfolio portfolio = service.createPortfolio(positions);

            assertThat(portfolio.getPositionCount()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("total invested amount calculation")
    class TotalInvestedAmount {

        @Test
        @DisplayName("returns empty for empty positions list")
        void returnsEmptyForEmptyList() {
            Optional<InvestedAmount> result = service.calculateTotalInvestedAmount(List.of());

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("calculates total invested amount")
        void calculatesTotalInvestedAmount() {
            List<Position> positions = List.of(
                    createPosition("AAPL", 100, "500", "550"), // 100 * 500 = 50000
                    createPosition("MSFT", 50, "300", "320"));  // 50 * 300 = 15000

            Optional<InvestedAmount> result = service.calculateTotalInvestedAmount(positions);

            assertThat(result).isPresent();
            assertThat(result.get().money().amount()).isEqualByComparingTo("65000");
        }
    }

    @Nested
    @DisplayName("total current value calculation")
    class TotalCurrentValue {

        @Test
        @DisplayName("returns zero for empty positions list")
        void returnsZeroForEmptyList() {
            CurrentValue result = service.calculateTotalCurrentValue(List.of());

            assertThat(result.isZero()).isTrue();
        }

        @Test
        @DisplayName("calculates total current value")
        void calculatesTotalCurrentValue() {
            List<Position> positions = List.of(
                    createPosition("AAPL", 100, "500", "550"), // 100 * 550 = 55000
                    createPosition("MSFT", 50, "300", "320"));  // 50 * 320 = 16000

            CurrentValue result = service.calculateTotalCurrentValue(positions);

            assertThat(result.money().amount()).isEqualByComparingTo("71000");
        }
    }

    @Nested
    @DisplayName("total profit and loss calculation")
    class TotalProfitAndLoss {

        @Test
        @DisplayName("returns empty for empty positions list")
        void returnsEmptyForEmptyList() {
            Optional<ProfitAndLoss> result = service.calculateTotalProfitAndLoss(List.of());

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("calculates total profit")
        void calculatesTotalProfit() {
            List<Position> positions = List.of(
                    createPosition("AAPL", 100, "500", "550"), // invested 50000, current 55000
                    createPosition("MSFT", 50, "300", "320"));  // invested 15000, current 16000
            // Total: invested 65000, current 71000, P&L = 6000 (9.23%)

            Optional<ProfitAndLoss> result = service.calculateTotalProfitAndLoss(positions);

            assertThat(result).isPresent();
            assertThat(result.get().amount().amount()).isEqualByComparingTo("6000");
            assertThat(result.get().isProfit()).isTrue();
        }

        @Test
        @DisplayName("calculates total loss")
        void calculatesTotalLoss() {
            List<Position> positions = List.of(
                    createPosition("AAPL", 100, "500", "450"), // invested 50000, current 45000
                    createPosition("MSFT", 50, "300", "280"));  // invested 15000, current 14000
            // Total: invested 65000, current 59000, P&L = -6000

            Optional<ProfitAndLoss> result = service.calculateTotalProfitAndLoss(positions);

            assertThat(result).isPresent();
            assertThat(result.get().amount().amount()).isEqualByComparingTo("-6000");
            assertThat(result.get().isLoss()).isTrue();
        }
    }

    @Nested
    @DisplayName("portfolio metrics")
    class PortfolioMetricsCalculation {

        @Test
        @DisplayName("calculates portfolio metrics")
        void calculatesMetrics() {
            List<Position> positions = List.of(
                    createPosition("AAPL", 100, "500", "550"),
                    createPosition("MSFT", 50, "300", "320"));

            PortfolioMetrics metrics = service.calculatePortfolioMetrics(positions);

            assertThat(metrics.totalPositions()).isEqualTo(2);
            assertThat(metrics.totalInvestedAmount()).isNotNull();
            assertThat(metrics.totalCurrentValue()).isNotNull();
            assertThat(metrics.profitAndLoss()).isNotNull();
        }
    }

    private Position createPosition(String symbol, int qty, String costBasis, String price) {
        return new Position(
                InstrumentSymbol.of(symbol),
                List.of(new AccountHolding(new AccountId(1L), Quantity.of(qty), CostBasis.pln(costBasis))),
                Price.pln(price));
    }
}
