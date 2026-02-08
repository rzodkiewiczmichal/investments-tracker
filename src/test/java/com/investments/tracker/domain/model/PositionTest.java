package com.investments.tracker.domain.model;

import com.investments.tracker.domain.exception.DomainException;
import com.investments.tracker.domain.model.value.AccountId;
import com.investments.tracker.domain.model.value.CostBasis;
import com.investments.tracker.domain.model.value.InstrumentSymbol;
import com.investments.tracker.domain.model.value.Quantity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Position aggregate")
class PositionTest {

    @Nested
    @DisplayName("creation")
    class Creation {

        @Test
        @DisplayName("creates Position with single holding")
        void createsWithSingleHolding() {
            Position position = createPosition("AAPL", 100, "500.00");

            assertThat(position.symbol().value()).isEqualTo("AAPL");
            assertThat(position.holdings()).hasSize(1);
            assertThat(position.calculateTotalQuantity().value()).isEqualByComparingTo("100");
        }

        @Test
        @DisplayName("creates Position from holdings list")
        void createsFromHoldingsList() {
            List<AccountHolding> holdings = List.of(
                    new AccountHolding(new AccountId(1L), Quantity.of(50), CostBasis.pln("500")),
                    new AccountHolding(new AccountId(2L), Quantity.of(30), CostBasis.pln("520")));

            Position position = new Position(InstrumentSymbol.of("AAPL"), holdings);

            assertThat(position.holdings()).hasSize(2);
            assertThat(position.calculateTotalQuantity().value()).isEqualByComparingTo("80");
        }

        @Test
        @DisplayName("throws exception for empty holdings list")
        void throwsForEmptyHoldings() {
            assertThatThrownBy(() ->
                    new Position(InstrumentSymbol.of("AAPL"), List.of()))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("at least one holding");
        }
    }

    @Nested
    @DisplayName("holdings management")
    class HoldingsManagement {

        @Test
        @DisplayName("adds new holding for new account")
        void addsNewHolding() {
            Position position = createPosition("AAPL", 50, "500");

            Position updated = position.addHolding(
                    new AccountId(2L),
                    Quantity.of(30),
                    CostBasis.pln("520"));

            assertThat(updated.holdings()).hasSize(2);
            assertThat(updated.getAccountCount()).isEqualTo(2);
            // Original should be unchanged
            assertThat(position.holdings()).hasSize(1);
        }

        @Test
        @DisplayName("merges holding for existing account with weighted average")
        void mergesHoldingWithWeightedAverage() {
            Position position = createPosition("AAPL", 50, "500");

            // Add 50 more shares at 600 PLN = (50*500 + 50*600) / 100 = 550 PLN avg
            Position updated = position.addHolding(
                    new AccountId(1L),
                    Quantity.of(50),
                    CostBasis.pln("600"));

            assertThat(updated.holdings()).hasSize(1);
            assertThat(updated.calculateTotalQuantity().value()).isEqualByComparingTo("100");
            assertThat(updated.calculateWeightedAverageCostBasis().money().amount())
                    .isEqualByComparingTo("550");
        }

        @Test
        @DisplayName("removes holding from account")
        void removesHolding() {
            Position position = createPosition("AAPL", 50, "500");
            position = position.addHolding(new AccountId(2L), Quantity.of(30), CostBasis.pln("520"));

            Position updated = position.removeHolding(new AccountId(2L));

            assertThat(updated.holdings()).hasSize(1);
            assertThat(updated.findHolding(new AccountId(2L))).isEmpty();
        }

        @Test
        @DisplayName("throws when removing last holding")
        void throwsWhenRemovingLastHolding() {
            Position position = createPosition("AAPL", 50, "500");

            assertThatThrownBy(() -> position.removeHolding(new AccountId(1L)))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("Cannot remove last holding");
        }

        @Test
        @DisplayName("throws when removing non-existent holding")
        void throwsWhenRemovingNonExistent() {
            Position position = createPosition("AAPL", 50, "500");
            position = position.addHolding(new AccountId(2L), Quantity.of(30), CostBasis.pln("520"));

            Position finalPosition = position;
            assertThatThrownBy(() -> finalPosition.removeHolding(new AccountId(99L)))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("Holding not found");
        }
    }

    @Nested
    @DisplayName("calculations")
    class Calculations {

        @Test
        @DisplayName("calculates total quantity from multiple holdings")
        void calculatesTotalQuantity() {
            Position position = createPosition("AAPL", 50, "500");
            position = position.addHolding(new AccountId(2L), Quantity.of(30), CostBasis.pln("520"));

            Quantity totalQuantity = position.calculateTotalQuantity();

            assertThat(totalQuantity.value()).isEqualByComparingTo("80");
        }

        @Test
        @DisplayName("calculates weighted average cost basis")
        void calculatesWeightedAverageCostBasis() {
            Position position = createPosition("AAPL", 50, "500"); // 50 * 500 = 25000
            position = position.addHolding(
                    new AccountId(2L),
                    Quantity.of(30),
                    CostBasis.pln("520")); // 30 * 520 = 15600

            // Total: (25000 + 15600) / 80 = 507.50
            CostBasis avgCostBasis = position.calculateWeightedAverageCostBasis();

            assertThat(avgCostBasis.money().amount()).isEqualByComparingTo("507.50");
        }

        @Test
        @DisplayName("calculates invested amount")
        void calculatesInvestedAmount() {
            Position position = createPosition("AAPL", 100, "500");

            // 100 * 500 = 50000
            assertThat(position.calculateInvestedAmount().money().amount())
                    .isEqualByComparingTo("50000");
        }
    }

    @Nested
    @DisplayName("equality (identity-based)")
    class Equality {

        @Test
        @DisplayName("equals by symbol only (identity)")
        void equalsBySymbolOnly() {
            Position p1 = createPosition("AAPL", 100, "500");
            Position p2 = createPosition("AAPL", 100, "500");

            assertThat(p1).isEqualTo(p2);
            assertThat(p1.hashCode()).isEqualTo(p2.hashCode());
        }

        @Test
        @DisplayName("equals same symbol with different state (DDD entity semantics)")
        void equalsSameSymbolDifferentState() {
            Position p1 = createPosition("AAPL", 100, "500");
            Position p2 = new Position(
                    InstrumentSymbol.of("AAPL"),
                    List.of(new AccountHolding(new AccountId(2L), Quantity.of(200), CostBasis.pln("600"))));

            // Same symbol = same entity, regardless of different holdings
            assertThat(p1).isEqualTo(p2);
            assertThat(p1.hashCode()).isEqualTo(p2.hashCode());
        }

        @Test
        @DisplayName("not equals different symbols")
        void notEqualsDifferentSymbols() {
            Position p1 = createPosition("AAPL", 100, "500");
            Position p2 = createPosition("MSFT", 100, "500");

            assertThat(p1).isNotEqualTo(p2);
        }
    }

    private Position createPosition(String symbol, int qty, String costBasis) {
        return new Position(
                InstrumentSymbol.of(symbol),
                List.of(new AccountHolding(new AccountId(1L), Quantity.of(qty), CostBasis.pln(costBasis))));
    }
}
