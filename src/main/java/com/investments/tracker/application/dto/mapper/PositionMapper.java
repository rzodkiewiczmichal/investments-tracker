package com.investments.tracker.application.dto.mapper;

import com.investments.tracker.application.dto.response.AccountHoldingDTO;
import com.investments.tracker.application.dto.response.MoneyDTO;
import com.investments.tracker.application.dto.response.PositionDetailResponse;
import com.investments.tracker.application.dto.response.PositionSummaryDTO;
import com.investments.tracker.domain.model.AccountHolding;
import com.investments.tracker.domain.model.Position;
import com.investments.tracker.domain.model.value.AccountId;
import com.investments.tracker.domain.model.value.CostBasis;
import com.investments.tracker.domain.model.value.CurrentValue;
import com.investments.tracker.domain.model.value.InvestedAmount;
import com.investments.tracker.domain.model.value.Money;
import com.investments.tracker.domain.model.value.ProfitAndLoss;
import com.investments.tracker.domain.model.value.Quantity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Mapper for Position domain objects to DTOs.
 */
@Component
public class PositionMapper {

    /**
     * Maps a Position to PositionSummaryDTO.
     *
     * @param position the position domain object
     * @return the position summary DTO
     */
    public PositionSummaryDTO toSummaryDTO(Position position) {
        PositionCalculations calc = calculateMetrics(position);

        return new PositionSummaryDTO(
                position.symbol().value(),
                calc.totalQuantity.toBigDecimal(),
                toMoneyDTO(calc.avgCostBasis.money()),
                toMoneyDTO(position.currentPrice().money()),
                toMoneyDTO(calc.currentValue.money()),
                toMoneyDTO(calc.investedAmount.money()),
                toMoneyDTO(calc.profitAndLoss.amount()),
                calc.profitAndLoss.percentage().value());
    }

    /**
     * Maps a Position to PositionDetailResponse.
     *
     * @param position the position domain object
     * @param accountNames map of account IDs to names for display
     * @return the position detail response
     */
    public PositionDetailResponse toDetailResponse(Position position, Map<AccountId, String> accountNames) {
        PositionCalculations calc = calculateMetrics(position);

        List<AccountHoldingDTO> holdingDTOs = position.holdings().stream()
                .map(holding -> toAccountHoldingDTO(holding, accountNames))
                .toList();

        return new PositionDetailResponse(
                position.symbol().value(),
                calc.totalQuantity.toBigDecimal(),
                toMoneyDTO(calc.avgCostBasis.money()),
                toMoneyDTO(position.currentPrice().money()),
                toMoneyDTO(calc.currentValue.money()),
                toMoneyDTO(calc.investedAmount.money()),
                toMoneyDTO(calc.profitAndLoss.amount()),
                calc.profitAndLoss.percentage().value(),
                holdingDTOs);
    }

    private PositionCalculations calculateMetrics(Position position) {
        Quantity totalQuantity = position.calculateTotalQuantity();
        CostBasis avgCostBasis = position.calculateWeightedAverageCostBasis();
        CurrentValue currentValue = position.calculateCurrentValue();
        InvestedAmount investedAmount = position.calculateInvestedAmount();
        ProfitAndLoss profitAndLoss = position.calculateProfitAndLoss();

        return new PositionCalculations(
                totalQuantity, avgCostBasis, currentValue, investedAmount, profitAndLoss);
    }

    private AccountHoldingDTO toAccountHoldingDTO(AccountHolding holding, Map<AccountId, String> accountNames) {
        String accountName = accountNames.getOrDefault(holding.accountId(), "Unknown Account");

        return new AccountHoldingDTO(
                holding.accountId().value(),
                accountName,
                holding.quantity().toBigDecimal(),
                toMoneyDTO(holding.costBasis().money()));
    }

    /** Converts Money to MoneyDTO. */
    private MoneyDTO toMoneyDTO(Money money) {
        return new MoneyDTO(money.amount(), money.currency().getCode());
    }

    private record PositionCalculations(
            Quantity totalQuantity,
            CostBasis avgCostBasis,
            CurrentValue currentValue,
            InvestedAmount investedAmount,
            ProfitAndLoss profitAndLoss) {
    }
}
