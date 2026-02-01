package com.investments.tracker.domain.model;

import com.investments.tracker.domain.model.value.AccountId;
import com.investments.tracker.domain.model.value.AccountName;
import com.investments.tracker.domain.model.value.BrokerName;

import java.util.Objects;

/**
 * A brokerage account holding investments.
 * <p>
 * Account is identified by its AccountId. Account types (IKE, IKZE, regular)
 * are tracked but don't affect calculations.
 * </p>
 */
public record Account(AccountId id, AccountName name, BrokerName brokerName) {

    public Account {
        Objects.requireNonNull(id, "Account ID cannot be null");
        Objects.requireNonNull(name, "Account name cannot be null");
        Objects.requireNonNull(brokerName, "Broker name cannot be null");
    }

    /**
     * Identity-based equality. Accounts are equal if they have the same ID.
     * This overrides record's default structural equality to follow DDD entity semantics.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Account other)) return false;
        return id.equals(other.id);
    }

    /**
     * Hash code based on identity (ID) only.
     */
    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
