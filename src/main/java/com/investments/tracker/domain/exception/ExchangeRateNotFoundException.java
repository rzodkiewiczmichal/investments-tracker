package com.investments.tracker.domain.exception;

import com.investments.tracker.domain.model.value.Currency;

/** Thrown when an exchange rate cannot be found for a currency pair. */
public class ExchangeRateNotFoundException extends DomainException {

    public ExchangeRateNotFoundException(Currency source, Currency target) {
        super("Exchange rate not found: " + source.getCode() + " -> " + target.getCode());
    }
}
