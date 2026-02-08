package com.investments.tracker.infrastructure.persistence.adapter;

import com.investments.tracker.domain.model.Account;
import com.investments.tracker.domain.model.value.AccountId;
import com.investments.tracker.domain.model.value.AccountName;
import com.investments.tracker.domain.model.value.BrokerName;
import com.investments.tracker.domain.repository.AccountRepository;
import com.investments.tracker.infrastructure.persistence.entity.AccountJdbcEntity;
import com.investments.tracker.infrastructure.persistence.mapper.AccountPersistenceMapper;
import com.investments.tracker.infrastructure.persistence.repository.AccountJdbcRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

/**
 * Spring Data JDBC implementation of the AccountRepository domain port.
 */
@Repository
public class AccountRepositoryAdapter implements AccountRepository {

    private final AccountJdbcRepository jdbcRepository;
    private final AccountPersistenceMapper mapper;

    public AccountRepositoryAdapter(AccountJdbcRepository jdbcRepository, AccountPersistenceMapper mapper) {
        this.jdbcRepository = jdbcRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<Account> findById(AccountId id) {
        return jdbcRepository.findById(id.value())
                .map(mapper::toDomain);
    }

    @Override
    public Collection<Account> findAll() {
        return jdbcRepository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Account save(Account account) {
        Long version = jdbcRepository.findById(account.id().value())
                .map(AccountJdbcEntity::version).orElse(null);
        AccountJdbcEntity entity = mapper.toEntity(account, version);
        AccountJdbcEntity saved = jdbcRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public void deleteById(AccountId id) {
        jdbcRepository.deleteById(id.value());
    }

    @Override
    public boolean existsById(AccountId id) {
        return jdbcRepository.existsById(id.value());
    }

    @Override
    public long count() {
        return jdbcRepository.count();
    }

    @Override
    public Optional<Account> findByName(AccountName name) {
        return jdbcRepository.findByName(name.value())
                .map(mapper::toDomain);
    }

    @Override
    public Account create(AccountName name, BrokerName brokerName) {
        AccountJdbcEntity entity = mapper.toNewEntity(name, brokerName);
        AccountJdbcEntity saved = jdbcRepository.save(entity);
        return mapper.toDomain(saved);
    }
}
