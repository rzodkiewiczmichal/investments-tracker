package com.investments.tracker.infrastructure.persistence.mapper;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.investments.tracker.domain.model.AccountHolding;
import com.investments.tracker.domain.model.Position;
import com.investments.tracker.domain.model.value.AccountId;
import com.investments.tracker.domain.model.value.CostBasis;
import com.investments.tracker.domain.model.value.Currency;
import com.investments.tracker.domain.model.value.InstrumentSymbol;
import com.investments.tracker.domain.model.value.Money;
import com.investments.tracker.domain.model.value.Quantity;
import com.investments.tracker.infrastructure.persistence.entity.AccountHoldingJdbcEntity;
import com.investments.tracker.infrastructure.persistence.entity.PositionJdbcEntity;

/** Mapper for converting between Position domain model and PositionJdbcEntity. */
@Component
public class PositionPersistenceMapper {

    /**
     * Converts a JDBC entity to a domain model.
     *
     * @param entity the JDBC entity to convert
     * @return the domain Position
     */
    public Position toDomain(PositionJdbcEntity entity) {
        Objects.requireNonNull(entity, "entity cannot be null");

        List<AccountHolding> holdings =
                entity.holdings().stream().map(this::mapToAccountHolding).toList();

        return new Position(new InstrumentSymbol(entity.instrumentSymbol()), holdings);
    }

    /**
     * Converts a domain model to a JDBC entity.
     *
     * @param position the domain Position to convert
     * @param version the current version for optimistic locking (null for new entities)
     * @return the JDBC entity
     */
    public PositionJdbcEntity toEntity(Position position, Long version) {
        Objects.requireNonNull(position, "position cannot be null");

        CostBasis avgCostBasis = position.calculateWeightedAverageCostBasis();

        Set<AccountHoldingJdbcEntity> holdingEntities =
                position.holdings().stream().map(this::mapToJdbcEntity).collect(Collectors.toSet());

        return new PositionJdbcEntity(
                position.symbol().value(),
                position.calculateTotalQuantity().toBigDecimal(),
                avgCostBasis.money().amount(),
                avgCostBasis.currency().getCode(),
                holdingEntities,
                version);
    }

    private AccountHolding mapToAccountHolding(AccountHoldingJdbcEntity entity) {
        Currency currency = Currency.valueOf(entity.costBasisCurrency());
        Money costBasisMoney = new Money(entity.costBasisAmount(), currency);

        return new AccountHolding(
                new AccountId(entity.accountId()),
                new Quantity(entity.quantity()),
                CostBasis.of(costBasisMoney));
    }

    private AccountHoldingJdbcEntity mapToJdbcEntity(AccountHolding holding) {
        return new AccountHoldingJdbcEntity(
                holding.accountId().value(),
                holding.quantity().toBigDecimal(),
                holding.costBasis().money().amount(),
                holding.costBasis().currency().getCode());
    }
}
