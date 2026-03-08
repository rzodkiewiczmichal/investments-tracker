package com.investments.tracker.infrastructure.persistence.repository;

import java.util.UUID;

import org.springframework.data.repository.ListCrudRepository;

import com.investments.tracker.infrastructure.persistence.entity.ImportSessionJdbcEntity;

/** Spring Data JDBC repository for ImportSession aggregate. */
public interface ImportSessionJdbcRepository
        extends ListCrudRepository<ImportSessionJdbcEntity, UUID> {}
