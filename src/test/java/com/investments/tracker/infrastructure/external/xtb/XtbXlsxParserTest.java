package com.investments.tracker.infrastructure.external.xtb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.investments.tracker.application.port.out.ParseResult;
import com.investments.tracker.domain.exception.ImportParsingException;
import com.investments.tracker.domain.model.RawTransaction;
import com.investments.tracker.domain.model.value.Currency;
import com.investments.tracker.domain.model.value.TransactionType;

@DisplayName("XtbXlsxParser")
class XtbXlsxParserTest {

    private final XtbXlsxParser parser = new XtbXlsxParser();

    @Test
    @DisplayName("should return XTB as broker name")
    void brokerNameIsXtb() {
        assertThat(parser.brokerName()).isEqualTo("XTB");
    }

    @Test
    @DisplayName("should parse stock purchase from Cash Operations")
    void shouldParseStockPurchase() throws IOException {
        byte[] xlsx = buildXlsx(new CashRow("Stock purchase", "Microsoft", "OPEN BUY 20 @ 420.50"));

        ParseResult result = parser.parse(new ByteArrayInputStream(xlsx));

        assertThat(result.transactions()).hasSize(1);
        RawTransaction tx = result.transactions().getFirst();
        assertThat(tx.brokerInstrumentName().value()).isEqualTo("Microsoft");
        assertThat(tx.type()).isEqualTo(TransactionType.BUY);
        assertThat(tx.quantity().toBigDecimal()).isEqualByComparingTo("20");
        assertThat(tx.unitPrice().money().amount()).isEqualByComparingTo("420.5000");
    }

    @Test
    @DisplayName("should parse stock sell from Cash Operations")
    void shouldParseStockSell() throws IOException {
        byte[] xlsx = buildXlsx(new CashRow("Stock sell", "Microsoft", "CLOSE BUY 20/60 @ 430.00"));

        ParseResult result = parser.parse(new ByteArrayInputStream(xlsx));

        assertThat(result.transactions()).hasSize(1);
        RawTransaction tx = result.transactions().getFirst();
        assertThat(tx.type()).isEqualTo(TransactionType.SELL);
        assertThat(tx.quantity().toBigDecimal()).isEqualByComparingTo("20");
    }

    @Test
    @DisplayName("should skip non-stock operations")
    void shouldSkipNonStockOperations() throws IOException {
        byte[] xlsx =
                buildXlsx(
                        new CashRow("Stock purchase", "Microsoft", "OPEN BUY 10 @ 420.00"),
                        new CashRow("Dividend", "Microsoft", "MSFT.US USD 0.91/ SHR"),
                        new CashRow("Deposit", "", ""),
                        new CashRow("Close trade", "BITCOIN", "CLOSE BUY 1 @ 95000.00"));

        ParseResult result = parser.parse(new ByteArrayInputStream(xlsx));

        assertThat(result.transactions()).hasSize(1);
        assertThat(result.transactions().getFirst().brokerInstrumentName().value())
                .isEqualTo("Microsoft");
    }

    @Test
    @DisplayName("should resolve currency from Closed Positions ticker")
    void shouldResolveCurrencyFromClosedPositions() throws IOException {
        byte[] xlsx =
                buildXlsxWithClosedPositions(
                        new ClosedPosRow("Microsoft", "MSFT.US"),
                        new CashRow("Stock purchase", "Microsoft", "OPEN BUY 10 @ 420.00"));

        ParseResult result = parser.parse(new ByteArrayInputStream(xlsx));

        assertThat(result.transactions()).hasSize(1);
        assertThat(result.transactions().getFirst().currency()).isEqualTo(Currency.USD);
    }

    @Test
    @DisplayName("should build ticker hints from Closed Positions")
    void shouldBuildTickerHints() throws IOException {
        byte[] xlsx =
                buildXlsxWithClosedPositions(
                        new ClosedPosRow("Microsoft", "MSFT.US"),
                        new CashRow("Stock purchase", "Microsoft", "OPEN BUY 10 @ 420.00"));

        ParseResult result = parser.parse(new ByteArrayInputStream(xlsx));

        assertThat(result.tickerHints()).hasSize(1);
        assertThat(result.tickerHints().values().iterator().next().value()).isEqualTo("MSFT.US");
    }

    @Test
    @DisplayName("should throw on empty file (no stock transactions)")
    void shouldThrowOnEmptyFile() throws IOException {
        byte[] xlsx = buildXlsx(new CashRow("Dividend", "Microsoft", "MSFT.US USD 0.91/ SHR"));

        assertThatThrownBy(() -> parser.parse(new ByteArrayInputStream(xlsx)))
                .isInstanceOf(ImportParsingException.class);
    }

    @Test
    @DisplayName("should use PLN for unresolved instruments")
    void shouldUsePlnForUnresolvedInstruments() throws IOException {
        byte[] xlsx =
                buildXlsx(new CashRow("Stock purchase", "SomeCompany", "OPEN BUY 5 @ 100.00"));

        ParseResult result = parser.parse(new ByteArrayInputStream(xlsx));

        assertThat(result.transactions().getFirst().currency()).isEqualTo(Currency.PLN);
    }

    @Test
    @DisplayName("should set zero commission")
    void shouldSetZeroCommission() throws IOException {
        byte[] xlsx = buildXlsx(new CashRow("Stock purchase", "Microsoft", "OPEN BUY 10 @ 420.00"));

        ParseResult result = parser.parse(new ByteArrayInputStream(xlsx));

        assertThat(result.transactions().getFirst().commission().isZero()).isTrue();
    }

    // --- Test helpers ---

    record CashRow(String type, String instrument, String comment) {}

    record ClosedPosRow(String instrument, String ticker) {}

    private byte[] buildXlsx(CashRow... rows) throws IOException {
        return buildXlsxWithClosedPositions(null, rows);
    }

    private byte[] buildXlsxWithClosedPositions(ClosedPosRow closedRow, CashRow... cashRows)
            throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            // Closed Positions sheet
            XSSFSheet closedSheet = workbook.createSheet("Closed Positions");
            // Header rows (0-4)
            for (int i = 0; i < 5; i++) {
                closedSheet.createRow(i);
            }
            if (closedRow != null) {
                XSSFRow row = closedSheet.createRow(5);
                row.createCell(0).setCellValue(closedRow.instrument());
                row.createCell(2).setCellValue(closedRow.ticker());
            }

            // Cash Operations sheet
            XSSFSheet cashSheet = workbook.createSheet("Cash Operations");
            // Header rows (0-4)
            for (int i = 0; i < 5; i++) {
                cashSheet.createRow(i);
            }
            // Data rows
            for (int i = 0; i < cashRows.length; i++) {
                XSSFRow row = cashSheet.createRow(5 + i);
                row.createCell(0).setCellValue(cashRows[i].type());
                row.createCell(1).setCellValue(cashRows[i].instrument());
                row.createCell(5).setCellValue(cashRows[i].comment());
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }
}
