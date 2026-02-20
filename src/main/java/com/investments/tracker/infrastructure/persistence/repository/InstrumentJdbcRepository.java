package com.investments.tracker.infrastructure.persistence.repository;

import java.util.List;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;

import com.investments.tracker.infrastructure.persistence.entity.InstrumentJdbcEntity;

/** Spring Data JDBC repository for Instrument aggregate. */
public interface InstrumentJdbcRepository extends ListCrudRepository<InstrumentJdbcEntity, String> {

    @Query(
            "SELECT * FROM instruments WHERE LOWER(symbol) LIKE LOWER(:pattern) OR LOWER(name) LIKE LOWER(:pattern) ORDER BY symbol LIMIT 20")
    List<InstrumentJdbcEntity> searchBySymbolOrName(@Param("pattern") String pattern);
}
