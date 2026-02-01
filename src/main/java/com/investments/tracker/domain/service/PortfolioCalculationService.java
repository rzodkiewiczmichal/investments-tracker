package com.investments.tracker.domain.service;

import com.investments.tracker.domain.model.Portfolio;
import com.investments.tracker.domain.model.PortfolioMetrics;
import com.investments.tracker.domain.model.Position;
import com.investments.tracker.domain.model.value.CurrentValue;
import com.investments.tracker.domain.model.value.InvestedAmount;
import com.investments.tracker.domain.model.value.ProfitAndLoss;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Domain service for portfolio-level calculations.
 * <p>
 * Aggregates metrics from individual positions to provide
 * portfolio-level totals and summaries.
 * </p>
 * <p>
 * This is a stateless domain service with no dependencies on infrastructure.
 * </p>
 */
public class PortfolioCalculationService {

    /**
     * Creates a Portfolio from a list of positions.
     *
     * @param positions the list of positions
     * @return the Portfolio
     */
    public Portfolio createPortfolio(List<Position> positions) {
        Objects.requireNonNull(positions, "Positions cannot be null");
        return Portfolio.fromPositions(positions);
    }

    /**
     * Calculates total invested amount across all positions.
     *
     * @param positions the list of positions
     * @return optional invested amount (empty if no positions)
     */
    public Optional<InvestedAmount> calculateTotalInvestedAmount(List<Position> positions) {
        Objects.requireNonNull(positions, "Positions cannot be null");

        if (positions.isEmpty()) {
            return Optional.empty();
        }

        return positions.stream()
                .map(Position::calculateInvestedAmount)
                .reduce(InvestedAmount::add);
    }

    /**
     * Calculates total current value across all positions.
     *
     * @param positions the list of positions
     * @return the total current value (zero if no positions)
     */
    public CurrentValue calculateTotalCurrentValue(List<Position> positions) {
        Objects.requireNonNull(positions, "Positions cannot be null");

        return positions.stream()
                .map(Position::calculateCurrentValue)
                .reduce(CurrentValue::add)
                .orElse(CurrentValue.zero());
    }

    /**
     * Calculates total P&L across all positions.
     *
     * @param positions the list of positions
     * @return optional P&L (empty if no valid data)
     */
    public Optional<ProfitAndLoss> calculateTotalProfitAndLoss(List<Position> positions) {
        Objects.requireNonNull(positions, "Positions cannot be null");

        Optional<InvestedAmount> totalInvested = calculateTotalInvestedAmount(positions);
        CurrentValue totalCurrentValue = calculateTotalCurrentValue(positions);

        if (totalInvested.isEmpty() || totalCurrentValue.isZero()) {
            return Optional.empty();
        }

        return Optional.of(ProfitAndLoss.calculate(totalCurrentValue, totalInvested.get()));
    }

    /**
     * Creates a summary of portfolio metrics.
     *
     * @param positions the list of positions
     * @return portfolio metrics summary
     */
    public PortfolioMetrics calculatePortfolioMetrics(List<Position> positions) {
        Portfolio portfolio = createPortfolio(positions);
        return portfolio.metrics();
    }
}
