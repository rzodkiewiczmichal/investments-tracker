package com.investments.tracker.domain.service;

import java.util.Objects;

import com.investments.tracker.domain.exception.DomainException;
import com.investments.tracker.domain.model.Position;
import com.investments.tracker.domain.model.value.CostBasis;
import com.investments.tracker.domain.model.value.Currency;
import com.investments.tracker.domain.model.value.CurrentValue;
import com.investments.tracker.domain.model.value.ExchangeRate;
import com.investments.tracker.domain.model.value.InvestedAmount;
import com.investments.tracker.domain.model.value.Money;
import com.investments.tracker.domain.model.value.Price;
import com.investments.tracker.domain.model.value.ProfitAndLoss;
import com.investments.tracker.domain.model.value.Quantity;

/**
 * Domain service for cross-aggregate position calculations (ADR-024).
 *
 * <p>Handles calculations that require data from both Position and Instrument aggregates (e.g.,
 * CurrentValue and P&L need Position's quantity + Instrument's price).
 *
 * <p>This is a stateless domain service with no dependencies on infrastructure.
 */
public class PositionCalculationService {

    /**
     * Calculates current value for a position in PLN. Calculates in the instrument's native
     * currency first, then converts to PLN.
     *
     * @param position the position (provides quantity)
     * @param currentPrice current market price from Instrument aggregate
     * @param exchangeRate exchange rate from price currency to PLN
     * @return the current value in PLN
     */
    public CurrentValue calculateCurrentValue(
            Position position, Price currentPrice, ExchangeRate exchangeRate) {
        Objects.requireNonNull(position, "position cannot be null");
        Objects.requireNonNull(currentPrice, "currentPrice cannot be null");
        Objects.requireNonNull(exchangeRate, "exchangeRate cannot be null");

        Quantity totalQuantity = position.calculateTotalQuantity();
        CurrentValue nativeValue = CurrentValue.calculate(totalQuantity, currentPrice);

        Money plnMoney = nativeValue.money().convertTo(exchangeRate);
        return new CurrentValue(plnMoney);
    }

    /**
     * Calculates profit and loss for a position in PLN. Both invested amount and current value are
     * converted to PLN before calculating P&L.
     *
     * <p>Precondition: the position's cost basis currency must match the exchange rate's source
     * currency, since the same rate is used for both invested amount and current value conversion.
     * This holds because an instrument's cost basis and price are always denominated in the same
     * currency.
     *
     * @param position the position (provides quantity and cost basis)
     * @param currentPrice current market price from Instrument aggregate
     * @param exchangeRate exchange rate from instrument's native currency to PLN
     * @return the calculated profit and loss in PLN
     */
    public ProfitAndLoss calculateProfitAndLoss(
            Position position, Price currentPrice, ExchangeRate exchangeRate) {
        Objects.requireNonNull(position, "position cannot be null");
        Objects.requireNonNull(currentPrice, "currentPrice cannot be null");
        Objects.requireNonNull(exchangeRate, "exchangeRate cannot be null");

        Currency costBasisCurrency = position.calculateWeightedAverageCostBasis().currency();
        if (!costBasisCurrency.equals(exchangeRate.from())) {
            throw new DomainException(
                    "Exchange rate source currency ("
                            + exchangeRate.from()
                            + ") does not match position cost basis currency ("
                            + costBasisCurrency
                            + ")");
        }

        CurrentValue currentValuePln = calculateCurrentValue(position, currentPrice, exchangeRate);

        InvestedAmount nativeInvested = position.calculateInvestedAmount();
        Money plnInvestedMoney = nativeInvested.money().convertTo(exchangeRate);
        InvestedAmount investedAmountPln = new InvestedAmount(plnInvestedMoney);

        return ProfitAndLoss.calculate(currentValuePln, investedAmountPln);
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
        InvestedAmount existingInvested =
                InvestedAmount.calculate(existingQuantity, existingCostBasis);
        InvestedAmount newInvested =
                InvestedAmount.calculate(additionalQuantity, purchaseCostBasis);

        Quantity totalQuantity = existingQuantity.add(additionalQuantity);
        InvestedAmount totalInvested = existingInvested.add(newInvested);

        return CostBasis.of(totalInvested.money().divide(totalQuantity.toBigDecimal()));
    }
}
