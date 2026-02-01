package com.investments.tracker.domain.model.value;

import com.investments.tracker.domain.exception.InvalidQuantityException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Quantity value object")
class QuantityTest {

    @Nested
    @DisplayName("creation")
    class Creation {

        @Test
        @DisplayName("creates Quantity with valid value")
        void createsWithValidValue() {
            Quantity quantity = Quantity.of("100");

            assertThat(quantity.value()).isEqualByComparingTo("100.00000000");
        }

        @Test
        @DisplayName("creates Quantity from integer")
        void createsFromInteger() {
            Quantity quantity = Quantity.of(50);

            assertThat(quantity.value()).isEqualByComparingTo("50.00000000");
        }

        @Test
        @DisplayName("creates Quantity with fractional value")
        void createsWithFractionalValue() {
            Quantity quantity = Quantity.of("10.12345678");

            assertThat(quantity.value()).isEqualByComparingTo("10.12345678");
        }

        @Test
        @DisplayName("throws exception for null value")
        void throwsForNullValue() {
            assertThatThrownBy(() -> new Quantity(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("throws exception for zero value")
        void throwsForZeroValue() {
            assertThatThrownBy(() -> Quantity.of("0"))
                    .isInstanceOf(InvalidQuantityException.class)
                    .hasMessageContaining("cannot be zero");
        }

        @Test
        @DisplayName("throws exception for negative value")
        void throwsForNegativeValue() {
            assertThatThrownBy(() -> Quantity.of("-10"))
                    .isInstanceOf(InvalidQuantityException.class)
                    .hasMessageContaining("cannot be negative");
        }

        @Test
        @DisplayName("throws exception for too many decimal places")
        void throwsForTooManyDecimalPlaces() {
            assertThatThrownBy(() -> Quantity.of("10.123456789"))
                    .isInstanceOf(InvalidQuantityException.class)
                    .hasMessageContaining("cannot exceed 8 decimal places");
        }
    }

    @Nested
    @DisplayName("arithmetic operations")
    class ArithmeticOperations {

        @Test
        @DisplayName("adds two Quantity values")
        void addsQuantities() {
            Quantity q1 = Quantity.of("100");
            Quantity q2 = Quantity.of("50.5");

            Quantity result = q1.add(q2);

            assertThat(result.value()).isEqualByComparingTo("150.5");
        }

        @Test
        @DisplayName("subtracts two Quantity values")
        void subtractsQuantities() {
            Quantity q1 = Quantity.of("100");
            Quantity q2 = Quantity.of("30");

            Quantity result = q1.subtract(q2);

            assertThat(result.value()).isEqualByComparingTo("70");
        }

        @Test
        @DisplayName("throws exception when subtraction results in zero")
        void throwsWhenSubtractionResultsInZero() {
            Quantity q1 = Quantity.of("50");
            Quantity q2 = Quantity.of("50");

            assertThatThrownBy(() -> q1.subtract(q2))
                    .isInstanceOf(InvalidQuantityException.class)
                    .hasMessageContaining("cannot be zero");
        }

        @Test
        @DisplayName("multiplies Quantity by factor")
        void multipliesByFactor() {
            Quantity quantity = Quantity.of("100");

            Quantity result = quantity.multiply(new BigDecimal("1.5"));

            assertThat(result.value()).isEqualByComparingTo("150");
        }

        @Test
        @DisplayName("throws exception when multiplying by zero factor")
        void throwsWhenMultiplyingByZeroFactor() {
            Quantity quantity = Quantity.of("100");

            assertThatThrownBy(() -> quantity.multiply(BigDecimal.ZERO))
                    .isInstanceOf(InvalidQuantityException.class)
                    .hasMessageContaining("cannot be zero");
        }
    }

    @Nested
    @DisplayName("formatting")
    class Formatting {

        @Test
        @DisplayName("formats Quantity for display (strips trailing zeros)")
        void formatsForDisplay() {
            Quantity quantity = Quantity.of("100.50000000");

            String formatted = quantity.formatForDisplay();

            assertThat(formatted).isEqualTo("100.5");
        }

        @Test
        @DisplayName("formats integer Quantity for display")
        void formatsIntegerForDisplay() {
            Quantity quantity = Quantity.of(100);

            String formatted = quantity.formatForDisplay();

            assertThat(formatted).isEqualTo("100");
        }
    }

    @Nested
    @DisplayName("equality")
    class Equality {

        @Test
        @DisplayName("equals same Quantity values")
        void equalsSameValues() {
            Quantity q1 = Quantity.of("100");
            Quantity q2 = Quantity.of("100.00000000");

            assertThat(q1).isEqualTo(q2);
            assertThat(q1.hashCode()).isEqualTo(q2.hashCode());
        }
    }
}
