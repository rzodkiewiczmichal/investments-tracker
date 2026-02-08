package com.investments.tracker.domain.model;

import com.investments.tracker.domain.exception.DomainException;
import com.investments.tracker.domain.model.value.AccountId;
import com.investments.tracker.domain.model.value.CostBasis;
import com.investments.tracker.domain.model.value.InstrumentSymbol;
import com.investments.tracker.domain.model.value.InvestedAmount;
import com.investments.tracker.domain.model.value.Money;
import com.investments.tracker.domain.model.value.Quantity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Holdings of a specific instrument across accounts.
 * <p>
 * Position is the main aggregate for tracking investments. It contains
 * AccountHoldings for each broker account where the instrument is held.
 * </p>
 * <p>
 * Position does NOT contain currentPrice - that belongs to the Instrument
 * aggregate. Cross-aggregate calculations (CurrentValue, P&L) are performed
 * by PositionCalculationService (see ADR-024).
 * </p>
 * <p>
 * Key invariants:
 * <ul>
 *   <li>Total quantity equals sum of all AccountHolding quantities</li>
 *   <li>Weighted average cost basis correctly calculated from all holdings</li>
 *   <li>All quantities must be positive</li>
 *   <li>No duplicate account holdings for same account</li>
 * </ul>
 * </p>
 */
public record Position(
        InstrumentSymbol symbol,
        List<AccountHolding> holdings) {

    public Position {
        Objects.requireNonNull(symbol, "symbol cannot be null");
        Objects.requireNonNull(holdings, "holdings cannot be null");
        if (holdings.isEmpty()) {
            throw new DomainException("Position must have at least one holding");
        }
        // Create defensive copy to ensure immutability
        holdings = List.copyOf(holdings);
    }

    /**
     * Returns a new Position with an added or merged holding.
     * If a holding for the same account exists, merges with weighted average cost basis.
     *
     * @param accountId the account ID
     * @param quantity the quantity to add
     * @param costBasis the cost basis for the shares
     * @return new Position with the holding added
     */
    public Position addHolding(AccountId accountId, Quantity quantity, CostBasis costBasis) {
        Objects.requireNonNull(accountId, "accountId cannot be null");
        Objects.requireNonNull(quantity, "quantity cannot be null");
        Objects.requireNonNull(costBasis, "costBasis cannot be null");

        List<AccountHolding> newHoldings = new ArrayList<>();
        boolean found = false;

        for (AccountHolding existing : holdings) {
            if (existing.accountId().equals(accountId)) {
                // Merge with existing holding
                newHoldings.add(existing.addShares(quantity, costBasis));
                found = true;
            } else {
                newHoldings.add(existing);
            }
        }

        if (!found) {
            // Add new holding
            newHoldings.add(new AccountHolding(accountId, quantity, costBasis));
        }

        return new Position(this.symbol, newHoldings);
    }

    /**
     * Returns a new Position with the holding for the specified account removed.
     *
     * @param accountId the account ID
     * @return new Position without the holding
     * @throws DomainException if this is the last holding or holding not found
     */
    public Position removeHolding(AccountId accountId) {
        Objects.requireNonNull(accountId, "accountId cannot be null");

        if (holdings.size() == 1) {
            throw new DomainException("Cannot remove last holding - position must be deleted instead");
        }

        List<AccountHolding> newHoldings = holdings.stream()
                .filter(h -> !h.accountId().equals(accountId))
                .toList();

        if (newHoldings.size() == holdings.size()) {
            throw new DomainException("Holding not found for account: " + accountId);
        }

        return new Position(this.symbol, newHoldings);
    }

    /**
     * Finds a holding by account ID.
     *
     * @param accountId the account ID
     * @return optional holding
     */
    public Optional<AccountHolding> findHolding(AccountId accountId) {
        Objects.requireNonNull(accountId, "accountId cannot be null");
        return holdings.stream()
                .filter(h -> h.accountId().equals(accountId))
                .findFirst();
    }

    /**
     * Calculates the total quantity across all holdings.
     *
     * @return the total quantity
     */
    public Quantity calculateTotalQuantity() {
        return holdings.stream()
                .map(AccountHolding::quantity)
                .reduce(Quantity::add)
                .orElseThrow(() -> new DomainException("Position has no holdings"));
    }

    /**
     * Calculates the weighted average cost basis across all holdings.
     * <p>
     * Formula: (sum of qty*cost for each holding) / total quantity
     * </p>
     *
     * @return the weighted average cost basis
     */
    public CostBasis calculateWeightedAverageCostBasis() {
        return calculateWeightedAverageCostBasis(calculateTotalQuantity());
    }

    /**
     * Calculates the weighted average cost basis using pre-computed total quantity.
     *
     * @param totalQuantity the pre-computed total quantity
     * @return the weighted average cost basis
     */
    private CostBasis calculateWeightedAverageCostBasis(Quantity totalQuantity) {
        BigDecimal totalInvested = holdings.stream()
                .map(h -> h.costBasis().money().amount()
                        .multiply(h.quantity().toBigDecimal()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Money avgCost = Money.pln(totalInvested).divide(totalQuantity.toBigDecimal());
        return CostBasis.of(avgCost);
    }

    /**
     * Calculates the total invested amount.
     *
     * @return the invested amount (total quantity * weighted avg cost basis)
     */
    public InvestedAmount calculateInvestedAmount() {
        Quantity totalQuantity = calculateTotalQuantity();
        CostBasis avgCostBasis = calculateWeightedAverageCostBasis(totalQuantity);
        return InvestedAmount.calculate(totalQuantity, avgCostBasis);
    }

    /**
     * Gets the number of accounts holding this position.
     *
     * @return number of accounts
     */
    public int getAccountCount() {
        return holdings.size();
    }

    /**
     * Identity-based equality. Positions are equal if they have the same symbol.
     * This overrides record's default structural equality to follow DDD entity semantics.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Position other)) return false;
        return symbol.equals(other.symbol);
    }

    /**
     * Hash code based on identity (symbol) only.
     */
    @Override
    public int hashCode() {
        return symbol.hashCode();
    }
}
