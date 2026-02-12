package com.investments.tracker.domain.model.value;

import com.investments.tracker.domain.exception.DomainException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Value object representing a monetary amount with currency.
 * <p>
 * Money is immutable and uses DECIMAL(19,4) precision as per ADR-006.
 * All operations return new Money instances.
 * </p>
 */
public record Money(BigDecimal amount, Currency currency) {

    /**
     * Scale for money amounts (4 decimal places for calculation precision).
     */
    public static final int SCALE = 4;

    /**
     * Rounding mode for financial calculations (banker's rounding).
     */
    public static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_EVEN;

    /**
     * Canonical constructor with validation.
     */
    public Money {
        Objects.requireNonNull(amount, "amount cannot be null");
        Objects.requireNonNull(currency, "currency cannot be null");
        if (amount.scale() > SCALE) {
            throw new IllegalArgumentException(
                    "Amount cannot exceed " + SCALE + " decimal places, got: " + amount.scale());
        }
        // Normalize the scale to ensure consistent representation
        amount = amount.setScale(SCALE, ROUNDING_MODE);
    }

    /**
     * Creates Money in PLN with the given amount.
     *
     * @param amount the amount as a string
     * @return Money in PLN
     */
    public static Money pln(String amount) {
        return new Money(new BigDecimal(amount), Currency.PLN);
    }

    /**
     * Creates Money in PLN with the given amount.
     *
     * @param amount the amount as BigDecimal
     * @return Money in PLN
     */
    public static Money pln(BigDecimal amount) {
        return new Money(amount.setScale(SCALE, ROUNDING_MODE), Currency.PLN);
    }

    /**
     * Creates zero Money in PLN.
     *
     * @return zero PLN
     */
    public static Money zero() {
        return new Money(BigDecimal.ZERO, Currency.PLN);
    }

    /**
     * Adds another Money to this one.
     *
     * @param other the Money to add
     * @return new Money with the sum
     * @throws IllegalArgumentException if currencies don't match
     */
    public Money add(Money other) {
        requireSameCurrency(other);
        return new Money(this.amount.add(other.amount), this.currency);
    }

    /**
     * Subtracts another Money from this one.
     *
     * @param other the Money to subtract
     * @return new Money with the difference
     * @throws IllegalArgumentException if currencies don't match
     */
    public Money subtract(Money other) {
        requireSameCurrency(other);
        return new Money(this.amount.subtract(other.amount), this.currency);
    }

    /**
     * Multiplies this Money by a factor.
     *
     * @param factor the multiplication factor
     * @return new Money with the product
     */
    public Money multiply(BigDecimal factor) {
        Objects.requireNonNull(factor, "Factor cannot be null");
        return new Money(
                this.amount.multiply(factor).setScale(SCALE, ROUNDING_MODE),
                this.currency);
    }

    /**
     * Divides this Money by a divisor.
     *
     * @param divisor the divisor
     * @return new Money with the quotient
     * @throws ArithmeticException if divisor is zero
     */
    public Money divide(BigDecimal divisor) {
        Objects.requireNonNull(divisor, "Divisor cannot be null");
        if (divisor.compareTo(BigDecimal.ZERO) == 0) {
            throw new ArithmeticException("Cannot divide by zero");
        }
        return new Money(
                this.amount.divide(divisor, SCALE, ROUNDING_MODE),
                this.currency);
    }

    /**
     * Checks if this Money is positive (greater than zero).
     *
     * @return true if amount is positive
     */
    public boolean isPositive() {
        return amount.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Checks if this Money is negative (less than zero).
     *
     * @return true if amount is negative
     */
    public boolean isNegative() {
        return amount.compareTo(BigDecimal.ZERO) < 0;
    }

    /**
     * Checks if this Money is zero.
     *
     * @return true if amount is zero
     */
    public boolean isZero() {
        return amount.compareTo(BigDecimal.ZERO) == 0;
    }

    /**
     * Negates this Money.
     *
     * @return new Money with negated amount
     */
    public Money negate() {
        return new Money(amount.negate(), currency);
    }

    /**
     * Converts this Money to another currency using the given exchange rate.
     * The exchange rate's source currency must match this Money's currency.
     *
     * @param exchangeRate the exchange rate to apply
     * @return new Money in the target currency
     * @throws DomainException if exchange rate source doesn't match this currency
     */
    public Money convertTo(ExchangeRate exchangeRate) {
        Objects.requireNonNull(exchangeRate, "exchangeRate cannot be null");

        if (!this.currency.equals(exchangeRate.from())) {
            throw new DomainException(
                    "Exchange rate source currency (" + exchangeRate.from() +
                    ") does not match money currency (" + this.currency + ")");
        }

        if (exchangeRate.isIdentity()) {
            return this;
        }

        BigDecimal convertedAmount = this.amount.multiply(exchangeRate.rate())
                .setScale(SCALE, ROUNDING_MODE);
        return new Money(convertedAmount, exchangeRate.to());
    }

    /**
     * Formats for display using currency's decimal places.
     *
     * @return formatted string like "1234.56 PLN"
     */
    public String formatForDisplay() {
        return amount.setScale(currency.getDecimalPlaces(), ROUNDING_MODE) + " " + currency.getCode();
    }

    private void requireSameCurrency(Money other) {
        Objects.requireNonNull(other, "Other money cannot be null");
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                    "Cannot operate on different currencies: " + this.currency + " and " + other.currency);
        }
    }
}
