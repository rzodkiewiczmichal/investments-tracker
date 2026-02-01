package com.investments.tracker.domain.model.value;

import java.util.Objects;

/**
 * Value object representing the total invested amount.
 * <p>
 * Calculated as Quantity × CostBasis. Must be greater than zero.
 * </p>
 */
public record InvestedAmount(Money money) {

    /**
     * Canonical constructor with validation.
     */
    public InvestedAmount {
        Objects.requireNonNull(money, "Invested amount money cannot be null");
        if (!money.isPositive()) {
            throw new IllegalArgumentException("Invested amount must be positive, got: " + money.amount());
        }
    }

    /**
     * Creates an InvestedAmount in PLN from a string amount.
     *
     * @param amount the invested amount as string
     * @return new InvestedAmount in PLN
     */
    public static InvestedAmount pln(String amount) {
        return new InvestedAmount(Money.pln(amount));
    }

    /**
     * Calculates invested amount from quantity and cost basis.
     *
     * @param quantity the quantity of shares
     * @param costBasis the cost basis per share
     * @return the invested amount
     */
    public static InvestedAmount calculate(Quantity quantity, CostBasis costBasis) {
        Objects.requireNonNull(quantity, "Quantity cannot be null");
        Objects.requireNonNull(costBasis, "Cost basis cannot be null");
        return new InvestedAmount(costBasis.money().multiply(quantity.toBigDecimal()));
    }

    /**
     * Adds another InvestedAmount to this one.
     *
     * @param other the InvestedAmount to add
     * @return new InvestedAmount with the sum
     */
    public InvestedAmount add(InvestedAmount other) {
        Objects.requireNonNull(other, "Other invested amount cannot be null");
        return new InvestedAmount(this.money.add(other.money));
    }

    /**
     * Gets the currency of this invested amount.
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
