package com.investments.tracker.domain.model.value;

import com.investments.tracker.domain.exception.DomainException;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Value object representing an exchange rate between two currencies.
 * <p>
 * Immutable representation of how much 1 unit of the source currency
 * is worth in the target currency.
 * Example: ExchangeRate(GBP, PLN, 5.2500) means 1 GBP = 5.25 PLN.
 * </p>
 */
public record ExchangeRate(Currency from, Currency to, BigDecimal rate) {

    public ExchangeRate {
        Objects.requireNonNull(from, "from cannot be null");
        Objects.requireNonNull(to, "to cannot be null");
        Objects.requireNonNull(rate, "rate cannot be null");

        if (rate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException("Exchange rate must be positive, got: " + rate);
        }

        rate = rate.setScale(Money.SCALE, Money.ROUNDING_MODE);
    }

    /**
     * Creates an identity exchange rate (same currency to same currency).
     *
     * @param currency the currency
     * @return identity exchange rate with rate of 1.0
     */
    public static ExchangeRate identity(Currency currency) {
        return new ExchangeRate(currency, currency, BigDecimal.ONE);
    }

    /**
     * Checks if this is an identity rate (from == to).
     *
     * @return true if same currency
     */
    public boolean isIdentity() {
        return from.equals(to);
    }
}
