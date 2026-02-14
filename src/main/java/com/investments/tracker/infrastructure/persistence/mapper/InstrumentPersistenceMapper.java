package com.investments.tracker.infrastructure.persistence.mapper;

import com.investments.tracker.domain.model.Instrument;
import com.investments.tracker.domain.model.value.Currency;
import com.investments.tracker.domain.model.value.InstrumentName;
import com.investments.tracker.domain.model.value.InstrumentSymbol;
import com.investments.tracker.domain.model.value.InstrumentType;
import com.investments.tracker.infrastructure.persistence.entity.InstrumentJdbcEntity;
import com.investments.tracker.infrastructure.persistence.entity.InstrumentTypeValue;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Mapper for converting between Instrument domain model and InstrumentJdbcEntity.
 */
@Component
public class InstrumentPersistenceMapper {

    /**
     * Converts a JDBC entity to a domain model.
     *
     * @param entity the JDBC entity to convert
     * @return the domain Instrument
     */
    public Instrument toDomain(InstrumentJdbcEntity entity) {
        Objects.requireNonNull(entity, "entity cannot be null");

        return new Instrument(
                new InstrumentSymbol(entity.symbol()),
                new InstrumentName(entity.name()),
                mapToDomainType(entity.instrumentType()),
                Currency.valueOf(entity.currency())
        );
    }

    /**
     * Converts a domain model to a JDBC entity.
     *
     * @param instrument the domain Instrument to convert
     * @param version the current version for optimistic locking (null for new entities)
     * @return the JDBC entity
     */
    public InstrumentJdbcEntity toEntity(Instrument instrument, Long version) {
        Objects.requireNonNull(instrument, "instrument cannot be null");
        return new InstrumentJdbcEntity(
                instrument.symbol().value(),
                instrument.name().value(),
                mapToPersistenceType(instrument.type()).name(),
                instrument.currency().getCode(),
                version
        );
    }

    private InstrumentType mapToDomainType(String instrumentType) {
        InstrumentTypeValue typeValue = InstrumentTypeValue.valueOf(instrumentType);
        return switch (typeValue) {
            case STOCK -> InstrumentType.STOCK;
            case ETF -> InstrumentType.ETF;
            case BOND_ETF, POLISH_GOV_BOND -> InstrumentType.BOND;
        };
    }

    private InstrumentTypeValue mapToPersistenceType(InstrumentType domainType) {
        return switch (domainType) {
            case STOCK -> InstrumentTypeValue.STOCK;
            case ETF -> InstrumentTypeValue.ETF;
            case BOND -> InstrumentTypeValue.BOND_ETF;
        };
    }
}
