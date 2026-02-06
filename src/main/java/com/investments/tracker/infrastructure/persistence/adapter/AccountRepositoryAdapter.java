package com.investments.tracker.infrastructure.persistence.adapter;

import com.investments.tracker.domain.model.Account;
import com.investments.tracker.domain.model.value.AccountId;
import com.investments.tracker.domain.repository.AccountRepository;
import com.investments.tracker.infrastructure.persistence.entity.AccountJpaEntity;
import com.investments.tracker.infrastructure.persistence.mapper.AccountPersistenceMapper;
import com.investments.tracker.infrastructure.persistence.repository.AccountJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

/**
 * JPA-based implementation of the AccountRepository domain port.
 */
@Repository
public class AccountRepositoryAdapter implements AccountRepository {

    private final AccountJpaRepository jpaRepository;
    private final AccountPersistenceMapper mapper;

    public AccountRepositoryAdapter(AccountJpaRepository jpaRepository, AccountPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<Account> findById(AccountId id) {
        return jpaRepository.findById(id.value())
                .map(mapper::toDomain);
    }

    @Override
    public Collection<Account> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Account save(Account account) {
        AccountJpaEntity entity = mapper.toEntity(account);
        AccountJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public void deleteById(AccountId id) {
        jpaRepository.deleteById(id.value());
    }

    @Override
    public boolean existsById(AccountId id) {
        return jpaRepository.existsById(id.value());
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }
}
