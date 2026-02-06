package com.investments.tracker.infrastructure.persistence.mapper;

import com.investments.tracker.domain.model.Instrument;
import com.investments.tracker.domain.model.value.Currency;
import com.investments.tracker.domain.model.value.InstrumentName;
import com.investments.tracker.domain.model.value.InstrumentSymbol;
import com.investments.tracker.domain.model.value.InstrumentType;
import com.investments.tracker.domain.model.value.Money;
import com.investments.tracker.domain.model.value.Price;
import com.investments.tracker.infrastructure.persistence.entity.InstrumentJpaEntity;
import com.investments.tracker.infrastructure.persistence.entity.JpaInstrumentType;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Mapper for converting between Instrument domain model and InstrumentJpaEntity.
 */
@Component
public class InstrumentPersistenceMapper {

    /**
     * Converts a JPA entity to a domain model.
     *
     * @param entity the JPA entity to convert
     * @return the domain Instrument
     */
    public Instrument toDomain(InstrumentJpaEntity entity) {
        Objects.requireNonNull(entity, "entity cannot be null");

        Price currentPrice = createPriceFromEntity(entity);

        return new Instrument(
                new InstrumentSymbol(entity.getSymbol()),
                new InstrumentName(entity.getName()),
                mapToDomainType(entity.getInstrumentType()),
                currentPrice
        );
    }

    /**
     * Converts a domain model to a JPA entity.
     * Sets priceUpdatedAt to the current time.
     *
     * @param instrument the domain Instrument to convert
     * @return the JPA entity
     */
    public InstrumentJpaEntity toEntity(Instrument instrument) {
        Objects.requireNonNull(instrument, "instrument cannot be null");
        InstrumentJpaEntity entity = new InstrumentJpaEntity();
        entity.setSymbol(instrument.symbol().value());
        entity.setName(instrument.name().value());
        entity.setInstrumentType(mapToJpaType(instrument.type()));
        entity.setCurrentPriceAmount(instrument.currentPrice().money().amount());
        entity.setCurrentPriceCurrency(instrument.currentPrice().currency().getCode());
        entity.setPriceUpdatedAt(LocalDateTime.now());
        return entity;
    }

    /**
     * Creates a Price from entity's currentPriceAmount and currentPriceCurrency.
     * Returns null if price information is not available.
     *
     * @param entity the JPA entity
     * @return the Price or null if not available
     */
    private Price createPriceFromEntity(InstrumentJpaEntity entity) {
        if (entity.getCurrentPriceAmount() == null || entity.getCurrentPriceCurrency() == null) {
            return null;
        }
        Currency currency = Currency.valueOf(entity.getCurrentPriceCurrency());
        Money money = new Money(entity.getCurrentPriceAmount(), currency);
        return new Price(money);
    }

    /**
     * Maps JpaInstrumentType to domain InstrumentType.
     * STOCK -> STOCK, ETF -> ETF, BOND_ETF and POLISH_GOV_BOND -> BOND
     *
     * @param jpaType the JPA instrument type
     * @return the domain instrument type
     */
    private InstrumentType mapToDomainType(JpaInstrumentType jpaType) {
        return switch (jpaType) {
            case STOCK -> InstrumentType.STOCK;
            case ETF -> InstrumentType.ETF;
            case BOND_ETF, POLISH_GOV_BOND -> InstrumentType.BOND;
        };
    }

    /**
     * Maps domain InstrumentType to JpaInstrumentType.
     * BOND maps to BOND_ETF by default.
     *
     * @param domainType the domain instrument type
     * @return the JPA instrument type
     */
    private JpaInstrumentType mapToJpaType(InstrumentType domainType) {
        return switch (domainType) {
            case STOCK -> JpaInstrumentType.STOCK;
            case ETF -> JpaInstrumentType.ETF;
            case BOND -> JpaInstrumentType.BOND_ETF;
        };
    }
}
