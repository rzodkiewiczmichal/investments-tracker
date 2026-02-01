package com.investments.tracker.domain.model.value;

import java.util.Objects;

/**
 * Value object representing a broker/brokerage company name.
 * <p>
 * BrokerName identifies the financial institution (e.g., "XTB", "mBank", "Degiro").
 * An Account has both a BrokerName (the institution) and an AccountName (user's
 * label for their specific account at that broker, e.g., "My IKE", "Retirement Account").
 * </p>
 * <p>
 * One broker can have multiple accounts (e.g., regular account + IKE at XTB).
 * </p>
 */
public record BrokerName(String value) {

    public BrokerName {
        Objects.requireNonNull(value, "Broker name cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("Broker name cannot be blank");
        }
        value = value.trim();
    }

    public static BrokerName of(String value) {
        return new BrokerName(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
