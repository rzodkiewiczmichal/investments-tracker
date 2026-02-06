package com.investments.tracker.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Embeddable JPA class for account holdings within a position.
 */
@Embeddable
public class AccountHoldingEmbeddable {

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal quantity;

    @Column(name = "cost_basis_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal costBasisAmount;

    @Column(name = "cost_basis_currency", nullable = false, length = 3)
    private String costBasisCurrency;

    /**
     * Default constructor required by JPA.
     */
    public AccountHoldingEmbeddable() {
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getCostBasisAmount() {
        return costBasisAmount;
    }

    public void setCostBasisAmount(BigDecimal costBasisAmount) {
        this.costBasisAmount = costBasisAmount;
    }

    public String getCostBasisCurrency() {
        return costBasisCurrency;
    }

    public void setCostBasisCurrency(String costBasisCurrency) {
        this.costBasisCurrency = costBasisCurrency;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AccountHoldingEmbeddable other)) return false;
        return Objects.equals(accountId, other.accountId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId);
    }
}
