package com.investments.tracker.domain.model;

import com.investments.tracker.domain.model.value.AccountId;
import com.investments.tracker.domain.model.value.CostBasis;
import com.investments.tracker.domain.model.value.InstrumentSymbol;
import com.investments.tracker.domain.model.value.Price;
import com.investments.tracker.domain.model.value.Quantity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Portfolio aggregate")
class PortfolioTest {

    @Nested
    @DisplayName("empty portfolio")
    class EmptyPortfolio {

        @Test
        @DisplayName("creates empty portfolio")
        void createsEmpty() {
            Portfolio portfolio = Portfolio.fromPositions(List.of());

            assertThat(portfolio.isEmpty()).isTrue();
            assertThat(portfolio.getPositionCount()).isZero();
            assertThat(portfolio.getTotalInvestedAmount()).isEmpty();
            assertThat(portfolio.getTotalCurrentValue().isZero()).isTrue();
            assertThat(portfolio.getTotalProfitAndLoss()).isEmpty();
        }

        @Test
        @DisplayName("creates empty portfolio from empty list")
        void createsFromEmptyList() {
            Portfolio portfolio = Portfolio.fromPositions(List.of());

            assertThat(portfolio.isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("portfolio with positions")
    class PortfolioWithPositions {

        @Test
        @DisplayName("creates portfolio from positions")
        void createsFromPositions() {
            Position p1 = createPosition("AAPL", 100, "500", "550");
            Position p2 = createPosition("MSFT", 50, "300", "320");

            Portfolio portfolio = Portfolio.fromPositions(List.of(p1, p2));

            assertThat(portfolio.isEmpty()).isFalse();
            assertThat(portfolio.getPositionCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("calculates total invested amount")
        void calculatesTotalInvestedAmount() {
            Position p1 = createPosition("AAPL", 100, "500", "550"); // 100 * 500 = 50000
            Position p2 = createPosition("MSFT", 50, "300", "320");   // 50 * 300 = 15000

            Portfolio portfolio = Portfolio.fromPositions(List.of(p1, p2));

            assertThat(portfolio.getTotalInvestedAmount()).isPresent();
            assertThat(portfolio.getTotalInvestedAmount().get().money().amount())
                    .isEqualByComparingTo("65000");
        }

        @Test
        @DisplayName("calculates total current value")
        void calculatesTotalCurrentValue() {
            Position p1 = createPosition("AAPL", 100, "500", "550"); // 100 * 550 = 55000
            Position p2 = createPosition("MSFT", 50, "300", "320");   // 50 * 320 = 16000

            Portfolio portfolio = Portfolio.fromPositions(List.of(p1, p2));

            assertThat(portfolio.getTotalCurrentValue().money().amount())
                    .isEqualByComparingTo("71000");
        }

        @Test
        @DisplayName("calculates total profit and loss")
        void calculatesTotalPnl() {
            Position p1 = createPosition("AAPL", 100, "500", "550"); // invested 50000, current 55000
            Position p2 = createPosition("MSFT", 50, "300", "320");   // invested 15000, current 16000
            // Total: invested 65000, current 71000, P&L = 6000 (9.23%)

            Portfolio portfolio = Portfolio.fromPositions(List.of(p1, p2));

            var pnl = portfolio.getTotalProfitAndLoss();
            assertThat(pnl).isPresent();
            assertThat(pnl.get().amount().amount()).isEqualByComparingTo("6000");
            assertThat(pnl.get().isProfit()).isTrue();
        }

        @Test
        @DisplayName("tracks total positions count")
        void tracksTotalPositions() {
            Position p1 = createPosition("AAPL", 100, "500", "550");
            Position p2 = createPosition("MSFT", 50, "300", "320");

            Portfolio portfolio = Portfolio.fromPositions(List.of(p1, p2));

            PortfolioMetrics metrics = portfolio.metrics();
            assertThat(metrics.totalPositions()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("portfolio metrics")
    class PortfolioMetricsTest {

        @Test
        @DisplayName("formats summary for empty portfolio")
        void formatsSummaryForEmpty() {
            Portfolio portfolio = Portfolio.fromPositions(List.of());

            String summary = portfolio.metrics().formatSummary();

            assertThat(summary).isEqualTo("No positions found");
        }

        @Test
        @DisplayName("formats summary for portfolio with positions")
        void formatsSummaryWithPositions() {
            Position p1 = createPosition("AAPL", 100, "500", "550");

            Portfolio portfolio = Portfolio.fromPositions(List.of(p1));

            String summary = portfolio.metrics().formatSummary();

            assertThat(summary).contains("Total Value:");
            assertThat(summary).contains("Invested:");
            assertThat(summary).contains("P&L:");
            assertThat(summary).contains("Positions: 1");
        }
    }

    private Position createPosition(String symbol, int qty, String costBasis, String price) {
        return new Position(
                InstrumentSymbol.of(symbol),
                List.of(new AccountHolding(new AccountId(1L), Quantity.of(qty), CostBasis.pln(costBasis))),
                Price.pln(price));
    }
}
