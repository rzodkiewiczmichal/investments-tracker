package com.investments.tracker.infrastructure.external.degiro;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import com.investments.tracker.domain.model.value.Money;
import com.investments.tracker.domain.model.value.Price;
import com.investments.tracker.domain.model.value.Quantity;
import com.investments.tracker.domain.model.value.TransactionType;

/**
 * Parser for DEGIRO transaction history CSV exports.
 *
 * <p>DEGIRO exports use UTF-8 encoding, comma as field delimiter with quoted fields for values
 * containing commas (Polish number format). Transaction direction is determined by the sign of the
 * quantity field (positive = BUY, negative = SELL).
 */
@Component
public class DegiroCsvParser implements TransactionHistoryParser {

    private static final Logger log = LoggerFactory.getLogger(DegiroCsvParser.class);
    private static final String BROKER_NAME = "DEGIRO";
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    private static final int COL_DATE = 0;
    private static final int COL_TIME = 1;
    private static final int COL_PRODUCT = 2;
    private static final int COL_QUANTITY = 6;
    private static final int COL_PRICE = 7;
    private static final int COL_PRICE_CURRENCY = 8;
    private static final int COL_TRANSACTION_FEE = 14;
    private static final int MIN_COLUMNS = 16;

    @Override
    public ParseResult parse(InputStream file) {
        List<RawTransaction> transactions = new ArrayList<>();

        try (BufferedReader reader =
                new BufferedReader(new InputStreamReader(file, StandardCharsets.UTF_8))) {
            int lineNumber = 0;
            String line;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                if (lineNumber == 1) {
                    continue;
                }

                if (line.isBlank()) {
                    continue;
                }

                parseRow(line, lineNumber).ifPresent(transactions::add);
            }
        } catch (ImportParsingException e) {
            throw e;
        } catch (IOException e) {
            throw new ImportParsingException("Failed to read DEGIRO CSV file", e);
        }

        if (transactions.isEmpty()) {
            throw ImportParsingException.emptyFile();
        }

        return new ParseResult(transactions, Map.of());
    }

    @Override
    public String brokerName() {
        return BROKER_NAME;
    }

    private java.util.Optional<RawTransaction> parseRow(String line, int lineNumber) {
        List<String> fields = parseCsvLine(line);

        if (fields.size() < MIN_COLUMNS) {
            throw ImportParsingException.invalidFormat(
                    lineNumber,
                    "expected at least " + MIN_COLUMNS + " fields, got " + fields.size());
        }

        String product = fields.get(COL_PRODUCT).trim();
        String quantityStr = fields.get(COL_QUANTITY).trim();
        String priceStr = fields.get(COL_PRICE).trim();
        String priceCurrencyStr = fields.get(COL_PRICE_CURRENCY).trim();
        String feeStr = fields.get(COL_TRANSACTION_FEE).trim();
        String dateStr = fields.get(COL_DATE).trim();
        String timeStr = fields.get(COL_TIME).trim();

        if (quantityStr.isEmpty() || priceStr.isEmpty() || priceCurrencyStr.isEmpty()) {
            log.warn("Skipped row {} — missing quantity, price, or currency", lineNumber);
            return java.util.Optional.empty();
        }

        Currency priceCurrency;
        try {
            priceCurrency = Currency.valueOf(priceCurrencyStr);
        } catch (IllegalArgumentException e) {
            log.warn(
                    "Skipped row {} — unsupported currency '{}' for product '{}'",
                    lineNumber,
                    priceCurrencyStr,
                    product);
            return java.util.Optional.empty();
        }

        BigDecimal quantity = parsePolishNumber(quantityStr);
        TransactionType type = quantity.signum() >= 0 ? TransactionType.BUY : TransactionType.SELL;
        BigDecimal absQuantity = quantity.abs();

        BigDecimal price = parsePolishNumber(priceStr);
        Commission commission = parseCommission(feeStr, priceCurrency);
        LocalDateTime txDate = parseDateTime(dateStr, timeStr, lineNumber);

        return java.util.Optional.of(
                new RawTransaction(
                        BrokerInstrumentName.of(product),
                        type,
                        Quantity.of(absQuantity),
                        Price.of(new Money(price, priceCurrency)),
                        commission,
                        priceCurrency,
                        txDate));
    }

    private static Commission parseCommission(String feeStr, Currency priceCurrency) {
        if (feeStr.isEmpty()) {
            return Commission.zero();
        }
        BigDecimal fee = parsePolishNumber(feeStr).abs();
        return Commission.of(new Money(fee, priceCurrency));
    }

    private static LocalDateTime parseDateTime(String date, String time, int lineNumber) {
        try {
            return LocalDateTime.parse(date + " " + time, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            log.warn("Failed to parse date '{}' '{}' at row {}", date, time, lineNumber);
            return null;
        }
    }

    static BigDecimal parsePolishNumber(String value) {
        String normalized = value.trim().replace(".", "").replace(",", ".");
        return new BigDecimal(normalized);
    }

    static List<String> parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        fields.add(current.toString());

        return fields;
    }
}
