package com.investments.tracker.infrastructure.external.xtb;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.investments.tracker.domain.model.value.TransactionType;

/**
 * Parses XTB Cash Operations comment field to extract transaction direction, quantity and price.
 *
 * <p>XTB comment encodes the actual trade direction:
 *
 * <ul>
 *   <li>{@code OPEN BUY 20 @ 16.50} -- BUY: opening a long position
 *   <li>{@code OPEN BUY 3/20 @ 17.00} -- BUY: partial fill
 *   <li>{@code CLOSE BUY 20/60 @ 22.31} -- SELL: closing a long position (selling shares)
 *   <li>{@code OPEN SELL 10 @ 200.00} -- SELL: opening a short position
 *   <li>{@code CLOSE SELL 5/10 @ 100.00} -- BUY: closing a short position (buying back)
 *   <li>{@code OPEN BUY 0.5 @ 150.00} -- BUY: fractional share
 * </ul>
 *
 * <p>The Type column ("Stock purchase"/"Stock sell") does NOT reliably indicate direction. The
 * comment is the source of truth: OPEN BUY and CLOSE SELL are buys; CLOSE BUY and OPEN SELL are
 * sells.
 */
final class XtbCommentParser {

    private static final Pattern TRADE_PATTERN =
            Pattern.compile("(OPEN|CLOSE) (BUY|SELL) ([\\d.]+)(?:/[\\d.]+)? @ ([\\d.]+)");

    private XtbCommentParser() {}

    static Optional<ParsedComment> parse(String comment) {
        if (comment == null || comment.isBlank()) {
            return Optional.empty();
        }

        Matcher matcher = TRADE_PATTERN.matcher(comment);
        if (!matcher.find()) {
            return Optional.empty();
        }

        String action = matcher.group(1);
        String side = matcher.group(2);
        BigDecimal quantity = new BigDecimal(matcher.group(3));
        BigDecimal price = new BigDecimal(matcher.group(4));

        // OPEN BUY = buying shares, CLOSE SELL = buying back short -> both are BUY
        // CLOSE BUY = selling shares, OPEN SELL = shorting -> both are SELL
        TransactionType txType =
                ("OPEN".equals(action) == "BUY".equals(side))
                        ? TransactionType.BUY
                        : TransactionType.SELL;

        return Optional.of(new ParsedComment(txType, quantity, price));
    }

    record ParsedComment(TransactionType transactionType, BigDecimal quantity, BigDecimal price) {}
}
