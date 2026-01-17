package com.investments.tracker.fixtures;

import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;

/**
 * Helper class for integration tests.
 * <p>
 * Provides utility methods for:
 * - Database cleanup
 * - Test data insertion
 * - Common assertions
 * </p>
 *
 * @see <a href="../../docs/adr/ADR-012-test-architecture.md">ADR-012: Test Architecture</a>
 */
public final class IntegrationTestHelper {

    private final JdbcTemplate jdbcTemplate;

    public IntegrationTestHelper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Cleans all tables in the correct order (respecting foreign key constraints).
     */
    public void cleanDatabase() {
        // Clean in reverse order of foreign key dependencies
        jdbcTemplate.execute("DELETE FROM account_holdings WHERE true");
        jdbcTemplate.execute("DELETE FROM positions WHERE true");
        jdbcTemplate.execute("DELETE FROM instruments WHERE true");
        jdbcTemplate.execute("DELETE FROM accounts WHERE true");
    }

    /**
     * Inserts a test account directly into the database.
     * Returns the generated account ID.
     */
    public Long insertAccount(String name, String brokerName, String accountType) {
        jdbcTemplate.update(
                """
                INSERT INTO accounts (name, broker_name, account_type, created_at, updated_at, version)
                VALUES (?, ?, ?, NOW(), NOW(), 0)
                """,
                name, brokerName, accountType
        );
        return jdbcTemplate.queryForObject(
                "SELECT id FROM accounts WHERE name = ? ORDER BY id DESC LIMIT 1",
                Long.class,
                name
        );
    }

    /**
     * Inserts a test instrument directly into the database.
     */
    public void insertInstrument(String symbol, String name, String instrumentType) {
        jdbcTemplate.update(
                """
                INSERT INTO instruments (symbol, name, instrument_type, created_at, updated_at, version)
                VALUES (?, ?, ?, NOW(), NOW(), 0)
                """,
                symbol, name, instrumentType
        );
    }

    /**
     * Inserts a test instrument with price directly into the database.
     */
    public void insertInstrumentWithPrice(String symbol, String name, String instrumentType, BigDecimal price) {
        jdbcTemplate.update(
                """
                INSERT INTO instruments (symbol, name, instrument_type, current_price_amount, current_price_currency,
                                         price_updated_at, created_at, updated_at, version)
                VALUES (?, ?, ?, ?, 'PLN', NOW(), NOW(), NOW(), 0)
                """,
                symbol, name, instrumentType, price
        );
    }

    /**
     * Inserts a position directly into the database.
     */
    public void insertPosition(String instrumentSymbol, BigDecimal totalQuantity, BigDecimal avgCostBasis) {
        jdbcTemplate.update(
                """
                INSERT INTO positions (instrument_symbol, total_quantity, avg_cost_basis_amount,
                                       avg_cost_basis_currency, created_at, updated_at, version)
                VALUES (?, ?, ?, 'PLN', NOW(), NOW(), 0)
                """,
                instrumentSymbol, totalQuantity, avgCostBasis
        );
    }

    /**
     * Inserts an account holding directly into the database.
     */
    public void insertAccountHolding(String instrumentSymbol, Long accountId,
                                     BigDecimal quantity, BigDecimal costBasis) {
        jdbcTemplate.update(
                """
                INSERT INTO account_holdings (instrument_symbol, account_id, quantity,
                                              cost_basis_amount, cost_basis_currency, created_at, updated_at)
                VALUES (?, ?, ?, ?, 'PLN', NOW(), NOW())
                """,
                instrumentSymbol, accountId, quantity, costBasis
        );
    }

    /**
     * Updates the current price of an instrument.
     */
    public void updateInstrumentPrice(String symbol, BigDecimal price) {
        jdbcTemplate.update(
                """
                UPDATE instruments
                SET current_price_amount = ?, price_updated_at = NOW(), updated_at = NOW()
                WHERE symbol = ?
                """,
                price, symbol
        );
    }

    /**
     * Counts the number of records in a table.
     */
    public int countRecords(String tableName) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + tableName,
                Integer.class
        );
        return count != null ? count : 0;
    }

    /**
     * Checks if a record exists in a table.
     */
    public boolean recordExists(String tableName, String column, Object value) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + tableName + " WHERE " + column + " = ?",
                Integer.class,
                value
        );
        return count != null && count > 0;
    }
}
