package com.investments.tracker.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.investments.tracker.domain.model.AccountHolding;
import com.investments.tracker.domain.model.Portfolio;
import com.investments.tracker.domain.model.Position;
import com.investments.tracker.domain.model.value.AccountId;
import com.investments.tracker.domain.model.value.CostBasis;
import com.investments.tracker.domain.model.value.Currency;
import com.investments.tracker.domain.model.value.CurrentValue;
import com.investments.tracker.domain.model.value.ExchangeRate;
import com.investments.tracker.domain.model.value.InstrumentSymbol;
import com.investments.tracker.domain.model.value.InvestedAmount;
import com.investments.tracker.domain.model.value.Price;
import com.investments.tracker.domain.model.value.ProfitAndLoss;
import com.investments.tracker.domain.model.value.Quantity;

@DisplayName("PortfolioCalculationService")
class PortfolioCalculationServiceTest {

    private static final Map<Currency, ExchangeRate> PLN_RATES =
            Map.of(Currency.PLN, ExchangeRate.identity(Currency.PLN));

    private PortfolioCalculationService service;

    @BeforeEach
    void setUp() {
        service = new PortfolioCalculationService(new PositionCalculationService());
    }

    @Nested
    @DisplayName("portfolio creation")
    class PortfolioCreation {

        @Test
        @DisplayName("creates empty portfolio from empty list")
        void createsEmptyPortfolio() {
            Portfolio portfolio = service.createPortfolio(List.of(), Map.of(), PLN_RATES);

            assertThat(portfolio.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("creates portfolio from positions with prices")
        void createsPortfolioFromPositionsWithPrices() {
            Position aapl = createPosition("AAPL", 100, "500");
            Position msft = createPosition("MSFT", 50, "300");
            Map<InstrumentSymbol, Price> prices =
                    Map.of(
                            InstrumentSymbol.of("AAPL"), Price.pln("550"),
                            InstrumentSymbol.of("MSFT"), Price.pln("320"));

            Portfolio portfolio = service.createPortfolio(List.of(aapl, msft), prices, PLN_RATES);

            assertThat(portfolio.getPositionCount()).isEqualTo(2);
            assertThat(portfolio.getTotalCurrentValue()).isPresent();
            assertThat(portfolio.getTotalProfitAndLoss()).isPresent();
        }

        @Test
        @DisplayName("creates portfolio with null metrics when prices are unknown")
        void createsPortfolioWithNullMetricsWhenNoPrices() {
            Position aapl = createPosition("AAPL", 100, "500");
            Position msft = createPosition("MSFT", 50, "300");

            Portfolio portfolio = service.createPortfolio(List.of(aapl, msft), Map.of(), PLN_RATES);

            assertThat(portfolio.getPositionCount()).isEqualTo(2);
            assertThat(portfolio.getTotalInvestedAmount()).isPresent();
            assertThat(portfolio.getTotalCurrentValue()).isEmpty();
            assertThat(portfolio.getTotalProfitAndLoss()).isEmpty();
        }

        @Test
        @DisplayName(
                "creates portfolio with metrics from priced positions when some prices missing")
        void createsPortfolioWithMetricsFromPricedPositionsWhenPartialPrices() {
            Position aapl = createPosition("AAPL", 100, "500"); // invested 50000
            Position msft = createPosition("MSFT", 50, "300"); // invested 15000, no price
            Map<InstrumentSymbol, Price> prices =
                    Map.of(InstrumentSymbol.of("AAPL"), Price.pln("550")); // current 55000

            Portfolio portfolio = service.createPortfolio(List.of(aapl, msft), prices, PLN_RATES);

            assertThat(portfolio.getPositionCount()).isEqualTo(2);
            // Total invested includes ALL positions
            assertThat(portfolio.getTotalInvestedAmount()).isPresent();
            assertThat(portfolio.getTotalInvestedAmount().get().money().amount())
                    .isEqualByComparingTo("65000");
            // Current value only from priced positions (AAPL)
            assertThat(portfolio.getTotalCurrentValue()).isPresent();
            assertThat(portfolio.getTotalCurrentValue().get().money().amount())
                    .isEqualByComparingTo("55000");
            // P&L from priced positions only: 55000 - 50000 = 5000
            assertThat(portfolio.getTotalProfitAndLoss()).isPresent();
            assertThat(portfolio.getTotalProfitAndLoss().get().amount().amount())
                    .isEqualByComparingTo("5000");
        }
    }

    @Nested
    @DisplayName("total invested amount calculation")
    class TotalInvestedAmount {

        @Test
        @DisplayName("returns empty for empty positions list")
        void returnsEmptyForEmptyList() {
            Optional<InvestedAmount> result =
                    service.calculateTotalInvestedAmount(List.of(), PLN_RATES);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("calculates total invested amount")
        void calculatesTotalInvestedAmount() {
            List<Position> positions =
                    List.of(
                            createPosition("AAPL", 100, "500"), // 100 * 500 = 50000
                            createPosition("MSFT", 50, "300")); // 50 * 300 = 15000

            Optional<InvestedAmount> result =
                    service.calculateTotalInvestedAmount(positions, PLN_RATES);

            assertThat(result).isPresent();
            assertThat(result.get().money().amount()).isEqualByComparingTo("65000");
        }
    }

    @Nested
    @DisplayName("total current value calculation")
    class TotalCurrentValue {

        @Test
        @DisplayName("returns empty for empty positions list")
        void returnsEmptyForEmptyList() {
            Optional<CurrentValue> result =
                    service.calculateTotalCurrentValue(List.of(), Map.of(), PLN_RATES);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("calculates total current value when all prices available")
        void calculatesTotalCurrentValue() {
            Position aapl = createPosition("AAPL", 100, "500");
            Position msft = createPosition("MSFT", 50, "300");
            Map<InstrumentSymbol, Price> prices =
                    Map.of(
                            InstrumentSymbol.of("AAPL"), Price.pln("550"),
                            InstrumentSymbol.of("MSFT"), Price.pln("320"));

            Optional<CurrentValue> result =
                    service.calculateTotalCurrentValue(List.of(aapl, msft), prices, PLN_RATES);

            assertThat(result).isPresent();
            assertThat(result.get().money().amount()).isEqualByComparingTo("71000");
        }

        @Test
        @DisplayName("calculates current value from priced positions only when some prices missing")
        void calculatesFromPricedPositionsWhenSomePricesMissing() {
            Position aapl = createPosition("AAPL", 100, "500");
            Position msft = createPosition("MSFT", 50, "300");
            Map<InstrumentSymbol, Price> prices =
                    Map.of(InstrumentSymbol.of("AAPL"), Price.pln("550")); // 100 * 550 = 55000

            Optional<CurrentValue> result =
                    service.calculateTotalCurrentValue(List.of(aapl, msft), prices, PLN_RATES);

            assertThat(result).isPresent();
            assertThat(result.get().money().amount()).isEqualByComparingTo("55000");
        }

        @Test
        @DisplayName("returns empty when no positions have prices")
        void returnsEmptyWhenNoPricesAvailable() {
            Position aapl = createPosition("AAPL", 100, "500");
            Position msft = createPosition("MSFT", 50, "300");

            Optional<CurrentValue> result =
                    service.calculateTotalCurrentValue(List.of(aapl, msft), Map.of(), PLN_RATES);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("total profit and loss calculation")
    class TotalProfitAndLoss {

        @Test
        @DisplayName("returns empty for empty positions list")
        void returnsEmptyForEmptyList() {
            Optional<ProfitAndLoss> result =
                    service.calculateTotalProfitAndLoss(List.of(), Map.of(), PLN_RATES);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("calculates total profit")
        void calculatesTotalProfit() {
            Position aapl = createPosition("AAPL", 100, "500"); // invested 50000
            Position msft = createPosition("MSFT", 50, "300"); // invested 15000
            Map<InstrumentSymbol, Price> prices =
                    Map.of(
                            InstrumentSymbol.of("AAPL"), Price.pln("550"), // current 55000
                            InstrumentSymbol.of("MSFT"), Price.pln("320")); // current 16000
            // Total: invested 65000, current 71000, P&L = 6000

            Optional<ProfitAndLoss> result =
                    service.calculateTotalProfitAndLoss(List.of(aapl, msft), prices, PLN_RATES);

            assertThat(result).isPresent();
            assertThat(result.get().amount().amount()).isEqualByComparingTo("6000");
            assertThat(result.get().isProfit()).isTrue();
        }

        @Test
        @DisplayName("calculates total loss")
        void calculatesTotalLoss() {
            Position aapl = createPosition("AAPL", 100, "500"); // invested 50000
            Position msft = createPosition("MSFT", 50, "300"); // invested 15000
            Map<InstrumentSymbol, Price> prices =
                    Map.of(
                            InstrumentSymbol.of("AAPL"), Price.pln("450"), // current 45000
                            InstrumentSymbol.of("MSFT"), Price.pln("280")); // current 14000
            // Total: invested 65000, current 59000, P&L = -6000

            Optional<ProfitAndLoss> result =
                    service.calculateTotalProfitAndLoss(List.of(aapl, msft), prices, PLN_RATES);

            assertThat(result).isPresent();
            assertThat(result.get().amount().amount()).isEqualByComparingTo("-6000");
            assertThat(result.get().isLoss()).isTrue();
        }

        @Test
        @DisplayName("calculates P&L from priced positions only, ignoring unpriced")
        void calculatesPLFromPricedPositionsOnly() {
            Position aapl = createPosition("AAPL", 100, "500"); // invested 50000
            Position msft = createPosition("MSFT", 50, "300"); // invested 15000, no price
            Map<InstrumentSymbol, Price> prices =
                    Map.of(InstrumentSymbol.of("AAPL"), Price.pln("550")); // current 55000
            // P&L should be based on AAPL only: 55000 - 50000 = 5000

            Optional<ProfitAndLoss> result =
                    service.calculateTotalProfitAndLoss(List.of(aapl, msft), prices, PLN_RATES);

            assertThat(result).isPresent();
            assertThat(result.get().amount().amount()).isEqualByComparingTo("5000");
            assertThat(result.get().isProfit()).isTrue();
        }

        @Test
        @DisplayName("returns empty when no prices are available")
        void returnsEmptyWhenNoPrices() {
            Position aapl = createPosition("AAPL", 100, "500");
            Position msft = createPosition("MSFT", 50, "300");

            Optional<ProfitAndLoss> result =
                    service.calculateTotalProfitAndLoss(List.of(aapl, msft), Map.of(), PLN_RATES);

            assertThat(result).isEmpty();
        }
    }

    private Position createPosition(String symbol, int qty, String costBasis) {
        return new Position(
                InstrumentSymbol.of(symbol),
                List.of(
                        new AccountHolding(
                                new AccountId(1L), Quantity.of(qty), CostBasis.pln(costBasis))));
    }
}
