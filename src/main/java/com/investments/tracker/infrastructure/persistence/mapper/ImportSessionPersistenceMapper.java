package com.investments.tracker.infrastructure.persistence.mapper;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.investments.tracker.domain.model.ImportSession;
import com.investments.tracker.domain.model.InstrumentMapping;
import com.investments.tracker.domain.model.RawTransaction;
import com.investments.tracker.domain.model.value.AccountName;
import com.investments.tracker.domain.model.value.BrokerInstrumentName;
import com.investments.tracker.domain.model.value.BrokerName;
import com.investments.tracker.domain.model.value.Commission;
import com.investments.tracker.domain.model.value.Currency;
import com.investments.tracker.domain.model.value.ImportSessionId;
import com.investments.tracker.domain.model.value.ImportSessionStatus;
import com.investments.tracker.domain.model.value.InstrumentSymbol;
import com.investments.tracker.domain.model.value.Money;
import com.investments.tracker.domain.model.value.Price;
import com.investments.tracker.domain.model.value.Quantity;
import com.investments.tracker.domain.model.value.TransactionType;
import com.investments.tracker.infrastructure.persistence.entity.ImportSessionJdbcEntity;
import com.investments.tracker.infrastructure.persistence.entity.ImportSessionMappingJdbcEntity;
import com.investments.tracker.infrastructure.persistence.entity.ImportSessionTransactionJdbcEntity;

/** Mapper for converting between ImportSession domain model and JDBC entities. */
@Component
public class ImportSessionPersistenceMapper {

    public ImportSession toDomain(ImportSessionJdbcEntity entity) {
        Objects.requireNonNull(entity, "entity cannot be null");

        List<RawTransaction> transactions =
                entity.transactions().stream().map(this::mapToRawTransaction).toList();

        List<InstrumentMapping> mappings =
                entity.mappings().stream().map(this::mapToInstrumentMapping).toList();

        return new ImportSession(
                ImportSessionId.of(entity.id()),
                ImportSessionStatus.valueOf(entity.status()),
                BrokerName.of(entity.broker()),
                AccountName.of(entity.accountName()),
                transactions,
                mappings,
                entity.createdAt(),
                entity.completedAt());
    }

    public ImportSessionJdbcEntity toEntity(ImportSession session, Long version) {
        Objects.requireNonNull(session, "session cannot be null");

        List<ImportSessionTransactionJdbcEntity> transactionEntities =
                session.transactions().stream().map(this::mapToTransactionEntity).toList();

        Set<ImportSessionMappingJdbcEntity> mappingEntities =
                session.instrumentMappings().stream()
                        .map(this::mapToMappingEntity)
                        .collect(Collectors.toSet());

        return new ImportSessionJdbcEntity(
                session.id().value(),
                session.status().name(),
                session.broker().value(),
                session.accountName().value(),
                session.createdAt(),
                session.completedAt(),
                transactionEntities,
                mappingEntities,
                version);
    }

    private RawTransaction mapToRawTransaction(ImportSessionTransactionJdbcEntity entity) {
        Currency priceCurrency = Currency.valueOf(entity.unitPriceCurrency());
        Currency commCurrency = Currency.valueOf(entity.commissionCurrency());

        return new RawTransaction(
                BrokerInstrumentName.of(entity.brokerInstrumentName()),
                TransactionType.valueOf(entity.transactionType()),
                Quantity.of(entity.quantity()),
                Price.of(new Money(entity.unitPriceAmount(), priceCurrency)),
                Commission.of(new Money(entity.commissionAmount(), commCurrency)),
                Currency.valueOf(entity.currency()),
                entity.transactionDate());
    }

    private InstrumentMapping mapToInstrumentMapping(ImportSessionMappingJdbcEntity entity) {
        if (entity.catalogSymbol() == null) {
            return InstrumentMapping.unresolved(
                    BrokerInstrumentName.of(entity.brokerInstrumentName()));
        }
        return InstrumentMapping.resolved(
                BrokerInstrumentName.of(entity.brokerInstrumentName()),
                new InstrumentSymbol(entity.catalogSymbol()));
    }

    private ImportSessionTransactionJdbcEntity mapToTransactionEntity(RawTransaction tx) {
        return new ImportSessionTransactionJdbcEntity(
                null,
                tx.brokerInstrumentName().value(),
                tx.type().name(),
                tx.quantity().toBigDecimal(),
                tx.unitPrice().money().amount(),
                tx.unitPrice().money().currency().getCode(),
                tx.commission().money().amount(),
                tx.commission().currency().getCode(),
                tx.currency().getCode(),
                tx.transactionDate());
    }

    private ImportSessionMappingJdbcEntity mapToMappingEntity(InstrumentMapping mapping) {
        return new ImportSessionMappingJdbcEntity(
                mapping.brokerName().value(),
                mapping.isResolved() ? mapping.catalogSymbol().value() : null);
    }
}
