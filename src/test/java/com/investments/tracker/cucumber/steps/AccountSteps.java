package com.investments.tracker.cucumber.steps;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

/**
 * Cucumber step definitions for Account Management feature.
 *
 * @see <a href="requirements/functional/features/account-management.feature">Account Management
 *     Feature</a>
 */
public class AccountSteps {

    @LocalServerPort private int port;

    @Autowired private TestRestTemplate restTemplate;

    @Autowired private JdbcTemplate jdbcTemplate;

    private ResponseEntity<Map> response;
    private long accountCountBefore;

    // --- Given Steps ---

    @Given("I want to add a new brokerage account")
    public void iWantToAddANewBrokerageAccount() {
        accountCountBefore = countAccounts();
    }

    @Given("an account named {string} already exists")
    public void anAccountNamedAlreadyExists(String accountName) {
        CucumberTestHelper.ensureAccountExists(jdbcTemplate, accountName);
        accountCountBefore = countAccounts();
    }

    @Given("no accounts exist in the system")
    public void noAccountsExistInTheSystem() {
        accountCountBefore = 0;
    }

    // --- When Steps ---

    @When("I enter the following account data:")
    public void iEnterTheFollowingAccountData(DataTable dataTable) {
        Map<String, String> data = dataTable.asMap();
        Map<String, String> body = new HashMap<>();
        body.put("name", data.get("Name"));
        body.put("broker", data.get("Broker"));
        response = postAccount(body);
    }

    @When("I try to create an account without a name")
    public void iTryToCreateAnAccountWithoutAName() {
        Map<String, String> body = new HashMap<>();
        body.put("broker", "Some Broker");
        response = postAccount(body);
    }

    @When("I try to create an account without a broker name")
    public void iTryToCreateAnAccountWithoutABrokerName() {
        Map<String, String> body = new HashMap<>();
        body.put("name", "Some Account");
        response = postAccount(body);
    }

    @When("I try to create another account named {string}")
    public void iTryToCreateAnotherAccountNamed(String accountName) {
        Map<String, String> body = new HashMap<>();
        body.put("name", accountName);
        body.put("broker", "Different Broker");
        response = postAccount(body);
    }

    @When("I create a new account {string} with broker {string} during position entry")
    public void iCreateANewAccountWithBrokerDuringPositionEntry(String name, String broker) {
        Map<String, String> body = new HashMap<>();
        body.put("name", name);
        body.put("broker", broker);
        response = postAccount(body);
        assertThat(response.getStatusCode().value()).isEqualTo(201);
    }

    // --- Then Steps ---

    @Then("a new account {string} should be created")
    public void aNewAccountShouldBeCreated(String accountName) {
        assertThat(response.getStatusCode().value()).isEqualTo(201);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("name").toString()).isEqualTo(accountName);
    }

    @Then("the account should be available for position assignment")
    public void theAccountShouldBeAvailableForPositionAssignment() {
        ResponseEntity<Map> listResponse =
                restTemplate.getForEntity(
                        "http://localhost:" + port + "/api/v1/accounts", Map.class);
        assertThat(listResponse.getStatusCode().is2xxSuccessful()).isTrue();
        List<?> accounts = (List<?>) listResponse.getBody().get("accounts");
        assertThat(accounts).isNotEmpty();
    }

    @Then("I should see an account error {string}")
    public void iShouldSeeAnAccountError(String expectedMessage) {
        assertThat(response.getStatusCode().is4xxClientError())
                .as("Expected 4xx error but got: " + response.getStatusCode())
                .isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().toString())
                .containsIgnoringCase(expectedMessage.replace("\"", ""));
    }

    @Then("no account should be created")
    public void noAccountShouldBeCreated() {
        assertThat(countAccounts()).isEqualTo(accountCountBefore);
    }

    @Then("the position should be assigned to account {string}")
    public void thePositionShouldBeAssignedToAccount(String accountName) {
        List<Long> ids =
                jdbcTemplate.queryForList(
                        "SELECT id FROM accounts WHERE name = ?", Long.class, accountName);
        assertThat(ids).isNotEmpty();
    }

    // --- Helper Methods ---

    private ResponseEntity<Map> postAccount(Map<String, String> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
        return restTemplate.postForEntity(
                "http://localhost:" + port + "/api/v1/accounts", request, Map.class);
    }

    private long countAccounts() {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM accounts", Long.class);
        return count != null ? count : 0;
    }
}
