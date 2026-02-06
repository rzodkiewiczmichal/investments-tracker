package com.investments.tracker.infrastructure.persistence.mapper;

import com.investments.tracker.domain.model.Account;
import com.investments.tracker.domain.model.value.AccountId;
import com.investments.tracker.domain.model.value.AccountName;
import com.investments.tracker.domain.model.value.BrokerName;
import com.investments.tracker.infrastructure.persistence.entity.AccountJpaEntity;
import com.investments.tracker.infrastructure.persistence.entity.AccountType;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Mapper for converting between Account domain model and AccountJpaEntity.
 */
@Component
public class AccountPersistenceMapper {

    /**
     * Converts a JPA entity to a domain model.
     *
     * @param entity the JPA entity to convert
     * @return the domain Account
     */
    public Account toDomain(AccountJpaEntity entity) {
        Objects.requireNonNull(entity, "entity cannot be null");
        return new Account(
                new AccountId(entity.getId()),
                new AccountName(entity.getName()),
                new BrokerName(entity.getBrokerName())
        );
    }

    /**
     * Converts a domain model to a JPA entity.
     * Uses AccountType.NORMAL as the default account type.
     *
     * @param account the domain Account to convert
     * @return the JPA entity
     */
    public AccountJpaEntity toEntity(Account account) {
        Objects.requireNonNull(account, "account cannot be null");
        AccountJpaEntity entity = new AccountJpaEntity();
        entity.setId(account.id().value());
        entity.setName(account.name().value());
        entity.setBrokerName(account.brokerName().value());
        entity.setAccountType(AccountType.NORMAL);
        return entity;
    }
}
