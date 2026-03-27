package com.investments.tracker.infrastructure.external.xtb;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.investments.tracker.domain.model.value.TransactionType;

@DisplayName("XtbCommentParser")
class XtbCommentParserTest {

    @Test
    @DisplayName("should parse OPEN BUY as BUY")
    void shouldParseOpenBuyAsBuy() {
        Optional<XtbCommentParser.ParsedComment> result =
                XtbCommentParser.parse("OPEN BUY 20 @ 16.50");

        assertThat(result).isPresent();
        assertThat(result.get().transactionType()).isEqualTo(TransactionType.BUY);
        assertThat(result.get().quantity()).isEqualByComparingTo("20");
        assertThat(result.get().price()).isEqualByComparingTo("16.50");
    }

    @Test
    @DisplayName("should parse partial fill OPEN BUY as BUY")
    void shouldParsePartialFillOpenBuyAsBuy() {
        Optional<XtbCommentParser.ParsedComment> result =
                XtbCommentParser.parse("OPEN BUY 3/20 @ 17.00");

        assertThat(result).isPresent();
        assertThat(result.get().transactionType()).isEqualTo(TransactionType.BUY);
        assertThat(result.get().quantity()).isEqualByComparingTo("3");
        assertThat(result.get().price()).isEqualByComparingTo("17.00");
    }

    @Test
    @DisplayName("should parse CLOSE BUY as SELL (closing long = selling shares)")
    void shouldParseCloseBuyAsSell() {
        Optional<XtbCommentParser.ParsedComment> result =
                XtbCommentParser.parse("CLOSE BUY 20/60 @ 22.31");

        assertThat(result).isPresent();
        assertThat(result.get().transactionType()).isEqualTo(TransactionType.SELL);
        assertThat(result.get().quantity()).isEqualByComparingTo("20");
        assertThat(result.get().price()).isEqualByComparingTo("22.31");
    }

    @Test
    @DisplayName("should parse CLOSE SELL as BUY (closing short = buying back)")
    void shouldParseCloseSellAsBuy() {
        Optional<XtbCommentParser.ParsedComment> result =
                XtbCommentParser.parse("CLOSE SELL 5/10 @ 100.00");

        assertThat(result).isPresent();
        assertThat(result.get().transactionType()).isEqualTo(TransactionType.BUY);
        assertThat(result.get().quantity()).isEqualByComparingTo("5");
        assertThat(result.get().price()).isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("should parse OPEN SELL as SELL (opening short = selling)")
    void shouldParseOpenSellAsSell() {
        Optional<XtbCommentParser.ParsedComment> result =
                XtbCommentParser.parse("OPEN SELL 10 @ 200.00");

        assertThat(result).isPresent();
        assertThat(result.get().transactionType()).isEqualTo(TransactionType.SELL);
        assertThat(result.get().quantity()).isEqualByComparingTo("10");
        assertThat(result.get().price()).isEqualByComparingTo("200.00");
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
        assertThat(result.get().transactionType()).isEqualTo(TransactionType.BUY);
        assertThat(result.get().quantity()).isEqualByComparingTo("1000");
        assertThat(result.get().price()).isEqualByComparingTo("3.45");
    }

    @Test
    @DisplayName("should parse fractional share quantity")
    void shouldParseFractionalShareQuantity() {
        Optional<XtbCommentParser.ParsedComment> result =
                XtbCommentParser.parse("OPEN BUY 0.5 @ 150.00");

        assertThat(result).isPresent();
        assertThat(result.get().transactionType()).isEqualTo(TransactionType.BUY);
        assertThat(result.get().quantity()).isEqualByComparingTo("0.5");
        assertThat(result.get().price()).isEqualByComparingTo("150.00");
    }

    @Test
    @DisplayName("should parse fractional partial fill")
    void shouldParseFractionalPartialFill() {
        Optional<XtbCommentParser.ParsedComment> result =
                XtbCommentParser.parse("OPEN BUY 0.3/0.5 @ 150.00");

        assertThat(result).isPresent();
        assertThat(result.get().transactionType()).isEqualTo(TransactionType.BUY);
        assertThat(result.get().quantity()).isEqualByComparingTo("0.3");
        assertThat(result.get().price()).isEqualByComparingTo("150.00");
    }
}
