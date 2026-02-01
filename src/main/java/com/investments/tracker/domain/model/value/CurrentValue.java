package com.investments.tracker.domain.model.value;

import java.util.Objects;

/**
 * Value object representing the current market value.
 * <p>
 * Calculated as Quantity × Current Price. Typically positive for positions with holdings.
 * </p>
 */
public record CurrentValue(Money money) {

    /**
     * Canonical constructor with validation.
     */
    public CurrentValue {
        Objects.requireNonNull(money, "Current value money cannot be null");
        if (money.isNegative()) {
            throw new IllegalArgumentException("Current value cannot be negative, got: " + money.amount());
        }
    }

    /**
     * Creates a zero CurrentValue in PLN.
     *
     * @return zero CurrentValue
     */
    public static CurrentValue zero() {
        return new CurrentValue(Money.zero());
    }

    /**
     * Creates a CurrentValue in PLN from a string amount.
     *
     * @param amount the current value as string
     * @return new CurrentValue in PLN
     */
    public static CurrentValue pln(String amount) {
        return new CurrentValue(Money.pln(amount));
    }

    /**
     * Calculates current value from quantity and price.
     *
     * @param quantity the quantity of shares
     * @param price the current price per share
     * @return the current value
     */
    public static CurrentValue calculate(Quantity quantity, Price price) {
        Objects.requireNonNull(quantity, "Quantity cannot be null");
        Objects.requireNonNull(price, "Price cannot be null");
        return new CurrentValue(price.money().multiply(quantity.toBigDecimal()));
    }

    /**
     * Adds another CurrentValue to this one.
     *
     * @param other the CurrentValue to add
     * @return new CurrentValue with the sum
     */
    public CurrentValue add(CurrentValue other) {
        Objects.requireNonNull(other, "Other current value cannot be null");
        return new CurrentValue(this.money.add(other.money));
    }

    /**
     * Checks if this value is zero.
     *
     * @return true if zero
     */
    public boolean isZero() {
        return money.isZero();
    }

    /**
     * Gets the currency of this current value.
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
