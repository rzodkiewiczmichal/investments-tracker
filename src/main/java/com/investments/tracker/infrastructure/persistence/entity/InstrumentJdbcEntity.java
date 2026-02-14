package com.investments.tracker.infrastructure.persistence.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/** Spring Data JDBC entity for the instruments table. */
@Table("instruments")
public record InstrumentJdbcEntity(
        @Id String symbol,
        String name,
        @Column("instrument_type") String instrumentType,
        String currency,
        @Version Long version) {}
