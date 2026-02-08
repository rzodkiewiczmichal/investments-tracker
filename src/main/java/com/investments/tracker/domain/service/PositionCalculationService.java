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
 * Domain service for cross-aggregate position calculations (ADR-024).
 * <p>
 * Handles calculations that require data from both Position and Instrument
 * aggregates (e.g., CurrentValue and P&L need Position's quantity + Instrument's price).
 * </p>
 * <p>
 * This is a stateless domain service with no dependencies on infrastructure.
 * </p>
 */
public class PositionCalculationService {

    /**
     * Calculates current value for a position.
     *
     * @param position the position (provides quantity)
     * @param currentPrice current market price from Instrument aggregate
     * @return the current value
     */
    public CurrentValue calculateCurrentValue(Position position, Price currentPrice) {
        Objects.requireNonNull(position, "position cannot be null");
        Objects.requireNonNull(currentPrice, "currentPrice cannot be null");

        Quantity totalQuantity = position.calculateTotalQuantity();
        return CurrentValue.calculate(totalQuantity, currentPrice);
    }

    /**
     * Calculates profit and loss for a position.
     *
     * @param position the position (provides quantity and cost basis)
     * @param currentPrice current market price from Instrument aggregate
     * @return the calculated profit and loss with percentage
     */
    public ProfitAndLoss calculateProfitAndLoss(Position position, Price currentPrice) {
        CurrentValue currentValue = calculateCurrentValue(position, currentPrice);
        InvestedAmount investedAmount = position.calculateInvestedAmount();
        return ProfitAndLoss.calculate(currentValue, investedAmount);
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
