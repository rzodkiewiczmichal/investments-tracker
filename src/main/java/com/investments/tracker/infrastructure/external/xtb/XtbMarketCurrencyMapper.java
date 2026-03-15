package com.investments.tracker.infrastructure.external.xtb;

import java.util.Map;

import com.investments.tracker.domain.model.value.Currency;

/**
 * Maps XTB ticker market suffixes to currencies.
 *
 * <p>XTB tickers use SYMBOL.MARKET format (e.g., MSFT.US, CDR.PL). The market suffix determines the
 * trading currency.
 */
final class XtbMarketCurrencyMapper {

    private static final Map<String, Currency> MARKET_TO_CURRENCY =
            Map.of(
                    "US", Currency.USD,
                    "PL", Currency.PLN,
                    "UK", Currency.GBP,
                    "DE", Currency.EUR,
                    "NL", Currency.EUR,
                    "FR", Currency.EUR,
                    "IT", Currency.EUR,
                    "DK", Currency.DKK);

    private XtbMarketCurrencyMapper() {}

    /**
     * Resolves currency from a ticker's market suffix.
     *
     * @param ticker the full ticker (e.g., "MSFT.US", "CDR.PL")
     * @return the currency, or PLN as fallback for unknown markets
     */
    static Currency resolveCurrency(String ticker) {
        int dotIndex = ticker.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == ticker.length() - 1) {
            return Currency.PLN;
        }
        String market = ticker.substring(dotIndex + 1);
        return MARKET_TO_CURRENCY.getOrDefault(market, Currency.PLN);
    }
}
