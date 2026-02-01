package com.investments.tracker.domain.service;

import com.investments.tracker.domain.model.Position;
import com.investments.tracker.domain.model.value.CostBasis;
import com.investments.tracker.domain.model.value.CurrentValue;
import com.investments.tracker.domain.model.value.InvestedAmount;
import com.investments.tracker.domain.model.value.Price;
import com.investments.tracker.domain.model.value.ProfitAndLoss;
import com.investments.tracker.domain.model.value.Quantity;

import java.util.Objects;

/**
 * Domain service for position-related calculations.
 * <p>
 * Provides calculations that don't naturally belong to a single entity,
 * such as cost basis recalculation scenarios.
 * </p>
 * <p>
 * Note: Most calculations are available directly on Position aggregate.
 * Use this service for operations that span multiple value objects or
 * require parameters not owned by Position.
 * </p>
 * <p>
 * This is a stateless domain service with no dependencies on infrastructure.
 * </p>
 */
public class PositionCalculationService {

    /**
     * Calculates current value for a position given a different price
     * than the one stored in the position.
     *
     * @param position the position
     * @param currentPrice the current price to use
     * @return the current value
     */
    public CurrentValue calculateCurrentValue(Position position, Price currentPrice) {
        Objects.requireNonNull(position, "position cannot be null");
        Objects.requireNonNull(currentPrice, "currentPrice cannot be null");

        Quantity totalQuantity = position.calculateTotalQuantity();
        return CurrentValue.calculate(totalQuantity, currentPrice);
    }

    /**
     * Calculates the new weighted average cost basis after adding shares.
     *
     * @param existingQuantity the existing quantity
     * @param existingCostBasis the existing cost basis
     * @param additionalQuantity the quantity being added
     * @param purchaseCostBasis the cost basis of new shares
     * @return the new weighted average cost basis
     */
    public CostBasis calculateNewCostBasisAfterPurchase(
            Quantity existingQuantity,
            CostBasis existingCostBasis,
            Quantity additionalQuantity,
            CostBasis purchaseCostBasis) {

        Objects.requireNonNull(existingQuantity, "existingQuantity cannot be null");
        Objects.requireNonNull(existingCostBasis, "existingCostBasis cannot be null");
        Objects.requireNonNull(additionalQuantity, "additionalQuantity cannot be null");
        Objects.requireNonNull(purchaseCostBasis, "purchaseCostBasis cannot be null");

        // (existing qty * existing cost + new qty * new cost) / total qty
        InvestedAmount existingInvested = InvestedAmount.calculate(existingQuantity, existingCostBasis);
        InvestedAmount newInvested = InvestedAmount.calculate(additionalQuantity, purchaseCostBasis);

        Quantity totalQuantity = existingQuantity.add(additionalQuantity);
        InvestedAmount totalInvested = existingInvested.add(newInvested);

        return CostBasis.of(totalInvested.money().divide(totalQuantity.toBigDecimal()));
    }
}
