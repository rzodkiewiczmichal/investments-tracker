package com.investments.tracker.testutils;

import com.investments.tracker.domain.model.value.Currency;
import com.investments.tracker.domain.model.value.ExchangeRate;
import com.investments.tracker.domain.repository.ExchangeRateProvider;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Stub implementation of ExchangeRateProvider for integration tests.
 * Returns 1:1 rates to PLN for all currencies, preserving existing test behavior.
 */
@Component
@Primary
public class StubExchangeRateProvider implements ExchangeRateProvider {

    @Override
    public ExchangeRate getExchangeRateToPln(Currency source) {
        if (source == Currency.PLN) {
            return ExchangeRate.identity(Currency.PLN);
        }
        return new ExchangeRate(source, Currency.PLN, BigDecimal.ONE);
    }

    @Override
    public Map<Currency, ExchangeRate> getExchangeRatesToPln(Iterable<Currency> currencies) {
        Map<Currency, ExchangeRate> rates = new HashMap<>();
        for (Currency currency : currencies) {
            rates.put(currency, getExchangeRateToPln(currency));
        }
        return rates;
    }
}
