package com.investments.tracker.infrastructure.persistence.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite key class for AccountHoldingJpaEntity.
 */
public class AccountHoldingId implements Serializable {

    private String instrumentSymbol;
    private Long accountId;

    public AccountHoldingId() {
        // Required no-arg constructor
    }

    public AccountHoldingId(String instrumentSymbol, Long accountId) {
        this.instrumentSymbol = instrumentSymbol;
        this.accountId = accountId;
    }

    public String getInstrumentSymbol() {
        return instrumentSymbol;
    }

    public void setInstrumentSymbol(String instrumentSymbol) {
        this.instrumentSymbol = instrumentSymbol;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AccountHoldingId that)) return false;
        return Objects.equals(instrumentSymbol, that.instrumentSymbol)
                && Objects.equals(accountId, that.accountId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(instrumentSymbol, accountId);
    }
}
