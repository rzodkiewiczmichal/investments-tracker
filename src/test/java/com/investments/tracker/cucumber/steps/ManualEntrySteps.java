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

    private ResponseEntity<Map> positionResponse;
    private ResponseEntity<Map> errorResponse;
    private Map<String, Object> positionData;
    private boolean expectingError = false;

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
        for (Map<String, String> row : rows) {
            String field = row.get("Field");
            String value = row.get("Value");

            switch (field) {
                case "Instrument":
                    positionData.put("instrumentName", value);
                    positionData.put("instrumentSymbol", value.toUpperCase().replace(" ", "_"));
                    break;
                case "Quantity":
                    positionData.put("quantity", new BigDecimal(value));
                    break;
                case "Average Cost":
                    positionData.put("averageCost", new BigDecimal(value));
                    break;
                case "Account":
                    positionData.put("accountName", value);
                    break;
            }
        }

        // Submit the position
        submitPosition();
    }

    @When("I enter the following bond data:")
    public void iEnterTheFollowingBondData(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps();
        for (Map<String, String> row : rows) {
            String field = row.get("Field");
            String value = row.get("Value");

            switch (field) {
                case "Instrument Type":
                    positionData.put("instrumentType", "POLISH_GOVERNMENT_BOND");
                    break;
                case "Series":
                    positionData.put("instrumentSymbol", value);
                    positionData.put("instrumentName", value);
                    break;
                case "Invested Amount":
                    positionData.put("investedAmount", new BigDecimal(value));
                    break;
                case "Current Value":
                    positionData.put("currentValue", new BigDecimal(value));
                    break;
                case "Account":
                    positionData.put("accountName", value);
                    break;
            }
        }

        // Submit the position
        submitPosition();
    }

    @When("I try to save position without instrument name")
    public void iTryToSavePositionWithoutInstrumentName() {
        expectingError = true;
        positionData.put("quantity", new BigDecimal("100"));
        positionData.put("averageCost", new BigDecimal("50"));
        positionData.put("accountName", "Test Account");
        // instrumentName is missing
        submitPosition();
    }

    @When("I enter quantity as {string}")
    public void iEnterQuantityAs(String quantity) {
        expectingError = true;
        positionData.put("instrumentName", "Test Instrument");
        positionData.put("instrumentSymbol", "TEST");
        positionData.put("quantity", new BigDecimal(quantity));
        positionData.put("averageCost", new BigDecimal("100"));
        positionData.put("accountName", "Test Account");
        submitPosition();
    }

    @When("I enter average cost as {string}")
    public void iEnterAverageCostAs(String averageCost) {
        expectingError = true;
        positionData.put("instrumentName", "Test Instrument");
        positionData.put("instrumentSymbol", "TEST");
        positionData.put("quantity", new BigDecimal("100"));
        positionData.put("averageCost", new BigDecimal(averageCost));
        positionData.put("accountName", "Test Account");
        submitPosition();
    }

    // --- Then Steps ---

    @Then("a new position for {string} should be created")
    public void aNewPositionForShouldBeCreated(String instrumentName) {
        assertThat(positionResponse.getStatusCode().is2xxSuccessful())
                .as("Expected successful response but got: " + positionResponse.getStatusCode())
                .isTrue();
        assertThat(positionResponse.getBody()).isNotNull();
        assertThat(positionResponse.getBody().get("instrumentName").toString())
                .containsIgnoringCase(instrumentName);
    }

    @Then("the position should have {int} shares at {int} PLN average cost")
    public void thePositionShouldHaveSharesAtPLNAverageCost(Integer expectedQuantity, Integer expectedCost) {
        assertThat(positionResponse.getBody()).isNotNull();
        Object quantity = positionResponse.getBody().get("quantity");
        Object averageCost = positionResponse.getBody().get("averageCost");

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
        Object investedAmount = positionResponse.getBody().get("investedAmount");
        assertThat(new BigDecimal(investedAmount.toString()))
                .isEqualByComparingTo(new BigDecimal(expectedAmount));
    }

    @Then("the position should show current value of {int} PLN")
    public void thePositionShouldShowCurrentValueOfPLN(Integer expectedValue) {
        assertThat(positionResponse.getBody()).isNotNull();
        Object currentValue = positionResponse.getBody().get("currentValue");
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
}
