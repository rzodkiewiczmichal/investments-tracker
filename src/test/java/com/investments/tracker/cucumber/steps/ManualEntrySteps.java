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
                    testInstrumentSymbol = CucumberTestHelper.generateValidSymbol(value);
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
        testAccountId = CucumberTestHelper.ensureAccountExists(jdbcTemplate, accountName);

        // Build the request with the correct API contract
        positionData.put("instrumentSymbol", testInstrumentSymbol);
        positionData.put("instrumentName", instrumentName);
        if (!positionData.containsKey("instrumentType")) {
            positionData.put("instrumentType", "STOCK");
        }
        positionData.putIfAbsent("currency", "PLN");
        positionData.put("accountId", testAccountId);
        positionData.put("quantity", quantity);
        positionData.put("averageCost", averageCost);

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
        testAccountId = CucumberTestHelper.ensureAccountExists(jdbcTemplate, accountName);

        // Build request - for bonds, quantity is 1 and cost basis is the invested amount
        positionData.put("instrumentSymbol", testInstrumentSymbol);
        positionData.put("instrumentName", seriesName);
        if (!positionData.containsKey("instrumentType")) {
            positionData.put("instrumentType", "BOND");
        }
        positionData.putIfAbsent("currency", "PLN");
        positionData.put("accountId", testAccountId);
        positionData.put("quantity", BigDecimal.ONE);
        positionData.put("averageCost", investedAmount);

        // Submit the position
        submitPosition();
    }

    @When("I try to save position without instrument name")
    public void iTryToSavePositionWithoutInstrumentName() {
        expectingError = true;
        testAccountId = CucumberTestHelper.ensureAccountExists(jdbcTemplate, "Test Account");

        // Missing instrumentSymbol and instrumentName
        positionData.put("instrumentType", "STOCK");
        positionData.put("currency", "PLN");
        positionData.put("accountId", testAccountId);
        positionData.put("quantity", new BigDecimal("100"));
        positionData.put("averageCost", new BigDecimal("50"));
        submitPosition();
    }

    @When("I enter quantity as {string}")
    public void iEnterQuantityAs(String quantity) {
        expectingError = true;
        testAccountId = CucumberTestHelper.ensureAccountExists(jdbcTemplate, "Test Account");
        testInstrumentSymbol = "TEST";

        positionData.put("instrumentSymbol", testInstrumentSymbol);
        positionData.put("instrumentName", "Test Instrument");
        positionData.put("instrumentType", "STOCK");
        positionData.put("currency", "PLN");
        positionData.put("accountId", testAccountId);
        positionData.put("quantity", new BigDecimal(quantity));
        positionData.put("averageCost", new BigDecimal("100"));
        submitPosition();
    }

    @When("I enter average cost as {string}")
    public void iEnterAverageCostAs(String averageCost) {
        expectingError = true;
        testAccountId = CucumberTestHelper.ensureAccountExists(jdbcTemplate, "Test Account");
        testInstrumentSymbol = "TEST";

        positionData.put("instrumentSymbol", testInstrumentSymbol);
        positionData.put("instrumentName", "Test Instrument");
        positionData.put("instrumentType", "STOCK");
        positionData.put("currency", "PLN");
        positionData.put("accountId", testAccountId);
        positionData.put("quantity", new BigDecimal("100"));
        positionData.put("averageCost", new BigDecimal(averageCost));
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
        // The response uses instrumentSymbol - use the same symbol generation as in setup
        String expectedSymbol = CucumberTestHelper.generateValidSymbol(instrumentName);
        assertThat(positionResponse.getBody().get("instrumentSymbol").toString())
                .isEqualToIgnoringCase(expectedSymbol);
    }

    @Then("the position should have {int} shares at {int} PLN average cost")
    public void thePositionShouldHaveSharesAtPLNAverageCost(Integer expectedQuantity, Integer expectedCost) {
        assertThat(positionResponse.getBody()).isNotNull();
        Object quantity = positionResponse.getBody().get("quantity");
        Object averageCost = CucumberTestHelper.getNestedValue(positionResponse.getBody(), "averageCost", "amount");

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
            // For success responses, a 2xx with position data confirms the operation
            assertThat(positionResponse.getStatusCode().is2xxSuccessful())
                    .as("Expected successful response for: " + expectedMessage)
                    .isTrue();
            assertThat(positionResponse.getBody()).isNotNull();
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
        Object investedAmount = CucumberTestHelper.getNestedValue(positionResponse.getBody(), "investedAmount", "amount");
        assertThat(new BigDecimal(investedAmount.toString()))
                .isEqualByComparingTo(new BigDecimal(expectedAmount));
    }

    @Then("the position should show current value of {int} PLN")
    public void thePositionShouldShowCurrentValueOfPLN(Integer expectedValue) {
        assertThat(positionResponse.getBody()).isNotNull();
        Object currentValue = CucumberTestHelper.getNestedValue(positionResponse.getBody(), "currentValue", "amount");
        assertThat(new BigDecimal(currentValue.toString()))
                .isEqualByComparingTo(new BigDecimal(expectedValue));
    }

    @Then("the position should not show current value")
    public void thePositionShouldNotShowCurrentValue() {
        assertThat(positionResponse.getBody()).isNotNull();
        Object currentValue = positionResponse.getBody().get("currentValue");
        assertThat(currentValue).isNull();
    }

    @Then("the position should not show P&L")
    public void thePositionShouldNotShowPL() {
        assertThat(positionResponse.getBody()).isNotNull();
        Object profitLoss = positionResponse.getBody().get("profitLoss");
        assertThat(profitLoss).isNull();
        Object returnPercentage = positionResponse.getBody().get("returnPercentage");
        assertThat(returnPercentage).isNull();
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
}
