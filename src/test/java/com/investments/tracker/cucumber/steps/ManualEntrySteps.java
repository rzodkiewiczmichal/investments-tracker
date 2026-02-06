package com.investments.tracker.cucumber.steps;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Cucumber step definitions for Manual Entry feature.
 * <p>
 * Tests manual position entry functionality including:
 * - Stock position entry
 * - ETF position entry
 * - Validation error handling
 * </p>
 *
 * @see <a href="requirements/functional/features/manual-entry.feature">Manual Entry Feature</a>
 */
public class ManualEntrySteps {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private ResponseEntity<Map> positionResponse;
    private ResponseEntity<Map> errorResponse;
    private Map<String, Object> positionData;
    private boolean expectingError = false;
    private Long testAccountId;
    private String testInstrumentSymbol;

    // --- Given Steps ---

    @Given("I want to manually add a position")
    public void iWantToManuallyAddAPosition() {
        positionData = new HashMap<>();
        expectingError = false;
    }

    @Given("I want to manually add a bond position")
    public void iWantToManuallyAddABondPosition() {
        positionData = new HashMap<>();
        positionData.put("instrumentType", "BOND");
        expectingError = false;
    }

    // --- When Steps ---

    @When("I enter the following position data:")
    public void iEnterTheFollowingPositionData(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps();

        String instrumentName = null;
        String accountName = null;
        BigDecimal quantity = null;
        BigDecimal averageCost = null;

        for (Map<String, String> row : rows) {
            String field = row.get("Field");
            String value = row.get("Value");

            switch (field) {
                case "Instrument":
                    instrumentName = value;
                    testInstrumentSymbol = generateValidSymbol(value);
                    break;
                case "Quantity":
                    quantity = new BigDecimal(value);
                    break;
                case "Average Cost":
                    averageCost = new BigDecimal(value);
                    break;
                case "Account":
                    accountName = value;
                    break;
            }
        }

        // Ensure account exists and get its ID
        testAccountId = ensureAccountExists(accountName);

        // Ensure instrument exists with the current price (use average cost as current price for simplicity)
        ensureInstrumentExists(testInstrumentSymbol, instrumentName, averageCost);

        // Build the request with the correct API contract
        positionData.put("instrumentSymbol", testInstrumentSymbol);
        positionData.put("accountId", testAccountId);
        positionData.put("quantity", quantity);
        positionData.put("costBasis", averageCost);
        positionData.put("currentPrice", averageCost); // Use average cost as current price for new positions

        // Submit the position
        submitPosition();
    }

    @When("I enter the following bond data:")
    public void iEnterTheFollowingBondData(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps();

        String seriesName = null;
        String accountName = null;
        BigDecimal investedAmount = null;
        BigDecimal currentValue = null;

        for (Map<String, String> row : rows) {
            String field = row.get("Field");
            String value = row.get("Value");

            switch (field) {
                case "Instrument Type":
                    // POLISH_GOVERNMENT_BOND
                    break;
                case "Series":
                    seriesName = value;
                    testInstrumentSymbol = value;
                    break;
                case "Invested Amount":
                    investedAmount = new BigDecimal(value);
                    break;
                case "Current Value":
                    currentValue = new BigDecimal(value);
                    break;
                case "Account":
                    accountName = value;
                    break;
            }
        }

        // Ensure account exists
        testAccountId = ensureAccountExists(accountName);

        // Ensure instrument exists
        BigDecimal pricePerUnit = currentValue != null ? currentValue : investedAmount;
        ensureInstrumentExists(testInstrumentSymbol, seriesName, pricePerUnit);

        // Build request - for bonds, quantity is 1 and cost basis is the invested amount
        positionData.put("instrumentSymbol", testInstrumentSymbol);
        positionData.put("accountId", testAccountId);
        positionData.put("quantity", BigDecimal.ONE);
        positionData.put("costBasis", investedAmount);
        positionData.put("currentPrice", currentValue != null ? currentValue : investedAmount);

        // Submit the position
        submitPosition();
    }

    @When("I try to save position without instrument name")
    public void iTryToSavePositionWithoutInstrumentName() {
        expectingError = true;
        testAccountId = ensureAccountExists("Test Account");

        // Missing instrumentSymbol
        positionData.put("accountId", testAccountId);
        positionData.put("quantity", new BigDecimal("100"));
        positionData.put("costBasis", new BigDecimal("50"));
        positionData.put("currentPrice", new BigDecimal("50"));
        submitPosition();
    }

    @When("I enter quantity as {string}")
    public void iEnterQuantityAs(String quantity) {
        expectingError = true;
        testAccountId = ensureAccountExists("Test Account");
        testInstrumentSymbol = "TEST";
        ensureInstrumentExists(testInstrumentSymbol, "Test Instrument", new BigDecimal("100"));

        positionData.put("instrumentSymbol", testInstrumentSymbol);
        positionData.put("accountId", testAccountId);
        positionData.put("quantity", new BigDecimal(quantity));
        positionData.put("costBasis", new BigDecimal("100"));
        positionData.put("currentPrice", new BigDecimal("100"));
        submitPosition();
    }

    @When("I enter average cost as {string}")
    public void iEnterAverageCostAs(String averageCost) {
        expectingError = true;
        testAccountId = ensureAccountExists("Test Account");
        testInstrumentSymbol = "TEST";
        ensureInstrumentExists(testInstrumentSymbol, "Test Instrument", new BigDecimal("100"));

        positionData.put("instrumentSymbol", testInstrumentSymbol);
        positionData.put("accountId", testAccountId);
        positionData.put("quantity", new BigDecimal("100"));
        positionData.put("costBasis", new BigDecimal(averageCost));
        positionData.put("currentPrice", new BigDecimal("100"));
        submitPosition();
    }

    // --- Then Steps ---

    @Then("a new position for {string} should be created")
    public void aNewPositionForShouldBeCreated(String instrumentName) {
        assertThat(positionResponse.getStatusCode().is2xxSuccessful())
                .as("Expected successful response but got: " + positionResponse.getStatusCode() +
                        ", body: " + positionResponse.getBody())
                .isTrue();
        assertThat(positionResponse.getBody()).isNotNull();
        // The response uses symbol, not name - use the same symbol generation as in setup
        String expectedSymbol = generateValidSymbol(instrumentName);
        assertThat(positionResponse.getBody().get("symbol").toString())
                .isEqualToIgnoringCase(expectedSymbol);
    }

    @Then("the position should have {int} shares at {int} PLN average cost")
    public void thePositionShouldHaveSharesAtPLNAverageCost(Integer expectedQuantity, Integer expectedCost) {
        assertThat(positionResponse.getBody()).isNotNull();
        Object quantity = positionResponse.getBody().get("totalQuantity");
        Object averageCost = getNestedValue(positionResponse.getBody(), "weightedAverageCost", "amount");

        assertThat(new BigDecimal(quantity.toString()))
                .isEqualByComparingTo(new BigDecimal(expectedQuantity));
        assertThat(new BigDecimal(averageCost.toString()))
                .isEqualByComparingTo(new BigDecimal(expectedCost));
    }

    @Then("the position should have {int} units at {int} PLN average cost")
    public void thePositionShouldHaveUnitsAtPLNAverageCost(Integer expectedQuantity, Integer expectedCost) {
        // Same as shares, just different terminology for ETFs
        thePositionShouldHaveSharesAtPLNAverageCost(expectedQuantity, expectedCost);
    }

    @Then("I should see {string}")
    public void iShouldSee(String expectedMessage) {
        if (expectingError) {
            assertThat(errorResponse.getBody()).isNotNull();
            String body = errorResponse.getBody().toString();
            assertThat(body).containsIgnoringCase(expectedMessage.replace("\"", ""));
        } else {
            assertThat(positionResponse.getBody()).isNotNull();
            String body = positionResponse.getBody().toString();
            assertThat(body).containsIgnoringCase(expectedMessage.replace("\"", ""));
        }
    }

    @Then("a bond position should be created")
    public void aBondPositionShouldBeCreated() {
        assertThat(positionResponse.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(positionResponse.getBody()).isNotNull();
    }

    @Then("the position should show invested amount of {int} PLN")
    public void thePositionShouldShowInvestedAmountOfPLN(Integer expectedAmount) {
        assertThat(positionResponse.getBody()).isNotNull();
        Object investedAmount = getNestedValue(positionResponse.getBody(), "investedAmount", "amount");
        assertThat(new BigDecimal(investedAmount.toString()))
                .isEqualByComparingTo(new BigDecimal(expectedAmount));
    }

    @Then("the position should show current value of {int} PLN")
    public void thePositionShouldShowCurrentValueOfPLN(Integer expectedValue) {
        assertThat(positionResponse.getBody()).isNotNull();
        Object currentValue = getNestedValue(positionResponse.getBody(), "currentValue", "amount");
        assertThat(new BigDecimal(currentValue.toString()))
                .isEqualByComparingTo(new BigDecimal(expectedValue));
    }

    @Then("I should see an error {string}")
    public void iShouldSeeAnError(String expectedError) {
        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getStatusCode().is4xxClientError())
                .as("Expected 4xx error response but got: " + errorResponse.getStatusCode())
                .isTrue();
        assertThat(errorResponse.getBody()).isNotNull();
        assertThat(errorResponse.getBody().toString())
                .containsIgnoringCase(expectedError.replace("\"", ""));
    }

    @Then("no position should be created")
    public void noPositionShouldBeCreated() {
        assertThat(errorResponse.getStatusCode().is4xxClientError()).isTrue();
    }

    // --- Helper Methods ---

    private void submitPosition() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(positionData, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/v1/positions",
                request,
                Map.class
        );

        if (expectingError) {
            errorResponse = response;
        } else {
            positionResponse = response;
        }
    }

    /**
     * Ensures an account exists and returns its ID.
     * Creates the account directly in the database if it doesn't exist.
     */
    private Long ensureAccountExists(String accountName) {
        // Check if account exists
        List<Long> existingIds = jdbcTemplate.queryForList(
                "SELECT id FROM accounts WHERE name = ?",
                Long.class,
                accountName
        );

        if (!existingIds.isEmpty()) {
            return existingIds.get(0);
        }

        // Create account
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

    /**
     * Ensures an instrument exists with the given current price.
     * Creates or updates the instrument directly in the database.
     */
    private void ensureInstrumentExists(String symbol, String name, BigDecimal currentPrice) {
        // Check if instrument exists
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM instruments WHERE symbol = ?",
                Integer.class,
                symbol
        );

        if (count != null && count > 0) {
            // Update current price
            jdbcTemplate.update(
                    "UPDATE instruments SET current_price_amount = ?, current_price_currency = 'PLN', price_updated_at = CURRENT_TIMESTAMP WHERE symbol = ?",
                    currentPrice, symbol
            );
        } else {
            // Create instrument
            jdbcTemplate.update(
                    "INSERT INTO instruments (symbol, name, instrument_type, current_price_amount, current_price_currency, price_updated_at, version) VALUES (?, ?, ?, ?, 'PLN', CURRENT_TIMESTAMP, 0)",
                    symbol, name != null ? name : symbol, "STOCK", currentPrice
            );
        }
    }

    /**
     * Gets a nested value from a map.
     */
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

    /**
     * Generates a valid ticker symbol from an instrument name.
     * Ticker symbols must be 1-10 uppercase alphanumeric characters.
     */
    private String generateValidSymbol(String instrumentName) {
        // Remove all non-alphanumeric characters and convert to uppercase
        String symbol = instrumentName.toUpperCase().replaceAll("[^A-Z0-9]", "");
        // Truncate to max 10 characters
        if (symbol.length() > 10) {
            symbol = symbol.substring(0, 10);
        }
        // Ensure at least 1 character
        if (symbol.isEmpty()) {
            symbol = "UNKNOWN";
        }
        return symbol;
    }
}
