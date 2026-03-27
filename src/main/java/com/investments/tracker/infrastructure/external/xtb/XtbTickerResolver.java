package com.investments.tracker.infrastructure.external.xtb;

import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Resolves instrument names to tickers by reading the Closed Positions sheet.
 *
 * <p>Closed Positions sheet has instrument name in column A and ticker in column C (e.g.,
 * "Microsoft" -> "MSFT.US").
 */
final class XtbTickerResolver {

    private static final String CLOSED_POSITIONS_SHEET = "Closed Positions";
    private static final int DATA_START_ROW = 5;

    private XtbTickerResolver() {}

    /**
     * Builds a map of instrument name -> ticker from the Closed Positions sheet.
     *
     * @param workbook the XTB XLSX workbook
     * @return map of instrument full name -> ticker (e.g., "Microsoft" -> "MSFT.US")
     */
    static Map<String, String> resolve(XSSFWorkbook workbook) {
        Map<String, String> nameToTicker = new HashMap<>();

        Sheet sheet = workbook.getSheet(CLOSED_POSITIONS_SHEET);
        if (sheet == null) {
            return nameToTicker;
        }

        for (int i = DATA_START_ROW; i <= sheet.getLastRowNum(); i++) {
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

        return nameToTicker;
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
