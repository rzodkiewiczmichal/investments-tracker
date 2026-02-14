package com.investments.tracker.domain.model.value;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Percentage value object")
class PercentageTest {

    @Nested
    @DisplayName("creation")
    class Creation {

        @Test
        @DisplayName("creates Percentage with valid value")
        void createsWithValidValue() {
            Percentage percentage = new Percentage(new BigDecimal("10.5"));

            assertThat(percentage.value()).isEqualByComparingTo("10.5000");
        }

        @Test
        @DisplayName("creates zero Percentage")
        void createsZero() {
            Percentage percentage = Percentage.zero();

            assertThat(percentage.value()).isEqualByComparingTo("0.0000");
        }

        @Test
        @DisplayName("creates Percentage from fraction")
        void createsFromFraction() {
            Percentage percentage =
                    Percentage.fromFraction(new BigDecimal("25"), new BigDecimal("100"));

            assertThat(percentage.value()).isEqualByComparingTo("25.0000");
        }

        @Test
        @DisplayName("creates Percentage from Money")
        void createsFromMoney() {
            Money part = Money.pln("150");
            Money whole = Money.pln("1000");

            Percentage percentage = Percentage.fromMoney(part, whole);

            assertThat(percentage.value()).isEqualByComparingTo("15.0000");
        }

        @Test
        @DisplayName("throws exception when dividing by zero")
        void throwsWhenDividingByZero() {
            assertThatThrownBy(() -> Percentage.fromFraction(new BigDecimal("10"), BigDecimal.ZERO))
                    .isInstanceOf(ArithmeticException.class)
                    .hasMessageContaining("whole is zero");
        }
    }

    @Nested
    @DisplayName("comparison operations")
    class ComparisonOperations {

        @Test
        @DisplayName("detects positive Percentage")
        void detectsPositive() {
            Percentage percentage = new Percentage(new BigDecimal("10.5"));

            assertThat(percentage.isPositive()).isTrue();
            assertThat(percentage.isNegative()).isFalse();
            assertThat(percentage.isZero()).isFalse();
        }

        @Test
        @DisplayName("detects negative Percentage")
        void detectsNegative() {
            Percentage percentage = new Percentage(new BigDecimal("-5.25"));

            assertThat(percentage.isPositive()).isFalse();
            assertThat(percentage.isNegative()).isTrue();
            assertThat(percentage.isZero()).isFalse();
        }

        @Test
        @DisplayName("detects zero Percentage")
        void detectsZero() {
            Percentage percentage = Percentage.zero();

            assertThat(percentage.isPositive()).isFalse();
            assertThat(percentage.isNegative()).isFalse();
            assertThat(percentage.isZero()).isTrue();
        }
    }

    @Nested
    @DisplayName("formatting")
    class Formatting {

        @Test
        @DisplayName("formats Percentage for display")
        void formatsForDisplay() {
            Percentage percentage = new Percentage(new BigDecimal("10.5678"));

            String formatted = percentage.formatForDisplay();

            assertThat(formatted).isEqualTo("10.57%");
        }

        @Test
        @DisplayName("formats positive Percentage with sign")
        void formatsPositiveWithSign() {
            Percentage percentage = new Percentage(new BigDecimal("10.50"));

            String formatted = percentage.formatWithSign();

            assertThat(formatted).isEqualTo("+10.50%");
        }

        @Test
        @DisplayName("formats negative Percentage with sign")
        void formatsNegativeWithSign() {
            Percentage percentage = new Percentage(new BigDecimal("-5.25"));

            String formatted = percentage.formatWithSign();

            assertThat(formatted).isEqualTo("-5.25%");
        }
    }
}
