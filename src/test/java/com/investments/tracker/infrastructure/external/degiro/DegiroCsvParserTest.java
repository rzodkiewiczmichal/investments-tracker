package com.investments.tracker.infrastructure.external.degiro;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.investments.tracker.application.port.out.ParseResult;
import com.investments.tracker.domain.exception.ImportParsingException;
import com.investments.tracker.domain.model.RawTransaction;
import com.investments.tracker.domain.model.value.Currency;
import com.investments.tracker.domain.model.value.TransactionType;

class DegiroCsvParserTest {

    private final DegiroCsvParser parser = new DegiroCsvParser();

    @Test
    void shouldReturnDegiroAsBrokerName() {
        assertThat(parser.brokerName()).isEqualTo("DEGIRO");
    }

    @Test
    void shouldParseBuyTransaction() {
        String csv =
                header()
                        + "\n"
                        + "25-03-2024,14:30,NU HOLDINGS LTD CLASS A,KYG6683N1034,NSY,XNYS,20,\"12,2300\",USD,\"-244,60\",USD,\"-225,88\",\"1,0829\",\"-0,56\",\"-2,00\",\"-228,44\",,uuid-1";

        ParseResult result = parser.parse(toInputStream(csv));

        assertThat(result.transactions()).hasSize(1);
        RawTransaction tx = result.transactions().get(0);
        assertThat(tx.brokerInstrumentName().value()).isEqualTo("NU HOLDINGS LTD CLASS A");
        assertThat(tx.type()).isEqualTo(TransactionType.BUY);
        assertThat(tx.quantity().toBigDecimal()).isEqualByComparingTo("20");
        assertThat(tx.unitPrice().money().amount()).isEqualByComparingTo("12.23");
        assertThat(tx.currency()).isEqualTo(Currency.USD);
        assertThat(tx.commission().money().amount()).isEqualByComparingTo("2.00");
        assertThat(tx.transactionDate()).isEqualTo(LocalDateTime.of(2024, 3, 25, 14, 30));
    }

    @Test
    void shouldParseSellTransaction() {
        String csv =
                header()
                        + "\n"
                        + "09-04-2024,16:50,NVIDIA CORP,US67066G1040,NDQ,CDED,-5,\"837,3600\",USD,\"4186,80\",USD,\"3857,05\",\"1,0855\",\"-9,64\",\"-2,00\",\"3845,41\",,uuid-2";

        ParseResult result = parser.parse(toInputStream(csv));

        assertThat(result.transactions()).hasSize(1);
        RawTransaction tx = result.transactions().get(0);
        assertThat(tx.brokerInstrumentName().value()).isEqualTo("NVIDIA CORP");
        assertThat(tx.type()).isEqualTo(TransactionType.SELL);
        assertThat(tx.quantity().toBigDecimal()).isEqualByComparingTo("5");
        assertThat(tx.unitPrice().money().amount()).isEqualByComparingTo("837.36");
        assertThat(tx.currency()).isEqualTo(Currency.USD);
    }

    @Test
    void shouldParseEurTransaction() {
        String csv =
                header()
                        + "\n"
                        + "28-12-2023,14:40,NIKE INC CLASS B,US6541061031,TDG,XGAT,10,\"96,6500\",EUR,\"-966,50\",EUR,\"-966,50\",,\"0,00\",\"-3,90\",\"-970,40\",,uuid-3";

        ParseResult result = parser.parse(toInputStream(csv));

        assertThat(result.transactions()).hasSize(1);
        RawTransaction tx = result.transactions().get(0);
        assertThat(tx.currency()).isEqualTo(Currency.EUR);
        assertThat(tx.commission().money().amount()).isEqualByComparingTo("3.90");
    }

    @Test
    void shouldHandleEmptyCommission() {
        String csv =
                header()
                        + "\n"
                        + "11-05-2023,09:04,ISHARES NASDAQ 100 UCITS ETF USD (ACC),IE00B53SZB19,EAM,XAMS,3,\"695,7010\",EUR,\"-2087,10\",EUR,\"-2087,10\",,\"0,00\",,\"-2087,10\",,uuid-4";

        ParseResult result = parser.parse(toInputStream(csv));

        assertThat(result.transactions()).hasSize(1);
        RawTransaction tx = result.transactions().get(0);
        assertThat(tx.commission().isZero()).isTrue();
    }

    @Test
    void shouldParseMultipleTransactions() {
        String csv =
                header()
                        + "\n"
                        + "25-03-2024,14:30,NU HOLDINGS LTD CLASS A,KYG6683N1034,NSY,XNYS,20,\"12,2300\",USD,\"-244,60\",USD,\"-225,88\",\"1,0829\",\"-0,56\",\"-2,00\",\"-228,44\",,uuid-1\n"
                        + "09-04-2024,16:50,NVIDIA CORP,US67066G1040,NDQ,CDED,-5,\"837,3600\",USD,\"4186,80\",USD,\"3857,05\",\"1,0855\",\"-9,64\",\"-2,00\",\"3845,41\",,uuid-2";

        ParseResult result = parser.parse(toInputStream(csv));

        assertThat(result.transactions()).hasSize(2);
    }

    @Test
    void shouldThrowOnEmptyFile() {
        String csv = header();

        assertThatThrownBy(() -> parser.parse(toInputStream(csv)))
                .isInstanceOf(ImportParsingException.class);
    }

    @Test
    void shouldReturnEmptyTickerHints() {
        String csv =
                header()
                        + "\n"
                        + "25-03-2024,14:30,NU HOLDINGS LTD CLASS A,KYG6683N1034,NSY,XNYS,20,\"12,2300\",USD,\"-244,60\",USD,\"-225,88\",\"1,0829\",\"-0,56\",\"-2,00\",\"-228,44\",,uuid-1";

        ParseResult result = parser.parse(toInputStream(csv));

        assertThat(result.tickerHints()).isEmpty();
    }

    @Test
    void shouldParsePolishNumbers() {
        assertThat(DegiroCsvParser.parsePolishNumber("12,2300"))
                .isEqualByComparingTo(new BigDecimal("12.23"));
        assertThat(DegiroCsvParser.parsePolishNumber("-5"))
                .isEqualByComparingTo(new BigDecimal("-5"));
        assertThat(DegiroCsvParser.parsePolishNumber("1184,90"))
                .isEqualByComparingTo(new BigDecimal("1184.90"));
    }

    @Test
    void shouldParseCsvLineWithQuotedFields() {
        String line =
                "25-03-2024,14:30,NU HOLDINGS LTD CLASS A,KYG6683N1034,NSY,XNYS,20,\"12,2300\",USD";

        List<String> fields = DegiroCsvParser.parseCsvLine(line);

        assertThat(fields).hasSize(9);
        assertThat(fields.get(7)).isEqualTo("12,2300");
        assertThat(fields.get(2)).isEqualTo("NU HOLDINGS LTD CLASS A");
    }

    private static String header() {
        return "Data,Czas,Produkt,ISIN,Giełda referencyjna,Miejsce wykonania,Liczba,Kurs,,Wartość lokalna,,Wartość EUR,Kurs wymiany,Opłaty AutoFX,Opłata transakcyjna DEGIRO i/lub opłata stron,Razem EUR,Identyfikator zlecenia,";
    }

    private static InputStream toInputStream(String content) {
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }
}
