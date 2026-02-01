package com.investments.tracker.domain.model;

import com.investments.tracker.domain.model.value.CurrentValue;
import com.investments.tracker.domain.model.value.InvestedAmount;
import com.investments.tracker.domain.model.value.ProfitAndLoss;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Portfolio aggregate root representing the complete view of all positions.
 * <p>
 * Portfolio aggregates metrics from all positions to provide a unified portfolio view.
 * </p>
 * <p>
 * Portfolio doesn't own positions - it aggregates their metrics dynamically.
 * This is essentially a read model / projection pattern.
 * </p>
 */
public record Portfolio(List<Position> positions, PortfolioMetrics metrics) {

    /**
     * Canonical constructor with validation and defensive copy.
     */
    public Portfolio {
        Objects.requireNonNull(positions, "Positions cannot be null");
        Objects.requireNonNull(metrics, "Metrics cannot be null");
        positions = List.copyOf(positions);
    }

    /**
     * Creates a Portfolio from a list of positions.
     * Calculates aggregated metrics from all positions.
     *
     * @param positions the list of positions
     * @return new Portfolio instance
     */
    public static Portfolio fromPositions(List<Position> positions) {
        Objects.requireNonNull(positions, "Positions cannot be null");

        if (positions.isEmpty()) {
            return new Portfolio(List.of(), PortfolioMetrics.empty());
        }

        PortfolioMetrics metrics = calculateMetrics(positions);
        return new Portfolio(positions, metrics);
    }

    /**
     * Calculates aggregated metrics from positions.
     */
    private static PortfolioMetrics calculateMetrics(List<Position> positions) {
        // Calculate total invested amount
        InvestedAmount totalInvested = positions.stream()
                .map(Position::calculateInvestedAmount)
                .reduce(InvestedAmount::add)
                .orElse(null);

        // Calculate total current value (all positions have prices now)
        CurrentValue totalCurrentValue = positions.stream()
                .map(Position::calculateCurrentValue)
                .reduce(CurrentValue::add)
                .orElse(CurrentValue.zero());

        // Calculate P&L if we have invested amount
        ProfitAndLoss profitAndLoss = null;
        if (totalInvested != null && !totalCurrentValue.isZero()) {
            profitAndLoss = ProfitAndLoss.calculate(totalCurrentValue, totalInvested);
        }

        return new PortfolioMetrics(
                totalInvested,
                totalCurrentValue,
                profitAndLoss,
                positions.size());
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
     * @return current value
     */
    public CurrentValue getTotalCurrentValue() {
        return metrics.totalCurrentValue();
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
