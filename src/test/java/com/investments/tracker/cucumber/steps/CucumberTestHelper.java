package com.investments.tracker.cucumber.steps;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Shared utility methods for Cucumber step definitions.
 *
 * <p>Provides common database setup operations used across ManualEntrySteps, PositionSteps, and
 * PortfolioSteps.
 */
final class CucumberTestHelper {

    private CucumberTestHelper() {}

    /**
     * Ensures an account exists and returns its ID. Creates the account directly in the database if
     * it doesn't exist.
     */
    static Long ensureAccountExists(JdbcTemplate jdbcTemplate, String accountName) {
        List<Long> existingIds =
                jdbcTemplate.queryForList(
                        "SELECT id FROM accounts WHERE name = ?", Long.class, accountName);

        if (!existingIds.isEmpty()) {
            return existingIds.get(0);
        }

        jdbcTemplate.update(
                "INSERT INTO accounts (name, broker_name, account_type, version) VALUES (?, ?, ?, ?)",
                accountName,
                "Test Broker",
                "NORMAL",
                0);

        return jdbcTemplate.queryForObject(
                "SELECT id FROM accounts WHERE name = ?", Long.class, accountName);
    }

    /**
     * Ensures an instrument exists in the database. The currentPrice parameter is kept for backward
     * compatibility with step definitions but is no longer written to the database (prices are now
     * in CurrentPriceProvider/Redis).
     */
    static void ensureInstrumentExists(
            JdbcTemplate jdbcTemplate, String symbol, String name, BigDecimal currentPrice) {
        Integer count =
                jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM instruments WHERE symbol = ?", Integer.class, symbol);

        if (count == null || count == 0) {
            jdbcTemplate.update(
                    "INSERT INTO instruments (symbol, name, instrument_type, currency, version) VALUES (?, ?, ?, 'PLN', 0)",
                    symbol,
                    name != null ? name : symbol,
                    "STOCK");
        }
    }

    /** Creates a position with account holding in the database. */
    static void createPosition(
            JdbcTemplate jdbcTemplate,
            String symbol,
            Long accountId,
            BigDecimal quantity,
            BigDecimal costBasis) {
        Integer count =
                jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM positions WHERE instrument_symbol = ?",
                        Integer.class,
                        symbol);

        if (count != null && count > 0) {
            jdbcTemplate.update(
                    "UPDATE positions SET total_quantity = ?, avg_cost_basis_amount = ?, updated_at = CURRENT_TIMESTAMP WHERE instrument_symbol = ?",
                    quantity,
                    costBasis,
                    symbol);
            upsertHolding(jdbcTemplate, symbol, accountId, quantity, costBasis);
        } else {
            jdbcTemplate.update(
                    "INSERT INTO positions (instrument_symbol, total_quantity, avg_cost_basis_amount, avg_cost_basis_currency, version) VALUES (?, ?, ?, 'PLN', 0)",
                    symbol,
                    quantity,
                    costBasis);
            jdbcTemplate.update(
                    "INSERT INTO account_holdings (instrument_symbol, account_id, quantity, cost_basis_amount, cost_basis_currency) VALUES (?, ?, ?, ?, 'PLN')",
                    symbol,
                    accountId,
                    quantity,
                    costBasis);
        }
    }

    /**
     * Ensures an instrument exists without a current price. No price is set in CurrentPriceProvider
     * (callers should not populate it).
     */
    static void ensureInstrumentExistsWithoutPrice(
            JdbcTemplate jdbcTemplate, String symbol, String name) {
        Integer count =
                jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM instruments WHERE symbol = ?", Integer.class, symbol);

        if (count == null || count == 0) {
            jdbcTemplate.update(
                    "INSERT INTO instruments (symbol, name, instrument_type, currency, version) VALUES (?, ?, ?, 'PLN', 0)",
                    symbol,
                    name != null ? name : symbol,
                    "STOCK");
        }
    }

    /**
     * Generates a valid ticker symbol from an instrument name. Ticker symbols must be 1-10
     * uppercase alphanumeric characters.
     */
    static String generateValidSymbol(String instrumentName) {
        String symbol = instrumentName.toUpperCase().replaceAll("[^A-Z0-9]", "");
        if (symbol.length() > 10) {
            symbol = symbol.substring(0, 10);
        }
        if (symbol.isEmpty()) {
            symbol = "UNKNOWN";
        }
        return symbol;
    }

    /** Gets a nested value from a map (e.g., for accessing nested JSON response fields). */
    @SuppressWarnings("unchecked")
    static Object getNestedValue(Map<String, Object> map, String... keys) {
        Object current = map;
        for (String key : keys) {
            if (current instanceof Map) {
                current = ((Map<String, Object>) current).get(key);
            } else {
                return null;
            }
        }
        return current;
    }

    private static void upsertHolding(
            JdbcTemplate jdbcTemplate,
            String symbol,
            Long accountId,
            BigDecimal quantity,
            BigDecimal costBasis) {
        Integer holdingCount =
                jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM account_holdings WHERE instrument_symbol = ? AND account_id = ?",
                        Integer.class,
                        symbol,
                        accountId);
        if (holdingCount != null && holdingCount > 0) {
            jdbcTemplate.update(
                    "UPDATE account_holdings SET quantity = ?, cost_basis_amount = ?, updated_at = CURRENT_TIMESTAMP WHERE instrument_symbol = ? AND account_id = ?",
                    quantity,
                    costBasis,
                    symbol,
                    accountId);
        } else {
            jdbcTemplate.update(
                    "INSERT INTO account_holdings (instrument_symbol, account_id, quantity, cost_basis_amount, cost_basis_currency) VALUES (?, ?, ?, ?, 'PLN')",
                    symbol,
                    accountId,
                    quantity,
                    costBasis);
        }
    }
}
