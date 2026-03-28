package com.investments.tracker.infrastructure.external.mbank;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.investments.tracker.application.port.out.ParseResult;
import com.investments.tracker.application.port.out.TransactionHistoryParser;
import com.investments.tracker.domain.exception.ImportParsingException;
import com.investments.tracker.domain.model.RawTransaction;
import com.investments.tracker.domain.model.value.BrokerInstrumentName;
import com.investments.tracker.domain.model.value.Commission;
import com.investments.tracker.domain.model.value.Money;
import com.investments.tracker.domain.model.value.Price;
import com.investments.tracker.domain.model.value.Quantity;
import com.investments.tracker.domain.model.value.TransactionType;

/**
 * Parser for mBank eMAKLER transaction history CSV exports.
 *
 * <p>mBank exports use Windows-1250 encoding, semicolons as field delimiters, Polish number format
 * (comma decimal, space thousands), and include 34 metadata header lines plus 1 column header line
 * before data rows begin.
 */
@Component
public class MBankCsvParser implements TransactionHistoryParser {

    private static final String BROKER_NAME = "mBank";
    private static final Charset WINDOWS_1250 = Charset.forName("Windows-1250");
    private static final DateTimeFormatter MBANK_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int METADATA_LINES = 34;
    private static final int HEADER_LINES = 1;
    private static final int LINES_TO_SKIP = METADATA_LINES + HEADER_LINES;

    @Override
    public ParseResult parse(InputStream file) {
        List<RawTransaction> transactions = new ArrayList<>();

        try (BufferedReader reader =
                new BufferedReader(new InputStreamReader(file, WINDOWS_1250))) {
            int lineNumber = 0;
            String line;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                if (lineNumber <= LINES_TO_SKIP) {
                    continue;
                }

                if (line.isBlank()) {
                    continue;
                }

                int currentLine = lineNumber;
                Optional<MBankCsvRow> row = MBankCsvRow.parse(line, currentLine);
                row.ifPresent(r -> transactions.add(toRawTransaction(r, currentLine)));
            }
        } catch (ImportParsingException e) {
            throw e;
        } catch (IOException e) {
            throw new ImportParsingException("Failed to read mBank CSV file", e);
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

    private RawTransaction toRawTransaction(MBankCsvRow row, int lineNumber) {
        TransactionType type = parseTransactionType(row.side(), lineNumber);
        Money priceMoney = new Money(row.unitPrice(), row.priceCurrency());
        Money commissionMoney = new Money(row.commission(), row.commissionCurrency());
        LocalDateTime txDate = parseDate(row.timestamp());

        return new RawTransaction(
                BrokerInstrumentName.of(row.instrumentName()),
                type,
                Quantity.of(row.quantity()),
                Price.of(priceMoney),
                Commission.of(commissionMoney),
                row.priceCurrency(),
                txDate);
    }

    private static LocalDateTime parseDate(String timestamp) {
        if (timestamp == null || timestamp.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(timestamp, MBANK_DATE_FORMAT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private TransactionType parseTransactionType(String side, int lineNumber) {
        return switch (side) {
            case "K" -> TransactionType.BUY;
            case "S" -> TransactionType.SELL;
            default ->
                    throw ImportParsingException.invalidFormat(
                            lineNumber, "unknown transaction side: " + side + " (expected K or S)");
        };
    }
}
