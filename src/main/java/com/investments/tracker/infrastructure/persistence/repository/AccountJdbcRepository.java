package com.investments.tracker.infrastructure.persistence.repository;

import com.investments.tracker.infrastructure.persistence.entity.AccountJdbcEntity;
import org.springframework.data.repository.ListCrudRepository;

import java.util.Optional;

/**
 * Spring Data JDBC repository for Account aggregate.
 */
public interface AccountJdbcRepository extends ListCrudRepository<AccountJdbcEntity, Long> {

    Optional<AccountJdbcEntity> findByName(String name);
}
