package com.investments.tracker.infrastructure.persistence.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/** Spring Data JDBC entity for the accounts table. */
@Table("accounts")
public record AccountJdbcEntity(
        @Id Long id,
        String name,
        @Column("broker_name") String brokerName,
        @Column("account_type") String accountType,
        @Version Long version) {}
