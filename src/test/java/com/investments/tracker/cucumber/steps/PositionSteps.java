package com.investments.tracker.cucumber.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Cucumber step definitions for Position Details feature.
 * <p>
 * Tests position detail functionality including:
 * - Individual position metrics
 * - Position listing
 * - P&L calculations
 * </p>
 *
 * @see <a href="requirements/functional/features/position-details.feature">Position Details Feature</a>
 */
public class PositionSteps {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private ResponseEntity<Map> positionResponse;
    private ResponseEntity<List> positionsListResponse;
    private Map<String, UUID> positionIds = new HashMap<>();

    // --- Given Steps ---

    @Given("I own {int} shares of {string} with average cost of {int} PLN")
    public void iOwnSharesOfWithAverageCostOfPLN(Integer quantity, String instrument, Integer averageCost) {
        // Create position via API or directly in database
        Map<String, Object> positionData = new HashMap<>();
        positionData.put("instrumentName", instrument);
        positionData.put("instrumentSymbol", instrument.toUpperCase().replace(" ", "_"));
        positionData.put("instrumentType", "STOCK");
        positionData.put("quantity", new BigDecimal(quantity));
        positionData.put("averageCost", new BigDecimal(averageCost));
        positionData.put("accountName", "Test Account");

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/v1/positions",
                positionData,
                Map.class
        );

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            Object id = response.getBody().get("id");
            if (id != null) {
                positionIds.put(instrument, UUID.fromString(id.toString()));
            }
        }
    }

    @Given("I own {int} units of {string} with average cost of {int} PLN")
    public void iOwnUnitsOfWithAverageCostOfPLN(Integer quantity, String instrument, Integer averageCost) {
        // Same as shares, just for ETFs
        Map<String, Object> positionData = new HashMap<>();
        positionData.put("instrumentName", instrument);
        positionData.put("instrumentSymbol", instrument.toUpperCase().replace(" ", "_"));
        positionData.put("instrumentType", "ETF");
        positionData.put("quantity", new BigDecimal(quantity));
        positionData.put("averageCost", new BigDecimal(averageCost));
        positionData.put("accountName", "Test Account");

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/v1/positions",
                positionData,
                Map.class
        );

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            Object id = response.getBody().get("id");
            if (id != null) {
                positionIds.put(instrument, UUID.fromString(id.toString()));
            }
        }
    }

    @Given("I own Polish government bonds with invested amount of {int} PLN")
    public void iOwnPolishGovernmentBondsWithInvestedAmountOfPLN(Integer investedAmount) {
        // Create bond position
        Map<String, Object> positionData = new HashMap<>();
        positionData.put("instrumentName", "Polish Government Bond");
        positionData.put("instrumentSymbol", "PL_BOND");
        positionData.put("instrumentType", "POLISH_GOVERNMENT_BOND");
        positionData.put("investedAmount", new BigDecimal(investedAmount));
        positionData.put("accountName", "Bond Account");

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/v1/positions",
                positionData,
                Map.class
        );

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            Object id = response.getBody().get("id");
            if (id != null) {
                positionIds.put("Polish Government Bond", UUID.fromString(id.toString()));
            }
        }
    }

    @Given("the current value of these bonds is {int} PLN")
    public void theCurrentValueOfTheseBondsIsPLN(Integer currentValue) {
        // Update the bond's current value
        // Implementation depends on how prices are managed
    }

    // --- When Steps ---

    @When("I view the position details for {string}")
    public void iViewThePositionDetailsFor(String instrument) {
        UUID positionId = positionIds.get(instrument);
        if (positionId != null) {
            positionResponse = restTemplate.getForEntity(
                    "http://localhost:" + port + "/api/v1/positions/" + positionId,
                    Map.class
            );
        } else {
            // Try to find position by instrument name
            positionResponse = restTemplate.getForEntity(
                    "http://localhost:" + port + "/api/v1/positions?instrument=" + instrument,
                    Map.class
            );
        }
    }

    @When("I view the position details for Polish government bonds")
    public void iViewThePositionDetailsForPolishGovernmentBonds() {
        iViewThePositionDetailsFor("Polish Government Bond");
    }

    @When("I view the positions list")
    public void iViewThePositionsList() {
        positionsListResponse = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/v1/positions",
                List.class
        );
    }

    // --- Then Steps ---

    @Then("I should see quantity of {int} shares")
    public void iShouldSeeQuantityOfShares(Integer expectedQuantity) {
        assertThat(positionResponse.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(positionResponse.getBody()).isNotNull();
        Object quantity = positionResponse.getBody().get("quantity");
        assertThat(new BigDecimal(quantity.toString()))
                .isEqualByComparingTo(new BigDecimal(expectedQuantity));
    }

    @Then("I should see quantity of {int} units")
    public void iShouldSeeQuantityOfUnits(Integer expectedQuantity) {
        iShouldSeeQuantityOfShares(expectedQuantity);
    }

    @Then("I should see average cost basis of {int} PLN")
    public void iShouldSeeAverageCostBasisOfPLN(Integer expectedCost) {
        assertThat(positionResponse.getBody()).isNotNull();
        Object averageCost = positionResponse.getBody().get("averageCost");
        assertThat(new BigDecimal(averageCost.toString()))
                .isEqualByComparingTo(new BigDecimal(expectedCost));
    }

    @Then("I should see invested amount of {int} PLN")
    public void iShouldSeeInvestedAmountOfPLN(Integer expectedAmount) {
        assertThat(positionResponse.getBody()).isNotNull();
        Object investedAmount = positionResponse.getBody().get("investedAmount");
        assertThat(new BigDecimal(investedAmount.toString()))
                .isEqualByComparingTo(new BigDecimal(expectedAmount));
    }

    @Then("I should see current value of {int} PLN")
    public void iShouldSeeCurrentValueOfPLN(Integer expectedValue) {
        assertThat(positionResponse.getBody()).isNotNull();
        Object currentValue = positionResponse.getBody().get("currentValue");
        assertThat(new BigDecimal(currentValue.toString()))
                .isEqualByComparingTo(new BigDecimal(expectedValue));
    }

    @Then("I should see P&L of +{int} PLN")
    public void iShouldSeePLOfPlusPLN(Integer expectedPL) {
        assertThat(positionResponse.getBody()).isNotNull();
        Object profitLoss = positionResponse.getBody().get("profitLoss");
        assertThat(new BigDecimal(profitLoss.toString()))
                .isEqualByComparingTo(new BigDecimal(expectedPL));
    }

    @Then("I should see P&L of -{int} PLN")
    public void iShouldSeePLOfMinusPLN(Integer expectedPL) {
        assertThat(positionResponse.getBody()).isNotNull();
        Object profitLoss = positionResponse.getBody().get("profitLoss");
        assertThat(new BigDecimal(profitLoss.toString()))
                .isEqualByComparingTo(new BigDecimal(-expectedPL));
    }

    @Then("I should see P&L percentage of +{double}%")
    public void iShouldSeePLPercentageOfPlus(Double expectedPercentage) {
        assertThat(positionResponse.getBody()).isNotNull();
        Object profitLossPercentage = positionResponse.getBody().get("profitLossPercentage");
        assertThat(new BigDecimal(profitLossPercentage.toString()))
                .isEqualByComparingTo(new BigDecimal(expectedPercentage));
    }

    @Then("I should see P&L percentage of -{double}%")
    public void iShouldSeePLPercentageOfMinus(Double expectedPercentage) {
        assertThat(positionResponse.getBody()).isNotNull();
        Object profitLossPercentage = positionResponse.getBody().get("profitLossPercentage");
        assertThat(new BigDecimal(profitLossPercentage.toString()))
                .isEqualByComparingTo(new BigDecimal(-expectedPercentage));
    }

    @Then("I should see the position XIRR if available")
    public void iShouldSeeThePositionXIRRIfAvailable() {
        // XIRR is optional for v0.1
        // May or may not be present in the response
    }

    @Then("I should see {int} positions")
    public void iShouldSeePositions(Integer expectedCount) {
        assertThat(positionsListResponse.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(positionsListResponse.getBody()).isNotNull();
        assertThat(positionsListResponse.getBody()).hasSize(expectedCount);
    }

    @Then("each position should show instrument name")
    public void eachPositionShouldShowInstrumentName() {
        assertThat(positionsListResponse.getBody()).isNotNull();
        for (Object position : positionsListResponse.getBody()) {
            Map<String, Object> posMap = (Map<String, Object>) position;
            assertThat(posMap).containsKey("instrumentName");
        }
    }

    @Then("each position should show current value")
    public void eachPositionShouldShowCurrentValue() {
        assertThat(positionsListResponse.getBody()).isNotNull();
        for (Object position : positionsListResponse.getBody()) {
            Map<String, Object> posMap = (Map<String, Object>) position;
            assertThat(posMap).containsKey("currentValue");
        }
    }

    @Then("each position should show P&L percentage")
    public void eachPositionShouldShowPLPercentage() {
        assertThat(positionsListResponse.getBody()).isNotNull();
        for (Object position : positionsListResponse.getBody()) {
            Map<String, Object> posMap = (Map<String, Object>) position;
            assertThat(posMap).containsKey("profitLossPercentage");
        }
    }

    @Then("positions should be sorted by current value descending")
    public void positionsShouldBeSortedByCurrentValueDescending() {
        assertThat(positionsListResponse.getBody()).isNotNull();
        List<Map<String, Object>> positions = positionsListResponse.getBody();

        if (positions.size() > 1) {
            for (int i = 0; i < positions.size() - 1; i++) {
                BigDecimal current = new BigDecimal(positions.get(i).get("currentValue").toString());
                BigDecimal next = new BigDecimal(positions.get(i + 1).get("currentValue").toString());
                assertThat(current).isGreaterThanOrEqualTo(next);
            }
        }
    }
}
