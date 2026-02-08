package com.investments.tracker.application.dto.mapper;

import com.investments.tracker.application.dto.response.AccountHoldingDTO;
import com.investments.tracker.application.dto.response.MoneyDTO;
import com.investments.tracker.application.dto.response.PositionDetailResponse;
import com.investments.tracker.application.dto.response.PositionSummaryDTO;
import com.investments.tracker.domain.model.AccountHolding;
import com.investments.tracker.domain.model.Instrument;
import com.investments.tracker.domain.model.Position;
import com.investments.tracker.domain.model.value.AccountId;
import com.investments.tracker.domain.model.value.CostBasis;
import com.investments.tracker.domain.model.value.CurrentValue;
import com.investments.tracker.domain.model.value.InvestedAmount;
import com.investments.tracker.domain.model.value.Money;
import com.investments.tracker.domain.model.value.Price;
import com.investments.tracker.domain.model.value.ProfitAndLoss;
import com.investments.tracker.domain.model.value.Quantity;
import com.investments.tracker.domain.service.PositionCalculationService;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Mapper for Position domain objects to DTOs.
 */
@Component
public class PositionMapper {

    private final PositionCalculationService positionCalculationService;

    public PositionMapper(PositionCalculationService positionCalculationService) {
        this.positionCalculationService = positionCalculationService;
    }

    /**
     * Maps a Position to PositionSummaryDTO, enriched with Instrument data.
     *
     * @param position the position domain object
     * @param instrument the instrument providing name, type, and current price
     * @return the position summary DTO
     */
    public PositionSummaryDTO toSummaryDTO(Position position, Instrument instrument) {
        PositionCalculations calc = calculateMetrics(position, instrument.currentPrice());

        return new PositionSummaryDTO(
                position.symbol().value(),
                instrument.name().value(),
                instrument.type().name(),
                calc.totalQuantity.toBigDecimal(),
                toMoneyDTO(calc.avgCostBasis.money()),
                calc.currentValue != null ? toMoneyDTO(calc.currentValue.money()) : null,
                toMoneyDTO(calc.investedAmount.money()),
                calc.profitAndLoss != null ? toMoneyDTO(calc.profitAndLoss.amount()) : null,
                calc.profitAndLoss != null ? calc.profitAndLoss.percentage().value() : null);
    }

    /**
     * Maps a Position to PositionDetailResponse, enriched with Instrument data.
     *
     * @param position the position domain object
     * @param instrument the instrument providing name, type, and current price
     * @param accountNames map of account IDs to names for display
     * @return the position detail response
     */
    public PositionDetailResponse toDetailResponse(Position position, Instrument instrument,
                                                   Map<AccountId, String> accountNames) {
        Price currentPrice = instrument.currentPrice();
        PositionCalculations calc = calculateMetrics(position, currentPrice);

        List<AccountHoldingDTO> holdingDTOs = position.holdings().stream()
                .map(holding -> toAccountHoldingDTO(holding, accountNames))
                .toList();

        return new PositionDetailResponse(
                position.symbol().value(),
                instrument.name().value(),
                instrument.type().name(),
                calc.totalQuantity.toBigDecimal(),
                toMoneyDTO(calc.avgCostBasis.money()),
                currentPrice != null ? toMoneyDTO(currentPrice.money()) : null,
                calc.currentValue != null ? toMoneyDTO(calc.currentValue.money()) : null,
                toMoneyDTO(calc.investedAmount.money()),
                calc.profitAndLoss != null ? toMoneyDTO(calc.profitAndLoss.amount()) : null,
                calc.profitAndLoss != null ? calc.profitAndLoss.percentage().value() : null,
                holdingDTOs);
    }

    private PositionCalculations calculateMetrics(Position position, @Nullable Price currentPrice) {
        Quantity totalQuantity = position.calculateTotalQuantity();
        CostBasis avgCostBasis = position.calculateWeightedAverageCostBasis();
        InvestedAmount investedAmount = position.calculateInvestedAmount();

        CurrentValue currentValue = null;
        ProfitAndLoss profitAndLoss = null;

        if (currentPrice != null) {
            currentValue = positionCalculationService.calculateCurrentValue(position, currentPrice);
            profitAndLoss = positionCalculationService.calculateProfitAndLoss(position, currentPrice);
        }

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
            @Nullable CurrentValue currentValue,
            InvestedAmount investedAmount,
            @Nullable ProfitAndLoss profitAndLoss) {
    }
}
