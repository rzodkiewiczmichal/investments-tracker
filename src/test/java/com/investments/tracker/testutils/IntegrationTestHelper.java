package com.investments.tracker.testutils;

import java.math.BigDecimal;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Helper class for direct database operations in tests.
 *
 * <p>Provides utility methods for: - Database cleanup (before scenarios/tests) - Direct test data
 * insertion (bypassing API for faster setup) - Record counting and existence checks
 *
 * <p><b>Usage:</b> This helper can be used in: - Integration tests (testing repositories,
 * controllers) - BDD step definitions (for fast data setup in Given steps) - Any test requiring
 * direct database access
 *
 * @see <a href="../../docs/adr/ADR-012-test-architecture.md">ADR-012: Test Architecture</a>
 */
public final class IntegrationTestHelper {

    private final JdbcTemplate jdbcTemplate;

    public IntegrationTestHelper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /** Cleans all tables in the correct order (respecting foreign key constraints). */
    public void cleanDatabase() {
        // Clean in reverse order of foreign key dependencies
        jdbcTemplate.execute("DELETE FROM account_holdings WHERE true");
        jdbcTemplate.execute("DELETE FROM positions WHERE true");
        jdbcTemplate.execute("DELETE FROM instruments WHERE true");
        jdbcTemplate.execute("DELETE FROM accounts WHERE true");
    }

    /** Inserts a test account directly into the database. Returns the generated account ID. */
    public Long insertAccount(String name, String brokerName, String accountType) {
        jdbcTemplate.update(
                """
                INSERT INTO accounts (name, broker_name, account_type, created_at, updated_at, version)
                VALUES (?, ?, ?, NOW(), NOW(), 0)
                """,
                name,
                brokerName,
                accountType);
        return jdbcTemplate.queryForObject(
                "SELECT id FROM accounts WHERE name = ? ORDER BY id DESC LIMIT 1",
                Long.class,
                name);
    }

    /** Inserts a test instrument directly into the database. */
    public void insertInstrument(String symbol, String name, String instrumentType) {
        insertInstrument(symbol, name, instrumentType, "PLN");
    }

    /** Inserts a test instrument with specified currency directly into the database. */
    public void insertInstrument(
            String symbol, String name, String instrumentType, String currency) {
        jdbcTemplate.update(
                """
                INSERT INTO instruments (symbol, name, instrument_type, currency, created_at, updated_at, version)
                VALUES (?, ?, ?, ?, NOW(), NOW(), 0)
                """,
                symbol,
                name,
                instrumentType,
                currency);
    }

    /** Inserts a position directly into the database. */
    public void insertPosition(
            String instrumentSymbol, BigDecimal totalQuantity, BigDecimal avgCostBasis) {
        jdbcTemplate.update(
                """
                INSERT INTO positions (instrument_symbol, total_quantity, avg_cost_basis_amount,
                                       avg_cost_basis_currency, created_at, updated_at, version)
                VALUES (?, ?, ?, 'PLN', NOW(), NOW(), 0)
                """,
                instrumentSymbol,
                totalQuantity,
                avgCostBasis);
    }

    /** Inserts an account holding directly into the database. */
    public void insertAccountHolding(
            String instrumentSymbol, Long accountId, BigDecimal quantity, BigDecimal costBasis) {
        jdbcTemplate.update(
                """
                INSERT INTO account_holdings (instrument_symbol, account_id, quantity,
                                              cost_basis_amount, cost_basis_currency, created_at, updated_at)
                VALUES (?, ?, ?, ?, 'PLN', NOW(), NOW())
                """,
                instrumentSymbol,
                accountId,
                quantity,
                costBasis);
    }

    /** Counts the number of records in a table. */
    public int countRecords(String tableName) {
        Integer count =
                jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + tableName, Integer.class);
        return count != null ? count : 0;
    }

    /** Checks if a record exists in a table. */
    public boolean recordExists(String tableName, String column, Object value) {
        Integer count =
                jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM " + tableName + " WHERE " + column + " = ?",
                        Integer.class,
                        value);
        return count != null && count > 0;
    }
}
