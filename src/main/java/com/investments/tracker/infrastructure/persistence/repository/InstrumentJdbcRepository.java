package com.investments.tracker.infrastructure.persistence.repository;

import com.investments.tracker.infrastructure.persistence.entity.InstrumentJdbcEntity;
import org.springframework.data.repository.ListCrudRepository;

/**
 * Spring Data JDBC repository for Instrument aggregate.
 */
public interface InstrumentJdbcRepository extends ListCrudRepository<InstrumentJdbcEntity, String> {
}
