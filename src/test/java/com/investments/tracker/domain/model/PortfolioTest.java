package com.investments.tracker.domain.model;

import com.investments.tracker.domain.model.value.AccountId;
import com.investments.tracker.domain.model.value.CostBasis;
import com.investments.tracker.domain.model.value.CurrentValue;
import com.investments.tracker.domain.model.value.InstrumentSymbol;
import com.investments.tracker.domain.model.value.InvestedAmount;
import com.investments.tracker.domain.model.value.Money;
import com.investments.tracker.domain.model.value.ProfitAndLoss;
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
            Portfolio portfolio = new Portfolio(List.of(), PortfolioMetrics.empty());

            assertThat(portfolio.isEmpty()).isTrue();
            assertThat(portfolio.getPositionCount()).isZero();
            assertThat(portfolio.getTotalInvestedAmount()).isEmpty();
            assertThat(portfolio.getTotalCurrentValue()).isEmpty();
            assertThat(portfolio.getTotalProfitAndLoss()).isEmpty();
        }
    }

    @Nested
    @DisplayName("portfolio with positions")
    class PortfolioWithPositions {

        @Test
        @DisplayName("creates portfolio from positions and metrics")
        void createsFromPositionsAndMetrics() {
            Position p1 = createPosition("AAPL", 100, "500");
            Position p2 = createPosition("MSFT", 50, "300");
            PortfolioMetrics metrics = new PortfolioMetrics(
                    new InvestedAmount(Money.pln("65000")),
                    new CurrentValue(Money.pln("71000")),
                    ProfitAndLoss.calculate(
                            new CurrentValue(Money.pln("71000")),
                            new InvestedAmount(Money.pln("65000"))),
                    2);

            Portfolio portfolio = new Portfolio(List.of(p1, p2), metrics);

            assertThat(portfolio.isEmpty()).isFalse();
            assertThat(portfolio.getPositionCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("returns invested amount")
        void returnsInvestedAmount() {
            PortfolioMetrics metrics = new PortfolioMetrics(
                    new InvestedAmount(Money.pln("65000")),
                    null,
                    null,
                    1);
            Portfolio portfolio = new Portfolio(List.of(createPosition("AAPL", 100, "500")), metrics);

            assertThat(portfolio.getTotalInvestedAmount()).isPresent();
            assertThat(portfolio.getTotalInvestedAmount().get().money().amount())
                    .isEqualByComparingTo("65000");
        }

        @Test
        @DisplayName("returns empty current value when prices are unknown")
        void returnsEmptyCurrentValueWhenPricesUnknown() {
            PortfolioMetrics metrics = new PortfolioMetrics(
                    new InvestedAmount(Money.pln("65000")),
                    null,
                    null,
                    1);
            Portfolio portfolio = new Portfolio(List.of(createPosition("AAPL", 100, "500")), metrics);

            assertThat(portfolio.getTotalCurrentValue()).isEmpty();
            assertThat(portfolio.getTotalProfitAndLoss()).isEmpty();
        }

        @Test
        @DisplayName("tracks total positions count")
        void tracksTotalPositions() {
            Position p1 = createPosition("AAPL", 100, "500");
            Position p2 = createPosition("MSFT", 50, "300");
            PortfolioMetrics metrics = new PortfolioMetrics(null, null, null, 2);

            Portfolio portfolio = new Portfolio(List.of(p1, p2), metrics);

            assertThat(portfolio.metrics().totalPositions()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("portfolio metrics")
    class PortfolioMetricsTest {

        @Test
        @DisplayName("formats summary for empty portfolio")
        void formatsSummaryForEmpty() {
            PortfolioMetrics metrics = PortfolioMetrics.empty();

            String summary = metrics.formatSummary();

            assertThat(summary).isEqualTo("No positions yet");
        }

        @Test
        @DisplayName("formats summary with all metrics available")
        void formatsSummaryWithAllMetrics() {
            PortfolioMetrics metrics = new PortfolioMetrics(
                    new InvestedAmount(Money.pln("50000")),
                    new CurrentValue(Money.pln("55000")),
                    ProfitAndLoss.calculate(
                            new CurrentValue(Money.pln("55000")),
                            new InvestedAmount(Money.pln("50000"))),
                    1);

            String summary = metrics.formatSummary();

            assertThat(summary).contains("Total Value:");
            assertThat(summary).contains("Invested:");
            assertThat(summary).contains("P&L:");
            assertThat(summary).contains("Positions: 1");
        }

        @Test
        @DisplayName("formats summary without current value when prices unknown")
        void formatsSummaryWithoutCurrentValue() {
            PortfolioMetrics metrics = new PortfolioMetrics(
                    new InvestedAmount(Money.pln("50000")),
                    null,
                    null,
                    1);

            String summary = metrics.formatSummary();

            assertThat(summary).contains("Invested:");
            assertThat(summary).contains("Positions: 1");
            assertThat(summary).doesNotContain("Total Value:");
            assertThat(summary).doesNotContain("P&L:");
        }
    }

    private Position createPosition(String symbol, int qty, String costBasis) {
        return new Position(
                InstrumentSymbol.of(symbol),
                List.of(new AccountHolding(new AccountId(1L), Quantity.of(qty), CostBasis.pln(costBasis))));
    }
}
