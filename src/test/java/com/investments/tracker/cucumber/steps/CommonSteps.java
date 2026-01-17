package com.investments.tracker.cucumber.steps;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Common Cucumber step definitions shared across all features.
 * <p>
 * Handles:
 * - Background steps that appear in multiple features
 * - Database cleanup before scenarios
 * - Common setup operations
 * </p>
 * <p>
 * <b>Why some Given steps are empty:</b><br>
 * Cucumber requires step definitions for ALL steps in feature files, including
 * Background steps. These "context-setting" steps (e.g., "Given I have positions
 * in multiple accounts") describe the precondition state but don't perform actions.
 * The actual data setup happens in scenario-specific steps. These empty implementations
 * satisfy Cucumber's requirement while keeping the feature files readable.
 * </p>
 */
public class CommonSteps {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Clean database before each scenario to ensure test isolation.
     */
    @Before
    public void cleanDatabase() {
        // Clean in reverse order of foreign key dependencies
        jdbcTemplate.execute("DELETE FROM account_holdings WHERE true");
        jdbcTemplate.execute("DELETE FROM positions WHERE true");
        jdbcTemplate.execute("DELETE FROM instruments WHERE true");
        jdbcTemplate.execute("DELETE FROM accounts WHERE true");
    }

    @Given("I have positions in multiple brokerage accounts")
    public void iHavePositionsInMultipleBrokerageAccounts() {
        // This step sets up the context that positions exist
        // Actual position creation is done in specific scenario steps
    }

    @Given("all positions have been imported into the system")
    public void allPositionsHaveBeenImportedIntoTheSystem() {
        // Background step indicating positions are already in the system
    }

    @Given("current prices are up-to-date")
    public void currentPricesAreUpToDate() {
        // Background step indicating prices are current
        // In tests, we control prices via test data
    }

    @Given("I have positions in multiple instruments")
    public void iHavePositionsInMultipleInstruments() {
        // Background step for position details feature
    }

    @Given("I have no positions in any account")
    public void iHaveNoPositionsInAnyAccount() {
        // Already cleaned by @Before hook
    }

    protected String baseUrl() {
        return "http://localhost:" + port;
    }
}
