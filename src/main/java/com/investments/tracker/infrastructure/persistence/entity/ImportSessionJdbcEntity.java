package com.investments.tracker.infrastructure.persistence.entity;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Spring Data JDBC entity for the import_sessions table. Aggregate root containing transaction and
 * mapping children.
 */
@Table("import_sessions")
public record ImportSessionJdbcEntity(
        @Id UUID id,
        @Column("status") String status,
        @Column("broker") String broker,
        @Column("account_name") String accountName,
        @Column("created_at") LocalDateTime createdAt,
        @Column("completed_at") LocalDateTime completedAt,
        @MappedCollection(idColumn = "import_session_id")
                Set<ImportSessionTransactionJdbcEntity> transactions,
        @MappedCollection(idColumn = "import_session_id")
                Set<ImportSessionMappingJdbcEntity> mappings,
        @Version Long version) {}
