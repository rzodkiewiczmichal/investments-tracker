package com.investments.tracker.infrastructure.persistence.entity;

import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

/**
 * Spring Data JDBC entity for the account_holdings table.
 * Child entity within the Position aggregate.
 */
@Table("account_holdings")
public record AccountHoldingJdbcEntity(
        @Column("account_id") Long accountId,
        BigDecimal quantity,
        @Column("cost_basis_amount") BigDecimal costBasisAmount,
        @Column("cost_basis_currency") String costBasisCurrency) {
}
