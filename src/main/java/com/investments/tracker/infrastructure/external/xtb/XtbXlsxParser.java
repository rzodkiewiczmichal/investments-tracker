package com.investments.tracker.infrastructure.external.xtb;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.investments.tracker.application.port.out.ParseResult;
import com.investments.tracker.application.port.out.TransactionHistoryParser;
import com.investments.tracker.domain.exception.ImportParsingException;
import com.investments.tracker.domain.model.RawTransaction;
import com.investments.tracker.domain.model.value.BrokerInstrumentName;
import com.investments.tracker.domain.model.value.Commission;
import com.investments.tracker.domain.model.value.Currency;
import com.investments.tracker.domain.model.value.InstrumentSymbol;
import com.investments.tracker.domain.model.value.Money;
import com.investments.tracker.domain.model.value.Price;
import com.investments.tracker.domain.model.value.Quantity;
import com.investments.tracker.domain.model.value.TransactionType;

/**
 * Parser for XTB xStation XLSX transaction history exports.
 *
 * <p>Reads the Cash Operations sheet for buy/sell transactions. Quantity and price are extracted
 * from the Comment field. Ticker resolution is done by cross-referencing with the Closed Positions
 * sheet.
 */
@Component
public class XtbXlsxParser implements TransactionHistoryParser {

    private static final Logger log = LoggerFactory.getLogger(XtbXlsxParser.class);
    private static final String BROKER_NAME = "XTB";
    private static final String CASH_OPERATIONS_SHEET = "Cash Operations";
    private static final int DATA_START_ROW = 5;

    @Override
    public ParseResult parse(InputStream file) {
        try (XSSFWorkbook workbook = new XSSFWorkbook(file)) {
            Map<String, String> nameToTicker = XtbTickerResolver.resolve(workbook);
            List<RawTransaction> transactions = parseTransactions(workbook, nameToTicker);

            if (transactions.isEmpty()) {
                throw ImportParsingException.emptyFile();
            }

            Map<BrokerInstrumentName, InstrumentSymbol> tickerHints =
                    buildTickerHints(nameToTicker);

            return new ParseResult(transactions, tickerHints);
        } catch (ImportParsingException e) {
            throw e;
        } catch (IOException e) {
            throw new ImportParsingException("Failed to read XTB XLSX file", e);
        }
    }

    @Override
    public String brokerName() {
        return BROKER_NAME;
    }

    private List<RawTransaction> parseTransactions(
            XSSFWorkbook workbook, Map<String, String> nameToTicker) {
        Sheet sheet = workbook.getSheet(CASH_OPERATIONS_SHEET);
        if (sheet == null) {
            throw new ImportParsingException("Sheet '" + CASH_OPERATIONS_SHEET + "' not found");
        }

        List<RawTransaction> transactions = new ArrayList<>();

        for (int i = DATA_START_ROW; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) {
                continue;
            }

            String type = getStringValue(row.getCell(0));
            if (!isStockTransaction(type)) {
                continue;
            }

            String instrumentName = getStringValue(row.getCell(1));
            if (instrumentName.isBlank() || instrumentName.equalsIgnoreCase("Total")) {
                continue;
            }

            String comment = getStringValue(row.getCell(5));
            var parsed = XtbCommentParser.parse(comment);
            if (parsed.isEmpty()) {
                log.warn(
                        "Skipped row {} -- type='{}', instrument='{}', comment='{}' (no match)",
                        i,
                        type,
                        instrumentName,
                        comment);
                continue;
            }

            var commentData = parsed.get();
            TransactionType txType = commentData.transactionType();

            String ticker = nameToTicker.get(instrumentName);
            Currency currency =
                    ticker != null ? XtbMarketCurrencyMapper.resolveCurrency(ticker) : Currency.PLN;

            String brokerName = ticker != null ? ticker : instrumentName;

            transactions.add(
                    new RawTransaction(
                            BrokerInstrumentName.of(brokerName),
                            txType,
                            Quantity.of(commentData.quantity()),
                            Price.of(new Money(commentData.price(), currency)),
                            Commission.zero(),
                            currency));
        }

        return transactions;
    }

    private Map<BrokerInstrumentName, InstrumentSymbol> buildTickerHints(
            Map<String, String> nameToTicker) {
        Map<BrokerInstrumentName, InstrumentSymbol> hints = new HashMap<>();
        for (var entry : nameToTicker.entrySet()) {
            try {
                InstrumentSymbol symbol = InstrumentSymbol.of(entry.getValue());
                hints.put(BrokerInstrumentName.of(symbol.value()), symbol);
            } catch (Exception ignored) {
                // Skip invalid ticker formats
            }
        }
        return hints;
    }

    private static boolean isStockTransaction(String type) {
        return "Stock purchase".equals(type) || "Stock sell".equals(type);
    }

    private static String getStringValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        CellType type = cell.getCellType();
        if (type == CellType.FORMULA) {
            type = cell.getCachedFormulaResultType();
        }
        return switch (type) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            default -> "";
        };
    }
}
