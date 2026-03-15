package com.investments.tracker.infrastructure.external.xtb;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Resolves instrument names to tickers by cross-referencing with the Closed Positions sheet and
 * dividend comments in the Cash Operations sheet.
 *
 * <p>Closed Positions sheet has instrument name in column A and ticker in column C. Dividend
 * comments in Cash Operations have patterns like {@code "MSFT.US USD 0.9100/ SHR"}.
 */
final class XtbTickerResolver {

    private static final String CLOSED_POSITIONS_SHEET = "Closed Positions";
    private static final String CASH_OPERATIONS_SHEET = "Cash Operations";
    private static final int CLOSED_POSITIONS_DATA_START_ROW = 5;
    private static final int CASH_OPERATIONS_DATA_START_ROW = 5;
    private static final Pattern DIVIDEND_TICKER_PATTERN =
            Pattern.compile("^([A-Z0-9]+\\.[A-Z]{2})\\s+[A-Z]{3}\\s+");

    private XtbTickerResolver() {}

    /**
     * Builds a map of instrument name → ticker from the Closed Positions sheet and dividend
     * comments.
     *
     * @param workbook the XTB XLSX workbook
     * @return map of instrument full name → ticker (e.g., "Microsoft" → "MSFT.US")
     */
    static Map<String, String> resolve(XSSFWorkbook workbook) {
        Map<String, String> nameToTicker = new HashMap<>();
        resolveFromClosedPositions(workbook, nameToTicker);
        resolveFromDividendComments(workbook, nameToTicker);
        return nameToTicker;
    }

    private static void resolveFromClosedPositions(
            XSSFWorkbook workbook, Map<String, String> nameToTicker) {
        Sheet sheet = workbook.getSheet(CLOSED_POSITIONS_SHEET);
        if (sheet == null) {
            return;
        }

        for (int i = CLOSED_POSITIONS_DATA_START_ROW; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) {
                continue;
            }

            String name = getStringValue(row.getCell(0));
            String ticker = getStringValue(row.getCell(2));

            if (!name.isBlank() && !ticker.isBlank() && !name.equalsIgnoreCase("Total")) {
                nameToTicker.putIfAbsent(name, ticker);
            }
        }
    }

    private static void resolveFromDividendComments(
            XSSFWorkbook workbook, Map<String, String> nameToTicker) {
        Sheet sheet = workbook.getSheet(CASH_OPERATIONS_SHEET);
        if (sheet == null) {
            return;
        }

        for (int i = CASH_OPERATIONS_DATA_START_ROW; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) {
                continue;
            }

            String type = getStringValue(row.getCell(0));
            if (!"Dividend".equals(type)) {
                continue;
            }

            String instrumentName = getStringValue(row.getCell(1));
            String comment = getStringValue(row.getCell(5));

            if (instrumentName.isBlank() || comment.isBlank()) {
                continue;
            }

            Matcher matcher = DIVIDEND_TICKER_PATTERN.matcher(comment);
            if (matcher.find()) {
                nameToTicker.putIfAbsent(instrumentName, matcher.group(1));
            }
        }
    }

    private static String getStringValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            default -> "";
        };
    }
}
