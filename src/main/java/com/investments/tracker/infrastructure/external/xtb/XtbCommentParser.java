package com.investments.tracker.infrastructure.external.xtb;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses XTB Cash Operations comment field to extract quantity and price.
 *
 * <p>Comment patterns:
 *
 * <ul>
 *   <li>{@code OPEN BUY 20 @ 16.50} — simple buy, qty=20, price=16.50
 *   <li>{@code OPEN BUY 3/20 @ 17.00} — partial fill buy, qty=3, price=17.00
 *   <li>{@code CLOSE BUY 20/60 @ 22.31} — sell (close), qty=20, price=22.31
 * </ul>
 */
final class XtbCommentParser {

    private static final Pattern TRADE_PATTERN =
            Pattern.compile("(OPEN|CLOSE) BUY (\\d+)(?:/\\d+)? @ ([\\d.]+)");

    private XtbCommentParser() {}

    static Optional<ParsedComment> parse(String comment) {
        if (comment == null || comment.isBlank()) {
            return Optional.empty();
        }

        Matcher matcher = TRADE_PATTERN.matcher(comment);
        if (!matcher.find()) {
            return Optional.empty();
        }

        BigDecimal quantity = new BigDecimal(matcher.group(2));
        BigDecimal price = new BigDecimal(matcher.group(3));

        return Optional.of(new ParsedComment(quantity, price));
    }

    record ParsedComment(BigDecimal quantity, BigDecimal price) {}
}
