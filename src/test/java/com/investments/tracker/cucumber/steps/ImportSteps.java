package com.investments.tracker.cucumber.steps;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

/** Cucumber step definitions for broker transaction history import. */
public class ImportSteps {

    private static final Charset WINDOWS_1250 = Charset.forName("Windows-1250");

    @LocalServerPort private int port;

    @Autowired private TestRestTemplate restTemplate;

    @Autowired private JdbcTemplate jdbcTemplate;

    private ResponseEntity<Map> importResponse;
    private ResponseEntity<Map> confirmResponse;
    private ResponseEntity<Map> pricesResponse;
    private String importSessionId;

    // XTB XLSX builder state (accumulated across steps)
    private List<Map<String, String>> pendingClosedPositions = new ArrayList<>();
    private List<Map<String, String>> pendingCashOperations = new ArrayList<>();

    @Given("account {string} exists with broker {string}")
    public void accountExistsWithBroker(String accountName, String broker) {
        Long id = CucumberTestHelper.ensureAccountExists(jdbcTemplate, accountName);
        jdbcTemplate.update("UPDATE accounts SET broker_name = ? WHERE id = ?", broker, id);
    }

    @Given("a position for {string} exists with {int} units at {double} cost in account {string}")
    public void positionExistsInAccount(
            String symbol, int quantity, double costBasis, String accountName) {
        Long accountId = CucumberTestHelper.ensureAccountExists(jdbcTemplate, accountName);
        CucumberTestHelper.createPosition(
                jdbcTemplate,
                symbol,
                accountId,
                BigDecimal.valueOf(quantity),
                BigDecimal.valueOf(costBasis));
    }

    @When("I upload an mBank CSV with the following transactions:")
    public void iUploadMBankCsv(DataTable dataTable) {
        iUploadMBankCsvForAccount("mBank eMAKLER", dataTable);
    }

    @When("I upload an mBank CSV for account {string} with the following transactions:")
    public void iUploadMBankCsvForAccount(String accountName, DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps();
        String csv = buildMBankCsv(rows);
        uploadMBankCsv(accountName, csv);
    }

    @When("I upload an mBank CSV for account {string} with mixed currency transactions:")
    public void iUploadMBankCsvWithMixedCurrencies(String accountName, DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps();
        String csv = buildMBankCsvWithCurrency(rows);
        uploadMBankCsv(accountName, csv);
    }

    private void uploadMBankCsv(String accountName, String csv) {
        byte[] csvBytes = csv.getBytes(WINDOWS_1250);

        ByteArrayResource resource =
                new ByteArrayResource(csvBytes) {
                    @Override
                    public String getFilename() {
                        return "transactions.csv";
                    }
                };

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("broker", "mBank");
        body.add("accountName", accountName);
        body.add("file", resource);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        importResponse =
                restTemplate.postForEntity(
                        baseUrl() + "/api/v1/imports", new HttpEntity<>(body, headers), Map.class);

        if (importResponse.getStatusCode().is2xxSuccessful() && importResponse.getBody() != null) {
            importSessionId = (String) importResponse.getBody().get("importSessionId");
        }
    }

    @Then("the import session status should be {string}")
    public void importSessionStatusShouldBe(String expectedStatus) {
        Map<String, Object> responseBody = getLatestResponseBody();
        assertThat(responseBody.get("status")).isEqualTo(expectedStatus);
    }

    @Then("the import summary should show {int} matched instruments")
    public void importSummaryShouldShowMatchedInstruments(int expected) {
        Map<String, Object> summary = getSummary();
        assertThat(summary.get("matchedInstruments")).isEqualTo(expected);
    }

    @Then("the import summary should show {int} unmatched instruments")
    public void importSummaryShouldShowUnmatchedInstruments(int expected) {
        Map<String, Object> summary = getSummary();
        assertThat(summary.get("unmatchedInstruments")).isEqualTo(expected);
    }

    @When("I confirm the import for account {string}")
    public void iConfirmImportForAccount(String accountName) {
        Map<String, Object> request = new HashMap<>();
        request.put("mappings", List.of());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        confirmResponse =
                restTemplate.postForEntity(
                        baseUrl() + "/api/v1/imports/" + importSessionId + "/confirm",
                        new HttpEntity<>(request, headers),
                        Map.class);
    }

    @When("I confirm the import with mappings for account {string}:")
    public void iConfirmImportWithMappings(String accountName, DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps();
        List<Map<String, String>> mappings = new ArrayList<>();
        for (Map<String, String> row : rows) {
            Map<String, String> mapping = new HashMap<>();
            mapping.put("brokerName", row.get("brokerName"));
            mapping.put("catalogSymbol", row.get("catalogSymbol"));
            mappings.add(mapping);
        }

        Map<String, Object> request = new HashMap<>();
        request.put("mappings", mappings);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        confirmResponse =
                restTemplate.postForEntity(
                        baseUrl() + "/api/v1/imports/" + importSessionId + "/confirm",
                        new HttpEntity<>(request, headers),
                        Map.class);
    }

    @When("I confirm the import without mappings for account {string}")
    public void iConfirmImportWithoutMappings(String accountName) {
        iConfirmImportForAccount(accountName);
    }

    @When("I provide prices for the import:")
    public void iProvidePricesForImport(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps();
        List<Map<String, String>> prices = new ArrayList<>();
        for (Map<String, String> row : rows) {
            Map<String, String> entry = new HashMap<>();
            entry.put("symbol", row.get("symbol"));
            entry.put("price", row.get("price"));
            entry.put("currency", row.get("currency"));
            prices.add(entry);
        }

        Map<String, Object> request = new HashMap<>();
        request.put("prices", prices);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        pricesResponse =
                restTemplate.postForEntity(
                        baseUrl() + "/api/v1/imports/" + importSessionId + "/prices",
                        new HttpEntity<>(request, headers),
                        Map.class);
    }

    @Then("the import confirmation should fail with status {int}")
    public void importConfirmationShouldFailWithStatus(int expectedStatus) {
        assertThat(confirmResponse.getStatusCode().value()).isEqualTo(expectedStatus);
    }

    @Then("a position for {string} should exist with quantity {int}")
    public void positionShouldExistWithQuantity(String symbol, int expectedQuantity) {
        BigDecimal totalQuantity =
                jdbcTemplate.queryForObject(
                        "SELECT total_quantity FROM positions WHERE instrument_symbol = ?",
                        BigDecimal.class,
                        symbol);
        assertThat(totalQuantity).isNotNull();
        assertThat(totalQuantity.intValue()).isEqualTo(expectedQuantity);
    }

    @Given(
            "the instrument catalog contains {string} with name {string} and type {string} on market {string} in currency {string}")
    public void theInstrumentCatalogContainsOnMarket(
            String symbol, String name, String type, String market, String currency) {
        CucumberTestHelper.ensureInstrumentExists(
                jdbcTemplate, symbol, name, type, market, currency);
    }

    @Given("a broker mapping exists for {string} mapping {string} to {string}")
    public void brokerMappingExists(String broker, String brokerInstrumentName, String symbol) {
        CucumberTestHelper.ensureInstrumentExists(
                jdbcTemplate, symbol, symbol, "STOCK", "US", "USD");
        jdbcTemplate.update(
                "INSERT INTO broker_instrument_mappings (broker, broker_instrument_name, catalog_symbol) "
                        + "VALUES (?, ?, ?) "
                        + "ON CONFLICT (broker, broker_instrument_name) DO UPDATE SET catalog_symbol = EXCLUDED.catalog_symbol",
                broker,
                brokerInstrumentName,
                symbol);
    }

    @When("I upload an XTB XLSX for account {string} with closed positions and transactions:")
    public void iUploadXtbXlsxWithClosedPositions(String accountName, DataTable dataTable) {
        pendingClosedPositions = dataTable.asMaps();
        pendingCashOperations = new ArrayList<>();
        // Cash operations will be provided in the next step
        // Store accountName for later upload
        this.pendingXtbAccountName = accountName;
    }

    @When("the following XTB cash operations:")
    public void theFollowingXtbCashOperations(DataTable dataTable) {
        pendingCashOperations = dataTable.asMaps();
        uploadXtbXlsx(pendingXtbAccountName, pendingClosedPositions, pendingCashOperations);
    }

    @When("I upload an XTB XLSX for account {string} with transactions:")
    public void iUploadXtbXlsxWithTransactions(String accountName, DataTable dataTable) {
        List<Map<String, String>> cashRows = dataTable.asMaps();
        uploadXtbXlsx(accountName, List.of(), cashRows);
    }

    private String pendingXtbAccountName;

    private void uploadXtbXlsx(
            String accountName,
            List<Map<String, String>> closedPositions,
            List<Map<String, String>> cashOperations) {
        byte[] xlsxBytes = buildXtbXlsx(closedPositions, cashOperations);

        ByteArrayResource resource =
                new ByteArrayResource(xlsxBytes) {
                    @Override
                    public String getFilename() {
                        return "transactions.xlsx";
                    }
                };

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("broker", "XTB");
        body.add("accountName", accountName);
        body.add("file", resource);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        importResponse =
                restTemplate.postForEntity(
                        baseUrl() + "/api/v1/imports", new HttpEntity<>(body, headers), Map.class);

        if (importResponse.getStatusCode().is2xxSuccessful() && importResponse.getBody() != null) {
            importSessionId = (String) importResponse.getBody().get("importSessionId");
        }
    }

    private byte[] buildXtbXlsx(
            List<Map<String, String>> closedPositions, List<Map<String, String>> cashOperations) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            // Closed Positions sheet
            XSSFSheet closedSheet = workbook.createSheet("Closed Positions");
            for (int i = 0; i < 5; i++) {
                closedSheet.createRow(i);
            }
            for (int i = 0; i < closedPositions.size(); i++) {
                Map<String, String> cp = closedPositions.get(i);
                XSSFRow row = closedSheet.createRow(5 + i);
                row.createCell(0).setCellValue(cp.get("closedInstrument"));
                row.createCell(2).setCellValue(cp.get("closedTicker"));
            }

            // Cash Operations sheet
            XSSFSheet cashSheet = workbook.createSheet("Cash Operations");
            for (int i = 0; i < 5; i++) {
                cashSheet.createRow(i);
            }
            for (int i = 0; i < cashOperations.size(); i++) {
                Map<String, String> co = cashOperations.get(i);
                XSSFRow row = cashSheet.createRow(5 + i);
                row.createCell(0).setCellValue(co.get("type"));
                row.createCell(1).setCellValue(co.get("instrument"));
                row.createCell(5).setCellValue(co.get("comment"));
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to build XTB XLSX", e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getLatestResponseBody() {
        if (pricesResponse != null && pricesResponse.getStatusCode().is2xxSuccessful()) {
            return pricesResponse.getBody();
        }
        if (confirmResponse != null && confirmResponse.getStatusCode().is2xxSuccessful()) {
            return confirmResponse.getBody();
        }
        return importResponse.getBody();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getSummary() {
        Map<String, Object> body = getLatestResponseBody();
        return (Map<String, Object>) body.get("summary");
    }

    private String buildMBankCsv(List<Map<String, String>> rows) {
        List<Map<String, String>> rowsWithCurrency = new ArrayList<>();
        for (Map<String, String> row : rows) {
            Map<String, String> enriched = new HashMap<>(row);
            enriched.putIfAbsent("currency", "PLN");
            rowsWithCurrency.add(enriched);
        }
        return buildMBankCsvWithCurrency(rowsWithCurrency);
    }

    private String buildMBankCsvWithCurrency(List<Map<String, String>> rows) {
        StringBuilder sb = new StringBuilder();
        IntStream.rangeClosed(1, 34)
                .forEach(i -> sb.append("metadata line ").append(i).append("\n"));
        sb.append(
                "Czas transakcji;Papier;Giełda;K/S;Liczba;Kurs;Waluta;Prowizja;Waluta;Wartość;Waluta\n");

        for (Map<String, String> row : rows) {
            String instrument = row.get("instrument");
            String side = row.getOrDefault("side", "K");
            String quantity = row.get("quantity");
            String price = row.get("price").replace(".", ",");
            String commission = row.get("commission").replace(".", ",");
            String currency = row.getOrDefault("currency", "PLN");
            BigDecimal total =
                    new BigDecimal(row.get("price")).multiply(new BigDecimal(row.get("quantity")));
            String totalStr = total.toPlainString().replace(".", ",");

            sb.append("01.01.2025 10:00:00;")
                    .append(instrument)
                    .append(";WWA-GPW;")
                    .append(side)
                    .append(";")
                    .append(quantity)
                    .append(";")
                    .append(price)
                    .append(";")
                    .append(currency)
                    .append(";")
                    .append(commission)
                    .append(";")
                    .append(currency)
                    .append(";")
                    .append(totalStr)
                    .append(";")
                    .append(currency)
                    .append("\n");
        }
        return sb.toString();
    }

    private String baseUrl() {
        return "http://localhost:" + port;
    }
}
