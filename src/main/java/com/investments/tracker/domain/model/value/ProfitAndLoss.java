package com.investments.tracker.domain.model.value;

import java.util.Objects;

/**
 * Value object representing profit and loss.
 * <p>
 * Contains both the absolute amount (in PLN) and the percentage change.
 * Can be positive (profit) or negative (loss).
 * </p>
 */
public record ProfitAndLoss(Money amount, Percentage percentage) {

    /**
     * Canonical constructor with validation.
     */
    public ProfitAndLoss {
        Objects.requireNonNull(amount, "P&L amount cannot be null");
        Objects.requireNonNull(percentage, "P&L percentage cannot be null");
    }

    /**
     * Creates a zero P&L.
     *
     * @return zero ProfitAndLoss
     */
    public static ProfitAndLoss zero() {
        return new ProfitAndLoss(Money.zero(), Percentage.zero());
    }

    /**
     * Calculates P&L from current value and invested amount.
     * <p>
     * Formula:
     * - Amount: Current Value - Invested Amount
     * - Percentage: (Current Value - Invested Amount) / Invested Amount * 100
     * </p>
     *
     * @param currentValue the current market value
     * @param investedAmount the original invested amount
     * @return the calculated P&L
     */
    public static ProfitAndLoss calculate(CurrentValue currentValue, InvestedAmount investedAmount) {
        Objects.requireNonNull(currentValue, "Current value cannot be null");
        Objects.requireNonNull(investedAmount, "Invested amount cannot be null");

        Money plAmount = currentValue.money().subtract(investedAmount.money());
        Percentage plPercentage = Percentage.fromMoney(plAmount, investedAmount.money());

        return new ProfitAndLoss(plAmount, plPercentage);
    }

    /**
     * Checks if this is a profit (positive P&L).
     *
     * @return true if profit
     */
    public boolean isProfit() {
        return amount.isPositive();
    }

    /**
     * Checks if this is a loss (negative P&L).
     *
     * @return true if loss
     */
    public boolean isLoss() {
        return amount.isNegative();
    }

    /**
     * Checks if this is break-even (zero P&L).
     *
     * @return true if break-even
     */
    public boolean isBreakEven() {
        return amount.isZero();
    }

    /**
     * Gets the currency of this P&L.
     *
     * @return the currency
     */
    public Currency currency() {
        return amount.currency();
    }

    /**
     * Formats the amount for display.
     *
     * @return formatted amount string
     */
    public String formatAmountForDisplay() {
        String formatted = amount.formatForDisplay();
        if (isProfit()) {
            return "+" + formatted;
        }
        return formatted;
    }

    /**
     * Formats the percentage for display.
     *
     * @return formatted percentage string
     */
    public String formatPercentageForDisplay() {
        return percentage.formatWithSign();
    }

    /**
     * Formats both amount and percentage for display.
     *
     * @return formatted string like "+1000.00 PLN (+10.00%)"
     */
    public String formatForDisplay() {
        return formatAmountForDisplay() + " (" + formatPercentageForDisplay() + ")";
    }
}
