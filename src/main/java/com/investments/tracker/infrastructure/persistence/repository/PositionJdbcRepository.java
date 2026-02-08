package com.investments.tracker.infrastructure.persistence.repository;

import com.investments.tracker.infrastructure.persistence.entity.PositionJdbcEntity;
import org.springframework.data.repository.ListCrudRepository;

/**
 * Spring Data JDBC repository for Position aggregate.
 */
public interface PositionJdbcRepository extends ListCrudRepository<PositionJdbcEntity, String> {
}
