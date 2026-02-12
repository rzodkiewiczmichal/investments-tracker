package com.investments.tracker.domain.service;

import com.investments.tracker.domain.model.AccountHolding;
import com.investments.tracker.domain.model.Position;
import com.investments.tracker.domain.model.value.AccountId;
import com.investments.tracker.domain.model.value.CostBasis;
import com.investments.tracker.domain.model.value.Currency;
import com.investments.tracker.domain.model.value.CurrentValue;
import com.investments.tracker.domain.model.value.ExchangeRate;
import com.investments.tracker.domain.model.value.InstrumentSymbol;
import com.investments.tracker.domain.model.value.Price;
import com.investments.tracker.domain.model.value.ProfitAndLoss;
import com.investments.tracker.domain.model.value.Quantity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PositionCalculationService")
class PositionCalculationServiceTest {

    private static final ExchangeRate PLN_IDENTITY = ExchangeRate.identity(Currency.PLN);
    private PositionCalculationService service;

    @BeforeEach
    void setUp() {
        service = new PositionCalculationService();
    }

    @Nested
    @DisplayName("current value calculation")
    class CurrentValueCalculation {

        @Test
        @DisplayName("calculates current value with given price")
        void calculatesWithGivenPrice() {
            Position position = createPosition("AAPL", 100, "500");

            CurrentValue result = service.calculateCurrentValue(position, Price.pln("600"), PLN_IDENTITY);

            assertThat(result.money().amount()).isEqualByComparingTo("60000");
        }
    }

    @Nested
    @DisplayName("profit and loss calculation")
    class ProfitAndLossCalculation {

        @Test
        @DisplayName("calculates profit when price is above cost basis")
        void calculatesProfit() {
            Position position = createPosition("AAPL", 100, "500"); // invested: 50000

            ProfitAndLoss pnl = service.calculateProfitAndLoss(position, Price.pln("550"), PLN_IDENTITY); // current: 55000

            assertThat(pnl.amount().amount()).isEqualByComparingTo("5000");
            assertThat(pnl.percentage().value()).isEqualByComparingTo("10");
            assertThat(pnl.isProfit()).isTrue();
        }

        @Test
        @DisplayName("calculates loss when price is below cost basis")
        void calculatesLoss() {
            Position position = createPosition("AAPL", 100, "500"); // invested: 50000

            ProfitAndLoss pnl = service.calculateProfitAndLoss(position, Price.pln("450"), PLN_IDENTITY); // current: 45000

            assertThat(pnl.amount().amount()).isEqualByComparingTo("-5000");
            assertThat(pnl.percentage().value()).isEqualByComparingTo("-10");
            assertThat(pnl.isLoss()).isTrue();
        }

        @Test
        @DisplayName("calculates zero P&L when price equals cost basis")
        void calculatesZeroPnl() {
            Position position = createPosition("AAPL", 100, "500"); // invested: 50000

            ProfitAndLoss pnl = service.calculateProfitAndLoss(position, Price.pln("500"), PLN_IDENTITY); // current: 50000

            assertThat(pnl.amount().amount()).isEqualByComparingTo("0");
            assertThat(pnl.isProfit()).isFalse();
            assertThat(pnl.isLoss()).isFalse();
        }
    }

    @Nested
    @DisplayName("cost basis recalculation after purchase")
    class CostBasisRecalculation {

        @Test
        @DisplayName("calculates new cost basis after adding shares")
        void calculatesNewCostBasisAfterPurchase() {
            // Existing: 50 shares at 500 PLN = 25000
            // New: 50 shares at 600 PLN = 30000
            // Total: 100 shares, 55000 invested = 550 PLN avg
            CostBasis newCostBasis = service.calculateNewCostBasisAfterPurchase(
                    Quantity.of(50),
                    CostBasis.pln("500"),
                    Quantity.of(50),
                    CostBasis.pln("600"));

            assertThat(newCostBasis.money().amount()).isEqualByComparingTo("550");
        }
    }

    private Position createPosition(String symbol, int qty, String costBasis) {
        return new Position(
                InstrumentSymbol.of(symbol),
                List.of(new AccountHolding(new AccountId(1L), Quantity.of(qty), CostBasis.pln(costBasis))));
    }
}
