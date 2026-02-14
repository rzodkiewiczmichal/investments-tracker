package com.investments.tracker.cucumber.steps;

import com.investments.tracker.domain.model.value.InstrumentSymbol;
import com.investments.tracker.domain.model.value.Money;
import com.investments.tracker.domain.model.value.Price;
import com.investments.tracker.domain.repository.PriceCache;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PriceCache priceCache;

    private ResponseEntity<Map> positionResponse;
    private ResponseEntity<Map> positionsListResponse;
    private Map<String, String> instrumentSymbols = new HashMap<>();

    // --- Given Steps ---

    @Given("I own {int} shares of {string} with average cost of {int} PLN")
    public void iOwnSharesOfWithAverageCostOfPLN(Integer quantity, String instrument, Integer averageCost) {
        String symbol = CucumberTestHelper.generateValidSymbol(instrument);
        instrumentSymbols.put(instrument, symbol);

        Long accountId = CucumberTestHelper.ensureAccountExists(jdbcTemplate, "Test Account");
        CucumberTestHelper.ensureInstrumentExists(jdbcTemplate, symbol, instrument, new BigDecimal(averageCost));
        CucumberTestHelper.createPosition(jdbcTemplate, symbol, accountId, new BigDecimal(quantity), new BigDecimal(averageCost));
        priceCache.putPrice(InstrumentSymbol.of(symbol), new Price(Money.pln(new BigDecimal(averageCost))));
    }

    @Given("I own {int} units of {string} with average cost of {int} PLN")
    public void iOwnUnitsOfWithAverageCostOfPLN(Integer quantity, String instrument, Integer averageCost) {
        // Same as shares, just for ETFs
        iOwnSharesOfWithAverageCostOfPLN(quantity, instrument, averageCost);
    }

    @Given("I own {int} shares of {string}")
    public void iOwnSharesOf(Integer quantity, String instrument) {
        // Default average cost of 100 PLN
        iOwnSharesOfWithAverageCostOfPLN(quantity, instrument, 100);
    }

    @Given("I own {int} units of {string}")
    public void iOwnUnitsOf(Integer quantity, String instrument) {
        // Default average cost of 100 PLN
        iOwnSharesOfWithAverageCostOfPLN(quantity, instrument, 100);
    }

    @Given("I own Polish government bonds with invested amount of {int} PLN")
    public void iOwnPolishGovernmentBondsWithInvestedAmountOfPLN(Integer investedAmount) {
        String symbol = "PLGBOND";
        instrumentSymbols.put("Polish Government Bond", symbol);

        Long accountId = CucumberTestHelper.ensureAccountExists(jdbcTemplate, "Bond Account");
        CucumberTestHelper.ensureInstrumentExists(jdbcTemplate, symbol, "Polish Government Bond", new BigDecimal(investedAmount));
        CucumberTestHelper.createPosition(jdbcTemplate, symbol, accountId, BigDecimal.ONE, new BigDecimal(investedAmount));
        priceCache.putPrice(InstrumentSymbol.of(symbol), new Price(Money.pln(new BigDecimal(investedAmount))));
    }

    @Given("the current value of these bonds is {int} PLN")
    public void theCurrentValueOfTheseBondsIsPLN(Integer currentValue) {
        String symbol = instrumentSymbols.get("Polish Government Bond");
        if (symbol != null) {
            priceCache.putPrice(InstrumentSymbol.of(symbol), new Price(Money.pln(new BigDecimal(currentValue))));
        }
    }

    // --- When Steps ---

    @When("I view the position details for {string}")
    public void iViewThePositionDetailsFor(String instrument) {
        String symbol = instrumentSymbols.getOrDefault(instrument, CucumberTestHelper.generateValidSymbol(instrument));
        positionResponse = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/v1/positions/" + symbol,
                Map.class
        );
    }

    @When("I view the position details for Polish government bonds")
    public void iViewThePositionDetailsForPolishGovernmentBonds() {
        iViewThePositionDetailsFor("Polish Government Bond");
    }

    @When("I view the positions list")
    public void iViewThePositionsList() {
        positionsListResponse = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/v1/positions",
                Map.class
        );
    }

    // --- Then Steps ---

    @Then("I should see quantity of {int} shares")
    public void iShouldSeeQuantityOfShares(Integer expectedQuantity) {
        assertThat(positionResponse.getStatusCode().is2xxSuccessful())
                .as("Expected successful response but got: " + positionResponse.getStatusCode() + ", body: " + positionResponse.getBody())
                .isTrue();
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
        Object averageCost = CucumberTestHelper.getNestedValue(positionResponse.getBody(), "averageCost", "amount");
        assertThat(new BigDecimal(averageCost.toString()))
                .isEqualByComparingTo(new BigDecimal(expectedCost));
    }

    @Then("I should see invested amount of {int} PLN")
    public void iShouldSeeInvestedAmountOfPLN(Integer expectedAmount) {
        assertThat(positionResponse.getBody()).isNotNull();
        Object investedAmount = CucumberTestHelper.getNestedValue(positionResponse.getBody(), "investedAmount", "amount");
        assertThat(new BigDecimal(investedAmount.toString()))
                .isEqualByComparingTo(new BigDecimal(expectedAmount));
    }

    @Then("I should see current value of {int} PLN")
    public void iShouldSeeCurrentValueOfPLN(Integer expectedValue) {
        assertThat(positionResponse.getBody()).isNotNull();
        Object currentValue = CucumberTestHelper.getNestedValue(positionResponse.getBody(), "currentValue", "amount");
        assertThat(new BigDecimal(currentValue.toString()))
                .isEqualByComparingTo(new BigDecimal(expectedValue));
    }

    @Then("I should see position P&L of +{int} PLN")
    public void iShouldSeePositionPLOfPlusPLN(Integer expectedPL) {
        assertThat(positionResponse.getBody()).isNotNull();
        Object profitLoss = CucumberTestHelper.getNestedValue(positionResponse.getBody(), "profitLoss", "amount");
        assertThat(new BigDecimal(profitLoss.toString()))
                .isEqualByComparingTo(new BigDecimal(expectedPL));
    }

    @Then("I should see position P&L of -{int} PLN")
    public void iShouldSeePositionPLOfMinusPLN(Integer expectedPL) {
        assertThat(positionResponse.getBody()).isNotNull();
        Object profitLoss = CucumberTestHelper.getNestedValue(positionResponse.getBody(), "profitLoss", "amount");
        assertThat(new BigDecimal(profitLoss.toString()))
                .isEqualByComparingTo(new BigDecimal(-expectedPL));
    }

    @Then("I should see position P&L percentage of +{double}%")
    public void iShouldSeePositionPLPercentageOfPlus(Double expectedPercentage) {
        assertThat(positionResponse.getBody()).isNotNull();
        Object profitLossPercentage = positionResponse.getBody().get("returnPercentage");
        assertThat(new BigDecimal(profitLossPercentage.toString()).setScale(2, RoundingMode.HALF_EVEN))
                .isEqualByComparingTo(BigDecimal.valueOf(expectedPercentage));
    }

    @Then("I should see position P&L percentage of -{double}%")
    public void iShouldSeePositionPLPercentageOfMinus(Double expectedPercentage) {
        assertThat(positionResponse.getBody()).isNotNull();
        Object profitLossPercentage = positionResponse.getBody().get("returnPercentage");
        assertThat(new BigDecimal(profitLossPercentage.toString()).setScale(2, RoundingMode.HALF_EVEN))
                .isEqualByComparingTo(BigDecimal.valueOf(-expectedPercentage));
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
        @SuppressWarnings("unchecked")
        List<Object> positions = (List<Object>) positionsListResponse.getBody().get("positions");
        assertThat(positions).hasSize(expectedCount);
    }

    @Then("each position should show instrument name")
    public void eachPositionShouldShowInstrumentName() {
        assertThat(positionsListResponse.getBody()).isNotNull();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> positions = (List<Map<String, Object>>) positionsListResponse.getBody().get("positions");
        for (Map<String, Object> position : positions) {
            assertThat(position).containsKey("instrumentName");
        }
    }

    @Then("each position should show current value")
    public void eachPositionShouldShowCurrentValue() {
        assertThat(positionsListResponse.getBody()).isNotNull();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> positions = (List<Map<String, Object>>) positionsListResponse.getBody().get("positions");
        for (Map<String, Object> position : positions) {
            assertThat(position).containsKey("currentValue");
        }
    }

    @Then("each position should show P&L percentage")
    public void eachPositionShouldShowPLPercentage() {
        assertThat(positionsListResponse.getBody()).isNotNull();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> positions = (List<Map<String, Object>>) positionsListResponse.getBody().get("positions");
        for (Map<String, Object> position : positions) {
            assertThat(position).containsKey("returnPercentage");
        }
    }

    @Then("positions should be sorted by current value descending")
    public void positionsShouldBeSortedByCurrentValueDescending() {
        assertThat(positionsListResponse.getBody()).isNotNull();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> positions = (List<Map<String, Object>>) positionsListResponse.getBody().get("positions");

        if (positions.size() > 1) {
            for (int i = 0; i < positions.size() - 1; i++) {
                @SuppressWarnings("unchecked")
                Map<String, Object> currentValue = (Map<String, Object>) positions.get(i).get("currentValue");
                @SuppressWarnings("unchecked")
                Map<String, Object> nextValue = (Map<String, Object>) positions.get(i + 1).get("currentValue");
                BigDecimal current = new BigDecimal(currentValue.get("amount").toString());
                BigDecimal next = new BigDecimal(nextValue.get("amount").toString());
                assertThat(current).isGreaterThanOrEqualTo(next);
            }
        }
    }
}
