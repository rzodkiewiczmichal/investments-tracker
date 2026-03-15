package com.investments.tracker.infrastructure.external.xtb;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("XtbCommentParser")
class XtbCommentParserTest {

    @Test
    @DisplayName("should parse simple open buy")
    void shouldParseSimpleOpenBuy() {
        Optional<XtbCommentParser.ParsedComment> result =
                XtbCommentParser.parse("OPEN BUY 20 @ 16.50");

        assertThat(result).isPresent();
        assertThat(result.get().quantity()).isEqualByComparingTo("20");
        assertThat(result.get().price()).isEqualByComparingTo("16.50");
    }

    @Test
    @DisplayName("should parse partial fill buy")
    void shouldParsePartialFillBuy() {
        Optional<XtbCommentParser.ParsedComment> result =
                XtbCommentParser.parse("OPEN BUY 3/20 @ 17.00");

        assertThat(result).isPresent();
        assertThat(result.get().quantity()).isEqualByComparingTo("3");
        assertThat(result.get().price()).isEqualByComparingTo("17.00");
    }

    @Test
    @DisplayName("should parse close buy (sell)")
    void shouldParseCloseBuy() {
        Optional<XtbCommentParser.ParsedComment> result =
                XtbCommentParser.parse("CLOSE BUY 20/60 @ 22.31");

        assertThat(result).isPresent();
        assertThat(result.get().quantity()).isEqualByComparingTo("20");
        assertThat(result.get().price()).isEqualByComparingTo("22.31");
    }

    @Test
    @DisplayName("should return empty for dividend comment")
    void shouldReturnEmptyForDividendComment() {
        Optional<XtbCommentParser.ParsedComment> result =
                XtbCommentParser.parse("MSFT.US USD 0.9100/ SHR");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("should return empty for null comment")
    void shouldReturnEmptyForNull() {
        assertThat(XtbCommentParser.parse(null)).isEmpty();
    }

    @Test
    @DisplayName("should return empty for blank comment")
    void shouldReturnEmptyForBlank() {
        assertThat(XtbCommentParser.parse("   ")).isEmpty();
    }

    @Test
    @DisplayName("should parse large quantity")
    void shouldParseLargeQuantity() {
        Optional<XtbCommentParser.ParsedComment> result =
                XtbCommentParser.parse("OPEN BUY 1000 @ 3.45");

        assertThat(result).isPresent();
        assertThat(result.get().quantity()).isEqualByComparingTo("1000");
        assertThat(result.get().price()).isEqualByComparingTo("3.45");
    }
}
