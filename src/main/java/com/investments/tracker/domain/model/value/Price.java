package com.investments.tracker.domain.model.value;

import com.investments.tracker.domain.exception.InvalidPriceException;

import java.util.Objects;

/**
 * Value object representing a price per unit.
 * <p>
 * Price must be greater than zero. Wraps Money to ensure
 * the positivity constraint is enforced.
 * </p>
 */
public record Price(Money money) {

    /**
     * Canonical constructor with validation.
     */
    public Price {
        Objects.requireNonNull(money, "Price money cannot be null");
        if (money.isNegative()) {
            throw InvalidPriceException.negative(money.amount().toString());
        }
        if (money.isZero()) {
            throw InvalidPriceException.zero();
        }
    }

    /**
     * Creates a Price in PLN from a string amount.
     *
     * @param amount the price amount as string
     * @return new Price in PLN
     */
    public static Price pln(String amount) {
        return new Price(Money.pln(amount));
    }

    /**
     * Gets the currency of this price.
     *
     * @return the currency
     */
    public Currency currency() {
        return money.currency();
    }

    /**
     * Formats for display.
     *
     * @return formatted string
     */
    public String formatForDisplay() {
        return money.formatForDisplay();
    }
}
