package com.investments.tracker.domain.service;

import com.investments.tracker.domain.model.AccountHolding;
import com.investments.tracker.domain.model.Position;
import com.investments.tracker.domain.model.value.AccountId;
import com.investments.tracker.domain.model.value.CostBasis;
import com.investments.tracker.domain.model.value.CurrentValue;
import com.investments.tracker.domain.model.value.InstrumentSymbol;
import com.investments.tracker.domain.model.value.Price;
import com.investments.tracker.domain.model.value.Quantity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PositionCalculationService")
class PositionCalculationServiceTest {

    private PositionCalculationService service;

    @BeforeEach
    void setUp() {
        service = new PositionCalculationService();
    }

    @Nested
    @DisplayName("current value calculation with custom price")
    class CurrentValueCalculation {

        @Test
        @DisplayName("calculates current value with given price different from position price")
        void calculatesWithGivenPrice() {
            Position position = createPosition("AAPL", 100, "500", "550");

            CurrentValue result = service.calculateCurrentValue(position, Price.pln("600"));

            assertThat(result.money().amount()).isEqualByComparingTo("60000");
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

    private Position createPosition(String symbol, int qty, String costBasis, String price) {
        return new Position(
                InstrumentSymbol.of(symbol),
                List.of(new AccountHolding(new AccountId(1L), Quantity.of(qty), CostBasis.pln(costBasis))),
                Price.pln(price));
    }
}
