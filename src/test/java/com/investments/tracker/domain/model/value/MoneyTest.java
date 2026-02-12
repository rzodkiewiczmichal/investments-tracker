package com.investments.tracker.domain.model.value;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Money value object")
class MoneyTest {

    @Nested
    @DisplayName("creation")
    class Creation {

        @Test
        @DisplayName("creates Money with valid amount and currency")
        void createsWithValidAmountAndCurrency() {
            Money money = new Money(new BigDecimal("100.00"), Currency.PLN);

            assertThat(money.amount()).isEqualByComparingTo("100.0000");
            assertThat(money.currency()).isEqualTo(Currency.PLN);
        }

        @Test
        @DisplayName("creates Money in PLN from string")
        void createsPlnFromString() {
            Money money = Money.pln("1234.56");

            assertThat(money.amount()).isEqualByComparingTo("1234.5600");
            assertThat(money.currency()).isEqualTo(Currency.PLN);
        }

        @Test
        @DisplayName("creates Money in PLN from BigDecimal")
        void createsPlnFromBigDecimal() {
            Money money = Money.pln(new BigDecimal("1234.56"));

            assertThat(money.amount()).isEqualByComparingTo("1234.5600");
            assertThat(money.currency()).isEqualTo(Currency.PLN);
        }

        @Test
        @DisplayName("creates zero Money")
        void createsZero() {
            Money money = Money.zero();

            assertThat(money.amount()).isEqualByComparingTo("0.0000");
            assertThat(money.currency()).isEqualTo(Currency.PLN);
        }

        @Test
        @DisplayName("throws exception for null amount")
        void throwsForNullAmount() {
            assertThatThrownBy(() -> new Money(null, Currency.PLN))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("amount cannot be null");
        }

        @Test
        @DisplayName("throws exception for null currency")
        void throwsForNullCurrency() {
            assertThatThrownBy(() -> new Money(BigDecimal.TEN, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("currency cannot be null");
        }

        @Test
        @DisplayName("throws exception for amount with too many decimal places")
        void throwsForTooManyDecimalPlaces() {
            assertThatThrownBy(() -> new Money(new BigDecimal("100.12345"), Currency.PLN))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("cannot exceed 4 decimal places");
        }
    }

    @Nested
    @DisplayName("arithmetic operations")
    class ArithmeticOperations {

        @Test
        @DisplayName("adds two Money values")
        void addsMoneyValues() {
            Money m1 = Money.pln("100.00");
            Money m2 = Money.pln("50.25");

            Money result = m1.add(m2);

            assertThat(result.amount()).isEqualByComparingTo("150.2500");
        }

        @Test
        @DisplayName("subtracts two Money values")
        void subtractsMoneyValues() {
            Money m1 = Money.pln("100.00");
            Money m2 = Money.pln("30.50");

            Money result = m1.subtract(m2);

            assertThat(result.amount()).isEqualByComparingTo("69.5000");
        }

        @Test
        @DisplayName("multiplies Money by factor")
        void multipliesByFactor() {
            Money money = Money.pln("100.00");

            Money result = money.multiply(new BigDecimal("1.5"));

            assertThat(result.amount()).isEqualByComparingTo("150.0000");
        }

        @Test
        @DisplayName("divides Money by divisor")
        void dividesByDivisor() {
            Money money = Money.pln("100.00");

            Money result = money.divide(new BigDecimal("4"));

            assertThat(result.amount()).isEqualByComparingTo("25.0000");
        }

        @Test
        @DisplayName("throws exception when dividing by zero")
        void throwsWhenDividingByZero() {
            Money money = Money.pln("100.00");

            assertThatThrownBy(() -> money.divide(BigDecimal.ZERO))
                    .isInstanceOf(ArithmeticException.class)
                    .hasMessage("Cannot divide by zero");
        }

        @Test
        @DisplayName("negates Money")
        void negatesMoney() {
            Money money = Money.pln("100.00");

            Money result = money.negate();

            assertThat(result.amount()).isEqualByComparingTo("-100.0000");
        }
    }

    @Nested
    @DisplayName("comparison operations")
    class ComparisonOperations {

        @Test
        @DisplayName("detects positive Money")
        void detectsPositive() {
            Money money = Money.pln("100.00");

            assertThat(money.isPositive()).isTrue();
            assertThat(money.isNegative()).isFalse();
            assertThat(money.isZero()).isFalse();
        }

        @Test
        @DisplayName("detects negative Money")
        void detectsNegative() {
            Money money = Money.pln("-50.00");

            assertThat(money.isPositive()).isFalse();
            assertThat(money.isNegative()).isTrue();
            assertThat(money.isZero()).isFalse();
        }

        @Test
        @DisplayName("detects zero Money")
        void detectsZero() {
            Money money = Money.zero();

            assertThat(money.isPositive()).isFalse();
            assertThat(money.isNegative()).isFalse();
            assertThat(money.isZero()).isTrue();
        }
    }

    @Nested
    @DisplayName("formatting")
    class Formatting {

        @Test
        @DisplayName("formats Money for display")
        void formatsForDisplay() {
            Money money = Money.pln("1234.5678");

            String formatted = money.formatForDisplay();

            assertThat(formatted).isEqualTo("1234.57 PLN");
        }
    }

    @Nested
    @DisplayName("equality")
    class Equality {

        @Test
        @DisplayName("equals same Money values")
        void equalsSameValues() {
            Money m1 = Money.pln("100.00");
            Money m2 = Money.pln("100.0000");

            assertThat(m1).isEqualTo(m2);
            assertThat(m1.hashCode()).isEqualTo(m2.hashCode());
        }

        @Test
        @DisplayName("not equals different Money values")
        void notEqualsDifferentValues() {
            Money m1 = Money.pln("100.00");
            Money m2 = Money.pln("100.01");

            assertThat(m1).isNotEqualTo(m2);
        }
    }
}
