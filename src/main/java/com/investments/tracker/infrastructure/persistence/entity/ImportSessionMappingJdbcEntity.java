package com.investments.tracker.infrastructure.persistence.entity;

import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/** Spring Data JDBC entity for the import_session_mappings table. */
@Table("import_session_mappings")
public record ImportSessionMappingJdbcEntity(
        @Column("broker_instrument_name") String brokerInstrumentName,
        @Column("catalog_symbol") String catalogSymbol) {}
