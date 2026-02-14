package com.investments.tracker.domain.model;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.investments.tracker.domain.model.value.CurrentValue;
import com.investments.tracker.domain.model.value.InvestedAmount;
import com.investments.tracker.domain.model.value.ProfitAndLoss;

/**
 * Portfolio representing the complete view of all positions.
 *
 * <p>Portfolio doesn't own positions - it aggregates their metrics. This is essentially a read
 * model / projection pattern.
 *
 * <p>Metrics are calculated externally by PortfolioCalculationService (which needs price data from
 * Instrument aggregate).
 */
public record Portfolio(List<Position> positions, PortfolioMetrics metrics) {

    /** Canonical constructor with validation and defensive copy. */
    public Portfolio {
        Objects.requireNonNull(positions, "Positions cannot be null");
        Objects.requireNonNull(metrics, "Metrics cannot be null");
        positions = List.copyOf(positions);
    }

    /**
     * Checks if this portfolio is empty.
     *
     * @return true if no positions
     */
    public boolean isEmpty() {
        return positions.isEmpty();
    }

    /**
     * Gets the number of positions.
     *
     * @return position count
     */
    public int getPositionCount() {
        return positions.size();
    }

    /**
     * Gets total invested amount.
     *
     * @return invested amount, empty if portfolio is empty
     */
    public Optional<InvestedAmount> getTotalInvestedAmount() {
        return Optional.ofNullable(metrics.totalInvestedAmount());
    }

    /**
     * Gets total current value.
     *
     * @return current value, empty if prices are unknown
     */
    public Optional<CurrentValue> getTotalCurrentValue() {
        return Optional.ofNullable(metrics.totalCurrentValue());
    }

    /**
     * Gets total P&L.
     *
     * @return P&L, empty if unavailable
     */
    public Optional<ProfitAndLoss> getTotalProfitAndLoss() {
        return Optional.ofNullable(metrics.profitAndLoss());
    }
}
