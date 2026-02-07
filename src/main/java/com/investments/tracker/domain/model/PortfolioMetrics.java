package com.investments.tracker.domain.model;

import com.investments.tracker.domain.model.value.CurrentValue;
import com.investments.tracker.domain.model.value.InvestedAmount;
import com.investments.tracker.domain.model.value.ProfitAndLoss;

/**
 * Immutable record holding calculated portfolio metrics.
 * Note: totalInvestedAmount and profitAndLoss may be null when portfolio is empty.
 */
public record PortfolioMetrics(
        InvestedAmount totalInvestedAmount,
        CurrentValue totalCurrentValue,
        ProfitAndLoss profitAndLoss,
        int totalPositions) {

    public static PortfolioMetrics empty() {
        return new PortfolioMetrics(
                null,
                CurrentValue.zero(),
                null,
                0);
    }

    /**
     * Formats a summary for display.
     *
     * @return formatted summary
     */
    public String formatSummary() {
        if (totalPositions == 0) {
            return "No positions yet";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Total Value: ").append(totalCurrentValue.formatForDisplay());

        if (totalInvestedAmount != null) {
            sb.append("\nInvested: ").append(totalInvestedAmount.formatForDisplay());
        }

        if (profitAndLoss != null) {
            sb.append("\nP&L: ").append(profitAndLoss.formatForDisplay());
        }

        sb.append("\nPositions: ").append(totalPositions);

        return sb.toString();
    }
}
