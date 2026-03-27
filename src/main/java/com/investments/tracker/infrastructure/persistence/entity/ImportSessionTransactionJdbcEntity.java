package com.investments.tracker.infrastructure.persistence.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/** Spring Data JDBC entity for the import_session_transactions table. */
@Table("import_session_transactions")
public record ImportSessionTransactionJdbcEntity(
        @Id Long id,
        @Column("broker_instrument_name") String brokerInstrumentName,
        @Column("transaction_type") String transactionType,
        @Column("quantity") BigDecimal quantity,
        @Column("unit_price_amount") BigDecimal unitPriceAmount,
        @Column("unit_price_currency") String unitPriceCurrency,
        @Column("commission_amount") BigDecimal commissionAmount,
        @Column("commission_currency") String commissionCurrency,
        @Column("currency") String currency,
        @Column("transaction_date") LocalDateTime transactionDate) {}
