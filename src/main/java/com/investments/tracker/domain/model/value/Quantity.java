package com.investments.tracker.domain.model.value;

import com.investments.tracker.domain.exception.InvalidQuantityException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Value object representing a quantity of shares/units.
 * <p>
 * Quantity must be greater than zero and uses DECIMAL(19,8) precision
 * to support fractional shares as per ADR-006.
 * </p>
 */
public record Quantity(BigDecimal value) {

    /**
     * Scale for quantity values (8 decimal places for fractional shares).
     */
    public static final int SCALE = 8;

    /**
     * Rounding mode for quantity calculations.
     */
    public static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_EVEN;

    /**
     * Canonical constructor with validation.
     */
    public Quantity {
        Objects.requireNonNull(value, "Quantity value cannot be null");
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw InvalidQuantityException.negative(value.toString());
        }
        if (value.compareTo(BigDecimal.ZERO) == 0) {
            throw InvalidQuantityException.zero();
        }
        if (value.scale() > SCALE) {
            throw InvalidQuantityException.exceedsScale(value.scale(), SCALE);
        }
        // Normalize the scale to ensure consistent representation
        value = value.setScale(SCALE, ROUNDING_MODE);
    }

    /**
     * Creates a Quantity from a string value.
     *
     * @param value the quantity as a string
     * @return new Quantity
     */
    public static Quantity of(String value) {
        return new Quantity(new BigDecimal(value));
    }

    /**
     * Creates a Quantity from an integer value.
     *
     * @param value the quantity as an integer
     * @return new Quantity
     */
    public static Quantity of(int value) {
        return new Quantity(BigDecimal.valueOf(value));
    }

    /**
     * Adds another Quantity to this one.
     *
     * @param other the Quantity to add
     * @return new Quantity with the sum
     */
    public Quantity add(Quantity other) {
        Objects.requireNonNull(other, "Other quantity cannot be null");
        return new Quantity(this.value.add(other.value));
    }

    /**
     * Subtracts another Quantity from this one.
     *
     * @param other the Quantity to subtract
     * @return new Quantity with the difference
     * @throws InvalidQuantityException if result would be zero or negative
     */
    public Quantity subtract(Quantity other) {
        Objects.requireNonNull(other, "Other quantity cannot be null");
        BigDecimal result = this.value.subtract(other.value);
        if (result.compareTo(BigDecimal.ZERO) < 0) {
            throw InvalidQuantityException.negative(result.toString());
        }
        if (result.compareTo(BigDecimal.ZERO) == 0) {
            throw InvalidQuantityException.zero();
        }
        return new Quantity(result);
    }

    /**
     * Multiplies this Quantity by a factor.
     *
     * @param factor the multiplication factor
     * @return new Quantity with the product
     * @throws InvalidQuantityException if factor is zero or negative
     */
    public Quantity multiply(BigDecimal factor) {
        Objects.requireNonNull(factor, "Factor cannot be null");
        if (factor.compareTo(BigDecimal.ZERO) < 0) {
            throw InvalidQuantityException.negative(factor.toString());
        }
        if (factor.compareTo(BigDecimal.ZERO) == 0) {
            throw InvalidQuantityException.zero();
        }
        return new Quantity(this.value.multiply(factor).setScale(SCALE, ROUNDING_MODE));
    }

    /**
     * Converts quantity to a BigDecimal for calculations.
     *
     * @return the underlying BigDecimal value
     */
    public BigDecimal toBigDecimal() {
        return value;
    }

    /**
     * Formats for display (removes trailing zeros).
     *
     * @return formatted string
     */
    public String formatForDisplay() {
        return value.stripTrailingZeros().toPlainString();
    }
}
