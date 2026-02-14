package com.investments.tracker.infrastructure.persistence.repository;

import org.springframework.data.repository.ListCrudRepository;

import com.investments.tracker.infrastructure.persistence.entity.PositionJdbcEntity;

/** Spring Data JDBC repository for Position aggregate. */
public interface PositionJdbcRepository extends ListCrudRepository<PositionJdbcEntity, String> {}
