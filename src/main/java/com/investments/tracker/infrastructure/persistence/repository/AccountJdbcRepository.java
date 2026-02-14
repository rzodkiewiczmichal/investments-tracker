package com.investments.tracker.infrastructure.persistence.repository;

import java.util.Optional;

import org.springframework.data.repository.ListCrudRepository;

import com.investments.tracker.infrastructure.persistence.entity.AccountJdbcEntity;

/** Spring Data JDBC repository for Account aggregate. */
public interface AccountJdbcRepository extends ListCrudRepository<AccountJdbcEntity, Long> {

    Optional<AccountJdbcEntity> findByName(String name);
}
