package com.investments.tracker.domain.model.value;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Value object representing a percentage value.
 * <p>
 * Percentage is stored as a decimal (e.g., 10.5 represents 10.5%).
 * Can be positive (gain) or negative (loss).
 * </p>
 */
public record Percentage(BigDecimal value) {

    /**
     * Scale for percentage values.
     */
    public static final int SCALE = 4;

    /**
     * Rounding mode for percentage calculations.
     */
    public static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_EVEN;

    /**
     * 100 for percentage calculations.
     */
    private static final BigDecimal HUNDRED = new BigDecimal("100");

    /**
     * Canonical constructor with validation.
     */
    public Percentage {
        Objects.requireNonNull(value, "Percentage value cannot be null");
        // Normalize the scale to ensure consistent representation
        value = value.setScale(SCALE, ROUNDING_MODE);
    }

    /**
     * Creates a zero percentage.
     *
     * @return zero Percentage
     */
    public static Percentage zero() {
        return new Percentage(BigDecimal.ZERO);
    }

    /**
     * Calculates percentage from a part and a whole.
     * Formula: (part / whole) * 100
     *
     * @param part the part value
     * @param whole the whole value
     * @return the percentage
     * @throws ArithmeticException if whole is zero
     */
    public static Percentage fromFraction(BigDecimal part, BigDecimal whole) {
        Objects.requireNonNull(part, "Part cannot be null");
        Objects.requireNonNull(whole, "Whole cannot be null");
        if (whole.compareTo(BigDecimal.ZERO) == 0) {
            throw new ArithmeticException("Cannot calculate percentage when whole is zero");
        }
        return new Percentage(part.divide(whole, SCALE + 2, ROUNDING_MODE).multiply(HUNDRED));
    }

    /**
     * Calculates percentage from a part and a whole Money.
     * Formula: (part / whole) * 100
     *
     * @param part the part Money value
     * @param whole the whole Money value
     * @return the percentage
     * @throws ArithmeticException if whole is zero
     */
    public static Percentage fromMoney(Money part, Money whole) {
        Objects.requireNonNull(part, "Part cannot be null");
        Objects.requireNonNull(whole, "Whole cannot be null");
        return fromFraction(part.amount(), whole.amount());
    }

    /**
     * Checks if this percentage is positive.
     *
     * @return true if value is positive
     */
    public boolean isPositive() {
        return value.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Checks if this percentage is negative.
     *
     * @return true if value is negative
     */
    public boolean isNegative() {
        return value.compareTo(BigDecimal.ZERO) < 0;
    }

    /**
     * Checks if this percentage is zero.
     *
     * @return true if value is zero
     */
    public boolean isZero() {
        return value.compareTo(BigDecimal.ZERO) == 0;
    }

    /**
     * Adds another Percentage to this one.
     *
     * @param other the Percentage to add
     * @return new Percentage with the sum
     */
    public Percentage add(Percentage other) {
        Objects.requireNonNull(other, "Other percentage cannot be null");
        return new Percentage(this.value.add(other.value));
    }

    /**
     * Formats for display (e.g., "10.50%").
     *
     * @return formatted string
     */
    public String formatForDisplay() {
        return value.setScale(2, ROUNDING_MODE) + "%";
    }

    /**
     * Formats with sign for display (e.g., "+10.50%" or "-5.25%").
     *
     * @return formatted string with sign
     */
    public String formatWithSign() {
        String formatted = value.setScale(2, ROUNDING_MODE).toString();
        if (isPositive()) {
            return "+" + formatted + "%";
        }
        return formatted + "%";
    }
}
