package com.investments.tracker.infrastructure.external.finnhub;

import java.math.BigDecimal;

/**
 * JSON response from Finnhub quote endpoint.
 *
 * @param c current price
 * @param d change
 * @param dp percent change
 * @param h high price of the day
 * @param l low price of the day
 * @param o open price of the day
 * @param pc previous close price
 * @param t timestamp
 * @see <a href="https://finnhub.io/docs/api/quote">Finnhub Quote API</a>
 */
record FinnhubQuoteResponse(
        BigDecimal c,
        BigDecimal d,
        BigDecimal dp,
        BigDecimal h,
        BigDecimal l,
        BigDecimal o,
        BigDecimal pc,
        Long t) {

    /**
     * A quote is invalid when the current price is null or zero. Finnhub returns all-zero fields
     * for unknown or delisted symbols.
     */
    boolean isInvalid() {
        return c == null || c.compareTo(BigDecimal.ZERO) == 0;
    }
}
