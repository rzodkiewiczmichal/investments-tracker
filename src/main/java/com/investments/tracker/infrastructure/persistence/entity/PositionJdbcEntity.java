package com.investments.tracker.infrastructure.persistence.entity;

import java.math.BigDecimal;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Spring Data JDBC entity for the positions table. Aggregate root containing
 * AccountHoldingJdbcEntity children.
 */
@Table("positions")
public record PositionJdbcEntity(
        @Id @Column("instrument_symbol") String instrumentSymbol,
        @Column("total_quantity") BigDecimal totalQuantity,
        @Column("avg_cost_basis_amount") BigDecimal avgCostBasisAmount,
        @Column("avg_cost_basis_currency") String avgCostBasisCurrency,
        @MappedCollection(idColumn = "instrument_symbol") Set<AccountHoldingJdbcEntity> holdings,
        @Version Long version) {}
