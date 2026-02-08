package com.investments.tracker.infrastructure.persistence.mapper;

import com.investments.tracker.domain.model.Account;
import com.investments.tracker.domain.model.value.AccountId;
import com.investments.tracker.domain.model.value.AccountName;
import com.investments.tracker.domain.model.value.BrokerName;
import com.investments.tracker.infrastructure.persistence.entity.AccountJdbcEntity;
import com.investments.tracker.infrastructure.persistence.entity.AccountType;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Mapper for converting between Account domain model and AccountJdbcEntity.
 */
@Component
public class AccountPersistenceMapper {

    /**
     * Converts a JDBC entity to a domain model.
     *
     * @param entity the JDBC entity to convert
     * @return the domain Account
     */
    public Account toDomain(AccountJdbcEntity entity) {
        Objects.requireNonNull(entity, "entity cannot be null");
        return new Account(
                new AccountId(entity.id()),
                new AccountName(entity.name()),
                new BrokerName(entity.brokerName())
        );
    }

    /**
     * Converts a domain model to a JDBC entity for saving.
     *
     * @param account the domain Account to convert
     * @param version the current version for optimistic locking (null for new entities)
     * @return the JDBC entity
     */
    public AccountJdbcEntity toEntity(Account account, Long version) {
        Objects.requireNonNull(account, "account cannot be null");
        return new AccountJdbcEntity(
                account.id().value(),
                account.name().value(),
                account.brokerName().value(),
                AccountType.NORMAL.name(),
                version
        );
    }

    /**
     * Creates a new JDBC entity without an ID (for database-generated IDs).
     *
     * @param name the account name
     * @param brokerName the broker name
     * @return the JDBC entity with null ID and version
     */
    public AccountJdbcEntity toNewEntity(AccountName name, BrokerName brokerName) {
        Objects.requireNonNull(name, "name cannot be null");
        Objects.requireNonNull(brokerName, "brokerName cannot be null");
        return new AccountJdbcEntity(
                null,
                name.value(),
                brokerName.value(),
                AccountType.NORMAL.name(),
                null
        );
    }
}
