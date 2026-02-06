package com.investments.tracker.infrastructure.persistence.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity for the positions table.
 */
@Entity
@Table(name = "positions")
public class PositionJpaEntity {

    @Id
    @Column(name = "instrument_symbol", length = 50)
    private String instrumentSymbol;

    @Column(name = "total_quantity", nullable = false, precision = 19, scale = 8)
    private BigDecimal totalQuantity;

    @Column(name = "avg_cost_basis_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal avgCostBasisAmount;

    @Column(name = "avg_cost_basis_currency", nullable = false, length = 3)
    private String avgCostBasisCurrency;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "account_holdings",
            joinColumns = @JoinColumn(name = "instrument_symbol")
    )
    private List<AccountHoldingEmbeddable> holdings = new ArrayList<>();

    @Version
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Default constructor required by JPA.
     */
    public PositionJpaEntity() {
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public String getInstrumentSymbol() {
        return instrumentSymbol;
    }

    public void setInstrumentSymbol(String instrumentSymbol) {
        this.instrumentSymbol = instrumentSymbol;
    }

    public BigDecimal getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(BigDecimal totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public BigDecimal getAvgCostBasisAmount() {
        return avgCostBasisAmount;
    }

    public void setAvgCostBasisAmount(BigDecimal avgCostBasisAmount) {
        this.avgCostBasisAmount = avgCostBasisAmount;
    }

    public String getAvgCostBasisCurrency() {
        return avgCostBasisCurrency;
    }

    public void setAvgCostBasisCurrency(String avgCostBasisCurrency) {
        this.avgCostBasisCurrency = avgCostBasisCurrency;
    }

    public List<AccountHoldingEmbeddable> getHoldings() {
        return holdings;
    }

    public void setHoldings(List<AccountHoldingEmbeddable> holdings) {
        this.holdings = holdings;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
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
