package com.investments.tracker.infrastructure.external.mbank;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import com.investments.tracker.domain.exception.ImportParsingException;
import com.investments.tracker.domain.model.RawTransaction;
import com.investments.tracker.domain.model.value.Currency;
import com.investments.tracker.domain.model.value.TransactionType;

class MBankCsvParserTest {

    private static final Charset WINDOWS_1250 = Charset.forName("Windows-1250");
    private final MBankCsvParser parser = new MBankCsvParser();

    @Test
    void brokerNameIsMBank() {
        assertThat(parser.brokerName()).isEqualTo("mBank");
    }

    @Test
    void parsesValidCsvWithTransactions() {
        String csv =
                buildCsv(
                        "18.08.2025 14:28:01;ATREM;WWA-GPW;K;100;37,20;PLN;13,06;PLN;3 720,00;PLN",
                        "18.08.2025 13:28:37;ETFBCASH;WWA-GPW;S;50;140,40;PLN;11,27;PLN;7 020,00;PLN");

        List<RawTransaction> transactions = parser.parse(toInputStream(csv)).transactions();

        assertThat(transactions).hasSize(2);

        RawTransaction buy = transactions.get(0);
        assertThat(buy.brokerInstrumentName().value()).isEqualTo("ATREM");
        assertThat(buy.type()).isEqualTo(TransactionType.BUY);
        assertThat(buy.quantity().toBigDecimal()).isEqualByComparingTo("100");
        assertThat(buy.unitPrice().money().amount()).isEqualByComparingTo("37.2000");
        assertThat(buy.commission().money().amount()).isEqualByComparingTo("13.0600");
        assertThat(buy.currency()).isEqualTo(Currency.PLN);

        RawTransaction sell = transactions.get(1);
        assertThat(sell.brokerInstrumentName().value()).isEqualTo("ETFBCASH");
        assertThat(sell.type()).isEqualTo(TransactionType.SELL);
    }

    @Test
    void parsesEurTransaction() {
        String csv =
                buildCsv(
                        "16.09.2024 10:38:23;DTLE GR ETF;DEU-XETRA;K;709;3,4117;EUR;30,03;PLN;10 355,29;PLN");

        List<RawTransaction> transactions = parser.parse(toInputStream(csv)).transactions();

        assertThat(transactions).hasSize(1);
        RawTransaction tx = transactions.getFirst();
        assertThat(tx.brokerInstrumentName().value()).isEqualTo("DTLE GR ETF");
        assertThat(tx.currency()).isEqualTo(Currency.EUR);
        assertThat(tx.unitPrice().money().currency()).isEqualTo(Currency.EUR);
        assertThat(tx.commission().money().currency()).isEqualTo(Currency.PLN);
    }

    @Test
    void skipsBlankLinesAfterHeader() {
        String csv =
                buildCsv(
                        "18.08.2025 14:28:01;ATREM;WWA-GPW;K;10;37,20;PLN;1,00;PLN;372,00;PLN",
                        "",
                        "18.08.2025 13:28:37;ETFBCASH;WWA-GPW;S;5;140,40;PLN;1,00;PLN;702,00;PLN");

        List<RawTransaction> transactions = parser.parse(toInputStream(csv)).transactions();

        assertThat(transactions).hasSize(2);
    }

    @Test
    void throwsOnEmptyFile() {
        String csv = buildCsv();

        assertThatThrownBy(() -> parser.parse(toInputStream(csv)))
                .isInstanceOf(ImportParsingException.class)
                .hasMessageContaining("no transaction data");
    }

    @Test
    void skipsRowsWithUnsupportedCurrency() {
        String csv =
                buildCsv(
                        "18.08.2025 14:28:01;ATREM;WWA-GPW;K;10;37,20;PLN;1,00;PLN;372,00;PLN",
                        "18.01.2024 14:14:35;FW20H2420;WWA-GPW;S;1;2 204,00;PKT;9,00;PLN;44 080,00;PLN",
                        "18.08.2025 13:28:37;ETFBCASH;WWA-GPW;S;5;140,40;PLN;1,00;PLN;702,00;PLN");

        List<RawTransaction> transactions = parser.parse(toInputStream(csv)).transactions();

        assertThat(transactions).hasSize(2);
        assertThat(transactions.get(0).brokerInstrumentName().value()).isEqualTo("ATREM");
        assertThat(transactions.get(1).brokerInstrumentName().value()).isEqualTo("ETFBCASH");
    }

    @Test
    void throwsOnInvalidTransactionSide() {
        String csv =
                buildCsv("18.08.2025 14:28:01;ATREM;WWA-GPW;X;10;37,20;PLN;1,00;PLN;372,00;PLN");

        assertThatThrownBy(() -> parser.parse(toInputStream(csv)))
                .isInstanceOf(ImportParsingException.class)
                .hasMessageContaining("unknown transaction side: X");
    }

    private String buildCsv(String... dataRows) {
        StringBuilder sb = new StringBuilder();
        // 34 metadata lines
        IntStream.rangeClosed(1, 34)
                .forEach(i -> sb.append("metadata line ").append(i).append("\n"));
        // Header line
        sb.append(
                "Czas transakcji;Papier;Giełda;K/S;Liczba;Kurs;Waluta;Prowizja;Waluta;Wartość;Waluta\n");
        // Data rows
        for (String row : dataRows) {
            sb.append(row).append("\n");
        }
        return sb.toString();
    }

    private InputStream toInputStream(String content) {
        return new ByteArrayInputStream(content.getBytes(WINDOWS_1250));
    }
}
