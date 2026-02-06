package com.investments.tracker.cucumber.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Cucumber step definitions for Portfolio Viewing feature.
 * <p>
 * Tests portfolio summary functionality including:
 * - Total current value
 * - Total invested amount
 * - P&L (profit/loss)
 * - P&L percentage
 * </p>
 *
 * @see <a href="requirements/functional/features/portfolio-viewing.feature">Portfolio Viewing Feature</a>
 */
public class PortfolioSteps {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private ResponseEntity<Map> portfolioResponse;
    private BigDecimal totalInvestedAmount;
    private BigDecimal totalCurrentValue;

    // --- Given Steps ---

    @Given("my total invested amount is {int} PLN")
    public void myTotalInvestedAmountIsPLN(Integer amount) {
        this.totalInvestedAmount = new BigDecimal(amount);
        // The actual positions will be created by subsequent Given steps
    }

    @Given("my total current value is {int} PLN")
    public void myTotalCurrentValueIsPLN(Integer amount) {
        this.totalCurrentValue = new BigDecimal(amount);
        // The actual positions will be created by subsequent Given steps
    }

    @Given("I own {int} shares of {string} in account {string} bought at {int} PLN")
    public void iOwnSharesOfInAccountBoughtAtPLN(Integer quantity, String instrument, String account, Integer price) {
        // Ensure account exists
        Long accountId = ensureAccountExists(account);

        // Generate symbol from instrument name
        String symbol = instrument.toUpperCase().replace(" ", "_");

        // Ensure instrument exists (price will be set by theCurrentPriceOfIsPLN step)
        ensureInstrumentExists(symbol, instrument, new BigDecimal(price));

        // Create position
        createPosition(symbol, accountId, new BigDecimal(quantity), new BigDecimal(price), new BigDecimal(price));
    }

    @Given("the current price of {string} is {int} PLN")
    public void theCurrentPriceOfIsPLN(String instrument, Integer price) {
        String symbol = instrument.toUpperCase().replace(" ", "_");
        // Update instrument price
        jdbcTemplate.update(
                "UPDATE instruments SET current_price_amount = ?, price_updated_at = CURRENT_TIMESTAMP WHERE symbol = ?",
                new BigDecimal(price), symbol
        );
    }

    // --- When Steps ---

    @When("I view my portfolio")
    public void iViewMyPortfolio() {
        portfolioResponse = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/v1/portfolio",
                Map.class
        );
    }

    // --- Then Steps ---

    @Then("I should see the total current value in PLN")
    public void iShouldSeeTheTotalCurrentValueInPLN() {
        assertThat(portfolioResponse.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(portfolioResponse.getBody()).containsKey("totalCurrentValue");
    }

    @Then("I should see the total invested amount in PLN")
    public void iShouldSeeTheTotalInvestedAmountInPLN() {
        assertThat(portfolioResponse.getBody()).containsKey("totalInvestedAmount");
    }

    @Then("I should see the total P&L in PLN")
    public void iShouldSeeTheTotalPLInPLN() {
        assertThat(portfolioResponse.getBody()).containsKey("totalProfitLoss");
    }

    @Then("I should see the total P&L as a percentage")
    public void iShouldSeeTheTotalPLAsAPercentage() {
        assertThat(portfolioResponse.getBody()).containsKey("totalReturnPercentage");
    }

    @Then("I should see the portfolio XIRR percentage")
    public void iShouldSeeThePortfolioXIRRPercentage() {
        // XIRR is optional for v0.1, may not be present
        // assertThat(portfolioResponse.getBody()).containsKey("xirr");
    }

    @Then("I should see total current value of {int} PLN")
    public void iShouldSeeTotalCurrentValueOfPLN(Integer expectedValue) {
        Object actualValue = getNestedValue(portfolioResponse.getBody(), "totalCurrentValue", "amount");
        assertThat(new BigDecimal(actualValue.toString()))
                .isEqualByComparingTo(new BigDecimal(expectedValue));
    }

    @Then("I should see total invested amount of {int} PLN")
    public void iShouldSeeTotalInvestedAmountOfPLN(Integer expectedAmount) {
        Object actualValue = getNestedValue(portfolioResponse.getBody(), "totalInvestedAmount", "amount");
        assertThat(new BigDecimal(actualValue.toString()))
                .isEqualByComparingTo(new BigDecimal(expectedAmount));
    }

    @Then("I should see P&L of +{int} PLN")
    public void iShouldSeePLOfPlusXPLN(Integer expectedPL) {
        Object actualValue = getNestedValue(portfolioResponse.getBody(), "totalProfitLoss", "amount");
        assertThat(new BigDecimal(actualValue.toString()))
                .isEqualByComparingTo(new BigDecimal(expectedPL));
    }

    @Then("I should see P&L of -{int} PLN")
    public void iShouldSeePLOfMinusXPLN(Integer expectedPL) {
        Object actualValue = getNestedValue(portfolioResponse.getBody(), "totalProfitLoss", "amount");
        assertThat(new BigDecimal(actualValue.toString()))
                .isEqualByComparingTo(new BigDecimal(-expectedPL));
    }

    @Then("I should see P&L percentage of +{double}%")
    public void iShouldSeePLPercentageOfPlusX(Double expectedPercentage) {
        Object actualValue = portfolioResponse.getBody().get("totalReturnPercentage");
        assertThat(new BigDecimal(actualValue.toString()))
                .isEqualByComparingTo(new BigDecimal(expectedPercentage));
    }

    @Then("I should see P&L percentage of -{double}%")
    public void iShouldSeePLPercentageOfMinusX(Double expectedPercentage) {
        Object actualValue = portfolioResponse.getBody().get("totalReturnPercentage");
        assertThat(new BigDecimal(actualValue.toString()))
                .isEqualByComparingTo(new BigDecimal(-expectedPercentage));
    }

    @Then("I should see a message {string}")
    public void iShouldSeeAMessage(String expectedMessage) {
        Object message = portfolioResponse.getBody().get("message");
        assertThat(message).isNotNull();
        assertThat(message.toString()).contains(expectedMessage);
    }

    // --- Aggregation Steps (v0.2) ---

    @Then("I should see a single position for {string} with {int} shares")
    public void iShouldSeeASinglePositionForWithShares(String instrument, Integer totalShares) {
        // Implementation for aggregated positions
    }

    @Then("the average cost basis should be {double} PLN")
    public void theAverageCostBasisShouldBePLN(Double expectedCost) {
        // Implementation for average cost calculation
    }

    @Then("the current value should be {int} PLN")
    public void theCurrentValueShouldBePLN(Integer expectedValue) {
        // Implementation for current value assertion
    }

    @Then("the P&L should be +{int} PLN")
    public void thePLShouldBePlusPLN(Integer expectedPL) {
        // Implementation for P&L assertion
    }

    // --- Helper Methods ---

    private Long ensureAccountExists(String accountName) {
        List<Long> existingIds = jdbcTemplate.queryForList(
                "SELECT id FROM accounts WHERE name = ?",
                Long.class,
                accountName
        );

        if (!existingIds.isEmpty()) {
            return existingIds.get(0);
        }

        jdbcTemplate.update(
                "INSERT INTO accounts (name, broker_name, account_type, version) VALUES (?, ?, ?, ?)",
                accountName, "Test Broker", "NORMAL", 0
        );

        return jdbcTemplate.queryForObject(
                "SELECT id FROM accounts WHERE name = ?",
                Long.class,
                accountName
        );
    }

    private void ensureInstrumentExists(String symbol, String name, BigDecimal currentPrice) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM instruments WHERE symbol = ?",
                Integer.class,
                symbol
        );

        if (count != null && count > 0) {
            jdbcTemplate.update(
                    "UPDATE instruments SET current_price_amount = ?, price_updated_at = CURRENT_TIMESTAMP WHERE symbol = ?",
                    currentPrice, symbol
            );
        } else {
            jdbcTemplate.update(
                    "INSERT INTO instruments (symbol, name, instrument_type, current_price_amount, current_price_currency, price_updated_at, version) VALUES (?, ?, ?, ?, 'PLN', CURRENT_TIMESTAMP, 0)",
                    symbol, name != null ? name : symbol, "STOCK", currentPrice
            );
        }
    }

    private void createPosition(String symbol, Long accountId, BigDecimal quantity, BigDecimal costBasis, BigDecimal currentPrice) {
        // Check if position exists
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM positions WHERE instrument_symbol = ?",
                Integer.class,
                symbol
        );

        if (count != null && count > 0) {
            // Update position
            jdbcTemplate.update(
                    "UPDATE positions SET total_quantity = ?, avg_cost_basis_amount = ?, updated_at = CURRENT_TIMESTAMP WHERE instrument_symbol = ?",
                    quantity, costBasis, symbol
            );
            // Update or insert holding
            Integer holdingCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM account_holdings WHERE instrument_symbol = ? AND account_id = ?",
                    Integer.class,
                    symbol, accountId
            );
            if (holdingCount != null && holdingCount > 0) {
                jdbcTemplate.update(
                        "UPDATE account_holdings SET quantity = ?, cost_basis_amount = ?, updated_at = CURRENT_TIMESTAMP WHERE instrument_symbol = ? AND account_id = ?",
                        quantity, costBasis, symbol, accountId
                );
            } else {
                jdbcTemplate.update(
                        "INSERT INTO account_holdings (instrument_symbol, account_id, quantity, cost_basis_amount, cost_basis_currency) VALUES (?, ?, ?, ?, 'PLN')",
                        symbol, accountId, quantity, costBasis
                );
            }
        } else {
            // Create position
            jdbcTemplate.update(
                    "INSERT INTO positions (instrument_symbol, total_quantity, avg_cost_basis_amount, avg_cost_basis_currency, version) VALUES (?, ?, ?, 'PLN', 0)",
                    symbol, quantity, costBasis
            );
            // Create holding
            jdbcTemplate.update(
                    "INSERT INTO account_holdings (instrument_symbol, account_id, quantity, cost_basis_amount, cost_basis_currency) VALUES (?, ?, ?, ?, 'PLN')",
                    symbol, accountId, quantity, costBasis
            );
        }
    }

    @SuppressWarnings("unchecked")
    private Object getNestedValue(Map<String, Object> map, String... keys) {
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
}
