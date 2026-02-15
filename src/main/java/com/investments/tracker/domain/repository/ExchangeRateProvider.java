package com.investments.tracker.domain.repository;

import java.util.Collection;
import java.util.Map;

import com.investments.tracker.domain.exception.ExchangeRateNotFoundException;
import com.investments.tracker.domain.model.value.Currency;
import com.investments.tracker.domain.model.value.ExchangeRate;

/**
 * Port for retrieving current exchange rates.
 *
 * <p>This is a driven port that will be implemented by infrastructure adapters (e.g., external API,
 * database cache, mock for tests).
 */
public interface ExchangeRateProvider {

    /**
     * Gets the current exchange rate to convert from the source currency to PLN. If source is PLN,
     * returns an identity rate (1.0).
     *
     * @param source the source currency
     * @return exchange rate from source to PLN
     * @throws ExchangeRateNotFoundException if rate cannot be found
     */
    ExchangeRate getExchangeRateToPln(Currency source);

    /**
     * Gets all exchange rates to PLN for multiple currencies in a single call. More efficient than
     * calling getExchangeRateToPln multiple times.
     *
     * @param currencies the currencies to get rates for
     * @return map of currency to exchange rate to PLN
     * @throws ExchangeRateNotFoundException if any rate cannot be found
     */
    Map<Currency, ExchangeRate> getExchangeRatesToPln(Collection<Currency> currencies);
}
