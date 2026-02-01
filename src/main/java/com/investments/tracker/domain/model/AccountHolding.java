package com.investments.tracker.domain.model;

import com.investments.tracker.domain.model.value.AccountId;
import com.investments.tracker.domain.model.value.CostBasis;
import com.investments.tracker.domain.model.value.InvestedAmount;
import com.investments.tracker.domain.model.value.Quantity;

import java.util.Objects;

/**
 * A holding of an instrument in a specific account.
 * <p>
 * AccountHolding is part of the Position aggregate. It has a composite
 * identity of InstrumentSymbol + AccountId, but the InstrumentSymbol
 * is managed by the parent Position.
 * </p>
 * <p>
 * Contains the quantity held in this account and the cost basis for
 * shares purchased through this account.
 * </p>
 */
public record AccountHolding(AccountId accountId, Quantity quantity, CostBasis costBasis) {

    public AccountHolding {
        Objects.requireNonNull(accountId, "Account ID cannot be null");
        Objects.requireNonNull(quantity, "Quantity cannot be null");
        Objects.requireNonNull(costBasis, "Cost basis cannot be null");
    }

    /**
     * Returns a new AccountHolding with additional shares added.
     * Recalculates weighted average cost basis.
     *
     * @param additionalQuantity the quantity to add
     * @param purchaseCostBasis the cost basis for the new shares
     * @return new AccountHolding with combined quantity and recalculated cost basis
     */
    public AccountHolding addShares(Quantity additionalQuantity, CostBasis purchaseCostBasis) {
        Objects.requireNonNull(additionalQuantity, "Additional quantity cannot be null");
        Objects.requireNonNull(purchaseCostBasis, "Purchase cost basis cannot be null");

        // Calculate new weighted average cost basis
        // (existing qty * existing cost + new qty * new cost) / total qty
        InvestedAmount existingInvested = InvestedAmount.calculate(this.quantity, this.costBasis);
        InvestedAmount newInvested = InvestedAmount.calculate(additionalQuantity, purchaseCostBasis);

        Quantity newTotalQuantity = this.quantity.add(additionalQuantity);
        InvestedAmount totalInvested = existingInvested.add(newInvested);

        // New cost basis = total invested / total quantity
        CostBasis newCostBasis = CostBasis.of(
                totalInvested.money().divide(newTotalQuantity.toBigDecimal()));

        return new AccountHolding(this.accountId, newTotalQuantity, newCostBasis);
    }

}
