package com.investments.tracker.domain.model.value;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("ProfitAndLoss value object")
class ProfitAndLossTest {

    @Nested
    @DisplayName("calculation")
    class Calculation {

        @Test
        @DisplayName("calculates positive P&L (profit)")
        void calculatesPositivePnl() {
            CurrentValue currentValue = CurrentValue.pln("12000");
            InvestedAmount investedAmount = InvestedAmount.pln("10000");

            ProfitAndLoss pnl = ProfitAndLoss.calculate(currentValue, investedAmount);

            assertThat(pnl.amount().amount()).isEqualByComparingTo("2000");
            assertThat(pnl.percentage().value()).isEqualByComparingTo("20");
            assertThat(pnl.isProfit()).isTrue();
            assertThat(pnl.isLoss()).isFalse();
        }

        @Test
        @DisplayName("calculates negative P&L (loss)")
        void calculatesNegativePnl() {
            CurrentValue currentValue = CurrentValue.pln("8000");
            InvestedAmount investedAmount = InvestedAmount.pln("10000");

            ProfitAndLoss pnl = ProfitAndLoss.calculate(currentValue, investedAmount);

            assertThat(pnl.amount().amount()).isEqualByComparingTo("-2000");
            assertThat(pnl.percentage().value()).isEqualByComparingTo("-20");
            assertThat(pnl.isProfit()).isFalse();
            assertThat(pnl.isLoss()).isTrue();
        }

        @Test
        @DisplayName("calculates break-even P&L")
        void calculatesBreakEvenPnl() {
            CurrentValue currentValue = CurrentValue.pln("10000");
            InvestedAmount investedAmount = InvestedAmount.pln("10000");

            ProfitAndLoss pnl = ProfitAndLoss.calculate(currentValue, investedAmount);

            assertThat(pnl.amount().amount()).isEqualByComparingTo("0");
            assertThat(pnl.percentage().value()).isEqualByComparingTo("0");
            assertThat(pnl.isBreakEven()).isTrue();
        }

        @Test
        @DisplayName("creates zero P&L")
        void createsZeroPnl() {
            ProfitAndLoss pnl = ProfitAndLoss.zero();

            assertThat(pnl.amount().isZero()).isTrue();
            assertThat(pnl.percentage().isZero()).isTrue();
        }
    }

    @Nested
    @DisplayName("formatting")
    class Formatting {

        @Test
        @DisplayName("formats profit for display")
        void formatsProfitForDisplay() {
            CurrentValue currentValue = CurrentValue.pln("11000");
            InvestedAmount investedAmount = InvestedAmount.pln("10000");
            ProfitAndLoss pnl = ProfitAndLoss.calculate(currentValue, investedAmount);

            assertThat(pnl.formatAmountForDisplay()).isEqualTo("+1000.00 PLN");
            assertThat(pnl.formatPercentageForDisplay()).isEqualTo("+10.00%");
            assertThat(pnl.formatForDisplay()).isEqualTo("+1000.00 PLN (+10.00%)");
        }

        @Test
        @DisplayName("formats loss for display")
        void formatsLossForDisplay() {
            CurrentValue currentValue = CurrentValue.pln("9000");
            InvestedAmount investedAmount = InvestedAmount.pln("10000");
            ProfitAndLoss pnl = ProfitAndLoss.calculate(currentValue, investedAmount);

            assertThat(pnl.formatAmountForDisplay()).isEqualTo("-1000.00 PLN");
            assertThat(pnl.formatPercentageForDisplay()).isEqualTo("-10.00%");
            assertThat(pnl.formatForDisplay()).isEqualTo("-1000.00 PLN (-10.00%)");
        }
    }
}
