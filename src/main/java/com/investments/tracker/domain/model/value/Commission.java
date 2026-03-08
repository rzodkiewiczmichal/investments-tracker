package com.investments.tracker.domain.model.value;

import java.util.Objects;

import com.investments.tracker.domain.exception.DomainException;

/**
 * Value object representing a transaction commission.
 *
 * <p>Wraps {@link Money}, must be non-negative (zero allowed for commission-free trades).
 */
public record Commission(Money money) {

    public Commission {
        Objects.requireNonNull(money, "Commission money cannot be null");
        if (money.isNegative()) {
            throw new DomainException("Commission cannot be negative: " + money.amount());
        }
    }

    public static Commission of(Money money) {
        return new Commission(money);
    }

    public static Commission zero() {
        return new Commission(Money.zero());
    }

    public static Commission pln(String amount) {
        return new Commission(Money.pln(amount));
    }

    public boolean isZero() {
        return money.isZero();
    }

    public Currency currency() {
        return money.currency();
    }

    public String formatForDisplay() {
        return money.formatForDisplay();
    }
}
