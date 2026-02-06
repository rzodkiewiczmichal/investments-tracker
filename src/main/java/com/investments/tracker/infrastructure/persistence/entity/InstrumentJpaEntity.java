package com.investments.tracker.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA entity for the instruments table.
 */
@Entity
@Table(name = "instruments")
public class InstrumentJpaEntity {

    @Id
    @Column(length = 50)
    private String symbol;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "instrument_type", nullable = false)
    private JpaInstrumentType instrumentType;

    @Column(name = "current_price_amount", precision = 19, scale = 4)
    private BigDecimal currentPriceAmount;

    @Column(name = "current_price_currency", length = 3)
    private String currentPriceCurrency;

    @Column(name = "price_updated_at")
    private LocalDateTime priceUpdatedAt;

    @Version
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Default constructor required by JPA.
     */
    public InstrumentJpaEntity() {
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

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public JpaInstrumentType getInstrumentType() {
        return instrumentType;
    }

    public void setInstrumentType(JpaInstrumentType instrumentType) {
        this.instrumentType = instrumentType;
    }

    public BigDecimal getCurrentPriceAmount() {
        return currentPriceAmount;
    }

    public void setCurrentPriceAmount(BigDecimal currentPriceAmount) {
        this.currentPriceAmount = currentPriceAmount;
    }

    public String getCurrentPriceCurrency() {
        return currentPriceCurrency;
    }

    public void setCurrentPriceCurrency(String currentPriceCurrency) {
        this.currentPriceCurrency = currentPriceCurrency;
    }

    public LocalDateTime getPriceUpdatedAt() {
        return priceUpdatedAt;
    }

    public void setPriceUpdatedAt(LocalDateTime priceUpdatedAt) {
        this.priceUpdatedAt = priceUpdatedAt;
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
