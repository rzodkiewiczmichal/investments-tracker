package com.investments.tracker.domain.model.value;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.investments.tracker.domain.exception.InvalidSymbolException;

@DisplayName("InstrumentSymbol value object")
class InstrumentSymbolTest {

    @Nested
    @DisplayName("creation")
    class Creation {

        @Test
        @DisplayName("creates InstrumentSymbol from valid ticker")
        void createsFromValidTicker() {
            InstrumentSymbol symbol = InstrumentSymbol.of("AAPL");

            assertThat(symbol.value()).isEqualTo("AAPL");
            assertThat(symbol.isTicker()).isTrue();
            assertThat(symbol.isIsin()).isFalse();
        }

        @Test
        @DisplayName("creates InstrumentSymbol from valid ISIN")
        void createsFromValidIsin() {
            InstrumentSymbol symbol = InstrumentSymbol.of("US0378331005");

            assertThat(symbol.value()).isEqualTo("US0378331005");
            assertThat(symbol.isIsin()).isTrue();
            assertThat(symbol.isTicker()).isFalse();
        }

        @Test
        @DisplayName("creates InstrumentSymbol from Polish ISIN")
        void createsFromPolishIsin() {
            InstrumentSymbol symbol = InstrumentSymbol.of("PLPKO0000016");

            assertThat(symbol.value()).isEqualTo("PLPKO0000016");
            assertThat(symbol.isIsin()).isTrue();
        }

        @Test
        @DisplayName("normalizes to uppercase")
        void normalizesToUppercase() {
            InstrumentSymbol symbol = InstrumentSymbol.of("aapl");

            assertThat(symbol.value()).isEqualTo("AAPL");
        }

        @Test
        @DisplayName("trims whitespace")
        void trimsWhitespace() {
            InstrumentSymbol symbol = InstrumentSymbol.of("  AAPL  ");

            assertThat(symbol.value()).isEqualTo("AAPL");
        }

        @Test
        @DisplayName("throws exception for null value")
        void throwsForNullValue() {
            assertThatThrownBy(() -> InstrumentSymbol.of(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("throws exception for blank value")
        void throwsForBlankValue() {
            assertThatThrownBy(() -> InstrumentSymbol.of("   "))
                    .isInstanceOf(InvalidSymbolException.class)
                    .hasMessageContaining("cannot be blank");
        }

        @Test
        @DisplayName("throws exception for invalid format")
        void throwsForInvalidFormat() {
            assertThatThrownBy(() -> InstrumentSymbol.of("invalid-symbol!"))
                    .isInstanceOf(InvalidSymbolException.class)
                    .hasMessageContaining("Invalid instrument symbol format");
        }

        @Test
        @DisplayName("throws exception for too long ticker")
        void throwsForTooLongTicker() {
            assertThatThrownBy(() -> InstrumentSymbol.of("VERYLONGTICKER"))
                    .isInstanceOf(InvalidSymbolException.class)
                    .hasMessageContaining("Invalid instrument symbol format");
        }
    }

    @Nested
    @DisplayName("ISIN detection")
    class IsinDetection {

        @Test
        @DisplayName("detects valid US ISIN")
        void detectsUsIsin() {
            InstrumentSymbol symbol = InstrumentSymbol.of("US0378331005");
            assertThat(symbol.isIsin()).isTrue();
        }

        @Test
        @DisplayName("detects valid Polish ISIN")
        void detectsPolishIsin() {
            InstrumentSymbol symbol = InstrumentSymbol.of("PLPKO0000016");
            assertThat(symbol.isIsin()).isTrue();
        }

        @Test
        @DisplayName("does not detect ticker as ISIN")
        void doesNotDetectTickerAsIsin() {
            InstrumentSymbol symbol = InstrumentSymbol.of("CDR");
            assertThat(symbol.isIsin()).isFalse();
            assertThat(symbol.isTicker()).isTrue();
        }
    }

    @Nested
    @DisplayName("equality")
    class Equality {

        @Test
        @DisplayName("equals same symbol values")
        void equalsSameValues() {
            InstrumentSymbol s1 = InstrumentSymbol.of("AAPL");
            InstrumentSymbol s2 = InstrumentSymbol.of("AAPL");

            assertThat(s1).isEqualTo(s2);
            assertThat(s1.hashCode()).isEqualTo(s2.hashCode());
        }

        @Test
        @DisplayName("equals same symbol with different case")
        void equalsSameSymbolDifferentCase() {
            InstrumentSymbol s1 = InstrumentSymbol.of("AAPL");
            InstrumentSymbol s2 = InstrumentSymbol.of("aapl");

            assertThat(s1).isEqualTo(s2);
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringTest {

        @Test
        @DisplayName("returns symbol value")
        void returnsSymbolValue() {
            InstrumentSymbol symbol = InstrumentSymbol.of("AAPL");

            assertThat(symbol.toString()).isEqualTo("AAPL");
        }
    }
}
