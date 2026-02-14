package com.investments.tracker.infrastructure.persistence.repository;

import org.springframework.data.repository.ListCrudRepository;

import com.investments.tracker.infrastructure.persistence.entity.InstrumentJdbcEntity;

/** Spring Data JDBC repository for Instrument aggregate. */
public interface InstrumentJdbcRepository
        extends ListCrudRepository<InstrumentJdbcEntity, String> {}
