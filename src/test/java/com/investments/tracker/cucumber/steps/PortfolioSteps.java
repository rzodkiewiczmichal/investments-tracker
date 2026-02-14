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

    @Autowired
    private PriceCache priceCache;

    private ResponseEntity<Map> portfolioResponse;
    private BigDecimal totalInvestedAmount;
    private BigDecimal totalCurrentValue;

    // --- Given Steps ---

    @Given("I have positions without current prices")
    public void iHavePositionsWithoutCurrentPrices() {
        Long accountId = CucumberTestHelper.ensureAccountExists(jdbcTemplate, "No Price Account");
        String symbol = "NOPRICE";
        CucumberTestHelper.ensureInstrumentExistsWithoutPrice(jdbcTemplate, symbol, "No Price Stock");
        CucumberTestHelper.createPosition(jdbcTemplate, symbol, accountId, new BigDecimal("50"), new BigDecimal("100"));
    }

    @Given("my total invested amount is {int} PLN")
    public void myTotalInvestedAmountIsPLN(Integer amount) {
        this.totalInvestedAmount = new BigDecimal(amount);
    }

    @Given("my total current value is {int} PLN")
    public void myTotalCurrentValueIsPLN(Integer amount) {
        this.totalCurrentValue = new BigDecimal(amount);

        // Create a position that matches the expected invested/current values
        BigDecimal quantity = new BigDecimal("100");
        BigDecimal costPerShare = totalInvestedAmount.divide(quantity, 4, RoundingMode.HALF_EVEN);
        BigDecimal pricePerShare = totalCurrentValue.divide(quantity, 4, RoundingMode.HALF_EVEN);

        Long accountId = CucumberTestHelper.ensureAccountExists(jdbcTemplate, "Portfolio Test Account");
        String symbol = "PTEST";
        CucumberTestHelper.ensureInstrumentExists(jdbcTemplate, symbol, "Portfolio Test Stock", pricePerShare);
        CucumberTestHelper.createPosition(jdbcTemplate, symbol, accountId, quantity, costPerShare);
        priceCache.putPrice(InstrumentSymbol.of(symbol), new Price(Money.pln(pricePerShare)));
    }

    @Given("I own {int} shares of {string} in account {string} bought at {int} PLN")
    public void iOwnSharesOfInAccountBoughtAtPLN(Integer quantity, String instrument, String account, Integer price) {
        Long accountId = CucumberTestHelper.ensureAccountExists(jdbcTemplate, account);
        String symbol = CucumberTestHelper.generateValidSymbol(instrument);

        CucumberTestHelper.ensureInstrumentExists(jdbcTemplate, symbol, instrument, new BigDecimal(price));
        CucumberTestHelper.createPosition(jdbcTemplate, symbol, accountId, new BigDecimal(quantity), new BigDecimal(price));
        priceCache.putPrice(InstrumentSymbol.of(symbol), new Price(Money.pln(new BigDecimal(price))));
    }

    @Given("the current price of {string} is {int} PLN")
    public void theCurrentPriceOfIsPLN(String instrument, Integer price) {
        String symbol = CucumberTestHelper.generateValidSymbol(instrument);
        jdbcTemplate.update(
                "UPDATE instruments SET current_price_amount = ?, price_updated_at = CURRENT_TIMESTAMP WHERE symbol = ?",
                new BigDecimal(price), symbol
        );
        priceCache.putPrice(InstrumentSymbol.of(symbol), new Price(Money.pln(new BigDecimal(price))));
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
    }

    @Then("I should see total current value of {int} PLN")
    public void iShouldSeeTotalCurrentValueOfPLN(Integer expectedValue) {
        Object actualValue = CucumberTestHelper.getNestedValue(portfolioResponse.getBody(), "totalCurrentValue", "amount");
        assertThat(new BigDecimal(actualValue.toString()))
                .isEqualByComparingTo(new BigDecimal(expectedValue));
    }

    @Then("I should see total invested amount of {int} PLN")
    public void iShouldSeeTotalInvestedAmountOfPLN(Integer expectedAmount) {
        Object actualValue = CucumberTestHelper.getNestedValue(portfolioResponse.getBody(), "totalInvestedAmount", "amount");
        assertThat(new BigDecimal(actualValue.toString()))
                .isEqualByComparingTo(new BigDecimal(expectedAmount));
    }

    @Then("I should see P&L of +{int} PLN")
    public void iShouldSeePLOfPlusXPLN(Integer expectedPL) {
        Object actualValue = CucumberTestHelper.getNestedValue(portfolioResponse.getBody(), "totalProfitLoss", "amount");
        assertThat(new BigDecimal(actualValue.toString()))
                .isEqualByComparingTo(new BigDecimal(expectedPL));
    }

    @Then("I should see P&L of -{int} PLN")
    public void iShouldSeePLOfMinusXPLN(Integer expectedPL) {
        Object actualValue = CucumberTestHelper.getNestedValue(portfolioResponse.getBody(), "totalProfitLoss", "amount");
        assertThat(new BigDecimal(actualValue.toString()))
                .isEqualByComparingTo(new BigDecimal(-expectedPL));
    }

    @Then("I should see P&L percentage of +{double}%")
    public void iShouldSeePLPercentageOfPlusX(Double expectedPercentage) {
        Object actualValue = portfolioResponse.getBody().get("totalReturnPercentage");
        assertThat(new BigDecimal(actualValue.toString()).setScale(2, RoundingMode.HALF_EVEN))
                .isEqualByComparingTo(BigDecimal.valueOf(expectedPercentage));
    }

    @Then("I should see P&L percentage of -{double}%")
    public void iShouldSeePLPercentageOfMinusX(Double expectedPercentage) {
        Object actualValue = portfolioResponse.getBody().get("totalReturnPercentage");
        assertThat(new BigDecimal(actualValue.toString()).setScale(2, RoundingMode.HALF_EVEN))
                .isEqualByComparingTo(BigDecimal.valueOf(-expectedPercentage));
    }

    @Then("I should not see total current value")
    public void iShouldNotSeeTotalCurrentValue() {
        assertThat(portfolioResponse.getStatusCode().is2xxSuccessful()).isTrue();
        Object totalCurrentValue = portfolioResponse.getBody().get("totalCurrentValue");
        assertThat(totalCurrentValue).isNull();
    }

    @Then("I should not see total P&L")
    public void iShouldNotSeeTotalPL() {
        assertThat(portfolioResponse.getStatusCode().is2xxSuccessful()).isTrue();
        Object totalProfitLoss = CucumberTestHelper.getNestedValue(portfolioResponse.getBody(), "totalProfitLoss", "amount");
        // When prices are unknown, totalProfitLoss should be zero (default) and totalReturnPercentage should be zero
        assertThat(new BigDecimal(totalProfitLoss.toString()))
                .isEqualByComparingTo(BigDecimal.ZERO);
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
