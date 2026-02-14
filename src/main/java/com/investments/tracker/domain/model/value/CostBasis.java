package com.investments.tracker.domain.model.value;

import java.util.Objects;

import com.investments.tracker.domain.exception.InvalidPriceException;

/**
 * Value object representing the average cost basis per unit.
 *
 * <p>Cost basis must be greater than zero. Used to track the average purchase price of shares for
 * P&L calculations.
 */
public record CostBasis(Money money) {

    /** Canonical constructor with validation. */
    public CostBasis {
        Objects.requireNonNull(money, "Cost basis money cannot be null");
        if (money.isNegative()) {
            throw InvalidPriceException.negative(money.amount().toString());
        }
        if (money.isZero()) {
            throw InvalidPriceException.zero();
        }
    }

    /**
     * Creates a CostBasis in PLN from a string amount.
     *
     * @param amount the cost basis amount as string
     * @return new CostBasis in PLN
     */
    public static CostBasis pln(String amount) {
        return new CostBasis(Money.pln(amount));
    }

    /**
     * Creates a CostBasis from Money.
     *
     * @param money the Money value
     * @return new CostBasis
     */
    public static CostBasis of(Money money) {
        return new CostBasis(money);
    }

    /**
     * Gets the currency of this cost basis.
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
