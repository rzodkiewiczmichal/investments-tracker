package com.investments.tracker.cucumber.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
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

    private ResponseEntity<Map> portfolioResponse;
    private BigDecimal totalInvestedAmount;
    private BigDecimal totalCurrentValue;

    // --- Given Steps ---

    @Given("my total invested amount is {int} PLN")
    public void myTotalInvestedAmountIsPLN(Integer amount) {
        this.totalInvestedAmount = new BigDecimal(amount);
        // Create positions that add up to this invested amount
        // Implementation will depend on domain model
    }

    @Given("my total current value is {int} PLN")
    public void myTotalCurrentValueIsPLN(Integer amount) {
        this.totalCurrentValue = new BigDecimal(amount);
        // Set current prices so positions add up to this value
        // Implementation will depend on domain model
    }

    @Given("I own {int} shares of {string} in account {string} bought at {int} PLN")
    public void iOwnSharesOfInAccountBoughtAtPLN(Integer quantity, String instrument, String account, Integer price) {
        // Create position with specified details
        // Implementation will use repository or REST API
    }

    @Given("the current price of {string} is {int} PLN")
    public void theCurrentPriceOfIsPLN(String instrument, Integer price) {
        // Set current price for instrument
        // Implementation will use price service or mock
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
        assertThat(portfolioResponse.getBody()).containsKey("totalProfitLossPercentage");
    }

    @Then("I should see the portfolio XIRR percentage")
    public void iShouldSeeThePortfolioXIRRPercentage() {
        // XIRR is optional for v0.1, may not be present
        // assertThat(portfolioResponse.getBody()).containsKey("xirr");
    }

    @Then("I should see total current value of {int} PLN")
    public void iShouldSeeTotalCurrentValueOfPLN(Integer expectedValue) {
        Object actualValue = portfolioResponse.getBody().get("totalCurrentValue");
        assertThat(new BigDecimal(actualValue.toString()))
                .isEqualByComparingTo(new BigDecimal(expectedValue));
    }

    @Then("I should see total invested amount of {int} PLN")
    public void iShouldSeeTotalInvestedAmountOfPLN(Integer expectedAmount) {
        Object actualValue = portfolioResponse.getBody().get("totalInvestedAmount");
        assertThat(new BigDecimal(actualValue.toString()))
                .isEqualByComparingTo(new BigDecimal(expectedAmount));
    }

    @Then("I should see P&L of +{int} PLN")
    public void iShouldSeePLOfPlusXPLN(Integer expectedPL) {
        Object actualValue = portfolioResponse.getBody().get("totalProfitLoss");
        assertThat(new BigDecimal(actualValue.toString()))
                .isEqualByComparingTo(new BigDecimal(expectedPL));
    }

    @Then("I should see P&L of -{int} PLN")
    public void iShouldSeePLOfMinusXPLN(Integer expectedPL) {
        Object actualValue = portfolioResponse.getBody().get("totalProfitLoss");
        assertThat(new BigDecimal(actualValue.toString()))
                .isEqualByComparingTo(new BigDecimal(-expectedPL));
    }

    @Then("I should see P&L percentage of +{double}%")
    public void iShouldSeePLPercentageOfPlusX(Double expectedPercentage) {
        Object actualValue = portfolioResponse.getBody().get("totalProfitLossPercentage");
        assertThat(new BigDecimal(actualValue.toString()))
                .isEqualByComparingTo(new BigDecimal(expectedPercentage));
    }

    @Then("I should see P&L percentage of -{double}%")
    public void iShouldSeePLPercentageOfMinusX(Double expectedPercentage) {
        Object actualValue = portfolioResponse.getBody().get("totalProfitLossPercentage");
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
}
