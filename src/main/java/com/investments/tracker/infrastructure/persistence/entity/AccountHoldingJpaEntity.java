package com.investments.tracker.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA entity representing an account holding in the database.
 */
@Entity
@Table(name = "account_holdings")
@IdClass(AccountHoldingId.class)
public class AccountHoldingJpaEntity {

    @Id
    @Column(name = "instrument_symbol", length = 50)
    private String instrumentSymbol;

    @Id
    @Column(name = "account_id")
    private Long accountId;

    @Column(name = "quantity", precision = 19, scale = 8, nullable = false)
    private BigDecimal quantity;

    @Column(name = "cost_basis_amount", precision = 19, scale = 4, nullable = false)
    private BigDecimal costBasisAmount;

    @Column(name = "cost_basis_currency", length = 3, nullable = false)
    private String costBasisCurrency;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected AccountHoldingJpaEntity() {
        // JPA required no-arg constructor
    }

    public AccountHoldingJpaEntity(String instrumentSymbol, Long accountId, BigDecimal quantity,
                                   BigDecimal costBasisAmount, String costBasisCurrency) {
        this.instrumentSymbol = instrumentSymbol;
        this.accountId = accountId;
        this.quantity = quantity;
        this.costBasisAmount = costBasisAmount;
        this.costBasisCurrency = costBasisCurrency;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
