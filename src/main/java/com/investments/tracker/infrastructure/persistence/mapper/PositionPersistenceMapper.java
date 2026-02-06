package com.investments.tracker.infrastructure.persistence.mapper;

import com.investments.tracker.domain.model.AccountHolding;
import com.investments.tracker.domain.model.Position;
import com.investments.tracker.domain.model.value.AccountId;
import com.investments.tracker.domain.model.value.CostBasis;
import com.investments.tracker.domain.model.value.Currency;
import com.investments.tracker.domain.model.value.InstrumentSymbol;
import com.investments.tracker.domain.model.value.Money;
import com.investments.tracker.domain.model.value.Price;
import com.investments.tracker.domain.model.value.Quantity;
import com.investments.tracker.infrastructure.persistence.entity.AccountHoldingEmbeddable;
import com.investments.tracker.infrastructure.persistence.entity.PositionJpaEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * Mapper for converting between Position domain model and PositionJpaEntity.
 */
@Component
public class PositionPersistenceMapper {

    /**
     * Converts a JPA entity to a domain model.
     * CRITICAL: Requires non-null currentPrice parameter.
     *
     * @param entity the JPA entity to convert
     * @param currentPrice the current price of the instrument (must not be null)
     * @return the domain Position
     * @throws NullPointerException if entity or currentPrice is null
     */
    public Position toDomain(PositionJpaEntity entity, Price currentPrice) {
        Objects.requireNonNull(entity, "entity cannot be null");
        Objects.requireNonNull(currentPrice, "currentPrice cannot be null");

        List<AccountHolding> holdings = entity.getHoldings().stream()
                .map(this::mapToAccountHolding)
                .toList();

        return new Position(
                new InstrumentSymbol(entity.getInstrumentSymbol()),
                holdings,
                currentPrice
        );
    }

    /**
     * Converts a domain model to a JPA entity.
     * Calculates totalQuantity and avgCostBasis from position methods.
     *
     * @param position the domain Position to convert
     * @return the JPA entity
     */
    public PositionJpaEntity toEntity(Position position) {
        Objects.requireNonNull(position, "position cannot be null");

        PositionJpaEntity entity = new PositionJpaEntity();
        entity.setInstrumentSymbol(position.symbol().value());
        entity.setTotalQuantity(position.calculateTotalQuantity().toBigDecimal());

        CostBasis avgCostBasis = position.calculateWeightedAverageCostBasis();
        entity.setAvgCostBasisAmount(avgCostBasis.money().amount());
        entity.setAvgCostBasisCurrency(avgCostBasis.currency().getCode());

        List<AccountHoldingEmbeddable> holdingEmbeddables = position.holdings().stream()
                .map(this::mapToEmbeddable)
                .toList();
        entity.setHoldings(holdingEmbeddables);

        return entity;
    }

    /**
     * Maps AccountHoldingEmbeddable to domain AccountHolding.
     *
     * @param embeddable the JPA embeddable
     * @return the domain AccountHolding
     */
    private AccountHolding mapToAccountHolding(AccountHoldingEmbeddable embeddable) {
        Currency currency = Currency.valueOf(embeddable.getCostBasisCurrency());
        Money costBasisMoney = new Money(embeddable.getCostBasisAmount(), currency);

        return new AccountHolding(
                new AccountId(embeddable.getAccountId()),
                new Quantity(embeddable.getQuantity()),
                CostBasis.of(costBasisMoney)
        );
    }

    /**
     * Maps domain AccountHolding to AccountHoldingEmbeddable.
     *
     * @param holding the domain AccountHolding
     * @return the JPA embeddable
     */
    private AccountHoldingEmbeddable mapToEmbeddable(AccountHolding holding) {
        AccountHoldingEmbeddable embeddable = new AccountHoldingEmbeddable();
        embeddable.setAccountId(holding.accountId().value());
        embeddable.setQuantity(holding.quantity().toBigDecimal());
        embeddable.setCostBasisAmount(holding.costBasis().money().amount());
        embeddable.setCostBasisCurrency(holding.costBasis().currency().getCode());
        return embeddable;
    }
}
