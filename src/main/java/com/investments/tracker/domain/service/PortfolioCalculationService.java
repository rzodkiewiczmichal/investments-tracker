package com.investments.tracker.domain.service;

import com.investments.tracker.domain.model.Portfolio;
import com.investments.tracker.domain.model.PortfolioMetrics;
import com.investments.tracker.domain.model.Position;
import com.investments.tracker.domain.model.value.CurrentValue;
import com.investments.tracker.domain.model.value.InstrumentSymbol;
import com.investments.tracker.domain.model.value.InvestedAmount;
import com.investments.tracker.domain.model.value.Price;
import com.investments.tracker.domain.model.value.ProfitAndLoss;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Domain service for portfolio-level calculations.
 * <p>
 * Aggregates metrics from individual positions to provide
 * portfolio-level totals and summaries. Requires price data
 * from Instrument aggregate for current value and P&L calculations.
 * </p>
 * <p>
 * This is a stateless domain service with no dependencies on infrastructure.
 * </p>
 */
public class PortfolioCalculationService {

    private final PositionCalculationService positionCalculationService;

    public PortfolioCalculationService(PositionCalculationService positionCalculationService) {
        this.positionCalculationService = Objects.requireNonNull(
                positionCalculationService, "positionCalculationService cannot be null");
    }

    /**
     * Creates a Portfolio from positions and current prices.
     *
     * @param positions the list of positions
     * @param currentPrices map of instrument symbol to current price (only for instruments with known prices)
     * @return the Portfolio with calculated metrics
     */
    public Portfolio createPortfolio(List<Position> positions, Map<InstrumentSymbol, Price> currentPrices) {
        Objects.requireNonNull(positions, "positions cannot be null");
        Objects.requireNonNull(currentPrices, "currentPrices cannot be null");

        if (positions.isEmpty()) {
            return new Portfolio(List.of(), PortfolioMetrics.empty());
        }

        PortfolioMetrics metrics = calculateMetrics(positions, currentPrices);
        return new Portfolio(positions, metrics);
    }

    /**
     * Calculates total invested amount across all positions.
     * Does not require price data.
     *
     * @param positions the list of positions
     * @return optional invested amount (empty if no positions)
     */
    public Optional<InvestedAmount> calculateTotalInvestedAmount(List<Position> positions) {
        Objects.requireNonNull(positions, "positions cannot be null");

        if (positions.isEmpty()) {
            return Optional.empty();
        }

        return positions.stream()
                .map(Position::calculateInvestedAmount)
                .reduce(InvestedAmount::add);
    }

    /**
     * Calculates total current value across all positions.
     * Returns empty if any position lacks a current price.
     *
     * @param positions the list of positions
     * @param currentPrices map of instrument symbol to current price
     * @return optional current value (empty if any position lacks price)
     */
    public Optional<CurrentValue> calculateTotalCurrentValue(
            List<Position> positions, Map<InstrumentSymbol, Price> currentPrices) {
        Objects.requireNonNull(positions, "positions cannot be null");
        Objects.requireNonNull(currentPrices, "currentPrices cannot be null");

        if (positions.isEmpty()) {
            return Optional.empty();
        }

        boolean allPricesAvailable = positions.stream()
                .allMatch(p -> currentPrices.containsKey(p.symbol()));

        if (!allPricesAvailable) {
            return Optional.empty();
        }

        return positions.stream()
                .map(p -> positionCalculationService.calculateCurrentValue(p, currentPrices.get(p.symbol())))
                .reduce(CurrentValue::add);
    }

    /**
     * Calculates total P&L across all positions.
     * Returns empty if any position lacks a current price.
     *
     * @param positions the list of positions
     * @param currentPrices map of instrument symbol to current price
     * @return optional P&L (empty if prices are incomplete)
     */
    public Optional<ProfitAndLoss> calculateTotalProfitAndLoss(
            List<Position> positions, Map<InstrumentSymbol, Price> currentPrices) {
        Objects.requireNonNull(positions, "positions cannot be null");
        Objects.requireNonNull(currentPrices, "currentPrices cannot be null");

        Optional<InvestedAmount> totalInvested = calculateTotalInvestedAmount(positions);
        Optional<CurrentValue> totalCurrentValue = calculateTotalCurrentValue(positions, currentPrices);

        if (totalInvested.isEmpty() || totalCurrentValue.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(ProfitAndLoss.calculate(totalCurrentValue.get(), totalInvested.get()));
    }

    private PortfolioMetrics calculateMetrics(List<Position> positions, Map<InstrumentSymbol, Price> currentPrices) {
        InvestedAmount totalInvested = calculateTotalInvestedAmount(positions).orElse(null);
        CurrentValue totalCurrentValue = calculateTotalCurrentValue(positions, currentPrices).orElse(null);

        ProfitAndLoss profitAndLoss = null;
        if (totalInvested != null && totalCurrentValue != null) {
            profitAndLoss = ProfitAndLoss.calculate(totalCurrentValue, totalInvested);
        }

        return new PortfolioMetrics(
                totalInvested,
                totalCurrentValue,
                profitAndLoss,
                positions.size());
    }
}
